package com.soundleader.apprtctest.webrtc;

import android.os.Handler;
import android.util.Log;

import com.soundleader.apprtctest.beans.Result;
import com.soundleader.apprtctest.http.Constants;
import com.soundleader.apprtctest.http.RequestUrls;

import java.util.HashMap;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * 강사가 방을 만든 순간부터 작동 하도록 사용
 * */
public class RoomSessionManage {

    String TAG = "ROOM_S_L";

    Handler _handler;
    Retrofit _httpObj;
    String _room_id;

    HashMap<String, Object> data ;
    private static RoomSessionManage _instance;

    private RoomSessionManage(String id){
        rebuild(id);
    }

    private void rebuild(String id){
        _room_id = id;
        _handler = new Handler();

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        _httpObj = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        data = new HashMap<>();
        data.put("room_id", id);
    }

    public void removeRoom(String roomid){
        _handler.removeCallbacks(refreshProcess);
        RequestUrls request = _httpObj.create(RequestUrls.class);
        request.removeRoomList(roomid).enqueue(callback);
    }

    Runnable refreshProcess = new Runnable() {
        @Override
        public void run() {
            // refresh request
            Log.d(TAG,"refreshPRocess Start...");
            _handler.postDelayed(refreshProcess, 1000*1);
            RequestUrls request = _httpObj.create(RequestUrls.class);
            request.refreshRoom(data).enqueue(callback);
        }
    };

    Callback<Result> callback = new Callback<Result>() {
        @Override
        public void onResponse(Call<Result> call, Response<Result> response) {

        }

        @Override
        public void onFailure(Call<Result> call, Throwable t) {

        }
    };


    public static RoomSessionManage getInstance(String room_id){
        if(_instance == null){
            _instance = new RoomSessionManage(room_id);
        }else{
            _instance._handler.removeCallbacks(_instance.refreshProcess);
            _instance.rebuild(room_id);

        }
        return _instance;
    }

    public void start(){
        Log.d(TAG, "start");
        _handler.removeCallbacks(refreshProcess);
        _handler.post(refreshProcess);
    }

    public void stop(){
        Log.d(TAG, "stop");
        _handler.removeCallbacks(refreshProcess);
    }

}
