package com.example.socketio_client.Base;

import android.util.Log;

/**
 * Created by 白杨 on 2016/3/21.
 */
public class L {
    /**
     * 是否 发布 程序
     * 如果发布，改成 true
     * 测试使用 false
     */
    private static boolean release = false;
    private static String AA = "aa";
    public static void e(String tag,String msg){
        if(release){
            return;
        }
        Log.e(tag,msg);
    }
    public static void e(String msg){
        if(release){
            return;
        }
        Log.e(AA,msg);
    }
    public static void i(String tag,String msg){
        if(release){
            return;
        }
        Log.i(tag,msg);
    }
    public static void v(String tag,String msg){
        if(release){
            return;
        }
        Log.v(tag,msg);
    }
    public static void d(String tag,String msg){
        if(release){
            return;
        }
        Log.d(tag,msg);
    }
    public static void w(String tag,String msg){
        if(release){
            return;
        }
        Log.w(tag,msg);
    }
}
