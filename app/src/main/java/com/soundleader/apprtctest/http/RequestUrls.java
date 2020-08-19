package com.soundleader.apprtctest.http;

import com.soundleader.apprtctest.beans.Result;
import com.soundleader.apprtctest.beans.RoomCheck;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface RequestUrls {

    // 회원가입
    @FormUrlEncoded
    @POST("APIM0000.php")
    Call<Result> requestRegist(@FieldMap HashMap<String, Object> param);

    // 로그인
    @FormUrlEncoded
    @POST("APIM0001.php")
    Call<Result> requestLogin(@FieldMap HashMap<String, Object> param);

    @GET("APIC0000.php?")
    Call<Result> getRoomList(@Query("gubun") String gubun);

    @GET("APIC0000.php?")
    Call<RoomCheck> getCheckId(@Query("gubun") String gubun, @Query("id") String id);

    @FormUrlEncoded
    @POST("APIC0000.php")
    Call<RoomCheck> insertId(@FieldMap HashMap<String, Object> param);

    @GET("APIC0002.php?")
    Call<Result> checkRoom(@Query("roomid") String id);

    // Signaling
    @FormUrlEncoded
    @POST("APIR0000.php")
    Call<Result> signalingSend(@FieldMap HashMap<String, Object> param);

    // FoundAnswer
    @GET("APIR0001.php?")
    Call<Result> foundAnswer(@Query("roomid") String roomid, @Query("type") String type);

    // FOUDN Candidate
    @GET("APIR0001.php?")
    Call<Result> foundCandidate(@Query("roomid") String roomid, @Query("type") String type, @Query("from") String userid);


    // ROOM session
    @FormUrlEncoded
    @POST("APIR0002.php")
    Call<Result> refreshRoom(@FieldMap HashMap<String, Object> param);

    // ROOM Signaling remove
    @FormUrlEncoded
    @POST("APIR0003.php")
    Call<Result> removeSignaling(@FieldMap HashMap<String, Object> param);

    // ROOM Signaling remove
    @GET("APIR0004.php")
    Call<Result> resetRoomList(@Query("roomid") String roomid);

    // ROOM Signaling remove
    @GET("APIR0005.php")
    Call<Result> removeRoomList(@Query("roomid") String roomid);
}
