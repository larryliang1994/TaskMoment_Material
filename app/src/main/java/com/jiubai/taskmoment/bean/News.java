package com.jiubai.taskmoment.bean;

import java.io.Serializable;

/**
 * 消息类
 */
public class News implements Serializable{
    private String senderID;
    private Task task;
    private String title;
    private String content;
    private String time;

    public News() {
    }

    public News(String senderID, Task task, String title, String content, String time) {
        this.senderID = senderID;
        this.task = task;
        this.title = title;
        this.content = content;
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }
}