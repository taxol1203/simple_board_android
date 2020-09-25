package com.example.fcm_test;

public class PersonalData {
    private String member_idx;
    private String member_writer;
    private String member_title;
    private String member_date;
    private String member_hit;

    public String getMember_idx() {
        return member_idx;
    }
    public String getMember_title() {
        return member_title;
    }
    public String getMember_writer() {
        return member_writer;
    }
    public String getMember_date() {
        return member_date;
    }
    public String getMember_hit() {
        return member_hit;
    }

    public void setMember_idx(String member_idx) {
        this.member_idx = member_idx;
    }

    public void setMember_writer(String member_writer) {
        this.member_writer = member_writer;
    }

    public void setMember_title(String member_title) {
        this.member_title = member_title;
    }
    public void setMember_date(String member_date) {
        this.member_date = member_date;
    }
    public void setMember_hit(String member_hit) {
        this.member_hit = member_hit;
    }
}
