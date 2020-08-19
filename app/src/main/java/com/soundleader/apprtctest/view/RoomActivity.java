package com.soundleader.apprtctest.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.soundleader.apprtctest.AppRTCAudioManager;
import com.soundleader.apprtctest.R;
import com.soundleader.apprtctest.dialog.DialogButtonCallback;
import com.soundleader.apprtctest.dialog.MessageDialog;
import com.soundleader.apprtctest.usb.MIDIReceiver;
import com.soundleader.apprtctest.usb.MIDISelector;
import com.soundleader.apprtctest.usb.USBManager;
import com.soundleader.apprtctest.utils.Utils;
import com.soundleader.apprtctest.webrtc.RoomSessionManage;
import com.soundleader.apprtctest.webrtc.WEBRtcManager;

import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.FileVideoCapturer;
import org.webrtc.ScreenCapturerAndroid;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;


public class RoomActivity extends VideoBaseActivity implements WEBRtcManager.RtcConnectionCallback, MIDIReceiver.ReceiveCallback, MIDISelector.ConnectCallback {

    String TAG = "ROOM_A_L::";

    int lrValue = 0;
    int udValue = 0;

    /**
     * WebRTC Resources
     * */
    boolean screencaptureEnabled;
    private static int mediaProjectionPermissionResultCode;
    private static Intent mediaProjectionPermissionResultData;

    FragmentSmallVideo _smallVideoContainer;
    FragmentVideoOverlay _viewOverlay;
    FragmentManager _frManager;

    @Nullable
    private SurfaceViewRenderer _fullscreenRenderer;
    private SurfaceViewRenderer _remoteScreenRenderer;

    LinearLayout _progress;
    LinearLayout _logInfos;
    private static final int CAPTURE_PERMISSION_REQUEST_CODE = 1;

    Handler _handler = new Handler();

    RoomSessionManage _roomSessionManager;
    WEBRtcManager _rtcManager;

    boolean hasSession = false;

    boolean isFinish = false;

    String room_id;
    /**
     * USB Resources
     * */
    USBManager _usbManager;

    RecyclerView _myLogList;
    RecyclerView _remoteLogList;

    List<String> _myData;
    List<String> _remoteData;

    LogAdapter _myAdatper;
    LogAdapter _remoteAdatper;

    long uptime = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        uptime = System.currentTimeMillis();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_room);

        try {
            Utils.doWrite(RoomActivity.this, "m","######## enter RoomActivity #########");
            Utils.doWrite(RoomActivity.this, "o","######## enter RoomActivity #########");
        } catch (Exception e) {
            e.printStackTrace();
        }

        _usbManager = USBManager.getInstance(RoomActivity.this, this, this);

        Intent intent = getIntent();
        room_id = intent.getStringExtra(WEBRtcManager.EXTRA_ROOM_ID);
        Log.d(TAG, room_id);

        _logInfos = findViewById(R.id.container_log);
        _myLogList = findViewById(R.id.list_my_log);
        _remoteLogList = findViewById(R.id.list_remote_log);

        _myData = new ArrayList<>();
        _remoteData = new ArrayList<>();
        _myAdatper = new LogAdapter(_myData);
        _remoteAdatper = new LogAdapter(_remoteData);
        _myLogList.setAdapter(_myAdatper);
        _remoteLogList.setAdapter(_remoteAdatper);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(RoomActivity.this, RecyclerView.VERTICAL, false);
        _myLogList.setLayoutManager(linearLayoutManager);
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(RoomActivity.this, RecyclerView.VERTICAL, false);
        _remoteLogList.setLayoutManager(linearLayoutManager2);

        _roomSessionManager = RoomSessionManage.getInstance(room_id);
        _fullscreenRenderer = findViewById(R.id.fullscreen_video_view);

        _fullscreenRenderer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _viewOverlay.toggleFragmentWithAnim();
            }
        });
        _myLogList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    _viewOverlay.toggleFragmentWithAnim();
                }

                return false;
            }
        });
        _remoteLogList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    _viewOverlay.toggleFragmentWithAnim();
                }
                return false;
            }
            });
        _remoteScreenRenderer= findViewById(R.id.remote_video_view);
        _remoteScreenRenderer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _viewOverlay.toggleFragmentWithAnim();
            }
        });
        _remoteScreenRenderer.setVisibility(View.VISIBLE);
        _remoteScreenRenderer.setZOrderMediaOverlay(true);

        _viewOverlay = new FragmentVideoOverlay(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                onSend(v);
                onBackPressed();
            }
        });
        _viewOverlay.setInfos(intent.getStringExtra(WEBRtcManager.EXTRA_ROOM_TITLE), intent.getStringExtra(WEBRtcManager.EXTRA_TEACHER));

        _viewOverlay.setSmallControllListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(_remoteScreenRenderer.getVisibility() == View.VISIBLE){
                    _remoteScreenRenderer.setVisibility(View.GONE);
                }else{
                    _remoteScreenRenderer.setVisibility(View.VISIBLE);

                }
            }
        });
        _viewOverlay.setMicControllListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean enable = !_rtcManager.isEnableAudio();
                _rtcManager.setAudioEnable(enable);
                _viewOverlay.setMicEnabled(enable);
            }
        });
        _viewOverlay.setSmallControllerView(View.GONE);

        _viewOverlay.setUsbControllListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(_usbManager != null)
                    _usbManager.startConnectProcess();
            }
        });
        _viewOverlay.setCameraLRControllListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lrValue == 1){
                 //   setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    _fullscreenRenderer.setMirror(false);


                    lrValue = 0;
                }else if(lrValue == 0){
                 //   setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                    _fullscreenRenderer.setMirror(true);

                    lrValue = 1;
                }

            }
        });
        _viewOverlay.setCameraUDControllListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(udValue == 1){
                       setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

                    udValue = 0;
                }else if(udValue == 0){
                       setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);


                    udValue = 1;
                }

            }
        });
//        _smallVideoContainer = new FragmentSmallVideo(RoomActivity.this);
        _frManager = getSupportFragmentManager();
        FragmentTransaction tr = _frManager.beginTransaction();
        tr.replace(R.id.overlay, _viewOverlay);
//        tr.replace(R.id.overlay_video, _smallVideoContainer);
        tr.commit();

        _progress = findViewById(R.id.view_progress_room);
        _progress.setVisibility(View.VISIBLE);

        startScreenCapture();


        if(!_usbManager.isConnected()){
            showMessageDialog("확인","피아노 연결이 감지되지 않습니다. 화면을 클릭하여, 연결 버튼을 통해서 피아노를 연결 할 수 있습니다.", false, null);
        }



    }

    public void onSend(View v){
        if(_rtcManager != null)
            _rtcManager.sendMessage("wo0wowowowowowowo");
    }



    @Override
    public void onDialogCallback(HashMap<String, Object> result) {
        super.onDialogCallback(result);
        int code = (int) result.get(MessageDialog.EXTRA_CODE_RESULT);
        switch (code){
            case MessageDialog.CODE_CANCEL:
                break;
            case MessageDialog.CODE_CONFIRM:
                if(_rtcManager.isInitiator())
                    _roomSessionManager.removeRoom(room_id); // remove room from list
                _rtcManager.close();
                finish();
                break;
        }
    }

    private void onAudioManagerDevicesChanged(
            final AppRTCAudioManager.AudioDevice device, final Set<AppRTCAudioManager.AudioDevice> availableDevices) {
        // TODO(henrika): add callback handler.
    }

    @Override
    protected void onResume() {
        super.onResume();
        _usbManager.onResume();
        changeIndicator();
        if(hasSession){
            _roomSessionManager.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        _usbManager.onPause();
        _roomSessionManager.stop();
    }


    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(_rtcManager != null)
            _rtcManager.close();
    }


    @Override
    public void onBackPressed() {
        showMessageDialog("확인","연결을 종료하시겠습니까?", true);
    }

    @TargetApi(21)
    private void startScreenCapture() {
        MediaProjectionManager mediaProjectionManager =
                (MediaProjectionManager) getApplication().getSystemService(
                        Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), CAPTURE_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != CAPTURE_PERMISSION_REQUEST_CODE)
            return;
        mediaProjectionPermissionResultCode = resultCode;
        mediaProjectionPermissionResultData = data;

        _rtcManager = new WEBRtcManager(getIntent(), getDisplayMetrics(), createVideoCapturer(), RoomActivity.this, _user);
        _rtcManager.setResources(_fullscreenRenderer, _remoteScreenRenderer);

        _rtcManager.setCallback(this);


        if (_rtcManager.isInitiator())
            _rtcManager.createRoom();
        else
            _rtcManager.connectRoom();




    }



    @TargetApi(17)
    private DisplayMetrics getDisplayMetrics() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager =
                (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
        return displayMetrics;
    }

    private @Nullable
    VideoCapturer createVideoCapturer() {
        final VideoCapturer videoCapturer;
        String videoFileAsCamera = getIntent().getStringExtra(WEBRtcManager.EXTRA_VIDEO_FILE_AS_CAMERA);
        if (videoFileAsCamera != null) {
            try {
                videoCapturer = new FileVideoCapturer(videoFileAsCamera);
            } catch (IOException e) {
//                reportError("Failed to open video file for emulated camera");
                return null;
            }
        } else if (screencaptureEnabled) {
            return createScreenCapturer();
        } else if (useCamera2()) {
            if (!captureToTexture()) {
//                reportError(getString(R.string.camera2_texture_only_error));
                return null;
            }

            videoCapturer = createCameraCapturer(new Camera2Enumerator(this));
        } else {
            videoCapturer = createCameraCapturer(new Camera1Enumerator(captureToTexture()));
        }
        if (videoCapturer == null) {
            return null;
        }
        return videoCapturer;
    }


    private @Nullable
    VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }
        return null;
    }


    @TargetApi(21)
    private @Nullable
    VideoCapturer createScreenCapturer() {
        if (mediaProjectionPermissionResultCode != Activity.RESULT_OK) {
//            reportError("User didn't give permission to capture the screen.");
            return null;
        }
        return new ScreenCapturerAndroid(
                mediaProjectionPermissionResultData, new MediaProjection.Callback() {
            @Override
            public void onStop() {
//                reportError("User revoked permission to capture the screen.");
            }
        });
    }

    private boolean captureToTexture() {
        return getIntent().getBooleanExtra(WEBRtcManager.EXTRA_CAPTURETOTEXTURE_ENABLED, false);
    }

    private boolean useCamera2() {
        return Camera2Enumerator.isSupported(this) && getIntent().getBooleanExtra(WEBRtcManager.EXTRA_CAMERA2, true);
    }


    /**
     * Web Rtc Callback
     * */
    @Override
    public void onRtcConnectionChanged(int code, String msg) {
        switch (code){
            case WEBRtcManager.RtcConnectionCallback.CODE_CREATEROOM:
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        hasSession = true;
                        _roomSessionManager.start();
                        _progress.setVisibility(View.VISIBLE);

                    }
                });
                break;
            case WEBRtcManager.RtcConnectionCallback.CODE_FOUND_OTHER:
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        _progress.setVisibility(View.VISIBLE);
                    }
                });
                _roomSessionManager.stop();
                break;


                case WEBRtcManager.RtcConnectionCallback.CODE_CONNECTED:
                    uptime = System.currentTimeMillis();
                    _handler.post(new Runnable() {
                        @Override
                        public void run() {
                            _progress.setVisibility(View.GONE);
                            _rtcManager.setVideoView(false);
                            _viewOverlay.setOther(_rtcManager.getOtherID());
                            _viewOverlay.setUpdatime(uptime);
                            _viewOverlay.setMicEnabled(_rtcManager.isEnableAudio());
                            _viewOverlay.setSmallControllerView(View.VISIBLE);

                        }
                    });
                break;


            case WEBRtcManager.RtcConnectionCallback.CODE_DISCONNECTING:
                _viewOverlay.showProgress();
                break;


            case WEBRtcManager.RtcConnectionCallback.CODE_DISCONNECTED:
                if(_rtcManager.isInitiator()){
                    showMessageDialog("확인", "상대방과 연결이 끊어졌습니다. 다른 상대방을 기다리시겠습니까?", true, new DialogButtonCallback() {
                        @Override
                        public void onDialogCallback(HashMap<String, Object> result) {
                            _viewOverlay.hideProgress();
                            _viewOverlay.setSmallControllerView(View.GONE);
                            int code= (int) result.get(MessageDialog.EXTRA_CODE_RESULT);
                            switch (code){
                                case MessageDialog.CODE_CONFIRM:
                                    _rtcManager.tryAgain(_fullscreenRenderer, _remoteScreenRenderer);
                                    _roomSessionManager.start();
                                    break;
                                case MessageDialog.CODE_CANCEL:
                                    finish();
                                    break;
                            }
                        }
                    });
                }else{
                    showMessageDialog("확인", "상대방과 연결이 끊어졌습니다.", false, new DialogButtonCallback() {
                        @Override
                        public void onDialogCallback(HashMap<String, Object> result) {
                            finish();
                        }
                    });
                }
                break;



            case WEBRtcManager.RtcConnectionCallback.CODE_RECEIVEDATA:
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:SSS", Locale.getDefault());
                String currentDateandTime = sdf.format(new Date());
                try {
                    String msg2 = "["+currentDateandTime+"] : "+msg;
                    Utils.doWrite(RoomActivity.this, "o",msg2);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // SEND to USB
                addRemoteMsg(msg);
                _usbManager.onSend(msg);
                break;



            case WEBRtcManager.RtcConnectionCallback.CODE_FAILED:
                if(_rtcManager.isInitiator()){
                    showMessageDialog("확인", "상대방과의 연결에 실패하였습니다. 다른 상대방을 기다리시겠습니까?", true, new DialogButtonCallback() {
                        @Override
                        public void onDialogCallback(HashMap<String, Object> result) {
                            _viewOverlay.hideProgress();
                            _viewOverlay.setSmallControllerView(View.GONE);
                            int code= (int) result.get(MessageDialog.EXTRA_CODE_RESULT);
                            switch (code){
                                case MessageDialog.CODE_CONFIRM:
                                    _rtcManager.tryAgain(_fullscreenRenderer, _remoteScreenRenderer);
                                    _roomSessionManager.start();
                                    break;
                                case MessageDialog.CODE_CANCEL:
                                    finish();
                                    break;
                            }
                        }
                    });
                }else{
                    showMessageDialog("확인", "상대방과 연결에 실패하였습니다 잠시후 다시 시도해주세요..", false, new DialogButtonCallback() {
                        @Override
                        public void onDialogCallback(HashMap<String, Object> result) {
                            finish();
                        }
                    });
                }

                break;
        }



    }

    public void addMyMsg(String msg){
        // 200721 시험 테스트용 수정
//        long current = System.currentTimeMillis();
//        current = current - uptime;
//        msg = "["+current+"]"+ msg;
        msg = Utils.getNowDateTime() + ", Data : " + msg;
        _myData.add(msg);
        if(_myData.size() > 100){
            _myData.remove(0);
        }
        _handler.post(new Runnable() {
            @Override
            public void run() {
                _myAdatper.setData(_myData);
                _myLogList.smoothScrollToPosition(_myAdatper.getItemCount()-1);
            }
        });
    }

    public void addRemoteMsg(String msg){
        // 200721 시험 테스트용 수정
//        long current = System.currentTimeMillis();
//
//        current = current - uptime;
////        String time = "["+currentTime.get(Calendar.HOUR)+":"+currentTime.get(Calendar.MINUTE)+":"+currentTime.get(Calendar.SECOND)+":"+currentTime.get(Calendar.MILLISECOND)+"]";
////        String time = current + "";
//        msg = "["+current+"]"+ msg;
        msg = Utils.getNowDateTime() + ", Data : " + msg;
        _remoteData.add(msg);
        if(_remoteData.size() > 100){
            _remoteData.remove(0);
        }
        _handler.post(new Runnable() {
            @Override
            public void run() {
                _remoteAdatper.setData(_remoteData);
                _remoteLogList.smoothScrollToPosition(_remoteAdatper.getItemCount()-1);
            }
        });
    }

    /**
     * USB 관련 Callbacks
     * */
    public void changeIndicator(){
        _viewOverlay.changeIndicator(_usbManager.isConnected());
    }


    // Receive Usb Data
    @Override
    public void onDataReceive(String msg) { // Send to p2p Connection
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:SSS", Locale.getDefault());
        String currentDateandTime = sdf.format(new Date());
        try {
            String msg2 = "["+currentDateandTime+"] : "+msg;
            Utils.doWrite(RoomActivity.this, "m",msg2);
        } catch (Exception e) {
            e.printStackTrace();
        }


        _rtcManager.sendMessage(msg);
        addMyMsg(msg);
    }

    @Override
    public void onMsgReceive(String msg) {

    }

    @Override
    public void onConnectResult(int msg) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                changeIndicator();
                if(!_usbManager.isConnected()){
                    showMessageDialog("확인", "피아노와 연결이 끊어졌습니다.", false, null);
                }
            }
        });

    }

    @Override
    public void onReturnedMsg(String msg) {
        // For log Callback
    }
}
