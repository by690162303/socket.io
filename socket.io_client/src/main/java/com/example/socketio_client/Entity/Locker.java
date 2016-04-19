package com.example.socketio_client.Entity;

/**
 * Created by 白杨 on 2016/3/23.
 */
public class Locker {
    private String id; //唯一标识
    private String mcu; //MCU
    private String city; //市
    private String district; //区
    private String address;  //地址
    private String pwd;  //普通密码1
    private String pwd2; //普通密码2
    private String temp; //临时密码
    private String startdate;  //密码有效开始时间
    private String overdate;   //密码有效结束时间
    private String ICcard;

    public String getICcard() {
        return ICcard;
    }

    public void setICcard(String ICcard) {
        this.ICcard = ICcard;
    }

    public Locker() {
        this(null);
    }

    public Locker(String id) {
        this(id,null);

    }

    public Locker(String id, String mcu) {
        this(id,mcu,null);
    }

    public Locker(String id, String mcu, String pwd) {
        this(id,mcu,pwd,null);
    }

    public Locker(String id, String pwd, String pwd2, String mcu) {
        this(id,mcu,pwd,pwd2,null);
    }

    public Locker(String id, String mcu, String pwd, String pwd2, String temp) {
        this.id = id;
        this.mcu = mcu;
        this.pwd = pwd;
        this.pwd2 = pwd2;
        this.temp = temp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMcu() {
        return mcu;
    }

    public void setMcu(String mcu) {
        this.mcu = mcu;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getPwd2() {
        return pwd2;
    }

    public void setPwd2(String pwd2) {
        this.pwd2 = pwd2;
    }

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public String getStartdate() {
        return startdate;
    }

    public void setStartdate(String startdate) {
        this.startdate = startdate;
    }

    public String getOverdate() {
        return overdate;
    }

    public void setOverdate(String overdate) {
        this.overdate = overdate;
    }
}
