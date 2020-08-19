package com.soundleader.apprtctest.usb;

import android.app.Activity;
import android.content.Context;
import android.media.midi.MidiManager;
import android.media.midi.MidiReceiver;
import android.util.Log;

import com.soundleader.apprtctest.usb.midi.MidiConstants;

import java.io.IOException;

public class USBManager {

    public static final int CODE_CONNECTED = 1;
    public static final int CODE_DISCONNECTED = 0;

    String TAG = "USB_M_L";

    Activity _activity;

    MidiManager _midiManager;
    MIDIReceiver _receiver;
    private MIDISelector _selector;

    MIDIReceiver.ReceiveCallback _dataReceiver;
    MIDISelector.ConnectCallback _connectCallback;

    private static USBManager _instance;

    boolean isOutputConnected = false;
    boolean isInputConnected = false;

    public static USBManager getInstance(Activity activity, MIDIReceiver.ReceiveCallback callback1, MIDISelector.ConnectCallback callback2) {
        if (_instance == null) {
            _instance = new USBManager(activity, callback1, callback2);
        } else {
            _instance._activity = activity;
            _instance._dataReceiver = callback1;
            _instance._connectCallback = callback2;
            _instance._selector.onResum();
        }

        return _instance;
    }


    private USBManager(Activity activity, MIDIReceiver.ReceiveCallback callback1, MIDISelector.ConnectCallback callback2) {
        _activity = activity;
        _midiManager = (MidiManager) _activity.getSystemService(Context.MIDI_SERVICE);

        _dataReceiver = callback1;
        _connectCallback = callback2;

        _receiver = new MIDIReceiver(dataReceiver);
        _selector = new MIDISelector(_midiManager, _activity, connectCallback, _receiver);
        _selector.onResum();
    }

    public void startConnectProcess() {
        _selector.onResum();
        _selector.connectUSB(_activity);
        Log.d(TAG, "startConnectProcess");
    }

    public void disconnect() {
        _selector.close();
    }

    public boolean isConnected() {

        return (isOutputConnected && isInputConnected);

    }

    public void onResume() {
        if (_selector != null)
            _selector.onResum();
    }

    public void onPause() {
        if (_selector != null)
            _selector.onPause();
    }

    public void sendNoteOnTest() {
        onSend("s/i/r/20/30/40/50/60/70/e");
    }

    public void sendNoteOnTest2() {
        onSend("s/i/r/60/70/e");
    }

    public void onSend(String data) {

        String[] tempString = data.split("/");
        _connectCallback.onReturnedMsg(data + " count : " + tempString.length);
        int noteOnOff = 0;
        int channel = 0;

        for (int i = 0; i < tempString.length; i++) {
            _connectCallback.onReturnedMsg("[" + i + "]" + tempString[i]);

            if (tempString[i].equals("i")) {
                noteOnOff = 0;
            } else if (tempString[i].equals("o")) {
                noteOnOff = 1;
            } else if (tempString[i].equals("r")) {
                channel = 0;
            } else if (tempString[i].equals("l")) {
                channel = 1;
            } else if (!tempString[i].equals("s") && !tempString[i].equals("/") && !tempString[i].equals("i")
                    && !tempString[i].equals("o") && !tempString[i].equals("e") && !tempString[i].equals("r")
                    && !tempString[i].equals("l") && !tempString[i].equals("k") && !tempString[i].equals("00")) {
                try {
                    if (noteOnOff == 0) {
                        noteOn(channel, (Integer.parseInt(tempString[i]) + 20), 127);
                    }
                    if (noteOnOff == 1) {
                        noteOff(channel, (Integer.parseInt(tempString[i]) + 20), 0);
                    }
                } catch (Exception e) {
                    _connectCallback.onReturnedMsg("[E] " + e.getMessage());
                }
            }
        }
    }


    private void noteOff(int channel, int pitch, int velocity) {
        midiCommand(MidiConstants.STATUS_NOTE_OFF + channel, pitch, velocity);
    }


    private void noteOn(int channel, int pitch, int velocity) {
        midiCommand(MidiConstants.STATUS_NOTE_ON + channel, pitch, velocity);
    }

    private void midiCommand(int status, int data1, int data2) {
        byte[] mByteBuffer = new byte[3];
        mByteBuffer[0] = (byte) status;
        mByteBuffer[1] = (byte) data1;
        mByteBuffer[2] = (byte) data2;
        long now = System.nanoTime();
        _connectCallback.onReturnedMsg("midi Command " + mByteBuffer[1] +" time : "+now);
        midiSend(mByteBuffer, 3, now);
//        midiSend(mByteBuffer, 3);
    }


    private void midiCommand(int status, int data1) {
        byte[] mByteBuffer = new byte[2];
        mByteBuffer[0] = (byte) status;
        mByteBuffer[1] = (byte) data1;
        long now = System.nanoTime();
        midiSend(mByteBuffer, 2, now);
//        midiSend(mByteBuffer, 2);
    }

    private void midiSend(byte[] buffer, int count) {
        if (_selector != null) {
            try {
                // send event immediately
                MidiReceiver receiver = _selector.getReceiver();
                if (receiver != null) {
                    receiver.send(buffer, 0, count);
                }
            } catch (IOException e) {
                _connectCallback.onReturnedMsg("[E] "+e.getMessage());
//                Log.e(TAG, "mKeyboardReceiverSelector.send() failed " + e);
            }
        }
    }

    private void midiSend(byte[] buffer, int count, long timestamp) {
        if (_selector != null) {
            try {
                // send event immediately
                MidiReceiver receiver = _selector.getReceiver();
                if (receiver != null) {
                    receiver.send(buffer, 0, count, timestamp);
//                    receiver.onFlush();
                }
            } catch (IOException e) {
                _connectCallback.onReturnedMsg("[E] "+e.getMessage());
//                Log.e(TAG, "mKeyboardReceiverSelector.send() failed " + e);
            }
        }
    }

    MIDIReceiver.ReceiveCallback dataReceiver = new MIDIReceiver.ReceiveCallback() {
        @Override
        public void onDataReceive(String msg) {
            if (_dataReceiver != null) {
                _dataReceiver.onDataReceive(msg);
            }
        }

        @Override
        public void onMsgReceive(String msg) {
            if (_dataReceiver != null) {
                _dataReceiver.onMsgReceive(msg);
            }
        }
    };

    int connecitonState = CODE_DISCONNECTED;

    MIDISelector.ConnectCallback connectCallback = new MIDISelector.ConnectCallback() {
        @Override
        public void onConnectResult(int msg) {
            if (msg == MIDISelector.RESULT_OUTPUT_CONNECTED) {
                isOutputConnected = true;
            } else if (msg == MIDISelector.RESULT_INPUT_CONNECTED) {
                isInputConnected = true;
            } else if (msg == MIDISelector.RESULT_OUTPUT_DISCONNECTED || msg == MIDISelector.RESULT_OUTPUT_NOTCONNECTED) {
                isOutputConnected = false;
            } else if (msg == MIDISelector.RESULT_INPUT_DISCONNECTED || msg == MIDISelector.RESULT_INPUT_NOTCONNECTED) {
                isInputConnected = false;
            }

            if(isInputConnected && isOutputConnected && connecitonState != CODE_CONNECTED){
                connecitonState = CODE_CONNECTED;
                if (_connectCallback != null)
                    _connectCallback.onConnectResult(connecitonState);
            }else if ((!isInputConnected || !isOutputConnected) && connecitonState == CODE_CONNECTED){
                connecitonState = CODE_DISCONNECTED;

                // 연결해재


                if (_connectCallback != null)
                    _connectCallback.onConnectResult(connecitonState);
            }
        }

        @Override
        public void onReturnedMsg(String msg) {
            if (_connectCallback != null)
                _connectCallback.onReturnedMsg(msg);
        }
    };


}
