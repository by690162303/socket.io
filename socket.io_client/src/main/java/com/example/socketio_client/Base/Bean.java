package com.example.socketio_client.Base;

import java.text.SimpleDateFormat;
import java.util.Arrays;

/**
 * Created by 白杨 on 2016/3/21.
 */
public class Bean {
    public byte[] bRec=null;
    public String sRecTime="";
    public String sComPort="";
    public Bean(String sPort,byte[] buffer,int size){
        sComPort=sPort;
        bRec=new byte[size];
        for (int i = 0; i < size; i++)
        {
            bRec[i]=buffer[i];
        }
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd  hh:mm:ss");
        sRecTime = sDateFormat.format(new java.util.Date());
    }

    @Override
    public String toString() {
        return "Bean{" +
                "bRec=" + Arrays.toString(bRec) +
                ", sRecTime='" + sRecTime + '\'' +
                ", sComPort='" + sComPort + '\'' +
                '}';
    }
}
