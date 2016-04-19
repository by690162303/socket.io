package com.example.socketio_client.Server;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.example.socketio_client.Base.Bean;
import com.example.socketio_client.Base.Constants;
import com.example.socketio_client.Base.HexToUtil;
import com.example.socketio_client.Base.L;
import com.example.socketio_client.Base.SerialHelper;
import com.example.socketio_client.Entity.RequestCommand;
import com.example.socketio_client.Socket_Application.Socket_Application;
import com.example.socketio_client.parser.Standalone;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Arrays;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by 白杨 on 2016/3/18.
 */
public class Socket_IO extends Service {
    private Socket mSocket;
    private SerialControl mSerialControl;
    private static final String CONNECT = "connect"; //连接
    private static final int CONNECT_int = 0; //连接
    private static final String DISCONNECT = "disconnect"; //连接
    private static final int DISCONNECT_int = -1; //连接
    private static final int CONNECT_room = -2; //连接
    private static final int OPEN_SERIALPORT = -3; //连接
    private static final String COMMAND = "command"; //命令
    private static final int COMMAND_int = 1; //连接
    private static final int RESTART_int = 10; //重启次数
    private int DisConnect_num = 0;
    private static final String MCU = "mcu"; //MCU码
    private String MCU_M = "0000"; //MCU码
    private Standalone mStandalone;
    private boolean isJionroom = false; //判断服务是否再运行
    private boolean isError = false; //判断服务是否打开串口失败
    private RequestCommand mRequestCommand;
    private static final long longTime = 100000;
    private long nowCheck = 0;
    private long SendTime = 0;
    private Handler mHandler = new Handler() {
        @Override
        public  void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CONNECT_room:
                    //第一次 连接 是为了建立”room“ 第二次连接的时候要将 房间号就是mcu码
                    L.e("进入房间");
                    try {
                        mSocket = IO.socket(Constants.CHAT_SERVER_URL + MCU_M);
                        initSocket(mSocket);
                        //发送命令  ------
                        mSocket.emit(COMMAND, "0108" + MCU_M);
                        isJionroom = true;
//                        mStandalone.SendCleanerPWD("01");//总清命令
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                    break;
                case CONNECT_int:
                    if (!isJionroom) {
                        L.e("还没有获取到 MCU：" + MCU_M);
                        if (MCU_M == "0000") {
                            DisConnect_num ++;
                            if(DisConnect_num == RESTART_int){
                                //当断开次数达到断开上线 服务销毁自动重启
                                onDestroy();
                                L.e("--------------->>>>>> 重启服务");
                            }
                            mHandler.sendEmptyMessageDelayed(CONNECT_int, 5000);
                        } else {
                            DisConnect_num = 0 ; // 防止切换Socket触发的DISCONNECT让服务重启
                            mSocket.emit(MCU, MCU_M);
                            mSocket.disconnect();//建立房间后断开所属的原链接
                            mHandler.sendEmptyMessageDelayed(CONNECT_room, 50);
                        }
                    }
                    break;
                case COMMAND_int:

                    String order = (String) msg.obj;
                    switch (order.substring(0, 2)) {
                        case "00":
                            if(System.currentTimeMillis()-nowCheck<longTime){
                                L.e("最近查过了 不需要再查了");
                                return;
                            }
                            nowCheck = System.currentTimeMillis();
                            mStandalone.SendcheckState();
                            return;
                        case "10":
                            if(System.currentTimeMillis()-SendTime<longTime){
                                L.e("已经匹配过时间 不需要再匹配时间");
                                return;
                            }
                            SendTime = System.currentTimeMillis();
                            //校验时间
                            mStandalone.SendDATA(order.substring(2));
                            return;
                    }
                    mStandalone.parseBackGroundOrder(order); // 接收到服务器下发的指令 解析发送给小板
                    break;
                case DISCONNECT_int:
                    L.e("onDisConnect----->连接 丢失, --> ");//连接丢失可能是服务器重启，可能是客户端重启
                    //需要重新建立房间，重新进入房间（因为重连操作是）
                    //系统封装好的操作，进行重复连接。所以要改变链接地址
                    if (isJionroom) {
                        mSocket.close();
                        isJionroom = false;
                        L.e("重置----->连接");
                        try {
                            mSocket = IO.socket(Constants.CHAT_SERVER_URL);
                            initSocket(mSocket);
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                            isJionroom = true; //初始化程序出问题
                        }
                    }
                    DisConnect_num ++;
                    if(DisConnect_num == RESTART_int){
                        //当断开次数达到断开上线 服务销毁自动重启
                        onDestroy();
                        L.e("--------------->>>>>> 重启服务");
                    }
                    break;
                case OPEN_SERIALPORT:
                    //当串口打开失败出问题的时候 重复开启 重新打开串口
                    OpenComPort(mSerialControl);
                    break;
            }
        }
    };

    /**
     *Socket监听的回掉函数
     * **/
    /**
     * 连接错误
     */
    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            mHandler.sendEmptyMessage(DISCONNECT_int);
        }
    };
    /**
     * 成功连接
     */
    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            mHandler.sendEmptyMessage(CONNECT_int);
        }
    };
    /**
     * 监听接收命令
     */
    private Emitter.Listener onCommand = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                JSONObject data = (JSONObject) args[0];
                L.e("接收到：" + Arrays.toString(args));
                String command;
                String mcu;
                try {
                    mcu = data.getString("mcu");
                    command = data.getString("command"); //获取指令 接收的是JSON数据格式的字符串
                    mRequestCommand = new RequestCommand(System.currentTimeMillis(),command);
                    CheckListCommand(mRequestCommand);
                    L.e("接收MCU：" + mcu + " , Command：" + command);
                } catch (JSONException e) {
                    return;
                }
            } catch (Exception e) {
                L.e("异常格式：" + Arrays.toString(args));
            }
        }
    };
    /**
     * 丢失链接
     */
    private Emitter.Listener onDisConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            mHandler.sendEmptyMessage(DISCONNECT_int);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
    }
    /**
     * 初始化设置Socket
     *
     * @param mSocket
     */
    public void initSocket(Socket mSocket) {
        mSocket.on(CONNECT, onConnect);//连接
        mSocket.on(DISCONNECT, onDisConnect);//断开连接
        mSocket.on(COMMAND, onCommand); // 接收命令
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError); //连接错误
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError); //连接超时
        mSocket.connect();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mSerialControl = new SerialControl();
        mStandalone = new Standalone(mSerialControl);
        OpenComPort(mSerialControl); //开启串口
        //注册 socket.io的监听
        Socket_Application app = (Socket_Application) getApplication();
        mSocket = app.getmSocket();
        initSocket(mSocket);
        return START_STICKY; //系统自动重新启动
//        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DisConnect_num = 0 ;//重置断开统计次数
        CloseComPort(mSerialControl);
        mSocket.disconnect();
        mSocket.off(DISCONNECT, onDisConnect);
        mSocket.off();
        //当服务终止的时候 自动拉起服务
        Intent intent = new Intent("SOCKET.IO");
        sendBroadcast(intent);
    }

    /**
     * 关闭串口
     *
     * @param ComPort
     */
    private void CloseComPort(SerialHelper ComPort) {
        if (ComPort != null) {
            ComPort.stopSend();
            ComPort.close();
        }
    }
    /**
     * 打开串口
     *
     * @param ComPort
     */
    private void OpenComPort(SerialHelper ComPort) {
        try {
            ComPort.open();
        } catch (Exception e) {
            isError = true;
            L.e("打开串口失败:没有串口读/写权限! 1秒后重新开启串口");
            mHandler.sendEmptyMessageDelayed(OPEN_SERIALPORT,500);
        }
        if (!isError) {
            L.e("打开串口");
            mStandalone.SendcheckState(); // 获取MCU吗
        }
    }
    /**
     * 从串口接收到的数据
     *
     * @param ComRecData
     */
    private void DispRecData(Bean ComRecData) {
//        L.e("接收到指令 到 从小板返回时间:"+(System.currentTimeMillis()-x)+", --> "+new Date());
        if (ComRecData.bRec.length < 4)
            return;
        String stt = HexToUtil.ByteArrToHex(ComRecData.bRec, ComRecData.bRec.length);
        String str = null;
        if (stt.substring(2, 4) == "90") {
            str = "90";
        } else {
            //获取MCU吗
            if ((stt.substring(0, 4).equals("1180"))) {
                MCU_M = parseID2MCU(stt.substring(16, 40));
            }
            str = mStandalone.up2backgroundserver(stt); //将小板上报的 指令 去掉先导字符和结束字符
        }
        // 主机下发给小板指令之后 小板反馈的相应信息
        mSocket.emit(COMMAND, str);
    }
    /**
     * 将小板上传上来的ID解析成MCU 后台支持8为的MCU吗 所以要将12位的ID，剪成8位的
     * @param stt
     * @return
     */
    private String parseID2MCU(String stt) {
        byte[] source = stt.getBytes();
        byte[] bytes = new byte[8];
        int i = 0;
        int i2 = 0;
        for (i = 0; i < stt.length(); i = i + 2) {
            bytes[i2] = source[i + 1];
            i2++;
            if (i2 == 8) { //8位的MCU
                return new String(bytes);
            }
        }
        return new String(bytes); //12位的MCU
    }

    /**
     * 解析Command指令
     * @param req
     */
    private void CheckListCommand(RequestCommand req){
//        Enumeration<RequestCommand> enu = commands_v.elements();
//        while(enu.hasMoreElements()){
//            RequestCommand rcommand = enu.nextElement();
//            if(req.getCommand().equals(rcommand.getCommand())){
//                if((req.getRequest_time()-rcommand.getRequest_time())<3000){
//                    return;
//                }
//            }
//        }
//        commands_v.add(req);
        Message msg = mHandler.obtainMessage();
        msg.obj = req.getCommand();
        msg.what = COMMAND_int;
        mHandler.sendMessage(msg);
    }
    //----------------------------------------------------串口控制类
    private class SerialControl extends SerialHelper {
        public SerialControl() {
        }
        @Override
        protected void onDataReceived(Bean ComRecData) {
            DispRecData(ComRecData);
        }
    }
}
