package com.soundleader.apprtctest.usb;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiDeviceStatus;
import android.media.midi.MidiInputPort;
import android.media.midi.MidiManager;
import android.media.midi.MidiOutputPort;
import android.media.midi.MidiReceiver;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.soundleader.apprtctest.dialog.DialogList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

@TargetApi(Build.VERSION_CODES.M)
public class MIDISelector extends MidiManager.DeviceCallback {

    public static String TAG = "sldebug";
    public interface ConnectCallback {
        void onConnectResult(int msg);
        void onReturnedMsg(String msg);
    }

    public static final int RESULT_OUTPUT_NOTCONNECTED = -1;
    public static final int RESULT_OUTPUT_CONNECTED = 0;
    public static final int RESULT_OUTPUT_DISCONNECTED = 1;
    public static final int RESULT_INPUT_NOTCONNECTED = 2;
    public static final int RESULT_INPUT_CONNECTED = 3;
    public static final int RESULT_INPUT_DISCONNECTED = 4;


    protected HashMap<String, MidiInfo> ports = new HashMap<String, MidiInfo>();
    protected MidiManager mMidiManager;
    protected Activity mActivity;
    private MidiInfo mCurrentInfo;


    private MidiOutputPort mOutputPort;
    private MidiInputPort mInputPort;
    //    private MidiDispatcher mDispatcher = new MidiDispatcher();
    private MidiDevice mOpenDevice;
    private MIDIReceiver mReceiver;
    private ConnectCallback mCallback;

    protected DialogList mDialog;


    private final CopyOnWriteArrayList<MidiReceiver> mReceivers
            = new CopyOnWriteArrayList<MidiReceiver>();

    private boolean isRegisteredCallback = false;



    public MIDISelector(MidiManager midiManager, Activity activity, ConnectCallback callback, MIDIReceiver receiver) {
        mMidiManager = midiManager;

        mActivity = activity;
        mCallback = callback;
        mReceiver = receiver;


        MidiDeviceInfo[] infos = mMidiManager.getDevices();
        for (MidiDeviceInfo info : infos) {
            onDeviceAdded(info);
        }
    }

    public void onResum() {

        try{
            mMidiManager.unregisterDeviceCallback(this);
        }catch (Exception e){}

//        if(mOpenDevice != null){
//            mOutputPort.
//        }
        isRegisteredCallback = true;
        mMidiManager.registerDeviceCallback(this, null);

    }

    public void onPause() {
        isRegisteredCallback = false;
        mMidiManager.unregisterDeviceCallback(this);
    }


    public void connectUSB(Context context) {
        mDialog = new DialogList(context);
        mDialog.clear();
        mDialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mCurrentInfo = mDialog.getMidiDevice(i);
                onPortSelected(mCurrentInfo);
                mDialog.dismiss();
            }
        });
        ArrayList<MidiInfo> fields = getLists(ports.keySet().iterator());
        mDialog.setData(fields);
        mDialog.showDialog();
    }

    public void connectBLE(MidiInfo midiInfo){
        close();
        mCurrentInfo = midiInfo;
        onPortSelected(mCurrentInfo);
    }


    public void onPortSelected(final MidiInfo info) {
        close();

        if (info != null && info.getInfo() != null) {
            try {
                mMidiManager.openDevice(info.getInfo(), new MidiManager.OnDeviceOpenedListener() {
                    @Override
                    public void onDeviceOpened(MidiDevice device) {
                        mCallback.onReturnedMsg("onDeviceOpened");
                        try {
                            if (device == null) {
                                mCallback.onConnectResult(RESULT_OUTPUT_NOTCONNECTED);
                                mCallback.onReturnedMsg("device is null");
                            } else {
                                mOpenDevice = device;

                                mOutputPort = device.openOutputPort(info.getIndex());
                                mInputPort = device.openInputPort(info.getIndex());

                                if (mOutputPort == null) {
                                    mCallback.onReturnedMsg("mOutputPort null ");
//                                    mCallback.onConnectResult(RESULT_OUTPUT_NOTCONNECTED);
                                    return;
                                }
                                if (mInputPort == null) {
                                    mCallback.onReturnedMsg("mInputPort null ");
//                                    mCallback.onConnectResult(RESULT_INPUT_NOTCONNECTED);
                                    return;
                                }

                                mOutputPort.onConnect(new MidiReceiver() {
                                    @Override
                                    public void onSend(byte[] bytes, int i, int i1, long l) throws IOException {
//                                        Log.d(TAG, "onConnect :: " + toHexadecimal(bytes, i, i1));
//                                        mCallback.onReturnedMsg("output : "+ toHexadecimal(bytes, i, i1));
                                    }
                                });
                                mOutputPort.onDisconnect(new MidiReceiver() {
                                    @Override
                                    public void onSend(byte[] bytes, int i, int i1, long l) throws IOException {
//                                        Log.d(TAG, "onDisconnect :: " + toHexadecimal(bytes, i, i1));
                                        mCallback.onReturnedMsg("output dis : "+ toHexadecimal(bytes, i, i1));
                                    }
                                });
                                mOutputPort.connect(mReceiver);

                            }
                        }catch (Exception e){
                            mCallback.onReturnedMsg("Exception : "+e.getMessage());
                            mCallback.onConnectResult(RESULT_OUTPUT_NOTCONNECTED);
                        }
                    }
                }, null);

            } catch (Exception e) {
                mCallback.onReturnedMsg("Exception : "+e.getMessage());
                mCallback.onConnectResult(RESULT_OUTPUT_NOTCONNECTED);
            }
        } else {
            mCallback.onReturnedMsg("info is null: ");
            mCallback.onConnectResult(RESULT_OUTPUT_NOTCONNECTED);
        }
    }


    public ArrayList<MidiInfo> getLists(Iterator<String> iter) {
        ArrayList<MidiInfo> list = new ArrayList<>();
        while (iter.hasNext()) {
            list.add(ports.get(iter.next()));
        }
        return list;
    }




    @Override
    public void onDeviceAdded(MidiDeviceInfo device) {
        Log.d(TAG, "onDeviceAdded :: ");
        int portCount = getInfoPortCount(device);
        for (int i = 0; i < portCount; ++i) {
            MidiInfo wrapper = new MidiInfo(device, i);
            ports.put(wrapper.getKey(), wrapper);
            ArrayList<MidiInfo> fields = getLists(ports.keySet().iterator());
        }
    }



    @Override
    public void onDeviceRemoved(MidiDeviceInfo device) {
        Log.d(TAG, "onDeviceRemoved :: ");
        int portCount = getInfoPortCount(device);
        for (int i = 0; i < portCount; ++i) {
            MidiInfo wrapper = new MidiInfo(device, i);
            ports.remove(wrapper.getKey());
            // If the currently selected port was removed then select no port.
            if (device.equals(mCurrentInfo.mInfo) && i == mCurrentInfo.mIndex) {
                Log.d(TAG, "onDeviceRemoved :: correct info");
                close();

            }
            ArrayList<MidiInfo> fields = getLists(ports.keySet().iterator());
        }
    }



    @Override
    public void onDeviceStatusChanged(MidiDeviceStatus status) {
        mCallback.onReturnedMsg("onDeviceStatusChanged : ");
        // input port check
//        Log.d(TAG, "onDeviceStatusChanged :: ");
        if (mCurrentInfo != null) {
            Log.d(TAG, "onDeviceStatusChanged :: mCurrentInfo is not null");
            Log.d(TAG, "onDeviceStatusChanged :: input port status : " + status.isInputPortOpen(mCurrentInfo.getIndex()));
            if (status.isInputPortOpen(mCurrentInfo.getIndex())) {
                mCallback.onReturnedMsg("INPUT CONNECTED");
                mCallback.onConnectResult(RESULT_INPUT_CONNECTED);
            } else {
                mCallback.onReturnedMsg("INPUT DISCONNEcTED");
                mCallback.onConnectResult(RESULT_INPUT_DISCONNECTED);
            }
            Log.d(TAG, "onDeviceStatusChanged :: out port open count : " + status.getOutputPortOpenCount(mCurrentInfo.getIndex()));
            if (status.getOutputPortOpenCount(mCurrentInfo.getIndex()) > 0) {
                mCallback.onReturnedMsg("OUTPUT CONNECTED");
                mCallback.onConnectResult(RESULT_OUTPUT_CONNECTED);
            } else {
                mCallback.onReturnedMsg("OUTPUT DISCONNEcTED");
                mCallback.onConnectResult(RESULT_OUTPUT_DISCONNECTED);
            }
        } else {
            mCallback.onReturnedMsg("[onDeviceStatusChanged]INOUT NOT CONNECTED");
            Log.d(TAG, "onDeviceStatusChanged :: mCurrentInfo is null");
            mCallback.onConnectResult(RESULT_OUTPUT_NOTCONNECTED);
            mCallback.onConnectResult(RESULT_INPUT_NOTCONNECTED);
        }
    }




    public void checkConnection() {

    }

    private int getInfoPortCount(final MidiDeviceInfo info) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int iCount = info.getInputPortCount();
            int oCount = info.getOutputPortCount();

            if (iCount < oCount) {
                return iCount;
            } else {
                return oCount;
            }
        }
        return -1;
    }

    public void close() {

        try {
            if (mOutputPort != null) {
                mOutputPort.disconnect(mReceiver);
                mOutputPort.close();
            }
        } catch (Exception e) {
        }
        try {
            if (mInputPort != null) {
                mInputPort.close();
            }
        } catch (Exception e) {
        }
        try {
            if (mOpenDevice != null) {
                mOpenDevice.close();
            }
        } catch (Exception e) {
        }
        mOutputPort = null;
        mInputPort = null;
        mOpenDevice = null;
        mCallback.onReturnedMsg("[close]INOUT NOT CONNECTED");
        mCallback.onConnectResult(RESULT_INPUT_NOTCONNECTED);
        mCallback.onConnectResult(RESULT_OUTPUT_NOTCONNECTED);
    }


    private String toHexadecimal(byte[] digest, int offset, int count) {
        String hash = "";
        for (int i = offset; i < (count + offset); i++) {
            byte aux = digest[i];
            int b = aux & 0xff;
            if (Integer.toHexString(b).length() == 1) hash += "0";
            hash += (Integer.toHexString(b) + "/");
        }
        return hash;
    }
    public MidiReceiver getReceiver() {
        return mInputPort;
    }

}
