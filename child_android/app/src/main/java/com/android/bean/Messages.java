package com.android.bean;

public class Messages {
    private String id;
    private String acceptUserLogin;
    private String content;
    private String sendTime;
    private String sendUserLogin;
    private int subscribeValue;
    private int type;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAcceptUserLogin() {
        return acceptUserLogin;
    }

    public void setAcceptUserLogin(String acceptUserLogin) {
        this.acceptUserLogin = acceptUserLogin;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    public String getSendUserLogin() {
        return sendUserLogin;
    }

    public void setSendUserLogin(String sendUserLogin) {
        this.sendUserLogin = sendUserLogin;
    }

    public int getSubscribeValue() {
        return subscribeValue;
    }

    public void setSubscribeValue(int subscribeValue) {
        this.subscribeValue = subscribeValue;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
