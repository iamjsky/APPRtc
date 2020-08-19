package com.soundleader.apprtctest.webrtc.beans;

import android.content.Intent;
import android.util.DisplayMetrics;
import android.util.Log;

import com.soundleader.apprtctest.beans.Userdata;
import com.soundleader.apprtctest.http.RequestUrls;
import com.soundleader.apprtctest.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;
import org.webrtc.VideoCapturer;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class RoomConnectionParameters {


    public static final int INITIATOR_NEW = 1;
    public static final int INITIATOR_CONNECTOR = 2;

    private static final String new_baseUrl = "https://giants.co.kr/soundleader_webrtc/api/APIR0000.php";
    private static final String connector_baseUrl = "https://giants.co.kr/soundleader_webrtc/api/APIR0000.php";

    public final String roomTitle;
    public final String roomUrl;
    public final String roomId;
    public final int initiator;
    public Userdata user;

    public String remoteId;
    public String encr;

    public String type;
    public String enData;

    public Intent intent;
    public DisplayMetrics metrix;
    public VideoCapturer videoCapturer;

    public List<PeerConnection.IceServer> _servers;

    public String other = "";

    List<SessionDescription> _sessions;

    // For OFFER
    public RoomConnectionParameters(Intent intent, String roomId, String roomTitle, int initiator, Userdata user , String encr, DisplayMetrics metrix, VideoCapturer capturer) {
        this.intent =  intent;
        this.roomUrl = getRoomUrlFromId(roomId, initiator);
        this.roomId = roomId;
        this.roomTitle = roomTitle;
        this.initiator = initiator;
        this.user = user;
        this.encr = encr;
        this.metrix = metrix;
        this.videoCapturer = capturer;
        _servers = new ArrayList<>();
        _sessions = new ArrayList<>();
    }

    // For ANSWER
    public RoomConnectionParameters(Intent intent, String roomId, int initiator, Userdata user , DisplayMetrics metrix, VideoCapturer capturer) {
        this.intent=  intent;
        this.roomUrl = getRoomUrlFromId(roomId, initiator);
        this.roomId = roomId;
        this.roomTitle = "";
        this.initiator = initiator;
        this.user = user;
        this.encr = "";
        this.metrix = metrix;
        this.videoCapturer = capturer;
        _servers = new ArrayList<>();
        _sessions = new ArrayList<>();
    }


    public void addSessionDescription(SessionDescription session){
        _sessions.add(session);
    }

    public List<SessionDescription> getSessionList(){
        return _sessions;
    }


    private String getRoomUrlFromId(String roomid, int initiator){
        String result = "";
        if(initiator == INITIATOR_NEW){
            result += new_baseUrl + "?room_id="+roomid;
        }else {
            result += connector_baseUrl+ "?room_id="+roomid;
        }
        return result;
    }



}
