package com.soundleader.apprtctest.usb;

import android.annotation.TargetApi;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.M)
public class MIDIPrinter {
    public static final String TAG = "sldebug";
    public static final String[] CHANNEL_COMMAND_NAMES = { "o", "i",
            "PolyTouch", "Control", "Program", "Pressure", "Bend" };
    public static final String[] SYSTEM_COMMAND_NAMES = { "SysEx", // F0
            "TimeCode",    // F1
            "SongPos",     // F2
            "SongSel",     // F3
            "F4",          // F4
            "F5",          // F5
            "TuneReq",     // F6
            "EndSysex",    // F7
            "TimingClock", // F8
            "F9",          // F9
            "Start",       // FA
            "Continue",    // FB
            "Stop",        // FC
            "FD",          // FD
            "ActiveSensing", // FE
            "Reset"        // FF
    };

    public static String getName(int status) {
        if (status >= 0xF0) {
            int index = status & 0x0F;
            return SYSTEM_COMMAND_NAMES[index];
        } else if (status >= 0x80) {
            int index = (status >> 4) & 0x07;
            return CHANNEL_COMMAND_NAMES[index];
        } else {
            return "data";
        }
    }

}
