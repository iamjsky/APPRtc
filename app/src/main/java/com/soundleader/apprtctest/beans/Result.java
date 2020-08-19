package com.soundleader.apprtctest.beans;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Result {

    @SerializedName("candidateInfo")
    @Expose
    private List<CandidateInfo> candidateInfo = null;
    @SerializedName("userdata")
    @Expose
    private Userdata userdata;

    public List<CandidateInfo> getCandidateInfo() {
        return candidateInfo;
    }

    public void setCandidateInfo(List<CandidateInfo> candidateInfo) {
        this.candidateInfo = candidateInfo;
    }

    @SerializedName("status")
    @Expose
    private Status status;
    @SerializedName("room_data")
    @Expose
    private List<RoomData> roomData = null;
    @SerializedName("signalingdata")
    @Expose
    private SignalingData signal;

    @SerializedName("signalinginfo")
    @Expose
    private SignalInfo signalInfo;

    public SignalInfo getSignalInfo() {
        return signalInfo;
    }

    public void setSignalInfo(SignalInfo signalInfo) {
        this.signalInfo = signalInfo;
    }

    public List<RoomData> getRoomData() {
        return roomData;
    }

    public void setRoomData(List<RoomData> roomData) {
        this.roomData = roomData;
    }

    public Userdata getUserdata() {
        return userdata;
    }

    public void setUserdata(Userdata userdata) {
        this.userdata = userdata;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public SignalingData getSignal() {
        return signal;
    }

    public void setSignal(SignalingData signal) {
        this.signal = signal;
    }
}