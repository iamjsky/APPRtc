package com.soundleader.apprtctest.usb;

import android.annotation.TargetApi;
import android.media.midi.MidiDeviceInfo;
import android.os.Build;

public class MidiInfo {

    MidiDeviceInfo mInfo;
    int mType;
    int mIndex;

    public MidiInfo(MidiDeviceInfo info){
        mInfo = info;
    }
    public MidiInfo(MidiDeviceInfo info, int index) {
        mInfo = info;
        mIndex = index;
    }

    public MidiDeviceInfo getInfo() {
        return mInfo;
    }

    public int getIndex() {
        return mIndex;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public String getKey() {
        return mInfo.getId() + "_" + mIndex;
    }


}
