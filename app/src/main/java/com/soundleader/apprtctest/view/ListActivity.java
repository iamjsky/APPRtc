package com.soundleader.apprtctest.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.soundleader.apprtctest.R;
import com.soundleader.apprtctest.beans.Result;
import com.soundleader.apprtctest.beans.RoomCheck;
import com.soundleader.apprtctest.beans.RoomData;
import com.soundleader.apprtctest.beans.Status;
import com.soundleader.apprtctest.dialog.DialogButtonCallback;
import com.soundleader.apprtctest.dialog.MessageDialog;
import com.soundleader.apprtctest.dialog.ProgressDialog;
import com.soundleader.apprtctest.dialog.RoomCreateDialog;
import com.soundleader.apprtctest.http.Constants;
import com.soundleader.apprtctest.http.RequestUrls;
import com.soundleader.apprtctest.usb.MIDIReceiver;
import com.soundleader.apprtctest.usb.MIDISelector;
import com.soundleader.apprtctest.usb.USBManager;
import com.soundleader.apprtctest.utils.CSharedPref;
import com.soundleader.apprtctest.utils.ImageLoader;
import com.soundleader.apprtctest.utils.PermissionManager;
import com.soundleader.apprtctest.utils.Utils;
import com.soundleader.apprtctest.webrtc.WEBRtcManager;
import com.soundleader.apprtctest.webrtc.beans.RoomConnectionParameters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import info.androidhive.fontawesome.FontDrawable;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ListActivity extends BaseActivity  implements MIDISelector.ConnectCallback, MIDIReceiver.ReceiveCallback {

    final String TAG = "LIST_A_L :: ";

    //####### SETING VALUE ##########//
    private String keyprefResolution;
    private String keyprefFps;
    private String keyprefVideoBitrateType;
    private String keyprefVideoBitrateValue;
    private String keyprefAudioBitrateType;
    private String keyprefAudioBitrateValue;
    private String keyprefRoomServerUrl;
    private String keyprefRoom;
    private String keyprefRoomList;
    //####### SETING VALUE ##########//

    String _ROOMID = "";
    String _TITLE = "";

    PermissionManager _permissionChecker;
    Retrofit _httpObj;
    List<RoomData> _data;
    CSharedPref _pref;

    ProgressDialog _progressDialog;
    RecyclerView _recyclerView;
    AdapterRoomList _roomAdapter;
    DrawerLayout _wrapper;
    View _menuView;
    View _infoView;
    Button _logout;
    ImageView _usbIndicator;
    Button _usbConnect;
    SwipeRefreshLayout mSwipeRefreshLayout;
    ImageView _userProfile;
    TextView _userId;

    Handler _handler = new Handler();

    Runnable refresh = new Runnable() {
        @Override
        public void run() {
            getRoomList();
//            _handler.postDelayed(refresh, 1000);
        }
    };

    USBManager _usbManager;

    ListView _listView;
    ArrayAdapter<String> _testAdapter;
    List<String> tempData;
//    EditText _tempEdit;
//    Button _testBtn;
//    Button _testBtn2;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        _listView = findViewById(R.id.view_list);
//        _listView.setVisibility(View.GONE);
        tempData = new ArrayList<>();
        _testAdapter = new ArrayAdapter<>(ListActivity.this, android.R.layout.simple_list_item_1, tempData);
        _listView.setAdapter(_testAdapter);
        _listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        _listView.setStackFromBottom(true);
        _listView.setVisibility(View.GONE);
//        _tempEdit = findViewById(R.id.edit_test);
//        _testBtn = findViewById(R.id.btn_test);
//        _testBtn.setOnClickListener(onTestClick);
//        _testBtn2 = findViewById(R.id.btn_test2);
//        _testBtn2.setOnClickListener(onTestClick2);

//        _testBtn.setVisibility(View.VISIBLE);
//        _tempEdit.setVisibility(View.VISIBLE);
//        _testBtn2.setVisibility(View.VISIBLE);

        keyprefResolution = getString(R.string.pref_resolution_key);
        keyprefFps = getString(R.string.pref_fps_key);
        keyprefVideoBitrateType = getString(R.string.pref_maxvideobitrate_key);
        keyprefVideoBitrateValue = getString(R.string.pref_maxvideobitratevalue_key);
        keyprefAudioBitrateType = getString(R.string.pref_startaudiobitrate_key);
        keyprefAudioBitrateValue = getString(R.string.pref_startaudiobitratevalue_key);
        keyprefRoomServerUrl = getString(R.string.pref_room_server_url_key);
        keyprefRoom = getString(R.string.pref_room_key);
        keyprefRoomList = getString(R.string.pref_room_list_key);

        _httpObj = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();


        _progressDialog = new ProgressDialog(ListActivity.this,android.R.style.Theme_Translucent_NoTitleBar);
        _pref = new CSharedPref(ListActivity.this);

        _logout = findViewById(R.id.btn_logout);
        _logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _pref.resetLogin();
                Intent intent = new Intent(ListActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        _infoView = findViewById(R.id.view_info);
        _infoView.setVisibility(View.GONE);
        _wrapper = findViewById(R.id.wrapper);
        _menuView = findViewById(R.id.drawer);
        _userId = findViewById(R.id.text_name);
        _userProfile = findViewById(R.id.img_main_profile);

        _userId.setText(_user.getUserId());
        new ImageLoader().execute( _userProfile, _user.getUserProfile());
        _usbIndicator = findViewById(R.id.view_usb_indicator);
        _usbConnect = findViewById(R.id.btn_connect);
        _usbConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _usbManager.startConnectProcess();
            }
        });

        _permissionChecker = new PermissionManager();

        // List View Set
        _data = new ArrayList<>();
        Configuration config = getResources().getConfiguration();
        GridLayoutManager gridLayoutManager;
        gridLayoutManager = new GridLayoutManager(ListActivity.this, 2);
        if(config.orientation == Configuration.ORIENTATION_LANDSCAPE){
            gridLayoutManager = new GridLayoutManager(ListActivity.this, 3);
        }

        _recyclerView = findViewById(R.id.view_recycler);
        _recyclerView.setHasFixedSize(true);
        _recyclerView.setLayoutManager(gridLayoutManager );
        _roomAdapter = new AdapterRoomList(ListActivity.this, _data, onItemClick);
        _recyclerView.setAdapter(_roomAdapter);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.simpleSwipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                _data.clear();
                _roomAdapter.notifyDataSetChanged();
                getRoomList(500);
            }
        });

        // Floating Font set
        FloatingActionButton fab = findViewById(R.id.fab);
        FontDrawable drawable = new FontDrawable(this, R.string.fa_plus_solid, true, false);
        drawable.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        fab.setImageDrawable(drawable);
        fab.setOnClickListener(onAdd);


        if(!_user.getUserType().equals("0")){
            fab.setVisibility(View.GONE);
        }
        _infoView.setVisibility(View.GONE);
        _wrapper.closeDrawer(_menuView);
        _wrapper.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    @Override
    protected void onResume() {
        super.onResume();

        testLogAdd("[onResume]");
        _handler.post(refresh);
        _handler.postDelayed(refresh, 2000);
        _usbManager = USBManager.getInstance(ListActivity.this,this, this);
        usbConnectionChange();
    }

    @Override
    protected void onPause() {
        super.onPause();
        _handler.removeCallbacks(refresh);
    }

    @Override
    public void onBackPressed() {
        showMessageDialog("확인", "앱을 종료하시겠습니까?", true, new DialogButtonCallback() {
            @Override
            public void onDialogCallback(HashMap<String, Object> result) {
                int code = (int) result.get(MessageDialog.EXTRA_CODE_RESULT);
                switch (code){
                    case MessageDialog.CODE_CONFIRM:
                        finish();
                        break;
                }
            }
        });
    }




    AdapterView.OnItemClickListener onItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(_permissionChecker.checkPermission(ListActivity.this)){

                }else{
                    String[] needPermission = _permissionChecker.getPermissionList();
                    ListActivity.this.requestPermissions(needPermission, 200);
                    return;
                }
            }
            final RoomData data = _data.get(position);
            _progressDialog.show();
            RequestUrls request = _httpObj.create(RequestUrls.class);
            request.checkRoom(data.getRoomId()).enqueue(new Callback<Result>() {
                @Override
                public void onResponse(Call<Result> call, Response<Result> response) {
                    _progressDialog.dismiss();
                    Result result = response.body();
                    Status status = result.getStatus();
                    if(result != null) {
                        Log.e("rtc_debug", status.getCode() + "/" + status.getResult() );
                        switch (status.getCode()) {
                            case 200:
                                int iResult = status.getResult();
                                if(iResult == 1){
                                    _TITLE = data.getTitle();
                                    connectToRoom(data.getRoomId(), false, false, 0,RoomConnectionParameters.INITIATOR_CONNECTOR, data.getUserId());
                                }else{
                                    getRoomList();
                                    showMessageDialog("확인", "이미 가득 차거나 유효하지 않는 방입니다. 다른 방을 선택해 주세요.", false);
                                }
                                break;
                        }
                    }
                }
                @Override
                public void onFailure(Call<Result> call, Throwable t) {
                    _progressDialog.dismiss();
                }
            });
        }
    };





    public void onToggleMenu(View v){
        if(_wrapper.isDrawerOpen(_menuView)){
            _wrapper.closeDrawer(_menuView);
        }else{
            _wrapper.openDrawer(_menuView);
        }
    }




    /**
     * Get Room List
     * */
    public void getRoomList(){
        getRoomList(0);
    }

    public void getRoomList(int delay){
        _handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                RequestUrls request = _httpObj.create(RequestUrls.class);
                request.getRoomList("list").enqueue(onRoomListCallback);
            }
        }, delay);
    }

    Callback<Result> onRoomListCallback = new Callback<Result>() {
        @Override
        public void onResponse(Call<Result> call, Response<Result> response) {
//            Log.d(TAG, response.raw().toString());
            Result result = response.body();
            Status status = result.getStatus();
            if(result != null) {
                switch (status.getCode()) {
                    case 200:

//                        _infoView.setVisibility(View.GONE);

                        _data = result.getRoomData();
                        if(_data.size() == 0){
                            _infoView.setVisibility(View.VISIBLE);
                        }else{
                            _infoView.setVisibility(View.GONE);
                        }
                        _roomAdapter.setData(_data);

                        break;
                }
            }
            mSwipeRefreshLayout.setRefreshing(false);
        }

        @Override
        public void onFailure(Call<Result> call, Throwable t) {

        }
    };





    View.OnClickListener onAdd = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(_permissionChecker.checkPermission(ListActivity.this)){

                }else{
                    String[] needPermission = _permissionChecker.getPermissionList();
                    ListActivity.this.requestPermissions(needPermission, 200);
                    return;
                }
            }

            RoomCreateDialog dialog = new RoomCreateDialog(ListActivity.this);
            dialog.setCallback(onDialogResult);
            dialog.show();
        }
    };

    DialogButtonCallback onDialogResult = new DialogButtonCallback() {
        @Override
        public void onDialogCallback(HashMap<String, Object> result) {
            int code = (int) result.get(RoomCreateDialog.EXTRA_CODE);
            switch (code){
                case RoomCreateDialog.DIALOG_RESULT_CODE_CONFIRM:
                    // CREATE ROOM -> GO TO NEXT
                    _TITLE = (String) result.get(RoomCreateDialog.EXTRA_NAME);
                    _progressDialog.show();
                    _ROOMID = Utils.getRandomString(20);
                    // ROOM_ID CHECK
                    checkRoomId(_ROOMID);
                    break;
                case RoomCreateDialog.DIALOG_RESULT_CODE_CANCEL:
                    //IGNORE
                    break;
            }
        }
    };


    /**
     * Step 1. 자동 생성된 Room id 체크
     *  -> Room id 중복 방지를 위해 서버에 질의
     * */


    // Room id 체크
    public void checkRoomId(String id){
        RequestUrls request = _httpObj.create(RequestUrls.class);
        request.getCheckId("id", id).enqueue(onCheckCallback);
    }
    // Room id 체크 콜백
    Callback<RoomCheck> onCheckCallback = new Callback<RoomCheck>() {
        @Override
        public void onResponse(Call<RoomCheck> call, Response<RoomCheck> response) {
            Status result = response.body().getStatus();
            if(result != null){
                switch (result.getCode()){
                    case 200:
                        int val = result.getResult();
                        if(val == 0){
                            _progressDialog.dismiss();
                            // ROOM ID 등록
//                            insertRoomId(_ROOMID);
                            connectToRoom(_ROOMID, false, false, 0, RoomConnectionParameters.INITIATOR_NEW);
                        }else{
                            // 이미 존재하는 ROOM_ID  => 재시도;
                            _ROOMID = Utils.getRandomString(20);
                            // ROOM_ID CHECK
                            checkRoomId(_ROOMID);
                        }
                        break;
                    case 400:
                        _progressDialog.dismiss();
                        break;
                }
            }
        }
        @Override
        public void onFailure(Call<RoomCheck> call, Throwable t) {
            _progressDialog.dismiss();
        }
    };





    /**
     * Step 2. 체크 완료된 Room id 등록
     *  -> Room id 를 서버 DB에 등록
     * */
    public void insertRoomId(String id){
        int midx = _user.getIdx();
        HashMap<String, Object> postData = new HashMap<>();
        postData.put("title",_TITLE);
        postData.put("room_id", id);
        postData.put("midx", midx);
        RequestUrls request = _httpObj.create(RequestUrls.class);
        request.insertId(postData).enqueue(onInsertCallback);
    }

    Callback<RoomCheck> onInsertCallback = new Callback<RoomCheck>() {
        @Override
        public void onResponse(Call<RoomCheck> call, Response<RoomCheck> response) {
            _progressDialog.dismiss();
            Status result = response.body().getStatus();
            if(result != null){
                switch (result.getCode()){
                    case 200:
                        // 등록 완료
                        // 입장
//                        connectToRoom(_ROOMID, false, false, 0);
                        break;
                    case 400:
                        break;
                }
            }
        }

        @Override
        public void onFailure(Call<RoomCheck> call, Throwable t) {
            _progressDialog.dismiss();
        }
    };




    private void connectToRoom(String roomId, boolean loopback,
                               boolean useValuesFromIntent, int runTimeMs, int type, String teacher){

        String roomUrl = sharedPref.getString(keyprefRoomServerUrl, getString(R.string.pref_room_server_url_default));
//        roomUrl = "https://giants.co.kr/soundleader_webrtc/api/APIR0000.php";

        // Video call enabled flag.
        boolean videoCallEnabled = sharedPrefGetBoolean(R.string.pref_videocall_key,
                WEBRtcManager.EXTRA_VIDEO_CALL, R.string.pref_videocall_default, useValuesFromIntent);

        // Use screencapture option.
        boolean useScreencapture = sharedPrefGetBoolean(R.string.pref_screencapture_key,
                WEBRtcManager.EXTRA_SCREENCAPTURE, R.string.pref_screencapture_default, useValuesFromIntent);

        // Use Camera2 option.
        boolean useCamera2 = sharedPrefGetBoolean(R.string.pref_camera2_key, WEBRtcManager.EXTRA_CAMERA2,
                R.string.pref_camera2_default, useValuesFromIntent);



        // Check HW codec flag.
        boolean hwCodec = sharedPrefGetBoolean(R.string.pref_hwcodec_key,
                WEBRtcManager.EXTRA_HWCODEC_ENABLED, R.string.pref_hwcodec_default, useValuesFromIntent);

        // Check Capture to texture.
        boolean captureToTexture = sharedPrefGetBoolean(R.string.pref_capturetotexture_key,
                WEBRtcManager.EXTRA_CAPTURETOTEXTURE_ENABLED, R.string.pref_capturetotexture_default,
                useValuesFromIntent);

        // Check FlexFEC.
        boolean flexfecEnabled = sharedPrefGetBoolean(R.string.pref_flexfec_key,
                WEBRtcManager.EXTRA_FLEXFEC_ENABLED, R.string.pref_flexfec_default, useValuesFromIntent);

        // Check Disable Audio Processing flag.
        boolean noAudioProcessing = sharedPrefGetBoolean(R.string.pref_noaudioprocessing_key,
                WEBRtcManager.EXTRA_NOAUDIOPROCESSING_ENABLED, R.string.pref_noaudioprocessing_default,
                useValuesFromIntent);

        boolean aecDump = sharedPrefGetBoolean(R.string.pref_aecdump_key,
                WEBRtcManager.EXTRA_AECDUMP_ENABLED, R.string.pref_aecdump_default, useValuesFromIntent);

        boolean saveInputAudioToFile =
                sharedPrefGetBoolean(R.string.pref_enable_save_input_audio_to_file_key,
                        WEBRtcManager.EXTRA_SAVE_INPUT_AUDIO_TO_FILE_ENABLED,
                        R.string.pref_enable_save_input_audio_to_file_default, useValuesFromIntent);

        // Check OpenSL ES enabled flag.
        boolean useOpenSLES = sharedPrefGetBoolean(R.string.pref_opensles_key,
                WEBRtcManager.EXTRA_OPENSLES_ENABLED, R.string.pref_opensles_default, useValuesFromIntent);

        // Check Disable built-in AEC flag.
        boolean disableBuiltInAEC = sharedPrefGetBoolean(R.string.pref_disable_built_in_aec_key,
                WEBRtcManager.EXTRA_DISABLE_BUILT_IN_AEC, R.string.pref_disable_built_in_aec_default,
                useValuesFromIntent);

        // Check Disable built-in AGC flag.
        boolean disableBuiltInAGC = sharedPrefGetBoolean(R.string.pref_disable_built_in_agc_key,
                WEBRtcManager.EXTRA_DISABLE_BUILT_IN_AGC, R.string.pref_disable_built_in_agc_default,
                useValuesFromIntent);

        // Check Disable built-in NS flag.
        boolean disableBuiltInNS = sharedPrefGetBoolean(R.string.pref_disable_built_in_ns_key,
                WEBRtcManager.EXTRA_DISABLE_BUILT_IN_NS, R.string.pref_disable_built_in_ns_default,
                useValuesFromIntent);

        // Check Disable gain control
        boolean disableWebRtcAGCAndHPF = sharedPrefGetBoolean(
                R.string.pref_disable_webrtc_agc_and_hpf_key, WEBRtcManager.EXTRA_DISABLE_WEBRTC_AGC_AND_HPF,
                R.string.pref_disable_webrtc_agc_and_hpf_key, useValuesFromIntent);



        // Get video resolution from settings.
        int videoWidth = 0;
        int videoHeight = 0;
        if (useValuesFromIntent) {
            videoWidth = getIntent().getIntExtra(WEBRtcManager.EXTRA_VIDEO_WIDTH, 0);
            videoHeight = getIntent().getIntExtra(WEBRtcManager.EXTRA_VIDEO_HEIGHT, 0);
        }
        if (videoWidth == 0 && videoHeight == 0) {
            String resolution =
                    sharedPref.getString(keyprefResolution, getString(R.string.pref_resolution_default));
            String[] dimensions = resolution.split("[ x]+");
            if (dimensions.length == 2) {
                try {
                    videoWidth = Integer.parseInt(dimensions[0]);
                    videoHeight = Integer.parseInt(dimensions[1]);
                } catch (NumberFormatException e) {
                    videoWidth = 0;
                    videoHeight = 0;
//                    Log.e(TAG, "Wrong video resolution setting: " + resolution);
                }
            }
        }

        // Get camera fps from settings.
        int cameraFps = 0;
        if (useValuesFromIntent) {
            cameraFps = getIntent().getIntExtra(WEBRtcManager.EXTRA_VIDEO_FPS, 0);
        }
        if (cameraFps == 0) {
            String fps = sharedPref.getString(keyprefFps, getString(R.string.pref_fps_default));
            String[] fpsValues = fps.split("[ x]+");
            if (fpsValues.length == 2) {
                try {
                    cameraFps = Integer.parseInt(fpsValues[0]);
                } catch (NumberFormatException e) {
                    cameraFps = 0;
//                    Log.e(TAG, "Wrong camera fps setting: " + fps);
                }
            }
        }

        // Check capture quality slider flag.
//        boolean captureQualitySlider = sharedPrefGetBoolean(R.string.pref_capturequalityslider_key,
//                EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED,
//                R.string.pref_capturequalityslider_default, useValuesFromIntent);

        // Get video and audio start bitrate.
        int videoStartBitrate = 0;
        if (useValuesFromIntent) {
            videoStartBitrate = getIntent().getIntExtra(WEBRtcManager.EXTRA_VIDEO_BITRATE, 0);
        }
        if (videoStartBitrate == 0) {
            String bitrateTypeDefault = getString(R.string.pref_maxvideobitrate_default);
            String bitrateType = sharedPref.getString(keyprefVideoBitrateType, bitrateTypeDefault);
            if (!bitrateType.equals(bitrateTypeDefault)) {
                String bitrateValue = sharedPref.getString(
                        keyprefVideoBitrateValue, getString(R.string.pref_maxvideobitratevalue_default));
                videoStartBitrate = Integer.parseInt(bitrateValue);
            }
        }

        String videoCodec = sharedPrefGetString(R.string.pref_videocodec_key,
                WEBRtcManager.EXTRA_VIDEOCODEC, R.string.pref_videocodec_default, useValuesFromIntent);





        String audioCodec = sharedPrefGetString(R.string.pref_audiocodec_key,
                WEBRtcManager.EXTRA_AUDIOCODEC, R.string.pref_audiocodec_default, useValuesFromIntent);


        int audioStartBitrate = 0;
        if (useValuesFromIntent) {
            audioStartBitrate = getIntent().getIntExtra(WEBRtcManager.EXTRA_AUDIO_BITRATE, 0);
        }
        if (audioStartBitrate == 0) {
            String bitrateTypeDefault = getString(R.string.pref_startaudiobitrate_default);
            String bitrateType = sharedPref.getString(keyprefAudioBitrateType, bitrateTypeDefault);
            if (!bitrateType.equals(bitrateTypeDefault)) {
                String bitrateValue = sharedPref.getString(
                        keyprefAudioBitrateValue, getString(R.string.pref_startaudiobitratevalue_default));
                audioStartBitrate = Integer.parseInt(bitrateValue);
            }
        }


        // Check statistics display option.
//        boolean displayHud = sharedPrefGetBoolean(R.string.pref_displayhud_key,
//                EXTRA_DISPLAY_HUD, R.string.pref_displayhud_default, useValuesFromIntent);
//
        boolean tracing = sharedPrefGetBoolean(R.string.pref_tracing_key, WEBRtcManager.EXTRA_TRACING,
                R.string.pref_tracing_default, useValuesFromIntent);

        // Check Enable RtcEventLog.
        boolean rtcEventLogEnabled = sharedPrefGetBoolean(R.string.pref_enable_rtceventlog_key,
                WEBRtcManager.EXTRA_ENABLE_RTCEVENTLOG, R.string.pref_enable_rtceventlog_default,
                useValuesFromIntent);



        // Get datachannel options
        boolean dataChannelEnabled = sharedPrefGetBoolean(R.string.pref_enable_datachannel_key,
                WEBRtcManager.EXTRA_DATA_CHANNEL_ENABLED, R.string.pref_enable_datachannel_default,
                useValuesFromIntent);


        boolean ordered = sharedPrefGetBoolean(R.string.pref_ordered_key, WEBRtcManager.EXTRA_ORDERED,
                R.string.pref_ordered_default, useValuesFromIntent);
        boolean negotiated = sharedPrefGetBoolean(R.string.pref_negotiated_key,
                WEBRtcManager.EXTRA_NEGOTIATED, R.string.pref_negotiated_default, useValuesFromIntent);
        int maxRetrMs = sharedPrefGetInteger(R.string.pref_max_retransmit_time_ms_key,
                WEBRtcManager.EXTRA_MAX_RETRANSMITS_MS, R.string.pref_max_retransmit_time_ms_default,
                useValuesFromIntent);
        int maxRetr =
                sharedPrefGetInteger(R.string.pref_max_retransmits_key, WEBRtcManager.EXTRA_MAX_RETRANSMITS,
                        R.string.pref_max_retransmits_default, useValuesFromIntent);
        int id = sharedPrefGetInteger(R.string.pref_data_id_key, WEBRtcManager.EXTRA_ID,
                R.string.pref_data_id_default, useValuesFromIntent);
        String protocol = sharedPrefGetString(R.string.pref_data_protocol_key,
                WEBRtcManager.EXTRA_PROTOCOL, R.string.pref_data_protocol_default, useValuesFromIntent);



//        roomUrl = ""
        // Start AppRTCMobile activity.
//        Log.d(TAG, "Connecting to room " + roomId + " at URL " + roomUrl);
        if (validateUrl(roomUrl)) {

            Intent intent = new Intent(this, RoomActivity.class);
            intent.putExtra(WEBRtcManager.EXTRA_VIDEO_CALL, videoCallEnabled);
            intent.putExtra(WEBRtcManager.EXTRA_SCREENCAPTURE, useScreencapture);
            intent.putExtra(WEBRtcManager.EXTRA_CAMERA2, useCamera2);
            intent.putExtra(WEBRtcManager.EXTRA_VIDEO_WIDTH, videoWidth);
            intent.putExtra(WEBRtcManager.EXTRA_VIDEO_HEIGHT, videoHeight);
            intent.putExtra(WEBRtcManager.EXTRA_VIDEO_FPS, cameraFps);
            intent.putExtra(WEBRtcManager.EXTRA_VIDEO_BITRATE, videoStartBitrate);
            intent.putExtra(WEBRtcManager.EXTRA_VIDEOCODEC, videoCodec);
            intent.putExtra(WEBRtcManager.EXTRA_HWCODEC_ENABLED, hwCodec);
            intent.putExtra(WEBRtcManager.EXTRA_CAPTURETOTEXTURE_ENABLED, captureToTexture);
            intent.putExtra(WEBRtcManager.EXTRA_FLEXFEC_ENABLED, flexfecEnabled);
            intent.putExtra(WEBRtcManager.EXTRA_NOAUDIOPROCESSING_ENABLED, noAudioProcessing);
            intent.putExtra(WEBRtcManager.EXTRA_AECDUMP_ENABLED, aecDump);
            intent.putExtra(WEBRtcManager.EXTRA_SAVE_INPUT_AUDIO_TO_FILE_ENABLED, saveInputAudioToFile);
            intent.putExtra(WEBRtcManager.EXTRA_OPENSLES_ENABLED, useOpenSLES);
            intent.putExtra(WEBRtcManager.EXTRA_DISABLE_BUILT_IN_AEC, disableBuiltInAEC);
            intent.putExtra(WEBRtcManager.EXTRA_DISABLE_BUILT_IN_AGC, disableBuiltInAGC);
            intent.putExtra(WEBRtcManager.EXTRA_DISABLE_BUILT_IN_NS, disableBuiltInNS);
            intent.putExtra(WEBRtcManager.EXTRA_DISABLE_WEBRTC_AGC_AND_HPF, disableWebRtcAGCAndHPF);
            intent.putExtra(WEBRtcManager.EXTRA_AUDIO_BITRATE, audioStartBitrate);
            intent.putExtra(WEBRtcManager.EXTRA_AUDIOCODEC, audioCodec);
            intent.putExtra(WEBRtcManager.EXTRA_TRACING, tracing);
            intent.putExtra(WEBRtcManager.EXTRA_ENABLE_RTCEVENTLOG, rtcEventLogEnabled);
            intent.putExtra(WEBRtcManager.EXTRA_DATA_CHANNEL_ENABLED, dataChannelEnabled);

            if (dataChannelEnabled) {
                intent.putExtra(WEBRtcManager.EXTRA_ORDERED, ordered);
                intent.putExtra(WEBRtcManager.EXTRA_MAX_RETRANSMITS_MS, maxRetrMs);
                intent.putExtra(WEBRtcManager.EXTRA_MAX_RETRANSMITS, maxRetr);
                intent.putExtra(WEBRtcManager.EXTRA_PROTOCOL, protocol);
                intent.putExtra(WEBRtcManager.EXTRA_NEGOTIATED, negotiated);
                intent.putExtra(WEBRtcManager.EXTRA_ID, id);
            }

            intent.putExtra(WEBRtcManager.EXTRA_ROOM_ID, roomId);
            intent.putExtra(WEBRtcManager.EXTRA_ROOM_TITLE, _TITLE);
            intent.putExtra(WEBRtcManager.EXTRA_INITIATOR, type);
            intent.putExtra(WEBRtcManager.EXTRA_TEACHER, teacher);



            startActivityForResult(intent, 1);
        }

    }

    private void connectToRoom(String roomId, boolean loopback,
                               boolean useValuesFromIntent, int runTimeMs, int type) {
        connectToRoom(roomId, loopback, useValuesFromIntent, runTimeMs, type, _user.getUserId());

    }


    private void usbConnectionChange(){
        _usbIndicator.post(new Runnable() {
            @Override
            public void run() {
                FontDrawable drawable;
                if(_usbManager.isConnected()){
                    drawable  = new FontDrawable(ListActivity.this, R.string.fa_check_circle, true, false);
                    drawable.setTextColor(ContextCompat.getColor(ListActivity.this, R.color.primary2));
                    _usbConnect.setVisibility(View.GONE);
//                    _testBtn.setVisibility(View.VISIBLE);
//                    _tempEdit.setVisibility(View.VISIBLE);
//                    _testBtn2.setVisibility(View.VISIBLE);
                }else{
                    drawable  = new FontDrawable(ListActivity.this, R.string.fa_times_circle, true, false);
                    drawable.setTextColor(ContextCompat.getColor(ListActivity.this, R.color.colorOther2));
                    _usbConnect.setVisibility(View.VISIBLE);
//                    _testBtn.setVisibility(View.GONE);
//                    _tempEdit.setVisibility(View.GONE);
//                    _testBtn2.setVisibility(View.GONE);
                }
                _usbIndicator.setImageDrawable(drawable);
            }
        });
    }

    public void testLogAdd(final String msg){
        _listView.post(new Runnable() {
            @Override
            public void run() {
                tempData.add(msg);
                if(tempData.size() > 100){
                    tempData.remove(0);
                }
                _testAdapter.notifyDataSetChanged();
            }
        });
    }

    View.OnClickListener onTestClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            String id = _tempEdit.getText().toString().trim().replace(" ","");
//            if(id.length() > 0 ){
//                String sendData = "s/i/r/"+id+"/e";
//                _usbManager.onSend(sendData);
//            }
        }
    };



    View.OnClickListener onTestClick2 = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            _usbManager.sendNoteOnTest2();
        }
    };

    @Override
    public void onDataReceive(final String msg) {
        testLogAdd("[DATA]::"+msg);
    }

    @Override
    public void onMsgReceive(String msg) {

    }

    @Override
    public void onConnectResult(int msg) {
//        Log.d(TAG, "ConnectResult :: "+msg );
        testLogAdd("[CONN]::"+msg);
        usbConnectionChange();
//        Log.d(TAG, "ConnectResult  usbmager value:: "+_usbManager.isConnected() );
        if(!_usbManager.isConnected()){
            _usbIndicator.post(new Runnable() {
                @Override
                public void run() {
                    showMessageDialog("확인", "피아노아 연결이 끊어졌습니다.", false, null);
                }
            });
        }
    }

    @Override
    public void onReturnedMsg(String msg) {
        testLogAdd("[REST]::"+msg);
    }
}
