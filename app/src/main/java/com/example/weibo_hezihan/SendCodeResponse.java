package com.example.weibo_hezihan;

public class SendCodeResponse {
    private int code;
    private String msg;
    private boolean data;

    // Getters and Setters
    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }

    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }

    public boolean isData() { return data; }
    public void setData(boolean data) { this.data = data; }
}