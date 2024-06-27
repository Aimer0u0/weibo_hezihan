package com.example.weibo_hezihan;

public class LoginResponse {//登录响应
    private int code;
    private String msg;
    private String data;

    // Getters and Setters
    public int getCode() { return code; }//获取code
    public void setCode(int code) { this.code = code; }//设置code

    public String getMsg() { return msg; }//获取msg
    public void setMsg(String msg) { this.msg = msg; }//设置msg

    public String getData() { return data; }//获取data
    public void setData(String data) { this.data = data; }//设置data
}