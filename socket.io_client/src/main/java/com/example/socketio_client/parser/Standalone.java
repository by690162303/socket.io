package com.example.socketio_client.parser;

import android.os.Handler;
import android.os.Message;

import com.example.socketio_client.Base.L;
import com.example.socketio_client.Base.SerialHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by 白杨 on 2016/3/22.
 * 解析单机版上传的字符串
 * 接收的字符端长度为28为字节
 */
public class Standalone {
    private SerialHelper mSerialHelper;
    private static final long CHECKLONGSTATE = 8000; //重复下发指令的时间间隔
    private static final int SENDSTATE = 0; //用来标识执行的什么方法
    private static final int SENDPWD = 1;
    private static final int SENDIC = 2;
    private static final int SENDID = 3;
    private static final int SENDCLEAN = 4;
    private  int limit = 0;
    private final String ADD_ZERRO = "000000000000"; //无用 填充0
    private boolean isSendSTATE = false; //判断是否下发成功
    private boolean isSendPWD = false;
    private boolean isSendIC = false;
    private boolean isSendID = false;
    private boolean isSendClean = false;
    private String content1 = null;
    private String content2 = null;
    private String content3 = null;
    private String content4 = null;
    private String content5 = null;
    private List<String> listQueue = new ArrayList<>(); //密码指令队列
    private List<String> listQueue_IC = new ArrayList<>(); //密码指令队列
    private List<String> listQueue_ID = new ArrayList<>(); //密码指令队列
    private Handler mHandler = new Handler() {
        @Override
        public  void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SENDSTATE:
                    if (!isSendSTATE) {
                        if (msg.obj != null) {
                            content1 = (String) msg.obj;
                        }
                        mSerialHelper.sendHex(content1);
                        mHandler.sendEmptyMessageDelayed(SENDSTATE, CHECKLONGSTATE);
                    }else{
                        isSendSTATE = false;
                    }
                    break;
                case SENDPWD:
                    if (!isSendPWD) {
                        if (msg.obj != null) {
                            content2 = (String) msg.obj;
                        }
                        L.e("密码没有反馈继续下发");
                        mSerialHelper.sendHex(content2);
                        mHandler.sendEmptyMessageDelayed(SENDPWD, CHECKLONGSTATE);
                    }else{
                        if(listQueue.size()!=0){
                            Message mmsg = mHandler.obtainMessage();
                            mmsg.obj = listQueue.get(0);
                            mmsg.what = SENDPWD;
                            mHandler.sendMessage(mmsg);
                        }
                        isSendPWD = false;
                    }
                    break;
                case SENDIC:
                    if (!isSendIC) {
                        if (msg.obj != null) {
                            content3 = (String) msg.obj;
                        }
                        mSerialHelper.sendHex(content3);
                        mHandler.sendEmptyMessageDelayed(SENDIC, CHECKLONGSTATE);
                    }else{
                        if(listQueue_IC.size()!=0){
                            Message mmsg2 = mHandler.obtainMessage();
                            mmsg2.obj = listQueue_IC.get(0);
                            mmsg2.what = SENDPWD;
                            mHandler.sendMessage(mmsg2);
                        }
                        isSendIC = false;
                    }
                    break;
                case SENDID:
                    if (!isSendID) {
                        if (msg.obj != null) {
                            content4 = (String) msg.obj;
                        }
                        mSerialHelper.sendHex(content4);
                        mHandler.sendEmptyMessageDelayed(SENDID, CHECKLONGSTATE);
                    }else{
                        if(listQueue_ID.size()!=0){
                            Message mmsg3 = mHandler.obtainMessage();
                            mmsg3.obj = listQueue_ID.get(0);
                            mmsg3.what = SENDPWD;
                            mHandler.sendMessage(mmsg3);
                        }
                        isSendID = false;
                    }
                    break;
                case SENDCLEAN:
                    if (!isSendClean) {
                        if (msg.obj != null) {
                            content5 = (String) msg.obj;
                        }
                        mSerialHelper.sendHex(content5);
                        mHandler.sendEmptyMessageDelayed(SENDCLEAN, CHECKLONGSTATE);
                    }else{
                        isSendClean = false;
                    }
                    break;
            }
        }
    };

    public Standalone(SerialHelper mSerialHelper) {
        this.mSerialHelper = mSerialHelper;
    }

    //开机和重新启动时单片机自动上报单片机状态


    public void showMSG(String msg) {
        L.e(msg);
    }

    /**
     * 下发检查状态指令
     *
     * @return xx
     */
    public void SendcheckState() {
        // 11 80（单片机状态）1603221043（年月日分秒）【12位单片机唯一ID】【5填充0无实际意义】12（结束符）
        String content = "1100" + ADD_ZERRO + ADD_ZERRO + ADD_ZERRO + ADD_ZERRO + "0012";
        Message msg = mHandler.obtainMessage();
        msg.obj = content;
        msg.what = SENDSTATE;
        if(!isSendSTATE) {
            mHandler.sendMessageDelayed(msg, CHECKLONGSTATE);
        }
        //若是密码没有下发 要重新下发
        L.e("下发查询状态指令:" + content);
        mSerialHelper.sendHex(content);
    }


    /**
     * 下发密码
     *
     * @param pwd_state  密码类型：01.超级管理员、02.普通密码1、03.普通密码2、04.临时密码新增、05.保洁员密码(待定)、11.超级管理员密码修改、10.普通级密码2修改
     * @param pwd_length 密码长度 最大16位 （06、16）
     * @param pwd        密码内容
     * @param startTime  密码有效日期开始时间
     * @param overTime   密码有效日期结束时间
     */
    public void SendPassWord(String pwd_state, String pwd_length, String pwd, String startTime, String overTime) {
        //1101 00（密码编号 0 为新增密码）
        if (startTime == "" || startTime == null) {
            startTime = ADD_ZERRO;
        }else{
            startTime = StringToHex(startTime);
        }
        if (overTime == "" || overTime == null) {
            overTime = ADD_ZERRO;
        }else{
            overTime = StringToHex(overTime);
        }
        String content = "110100" + pwd_state + pwd_length + pwd + startTime + overTime + "000012";
        Message msg = mHandler.obtainMessage();
        msg.obj = content;
        msg.what = SENDPWD;
        //若是密码没有下发 要重新下发
        if(listQueue.size()==0) {
            L.e("下发 = = = = = = = = = = = = = 指令:" + content);
            if(!isSendPWD) {
                mHandler.sendMessageDelayed(msg, CHECKLONGSTATE);
            }
            listQueue.add(content);
            mSerialHelper.sendHex(content);
        }else{
            if(!content.equals( listQueue.get(0))){
                listQueue.add(content);
                L.e("添加+++++++++++++++++++++++指令:" + content);
            }else{
                L.e("重复 -----》》 密码指令  ————————》》 无效删除");
                return;
            }
        }

    }
    /**
     * 下发IC卡
     *
     * @param IC_content 卡内容
     * @param startTime  卡有效开始时间
     * @param overTime   卡有效结束时间
     */
    public void SendICcard(String IC_content, String startTime, String overTime) {
        //主机需检查是否返回OK。如果几秒钟后没有返回，则主机需要重发
        //1101 02（IC编号 卡编号）。
        // 卡编号 是自增 还是？？？？
        if (IC_content.length() < 8) {
            int a = (8 - IC_content.length());
            for (int i = 0; i < a; i++) {
                IC_content = IC_content + "0";
            }
        }
        if (startTime == "" || startTime == null) {
            startTime = ADD_ZERRO;
        }
        if (overTime == "" || overTime == null) {
            overTime = ADD_ZERRO;
        }
        String con = "110202" + IC_content + startTime + overTime + ADD_ZERRO + "000012";
        Message msg = mHandler.obtainMessage();
        msg.obj = con;
        msg.what = SENDIC;

        if(listQueue_IC.size()==0) {
            L.e("下发 = = = = = = = = = = = = = IC 指令:" + con);
            if(!isSendIC)
                mHandler.sendMessageDelayed(msg, CHECKLONGSTATE);
            listQueue_IC.add(con);
            mSerialHelper.sendHex(con);
        }else{
            if(!con.equals( listQueue.get(0))){
                listQueue_IC.add(con);
                L.e("添加+++++++++++++++++++++++指令:" + con);
            }else{
                L.e("重复 -----》》 密码指令  ————————》》 无效删除");
                return;
            }
        }
    }

    /**
     * 下发身份证
     *
     * @param ID_content 卡内容
     * @param startTime  卡有效开始时间
     * @param overTime   卡有效结束时间
     */
    public void SendIDcard(String ID_content, String startTime, String overTime) {
        //1101 02（ID编号 卡编号）
        if (startTime == "" || startTime == null) {
            startTime = ADD_ZERRO;
        }else{
            startTime = StringToHex(startTime);
        }
        if (overTime == "" || overTime == null) {
            overTime = ADD_ZERRO;
        }else{
            overTime = StringToHex(overTime);
        }
        String fill = "00000"; //填充
        String con = "110300" + ID_content + startTime + overTime + fill + "000012";
        Message msg = mHandler.obtainMessage();
        msg.obj = con;
        msg.what = SENDID;
        if(listQueue_ID.size()==0) {
            L.e("下发ID指令:" + con);
            if(!isSendID)
                mHandler.sendMessageDelayed(msg, CHECKLONGSTATE);
            listQueue_ID.add(con);
            mSerialHelper.sendHex(con);
        }else{
            if(!con.equals( listQueue.get(0))){
                listQueue_ID.add(con);
                L.e("添加+++++++++++++++++++++++指令:" + con);
            }else{
                L.e("重复 -----》》 密码指令  ————————》》 无效删除");
                return;
            }
        }
    }

    /**
     * 下发当前时间 yyMMddHHmmss
     */
    public void SendDATA(String str) {
        L.e("矫正时间：" + str+", "+  StringToHex(str));
        str = StringToHex(str);
        mSerialHelper.sendHex("1110" + str + ADD_ZERRO + ADD_ZERRO + ADD_ZERRO + "0012");
    }

    /**
     * 下发清除密码
     *
     * @param order 01.总清所有密码及IC\ID、02.清楚密码、03.清楚全部IC卡、04.清楚身份证
     */
    public void SendCleanerPWD(String order) {
        String con = "1111" + order + ADD_ZERRO + ADD_ZERRO + ADD_ZERRO + ADD_ZERRO + "12";
        Message msg = mHandler.obtainMessage();
        msg.obj = con;
        msg.what = SENDCLEAN;
        mHandler.sendMessageDelayed(msg, CHECKLONGSTATE);
        //若是密码没有下发 要重新下发
        L.e("下发清楚密码指令:" + con);
        mSerialHelper.sendHex(con);
    }

    /**
     * 解析后台传过来的指令
     *
     * @param order
     */
    public void parseBackGroundOrder(String order) {
        String str = order.substring(0, 4);
        switch (str) {
            case "0101":
                //超级密码下发 8
                String content = order.substring(4, 12);
                String con = CompressBCD(content);
                String time = order.substring(12, 24);
                L.e("超级密码：" + con);
                SendPassWord("01", "08", con, time, null);
                break;
            case "0102":
                //普通密码1下发6
                String content2 = order.substring(4, 10);

                String con2 = fill2PWD(CompressBCD(content2));
                String starttime = order.substring(10, 22);
                L.e("普通密码1（BCD）：" + con2);
                SendPassWord("02", "06", con2, starttime, null);
                break;
            case "0103":
                //普通密码2下发6
                String content3 = order.substring(4, 10);
                String con3 = fill2PWD(CompressBCD(content3));
                String starttime3 = order.substring(10, 22);
                String overttime = order.substring(22, 34);
                L.e("普通密码2（BCD）：" + con3 + "开始时间：" + starttime3 + ",结束时间：" + overttime);
                SendPassWord("03", "06", con3, starttime3, overttime);
                break;
            case "0104":
                //临时密码下发 5
                String content4 = order.substring(4, 9);
                String con4 = fill2PWD(CompressBCD(content4));
                String s = order.substring(10, 11);
                SimpleDateFormat simple = new SimpleDateFormat("yyMMddHHmmss");
                //小写的h是12小时制 大写的H是24小时制
                Date date = new Date();
                String stt = simple.format(date);
                int tt = Integer.parseInt(s);
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);//date 换成已经已知的Date对象
                cal.add(Calendar.HOUR_OF_DAY, +tt);// before 8 hour
                SimpleDateFormat format = new SimpleDateFormat("yyMMddHHmmss");
                String overTime4 = format.format(cal.getTime());
                L.e("临时密码（BCD）：" + con4 + "，有效时间：" + s + "," + stt + "-" + overTime4);
                SendPassWord("04", "05", con4, null, null);
                break;
            case "0105":
                //保洁密码下发6
                String content5 = order.substring(4, 10);
                String con5 = fill2PWD(CompressBCD(content5));
                String starttime5 = order.substring(10, 22);
                String overttime5 = order.substring(22, 34);
                L.e("保洁密码（BCD）：" + con5 + "，开始时间：" + starttime5 + ",结束时间:" + overttime5);
                SendPassWord("03", "06", con5, starttime5, overttime5);
                break;
            case "0201":
                //IC下发4
                String content6 = order.substring(4, 8);
                String starttime6 = order.substring(8, 20);
                L.e("IC卡：" + content6 + ",时间：" + starttime6);
                SendICcard(content6, starttime6, null);
                break;
            case "0301":
                //ID下发8
                String content7 = order.substring(4, 12);
                String time7 = order.substring(12, 24);
                L.e("ID卡：" + content7 + ",时间：" + time7);
                SendIDcard(content7, time7, null);
                break;
            case "110112":
                //总清8
                L.e("——————》  清楚所有密码 《——————");
                SendCleanerPWD("01");
                break;
        }
    }

    public void setmSerialHelper(SerialHelper mSerialHelper) {
        this.mSerialHelper = mSerialHelper;
    }

    /**
     * 将时间转换为十六进制
     * @param stt 160411...
     * @return 10040B...
     */
    public String StringToHex(String stt){
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < stt.length();i+=2) {
            int x = Integer.valueOf(stt.substring(i, i + 2));
            String st = Integer.toHexString(x);
            if (st.length() == 1) {
                st = "0" + st;
            }
            builder.append(st);
        }
        return builder.toString();
    }

    /**
     * 压缩BCD码 高位在前低位再后 补齐密码尾数 为 8位 不够 补 0
     *
     * @return
     */
    private String CompressBCD(String pwd) {
        L.e("压缩BCD 之前的密码：" + pwd);
        byte[] bytes = pwd.getBytes();
        int i = 0;
        byte temp = '0';
        for (i = 0; i < pwd.length(); i = i + 2) {
            if ((i + 1) < pwd.length()) {
                temp = bytes[i];
                bytes[i] = bytes[i + 1];
                bytes[i + 1] = temp;
            } else {
                temp = bytes[i];
                bytes[i] = '0';

            }
        }
        if ((bytes.length) % 2 == 0) {
            //偶数
            L.e("压缩BCD 码：" + new String(bytes));
            return fill2PWD(new String(bytes));
        } else {
            //奇数
            L.e("压缩BCD 码 2：" + new String(bytes) + (char) (temp));
            return fill2PWD(new String(bytes) + (char) (temp));
        }

    }

    /**
     * 补齐密码 到8位
     *
     * @param stt
     * @return
     */
    private String fill2PWD(String stt) {
        if (stt.length() < 16) {
            int a = (16 - stt.length());
            for (int i = 0; i < a; i++) {
                stt = stt + "0";
            }
        }
        return stt;
    }

    public String up2backgroundserver(String order) {
//        L.e("接收到的指令："+order);
        String ff = order.substring(0, 6);
        switch (ff){
            case "11AA01":
                L.e("下发密码反馈：" + order + ", 当前第 " + listQueue.size());
                //删除队列第一个指令，将下一条指令，发送出去
                if (listQueue.size() != 0) {
                    listQueue.remove(0);
                }
                isSendPWD = true;
                break;
            case "11AA02":
                L.e("IC反馈：" + order);
                if (listQueue_IC.size() != 0)
                    listQueue_IC.remove(0);
                isSendIC = true;
                break;
            case "11AA03":
                if (listQueue_ID.size() != 0)
                    listQueue_ID.remove(0);
                isSendID = true;
                L.e("ID反馈：" + order);
                break;
        }
            String aa = order.substring(0, 4);
            switch (aa) {
                //118101
                case "1180":
                    L.e("状态查询反馈：" + order);
                    isSendSTATE = true;
                    break;
                case "1181":
                    L.e("密码开门："+order);
                    break;
                case "1182":
                    //下发IC上报
                    //需要检查是否返回OK,如果几秒后没有返回，则主机需要重发
                case "1183":
                    //下发ID上报
                    //需要检查是否返回OK,如果几秒后没有返回，则主机需要重发
                case "11":
                    //下发清除密码上报
                    //需要检查是否返回OK,如果几秒后没有返回，则主机需要重发
                    L.e("总清反馈：" + order);
                    isSendClean = true;
                    break;
            }
            return order.substring(2, 54);
        }

}
