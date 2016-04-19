package com.example.socketio_client.Entity;

/**
 * Created by 白杨 on 2016/4/1.
 * 请求实体类，让接收到的指令一条一条去处理
 */
public class RequestCommand {
    private long request_time; //请求时间
    private long response_time; //访问时间
    private boolean isRespose; //是否相应
    private String command; //指令

    public RequestCommand(long request_time,  String command) {
        this.request_time = request_time;
        this.isRespose = false;
        this.command = command;
    }

    public long getRequest_time() {
        return request_time;
    }

    public void setRequest_time(long request_time) {
        this.request_time = request_time;
    }

    public long getResponse_time() {
        return response_time;
    }

    public void setResponse_time(long response_time) {
        this.response_time = response_time;
    }

    public boolean isRespose() {
        return isRespose;
    }

    public void setIsRespose(boolean isRespose) {
        this.isRespose = isRespose;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
