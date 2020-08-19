package com.soundleader.apprtctest.beans;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SignalInfo {

    @SerializedName("idx")
    @Expose
    private String idx;
    @SerializedName("roomid")
    @Expose
    private String roomid;
    @SerializedName("encr")
    @Expose
    private String encr;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("from")
    @Expose
    private String from;
    @SerializedName("data")
    @Expose
    private String data;
    @SerializedName("regdate")
    @Expose
    private String regdate;
    @SerializedName("status")
    @Expose
    private String status;

    public String getIdx() {
        return idx;
    }

    public void setIdx(String idx) {
        this.idx = idx;
    }

    public String getRoomid() {
        return roomid;
    }

    public void setRoomid(String roomid) {
        this.roomid = roomid;
    }

    public String getEncr() {
        return encr;
    }

    public void setEncr(String encr) {
        this.encr = encr;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getRegdate() {
        return regdate;
    }

    public void setRegdate(String regdate) {
        this.regdate = regdate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}