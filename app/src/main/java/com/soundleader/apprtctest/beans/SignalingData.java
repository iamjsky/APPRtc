package com.soundleader.apprtctest.beans;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SignalingData {
    @SerializedName("roomid")
    @Expose
    private String roomId;
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

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
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
}
