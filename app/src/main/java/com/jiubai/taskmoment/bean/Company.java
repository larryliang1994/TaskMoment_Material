package com.jiubai.taskmoment.bean;

/**
 * 公司类
 */
public class Company {
    private String name;
    private String cid;
    private String creator;

    public Company(String name, String cid, String creator) {
        this.name = name;
        this.cid = cid;
        this.creator = creator;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }
}