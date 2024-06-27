package com.example.weibo_hezihan;


public class SendCodeRequest {
    private String phone;

    public SendCodeRequest(String phone) {
        this.phone = phone;
    }

    // Getter and Setter
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}