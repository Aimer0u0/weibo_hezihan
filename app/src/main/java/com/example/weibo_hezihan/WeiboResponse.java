package com.example.weibo_hezihan;

import java.io.Serializable;
import java.util.List;

public class WeiboResponse {
    private int code;
    private String msg;
    private Data data;

    public static class Data {
        private List<Record> records;
        private int total;
        private int size;
        private int current;
        private int pages;

        // Getters and Setters
        public List<Record> getRecords() { return records; }
        public void setRecords(List<Record> records) { this.records = records; }

        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }

        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }

        public int getCurrent() { return current; }
        public void setCurrent(int current) { this.current = current; }

        public int getPages() { return pages; }
        public void setPages(int pages) { this.pages = pages; }
    }

    public static class Record implements Serializable {
        private int id;
        private int userId;
        private String username;
        private String phone;
        private String avatar;
        private String title;
        private String videoUrl;
        private String poster;
        private List<String> images;
        private int likeCount;
        private boolean likeFlag;
        private String createTime;

        // Getters and Setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public int getUserId() { return userId; }
        public void setUserId(int userId) { this.userId = userId; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getAvatar() { return avatar; }
        public void setAvatar(String avatar) { this.avatar = avatar; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getVideoUrl() { return videoUrl; }
        public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

        public String getPoster() { return poster; }
        public void setPoster(String poster) { this.poster = poster; }

        public List<String> getImages() { return images; }
        public void setImages(List<String> images) { this.images = images; }

        public int getLikeCount() { return likeCount; }
        public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

        public boolean isLikeFlag() { return likeFlag; }
        public void setLikeFlag(boolean likeFlag) { this.likeFlag = likeFlag; }

        public String getCreateTime() { return createTime; }
        public void setCreateTime(String createTime) { this.createTime = createTime; }
    }

    // Getters and Setters
    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }

    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }

    public Data getData() { return data; }
    public void setData(Data data) { this.data = data; }
}
