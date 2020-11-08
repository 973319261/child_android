package com.android.model;

/**
 * 消息事件
 */
public class MessageEvent {

    private int msgType;
    private String message;
    private Object payload;

    public MessageEvent(int msgType, String message) {
        this(msgType, message, null);
    }

    public MessageEvent(int msgType, Object payload) {
        this(msgType, null, payload);
    }

    public MessageEvent(int msgType, String message, Object payload) {
        this.message = message;
        this.msgType = msgType;
        this.payload = payload;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }
}
