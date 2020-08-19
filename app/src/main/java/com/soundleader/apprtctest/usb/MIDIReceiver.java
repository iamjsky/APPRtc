package com.soundleader.apprtctest.usb;

import android.annotation.TargetApi;
import android.media.midi.MidiReceiver;
import android.os.Build;

import com.soundleader.apprtctest.usb.midi.MidiConstants;

import java.io.IOException;

@TargetApi(Build.VERSION_CODES.M)
public class MIDIReceiver extends MidiReceiver {
    public static final String TAG = "sldebug";

    public interface ReceiveCallback {
        void onDataReceive(String msg);
        void onMsgReceive(String msg);
    }

    private ReceiveCallback mCallback;

    public MIDIReceiver(ReceiveCallback callback) {
        mCallback = callback;
    }



    @Override
    public void onSend(byte[] bytes, int offset, int count, long timestamp) throws IOException {
        if (mCallback != null) {

            byte[] tempData = bytes;
            byte statusByte = bytes[offset++];

            int status = statusByte & 0xFF;

            int numData = MidiConstants.getBytesPerMessage(statusByte) - 1; //TEST용

            StringBuilder sb = new StringBuilder();
            // 0xF0 이상 부터 Control Event
            // 0x80, 0x90 (noteOFF/ON) 만 사용
            if(status == 0x80 || status == 0x90){
                sb.append("s/");

                //세기가 0 이면 NOTE OFF
                if(tempData[3] == 0){
                    sb.append("o");
                }else{
                    sb.append(MIDIPrinter.getName(status));
                }


                sb.append("/");
                sb.append(tempData[2]-20);
                sb.append("/e");
            }else {
                return;

//                Log.e(TAG, "tempData.length : " + tempData.length);
//                Log.e(TAG, "tempData[0] : " + tempData[0] + " tempData[1] : " + tempData[1] + " tempData[2] : " + tempData[2] + " tempData[3] : " + tempData[3]);
//                Log.e(TAG, "tempData[0] : " + String.format("%02X ", tempData[0]) + " tempData[1] : " + String.format("%02X ", tempData[1]) +
//                " tempData[2] : " + String.format("%02X ", tempData[2]) + " tempData[3] : " + String.format("%02X ", tempData[3]));
            }
            mCallback.onDataReceive(sb.toString());
        }
    }

}
