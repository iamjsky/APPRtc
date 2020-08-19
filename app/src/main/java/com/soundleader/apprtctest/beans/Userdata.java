package com.soundleader.apprtctest.beans;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Userdata {
    @SerializedName("idx")
    @Expose
    private Integer idx;
    @SerializedName("user_id")
    @Expose
    private String userId;
    @SerializedName("user_type")
    @Expose
    private String userType;

    @SerializedName("profile_img")
    @Expose
    private String userProfile;

    public String getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(String userProfile) {
        this.userProfile = userProfile;
    }

    public Integer getIdx() {
        return idx;
    }

    public void setIdx(Integer idx) {
        this.idx = idx;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }



}