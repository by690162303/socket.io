package com.example.socketio_client;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.socketio_client.Server.Socket_IO;

/**
 * Created by 白杨 on 2016/3/18.
 */
public class AutoCompleteBroadReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //开启自启服务
        Intent intents = new Intent(context, Socket_IO.class);
        context.startService(intents); // 开启服务
}
}
