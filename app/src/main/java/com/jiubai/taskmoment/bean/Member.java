package com.jiubai.taskmoment.bean;

/**
 * 成员类
 */
public class Member {
    private String name;
    private String mobile;
    private String id;
    private String mid;

    public Member(String name, String mobile, String id, String mid) {
        this.name = name;
        this.mobile = mobile;
        this.id = id;
        this.mid = mid;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }
}