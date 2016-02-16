package com.jiubai.taskmoment.bean;

import java.io.Serializable;

/**
 * 评论类
 */
public class Comment implements Serializable{
    private String taskId;
    private String sender;
    private String senderId;
    private String receiver;
    private String receiverId;
    private String content;
    private long time;

    public Comment() {
    }

    public Comment(String taskId, String sender, String senderId, String content, long time) {
        this.taskId = taskId;
        this.sender = sender;
        this.senderId = senderId;
        this.content = content;
        this.time = time;
    }

    public Comment(String taskId, String sender, String senderId,
                   String receiver, String receiverId,
                   String content, long time) {
        this.taskId = taskId;
        this.sender = sender;
        this.senderId = senderId;
        this.receiver = receiver;
        this.receiverId = receiverId;
        this.content = content;
        this.time = time;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}