package com.example.socketio_client.Base;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

import android_serialport_api.SerialPort;

/**
 * @author benjaminwan
 *串口辅助工具类
 */
public abstract class SerialHelper{
	private SerialPort mSerialPort;
	private OutputStream mOutputStream;
	private InputStream mInputStream;
	private ReadThread mReadThread;
	private SendThread mSendThread;
	private static String sPort=Constants.SERIALPORTINTERFACE; //串口 后期固定
	private int iBaudRate=9600;
	private boolean _isOpen=false;
	private byte[] _bLoopData=new byte[]{0x30};
	private int iDelay=50;
	//----------------------------------------------------
	public SerialHelper(String sPort,int iBaudRate){
		this.sPort = sPort;
		this.iBaudRate=iBaudRate;
	}
	public SerialHelper(){
		this(Constants.SERIALPORTINTERFACE,9600);
	}
	public SerialHelper(String sPort){
		this(sPort,9600);
	}
	public SerialHelper(String sPort,String sBaudRate){
		this(sPort,Integer.parseInt(sBaudRate));
	}
	//----------------------------------------------------
	public void open() throws SecurityException, IOException,InvalidParameterException{
		mSerialPort =  new SerialPort(new File(sPort), iBaudRate, 0);
		mOutputStream = mSerialPort.getOutputStream();
		mInputStream = mSerialPort.getInputStream();
		mReadThread = new ReadThread();
		mReadThread.start();
		mSendThread = new SendThread();
		mSendThread.setSuspendFlag();
		mSendThread.start();
		_isOpen=true;
	}
	//----------------------------------------------------
	public void close(){
		if (mReadThread != null)
			mReadThread.interrupt();
		if (mSerialPort != null) {
			mSerialPort.close();
			mSerialPort = null;
		}
		_isOpen=false;
	}

	/**
	 * 发送数据
	 * @param bOutArray 要发送的数据 字节数组
	 */
	public void send(byte[] bOutArray){
		try
		{
			L.e("————————》  下发   《————————:"+ HexToUtil.ByteArrToHex(bOutArray));
			mOutputStream.write(bOutArray);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 将字符转化为 存储十六进制的数组
	 * @param sHex
	 */
	public void sendHex(String sHex){
		byte[] bOutArray = HexToUtil.HexToByteArr(sHex);
		send(bOutArray);
	}

	/**
	 * 发送字节数组
	 * @param sTxt
	 */
	public void sendTxt(String sTxt){
		byte[] bOutArray =sTxt.getBytes();
		send(bOutArray);
	}
	//----------------------------------------------------
	private class ReadThread extends Thread { //接收消息线程
		@Override
		public void run() {
			super.run();
			while(!isInterrupted()) { //isInterrupted方法返回线程当前的interrupted状态 判断程序是否有异常出现
				try
				{
					if (mInputStream == null) return;
					byte[] buffer=new byte[1024];
					int size = mInputStream.read(buffer);
					if (size > 0){
						Bean data = new Bean(sPort,buffer,size);
						onDataReceived(data); // 在线程里执行这个方法 监听读取 在外部实现该方法
					}
					try
					{
						Thread.sleep(50);//延时50ms
					} catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				} catch (Throwable e)
				{
					e.printStackTrace();
					return;
				}
			}
		}
	}
	//----------------------------------------------------
	private class SendThread extends Thread{  //发送线程
		public boolean suspendFlag = true;// 控制线程的执行
		@Override
		public void run() {
			super.run();
			while(!isInterrupted()) { //判断线程是否还再运行
				synchronized (this)
				{
					while (suspendFlag)
					{
						try
						{
							wait();
						} catch (InterruptedException e)
						{
							e.printStackTrace();
						}
					}
				}
				send(getbLoopData());
				try
				{
					Thread.sleep(iDelay);
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}

		//线程暂停
		public void setSuspendFlag() {
			this.suspendFlag = true;
		}

		//唤醒线程
		public synchronized void setResume() {
			this.suspendFlag = false;
			notify();
		}
	}
	//----------------------------------------------------
	//获取比特率
	public int getBaudRate()
	{
		return iBaudRate;
	}
	//设置比特率 
	public boolean setBaudRate(int iBaud)
	{
		if (_isOpen)
		{
			return false;
		} else
		{
			iBaudRate = iBaud;
			return true;
		}
	}
	//这个 就是 接收字符串 转换成 int 泪信号
	public boolean setBaudRate(String sBaud)
	{
		int iBaud = Integer.parseInt(sBaud);
		return setBaudRate(iBaud);
	}
	//----------------------------------------------------
	//端口吗
	public String getPort()
	{
		return sPort;
	}
	//设置端口
	public boolean setPort(String sPort)
	{
		if (_isOpen)
		{
			return false;
		} else
		{
			this.sPort = sPort;
			return true;
		}
	}
	//----------------------------------------------------
	public boolean isOpen()
	{
		return _isOpen;
	}
	//----------------------------------------------------
	public byte[] getbLoopData()
	{
		return _bLoopData;
	}
	//----------------------------------------------------
	public void setbLoopData(byte[] bLoopData)
	{
		this._bLoopData = bLoopData;
	}
	//----------------------------------------------------
	public void setTxtLoopData(String sTxt){
		this._bLoopData = sTxt.getBytes();
	}
	public void setHexLoopData(String sHex){
		this._bLoopData = HexToUtil.HexToByteArr(sHex);
	}

	/**
	 * 获取毫秒值
	 * @return
	 */
	public int getiDelay()
	{
		return iDelay;
	}

	/**
	 * 设置毫秒值
	 * @param iDelay
	 */
	public void setiDelay(int iDelay)
	{
		this.iDelay = iDelay;
	}

	/**
	 * 开始发送
	 */
	public void startSend()
	{
		if (mSendThread != null)
		{
			mSendThread.setResume();
		}
	}

	/**
	 * 停止发送
	 */
	public void stopSend()
	{
		if (mSendThread != null)
		{
			mSendThread.setSuspendFlag();
		}
	}
	//----------------------------------------------------
	protected abstract void onDataReceived(Bean ComRecData);
}