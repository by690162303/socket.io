package com.example.socketio_client.Socket_Application;

import android.app.Application;

import com.example.socketio_client.Base.Constants;
import com.example.socketio_client.Base.L;
import com.tencent.bugly.crashreport.CrashReport;

import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.socket.client.IO;
import io.socket.client.Socket;

/**
 * Created by 白杨 on 2016/3/18.
 */
public class Socket_Application extends Application {
    private Socket mSocket;
    public Socket getmSocket() {
        return mSocket;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        CrashReport.initCrashReport(getApplicationContext(), "900026814", false);
        try {
            initSSLContext();//
            mSocket = IO.socket(Constants.CHAT_SERVER_URL);//连接服务器
        } catch (URISyntaxException e) {
            e.printStackTrace();
            L.e("Socket_Application", "错误：" + e);
        }}
    /**
     * 不验证证书，信任所有证书
     */
    private void initSSLContext(){
        try{
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new MyTrustManager()}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new MyHostnameVerifier());
        }catch(Exception e){
      e.printStackTrace();
        }
    }



    private class MyHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            // TODO Auto-generated method stub
            return true;
        }

    }

    private class MyTrustManager implements X509TrustManager {

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)

                throws CertificateException {
            // TODO Auto-generated method stub
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            // TODO Auto-generated method stub
            return null;
        }

    }


}
