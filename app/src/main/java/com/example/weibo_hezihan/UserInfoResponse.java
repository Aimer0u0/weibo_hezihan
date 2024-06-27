package com.example.weibo_hezihan;

public class UserInfoResponse {
    private int code;
    private String msg;
    private Data data;

    public static class Data {
        private int id;
        private String username;
        private String phone;
        private String avatar;
        private boolean loginStatus;

        // Getters and Setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getAvatar() { return avatar; }
        public void setAvatar(String avatar) { this.avatar = avatar; }

        public boolean isLoginStatus() { return loginStatus; }
        public void setLoginStatus(boolean loginStatus) { this.loginStatus = loginStatus; }
    }

    // Getters and Setters
    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }

    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }

    public Data getData() { return data; }
    public void setData(Data data) { this.data = data; }
}
