package com.jiubai.taskmoment.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 任务类
 */
public class Task implements Serializable{
    private String id;
    private String portraitUrl;
    private String nickname;
    private String mid;
    private String grade;
    private String desc;
    private String executor;
    private String supervisor;
    private String auditor;
    private ArrayList<String> pictures;
    private ArrayList<Comment> comments;
    private long deadline;
    private long start_time;
    private long create_time;
    private String audit_result;
    private int sendState;

    public static final int SENDING = 0;
    public static final int SUCCESS = 1;
    public static final int FAILED = -1;

    public Task() {
    }

    public Task(String id, String portraitUrl, String nickname, String mid, String grade,
                String desc, String executor, String supervisor, String auditor,
                ArrayList<String> pictures, ArrayList<Comment> comments,
                long deadline, long start_time, long create_time,
                String audit_result, int sendState) {
        this.id = id;
        this.portraitUrl = portraitUrl;
        this.nickname = nickname;
        this.mid = mid;
        this.grade = grade;
        this.desc = desc;
        this.executor = executor;
        this.supervisor = supervisor;
        this.auditor = auditor;
        this.pictures = pictures;
        this.comments = comments;
        this.deadline = deadline;
        this.start_time = start_time;
        this.create_time = create_time;
        this.audit_result = audit_result;
        this.sendState = sendState;
    }

    public String getPortraitUrl() {
        return portraitUrl;
    }

    public void setPortraitUrl(String portraitUrl) {
        this.portraitUrl = portraitUrl;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getExecutor() {
        return executor;
    }

    public void setExecutor(String executor) {
        this.executor = executor;
    }

    public String getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(String supervisor) {
        this.supervisor = supervisor;
    }

    public String getAuditor() {
        return auditor;
    }

    public void setAuditor(String auditor) {
        this.auditor = auditor;
    }

    public ArrayList<String> getPictures() {
        return pictures;
    }

    public void setPictures(ArrayList<String> pictures) {
        this.pictures = pictures;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(ArrayList<Comment> comments) {
        this.comments = comments;
    }

    public long getDeadline() {
        return deadline;
    }

    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }

    public long getStartTime() {
        return start_time;
    }

    public void setStartTime(long start_time) {
        this.start_time = start_time;
    }

    public long getCreateTime() {
        return create_time;
    }

    public void setCreateTime(long create_time) {
        this.create_time = create_time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuditResult() {
        return audit_result;
    }

    public void setAuditResult(String audit_result) {
        this.audit_result = audit_result;
    }

    public int getSendState() {
        return sendState;
    }

    public void setSendState(int sendState) {
        this.sendState = sendState;
    }
}