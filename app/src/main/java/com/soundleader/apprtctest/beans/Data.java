package com.soundleader.apprtctest.beans;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Data {
    @SerializedName("code")
    @Expose
    private Integer code;
    @SerializedName("result")
    @Expose
    private Integer result;

}
