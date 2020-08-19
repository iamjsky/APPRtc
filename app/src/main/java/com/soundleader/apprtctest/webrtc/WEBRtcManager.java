package com.soundleader.apprtctest.webrtc;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.soundleader.apprtctest.AppRTCAudioManager;
import com.soundleader.apprtctest.beans.SignalInfo;
import com.soundleader.apprtctest.beans.Userdata;
import com.soundleader.apprtctest.utils.Utils;
import com.soundleader.apprtctest.webrtc.beans.RoomConnectionParameters;

import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSink;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.soundleader.apprtctest.utils.Utils.Decrypt;

public class WEBRtcManager implements WebRtcClient.WebRtcCallback, PeerConnectionClient.PeerConnectionEvents {

    public interface RtcConnectionCallback {
        int CODE_CREATEROOM = 100;
        int CODE_FOUND_OTHER = 200;
        int CODE_CONNECTING = 300;
        int CODE_CONNECTED = 400;
        int CODE_DISCONNECTED = 500;
        int CODE_DISCONNECTING = 600;
        int CODE_RECEIVEDATA = 700;
        int CODE_FAILED = 800;

        void onRtcConnectionChanged(int code, String msg);
    }

    String TAG = "RTCManager_L";
    public static final String EXTRA_BASE = "soundleader.webrtc.";
    public static final String EXTRA_INITIATOR = EXTRA_BASE + "initiator";
    public static final String EXTRA_TEACHER = EXTRA_BASE + "reacher";
    public static final String EXTRA_ROOM_ID = EXTRA_BASE + "roomid";
    public static final String EXTRA_ROOM_TITLE = EXTRA_BASE + "roomtitle";
    public static final String EXTRA_VIDEO_CALL = EXTRA_BASE +"VIDEO_CALL";
    public static final String EXTRA_VIDEO_FPS = EXTRA_BASE +"VIDEO_FPS";
    public static final String EXTRA_VIDEO_BITRATE =EXTRA_BASE + "VIDEO_BITRATE";
    public static final String EXTRA_VIDEOCODEC =EXTRA_BASE + "VIDEOCODEC";
    public static final String EXTRA_HWCODEC_ENABLED = EXTRA_BASE +"HWCODEC";
    public static final String EXTRA_CAPTURETOTEXTURE_ENABLED = EXTRA_BASE +"CAPTURETOTEXTURE";
    public static final String EXTRA_FLEXFEC_ENABLED = EXTRA_BASE +"FLEXFEC";
    public static final String EXTRA_AUDIO_BITRATE = EXTRA_BASE +"AUDIO_BITRATE";
    public static final String EXTRA_AUDIOCODEC =EXTRA_BASE + "AUDIOCODEC";
    public static final String EXTRA_NOAUDIOPROCESSING_ENABLED =EXTRA_BASE +"NOAUDIOPROCESSING";
    public static final String EXTRA_AECDUMP_ENABLED = EXTRA_BASE +"AECDUMP";
    public static final String EXTRA_SAVE_INPUT_AUDIO_TO_FILE_ENABLED = EXTRA_BASE +"SAVE_INPUT_AUDIO_TO_FILE";
    public static final String EXTRA_OPENSLES_ENABLED = EXTRA_BASE +"OPENSLES";
    public static final String EXTRA_DISABLE_BUILT_IN_AEC = EXTRA_BASE +"DISABLE_BUILT_IN_AEC";
    public static final String EXTRA_DISABLE_BUILT_IN_AGC = EXTRA_BASE +"DISABLE_BUILT_IN_AGC";
    public static final String EXTRA_DISABLE_BUILT_IN_NS = EXTRA_BASE +"DISABLE_BUILT_IN_NS";
    public static final String EXTRA_DISABLE_WEBRTC_AGC_AND_HPF =EXTRA_BASE +"DISABLE_WEBRTC_GAIN_CONTROL";
    public static final String EXTRA_DATA_CHANNEL_ENABLED = EXTRA_BASE +"DATA_CHANNEL_ENABLED";
    public static final String EXTRA_ORDERED = EXTRA_BASE +"ORDERED";
    public static final String EXTRA_MAX_RETRANSMITS_MS =EXTRA_BASE + "MAX_RETRANSMITS_MS";
    public static final String EXTRA_MAX_RETRANSMITS = EXTRA_BASE +"MAX_RETRANSMITS";
    public static final String EXTRA_PROTOCOL = EXTRA_BASE +"PROTOCOL";
    public static final String EXTRA_NEGOTIATED = EXTRA_BASE +"NEGOTIATED";
    public static final String EXTRA_ID = EXTRA_BASE +"ID";
    public static final String EXTRA_ENABLE_RTCEVENTLOG =EXTRA_BASE + "ENABLE_RTCEVENTLOG";
    public static final String EXTRA_SCREENCAPTURE = EXTRA_BASE+"SCREENCAPTURE";
    public static final String EXTRA_VIDEO_WIDTH = EXTRA_BASE+"VIDEO_WIDTH";
    public static final String EXTRA_VIDEO_HEIGHT =EXTRA_BASE+ "VIDEO_HEIGHT";
    public static final String EXTRA_TRACING = EXTRA_BASE+"TRACING";
    public static final String EXTRA_CAMERA2 = EXTRA_BASE+"CAMERA2";
    public static final String EXTRA_VIDEO_FILE_AS_CAMERA = EXTRA_BASE+"VIDEO_FILE_AS_CAMERA";

    RoomConnectionParameters _roomParam;
    PeerConnectionClient peerConnectionClient;
    PeerConnectionClient.PeerConnectionParameters peerConnectionParameters;

    String _encr;
    EglBase eglBase;

    WebRtcClient _client;

    private final ProxyVideoSink remoteProxyRenderer = new ProxyVideoSink();
    private final ProxyVideoSink localProxyVideoSink = new ProxyVideoSink();
    private final List<VideoSink> remoteSinks = new ArrayList<>();

    @Nullable
    private SurfaceViewRenderer fullscreenRenderer;
    private SurfaceViewRenderer smallscreenRenderer;
    @Nullable private AppRTCAudioManager audioManager;


    static WEBRtcManager _instance;

    Context _context;
    Intent intent;
    Handler _handler = new Handler();
    RtcConnectionCallback _callback;

    boolean isConnected = false;

    public static WEBRtcManager getInstance(Intent intent, DisplayMetrics metrix, VideoCapturer videoCapturer, Context context, Userdata user){
        if(_instance == null){
            _instance = new WEBRtcManager(intent, metrix, videoCapturer,context, user);
        }else{
            _instance._context = context;
        }
        return _instance;
    }

    public WEBRtcManager(Intent intent, DisplayMetrics metrix, VideoCapturer videoCapturer, Context context, Userdata user){
        _context = context;
        this.intent = intent;
        String room_id = intent.getStringExtra(WEBRtcManager.EXTRA_ROOM_ID);
        int initiator = intent.getIntExtra(WEBRtcManager.EXTRA_INITIATOR, -1);
        if (initiator == RoomConnectionParameters.INITIATOR_NEW) {
            String room_title = intent.getStringExtra(WEBRtcManager.EXTRA_ROOM_TITLE);
            _encr = Utils.getRandomString(8);
            _roomParam = new RoomConnectionParameters(intent, room_id, room_title, initiator, user, _encr, metrix, videoCapturer);
        } else {
            _encr = "";
            _roomParam = new RoomConnectionParameters(intent, room_id, initiator, user, metrix, videoCapturer);
        }
        _client = new WebRtcClient(this);
    }

    public void setResources(SurfaceViewRenderer full, SurfaceViewRenderer small){
        fullscreenRenderer = full;
        smallscreenRenderer = small;
        initResources();
    }

    public void setCallback(RtcConnectionCallback callback){
        _callback = callback;
    }

    public boolean isInitiator(){
        return (_roomParam.initiator == RoomConnectionParameters.INITIATOR_NEW);
    }

    public void createRoom(){
        Log.d(TAG, "createRoom roomid : "+ _roomParam.roomId);
        _client.createRoom(_roomParam);
    }

    public void connectRoom(){
            Log.d(TAG, "connectRoom roomid : "+ _roomParam.roomId);
        _client.connectRoom(_roomParam);
    }

    public void setVideoView(boolean isLocalFull) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                if (isLocalFull) {
//                    remoteProxyRenderer.setTarget(null);
                    localProxyVideoSink.setTarget(fullscreenRenderer);
                    if(smallscreenRenderer != null)
                        smallscreenRenderer.setVisibility(View.GONE);
                } else {
                    // After Connected
                    remoteProxyRenderer.setTarget(fullscreenRenderer);
                    localProxyVideoSink.setTarget(smallscreenRenderer);
                    if(smallscreenRenderer != null)
                        smallscreenRenderer.setVisibility(View.VISIBLE);
                    smallscreenRenderer.bringToFront();
                }
            }
        });
    }

    public void close(){
        Log.d(TAG, "close");
        if(peerConnectionClient != null){
            peerConnectionClient.close();
        }
    }

    /**
     * Room already created just need peerconnection creation process
     * -> call after disconnected for auto connection waitting
     * */
    public void tryAgain(SurfaceViewRenderer full, SurfaceViewRenderer small){
        isConnected = false;
        Log.d(TAG, "tryAgain");
        if(_roomParam.initiator == RoomConnectionParameters.INITIATOR_NEW){
            fullscreenRenderer = full;
            smallscreenRenderer = small;
            initResources();
            createPeerConnection(null);
            fullscreenRenderer.setVisibility(View.GONE);
            fullscreenRenderer.setVisibility(View.VISIBLE);
            OccurCallback(RtcConnectionCallback.CODE_CREATEROOM);
            _client.resetList();
        }
    }



    public void sendMessage(String msg){
        if(isConnected){
            peerConnectionClient.sendDataMsg(msg);
        }
    }





    public String getOtherID(){
        return _roomParam.other;
    }


    private void initResources(){
        eglBase = EglBase.create();
        fullscreenRenderer.init(eglBase.getEglBaseContext(), null);
        fullscreenRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        fullscreenRenderer.setEnableHardwareScaler(false /* enabled */);
        smallscreenRenderer.setZOrderMediaOverlay(true);
        fullscreenRenderer.setMirror(true);
        smallscreenRenderer.init(eglBase.getEglBaseContext(), null);
        smallscreenRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        smallscreenRenderer.setEnableHardwareScaler(true /* enabled */);
        smallscreenRenderer.setDuplicateParentStateEnabled(false);
        smallscreenRenderer.setMirror(true);

        setVideoView(false);
    }

    public void setAudioEnable(boolean val){
        Log.d(TAG, "setAudioEnable val : "+val);
        if(peerConnectionClient != null)
            peerConnectionClient.setAudioEnabled(val);
    }

    public boolean isEnableAudio(){
        if(peerConnectionClient != null)
            return peerConnectionClient.isAudioEnabled();
        else
            return false;
    }

    private void createPeerConnection(SessionDescription sdp){
        remoteSinks.clear();

        Log.d(TAG, "createPeerConnection");
        boolean tracing = intent.getBooleanExtra(EXTRA_TRACING, false);
        int videoWidth = intent.getIntExtra(EXTRA_VIDEO_WIDTH, 0);
        int videoHeight = intent.getIntExtra(EXTRA_VIDEO_HEIGHT, 0);
        boolean screencaptureEnabled = intent.getBooleanExtra(EXTRA_SCREENCAPTURE, false);
        // If capturing format is not specified for screencapture, use screen resolution.
        if (screencaptureEnabled && videoWidth == 0 && videoHeight == 0) {
            videoWidth = _roomParam.metrix.widthPixels;
            videoHeight = _roomParam.metrix.heightPixels;
        }



        PeerConnectionClient.DataChannelParameters dataChannelParameters = null;
        if (intent.getBooleanExtra(EXTRA_DATA_CHANNEL_ENABLED, false)) {
            dataChannelParameters = new PeerConnectionClient.DataChannelParameters(intent.getBooleanExtra(EXTRA_ORDERED, true),
                    intent.getIntExtra(EXTRA_MAX_RETRANSMITS_MS, -1),
                    intent.getIntExtra(EXTRA_MAX_RETRANSMITS, -1), intent.getStringExtra(EXTRA_PROTOCOL),
                    intent.getBooleanExtra(EXTRA_NEGOTIATED, false), intent.getIntExtra(EXTRA_ID, -1));
        }




        if(audioManager == null)
            audioManager = AppRTCAudioManager.create(_context);
        audioManager.start(new AppRTCAudioManager.AudioManagerEvents() {
            @Override
            public void onAudioDeviceChanged(
                    AppRTCAudioManager.AudioDevice audioDevice, Set<AppRTCAudioManager.AudioDevice> availableAudioDevices) {
//                onAudioManagerDevicesChanged(audioDevice, availableAudioDevices);
                Log.d(TAG, "audio Device Changed : " + audioDevice.name());
            }
        });

        peerConnectionParameters =
                new PeerConnectionClient.PeerConnectionParameters(intent.getBooleanExtra(EXTRA_VIDEO_CALL, true), false,
                        tracing, videoWidth, videoHeight, intent.getIntExtra(EXTRA_VIDEO_FPS, 0),
                        intent.getIntExtra(EXTRA_VIDEO_BITRATE, 0), intent.getStringExtra(EXTRA_VIDEOCODEC),
                        intent.getBooleanExtra(EXTRA_HWCODEC_ENABLED, true),
                        intent.getBooleanExtra(EXTRA_FLEXFEC_ENABLED, false),
                        intent.getIntExtra(EXTRA_AUDIO_BITRATE, 0), intent.getStringExtra(EXTRA_AUDIOCODEC),
                        intent.getBooleanExtra(EXTRA_NOAUDIOPROCESSING_ENABLED, false),
                        intent.getBooleanExtra(EXTRA_AECDUMP_ENABLED, false),
                        intent.getBooleanExtra(EXTRA_SAVE_INPUT_AUDIO_TO_FILE_ENABLED, false),
                        intent.getBooleanExtra(EXTRA_OPENSLES_ENABLED, false),
                        intent.getBooleanExtra(EXTRA_DISABLE_BUILT_IN_AEC, false),
                        intent.getBooleanExtra(EXTRA_DISABLE_BUILT_IN_AGC, false),
                        intent.getBooleanExtra(EXTRA_DISABLE_BUILT_IN_NS, false),
                        intent.getBooleanExtra(EXTRA_DISABLE_WEBRTC_AGC_AND_HPF, false),
                        intent.getBooleanExtra(EXTRA_ENABLE_RTCEVENTLOG, false), dataChannelParameters);





        peerConnectionClient = new PeerConnectionClient(
                _context, eglBase, peerConnectionParameters, this);
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        peerConnectionClient.createPeerConnectionFactory(options);

        if (peerConnectionParameters.videoMaxBitrate > 0) {
            peerConnectionClient.setVideoMaxBitrate(peerConnectionParameters.videoMaxBitrate);
        }
        remoteSinks.add(remoteProxyRenderer);
        peerConnectionClient.createPeerConnection(localProxyVideoSink, remoteSinks, _roomParam.videoCapturer, _roomParam._servers);
        if (_roomParam.initiator == RoomConnectionParameters.INITIATOR_NEW) {
            peerConnectionClient.createOffer();
        } else {
            peerConnectionClient.setRemoteDescription(sdp);
            peerConnectionClient.createAnswer();
        }

        peerConnectionClient.setAudioEnabled(false);
    }



    /**
     * Callbacks
     * */
    @Override
    public void onResult(WebRtcClient.WebRTCServerResult result) {
        // WeRtcClient Callback
        Log.d(TAG, "onResult");
        int code = (int) result.get(WebRtcClient.WebRTCServerResult.EXTRA_CODE);
        if (code == WebRtcClient.WebRTCServerResult.RESULT_CODE_SUCCESS) {
            int gubun = (int) result.get(WebRtcClient.WebRTCServerResult.EXTRA_GUBUN);
            switch (gubun) {
                case WebRtcClient.WebRTCServerResult.GUBUN_CREATE_RESULT:
                    Log.d(TAG, "GUBUN_CREATE_RESULT");
                    _roomParam = (RoomConnectionParameters) result.get(WebRtcClient.WebRTCServerResult.EXTRA_PARAMS);
                    createPeerConnection(null);
                    OccurCallback(RtcConnectionCallback.CODE_CREATEROOM);
                    break;
                case WebRtcClient.WebRTCServerResult.GUBUN_RECEIVER_SERVER:
                    Log.d(TAG, "GUBUN_RECEIVER_SERVER");
                    _roomParam = (RoomConnectionParameters) result.get(WebRtcClient.WebRTCServerResult.EXTRA_PARAMS);
                    break;
                case WebRtcClient.WebRTCServerResult.GUBUN_FOUND_OFFER:


                    Log.d(TAG, "GUBUN_FOUND_OFFER");
                    OccurCallback(RtcConnectionCallback.CODE_FOUND_OTHER);
                    SignalInfo info = (SignalInfo) result.get(WebRtcClient.WebRTCServerResult.EXTRA_PARAMS);
                    _roomParam.encr = info.getEncr();
                    _roomParam.other = info.getFrom();
                    // set current connection remote sdp
                    try {
                        String sessionInfo = Decrypt(info.getData(), _roomParam.encr);
                        SessionDescription sdp = new SessionDescription(SessionDescription.Type.OFFER, sessionInfo);
//                        peerConnectionClient.setRemoteDescription(sdp);
                        createPeerConnection(sdp);
                        _client.foundCandidate();
                    } catch (Exception e) {
                        Log.d(TAG, " GUBUN_FOUND_OFFER : in catch e :" + e.getMessage());
                    }




                    break;
                case WebRtcClient.WebRTCServerResult.GUBUN_FOUND_CONNECTOR:
                    Log.d(TAG, "GUBUN_FOUND_CONNECTOR");



                    OccurCallback(RtcConnectionCallback.CODE_FOUND_OTHER);
                    try {
                        info = (SignalInfo) result.get(WebRtcClient.WebRTCServerResult.EXTRA_PARAMS);
                        String sessionInfo = Decrypt(info.getData(), _roomParam.encr);
                        _roomParam.other = info.getFrom();
                        SessionDescription sdp = new SessionDescription(SessionDescription.Type.ANSWER, sessionInfo);
                        peerConnectionClient.setRemoteDescription(sdp);
                        // get candidate
                    } catch (Exception e) {
                        Log.d(TAG, "GUBUN_FOUND_CONNECTOR : in catch e :" + e.getMessage());
                        e.printStackTrace();
                    }



                    break;
                case WebRtcClient.WebRTCServerResult.GUBUN_FOUND_CANDIDATE:
                    List<IceCandidate> candidates = (List<IceCandidate>) result.get(WebRtcClient.WebRTCServerResult.EXTRA_PARAMS);
                    Log.d(TAG, "GUBUN_FOUND_CANDIDATE count : " + candidates.size());
                    for(IceCandidate candidate : candidates){
                        peerConnectionClient.addRemoteIceCandidate(candidate);
                    }
                    break;
            }
        } else {
            String msg = (String) result.get(WebRtcClient.WebRTCServerResult.EXTRA_MESSAGE);
            Log.d(TAG, "RESULT ERROR : in catch e :"+ msg);
            // ERROR
        }
    }



    /**
     * 현재 클라이언트의 SDP 획득
     * next step -> Signaling 을 위해 서버에 SDP 정보 전송
     * */
    @Override
    public void onLocalDescription(SessionDescription sdp) {
        Log.d(TAG, "onLocalDescription");
        if (_roomParam.initiator == RoomConnectionParameters.INITIATOR_NEW) {
            _client.sendSessionDescription(_roomParam, sdp);
        } else {
            _client.foundCandidate();
            if (_roomParam.encr != null) {
                // send
                _client.sendSessionDescription(_roomParam, sdp);
            } else {
                _roomParam.addSessionDescription(sdp);
            }
        }
    }



    /**
     * 현재 클라이언트의 ICE 서버 Candidate 획득
     * next step -> p2p 연결을 위해 서버에 Candidate 정보 전송
     * */
    @Override
    public void onIceCandidate(IceCandidate candidate) {
        Log.d(TAG, "onIceCandidate");
        _client.sendCandidate(_roomParam, candidate);
    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] candidates) {
        Log.d(TAG, "[onIceCandidatesRemoved] removed count : "+ candidates.length);
    }



    @Override
    public void onIceConnected() {
        Log.d(TAG, "onIceConnected");
    }



    @Override
    public void onIceDisconnected() {
        isConnected = false;
        Log.d(TAG, "onIceDisconnected");
        OccurCallback(RtcConnectionCallback.CODE_DISCONNECTING);
    }


    @Override
    public void onConnected() {
        isConnected = true;
        Log.d(TAG, "onConnected");
        _client.removeSignaling();
        OccurCallback(RtcConnectionCallback.CODE_CONNECTED);
    }


    @Override
    public void onDisconnected() {
        Log.d(TAG, "onDisconnected");
        _handler.post(new Runnable() {
            @Override
            public void run() {
                remoteProxyRenderer.setTarget(null);
                localProxyVideoSink.setTarget(null);

                if(peerConnectionClient != null)
                    peerConnectionClient.close();
                peerConnectionClient = null;

                if(smallscreenRenderer != null)
                    smallscreenRenderer.release();
                smallscreenRenderer = null;
                if(fullscreenRenderer !=null)
                    fullscreenRenderer.release();
                fullscreenRenderer = null;
                if(audioManager!=null)
                    audioManager.stop();
                audioManager = null;

                if(isConnected)
                    OccurCallback(RtcConnectionCallback.CODE_DISCONNECTED);
                else
                    OccurCallback(RtcConnectionCallback.CODE_FAILED);

                isConnected = false;
            }
        });
    }

    @Override
    public void onPeerConnectionClosed() {
        Log.d(TAG, "onPeerConnectionClosed");
    }

    @Override
    public void onPeerConnectionStatsReady(StatsReport[] reports) {
        Log.d(TAG, "onPeerConnectionStatsReady");
    }

    @Override
    public void onPeerConnectionError(String description) {
        Log.d(TAG, "onPeerConnectionError");
    }

    @Override
    public void onDataChanelMessageReceive(String message) {
        OccurCallback(RtcConnectionCallback.CODE_RECEIVEDATA, message);
    }


    private static class ProxyVideoSink implements VideoSink {
        private VideoSink target;

        @Override
        synchronized public void onFrame(VideoFrame frame) {
            if (target == null) {
                Log.d("VIDEO_L", "Dropping frame in proxy because target is null.");
                return;
            }
            target.onFrame(frame);
        }
        synchronized public void setTarget(VideoSink target) {
            this.target = target;
        }
    }


    public void OccurCallback(int code){
        OccurCallback(code, "");
    }


    public void OccurCallback(int code, String msg){
        if(_callback !=  null)
            _callback.onRtcConnectionChanged(code, msg);
    }


}
