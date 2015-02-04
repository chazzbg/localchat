package com.wordlfochazz.localchat.objects;

import java.io.Serializable;


public class MessageObject implements Serializable {
    public static final int TYPE_INCOMING = 0;
    public static final int TYPE_OUTGOING = 1;

    private String senderId;
    private String receiverId;
    private String message;
    private int type;
    private long time;
    private boolean has_file;
    private String sent_file;


    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean hasFile() {
        return has_file;
    }

    public void setHasFile(boolean has_file) {
        this.has_file = has_file;
    }

    public String getSent_file() {
        return sent_file;
    }

    public void setSent_file(String sent_file) {
        this.sent_file = sent_file;
    }

    @Override
    public String toString() {
        return "MessageObject{" +
                "senderId='" + senderId + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}

