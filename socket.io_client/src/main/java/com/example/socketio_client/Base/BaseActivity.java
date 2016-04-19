package com.example.socketio_client.Base;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Created by 白杨 on 2016/3/22.
 */
public class BaseActivity extends Activity{
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
    }
    public  void ShowMessage(String sMsg) {
        Toast.makeText(mContext, sMsg, Toast.LENGTH_SHORT).show();
    }
}
