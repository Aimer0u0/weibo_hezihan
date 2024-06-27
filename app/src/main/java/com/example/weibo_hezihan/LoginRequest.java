package com.example.weibo_hezihan;

public class LoginRequest {// 用于发送登录请求的数据类
    private String phone;
    private String smsCode;

    public LoginRequest(String phone, String smsCode) {
        this.phone = phone;
        this.smsCode = smsCode;
    }

    // Getters and Setters
    public String getPhone() { return phone; }// 获取手机号
    public void setPhone(String phone) { this.phone = phone; }// 设置手机号

    public String getSmsCode() { return smsCode; }// 获取短信验证码
    public void setSmsCode(String smsCode) { this.smsCode = smsCode; }// 设置短信验证码
}
