package com.soundleader.apprtctest.webrtc;

import android.os.Handler;
import android.util.Log;

import com.soundleader.apprtctest.beans.CandidateInfo;
import com.soundleader.apprtctest.beans.Result;
import com.soundleader.apprtctest.beans.RoomCheck;
import com.soundleader.apprtctest.beans.SignalingData;
import com.soundleader.apprtctest.beans.Status;
import com.soundleader.apprtctest.http.Constants;
import com.soundleader.apprtctest.http.RequestUrls;
import com.soundleader.apprtctest.utils.AsyncHttpURLConnection;
import com.soundleader.apprtctest.utils.Utils;
import com.soundleader.apprtctest.webrtc.beans.RoomConnectionParameters;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WebRtcClient {

    String TAG = "WEBRTCC_L";

    Handler _handler = new Handler();

    public class WebRTCServerResult extends HashMap<String, Object> {
        public static final int RESULT_CODE_SUCCESS = 0;
        public static final int RESULT_CODE_ERROR = 1;

        public static final int GUBUN_CREATE_RESULT = 1;
        public static final int GUBUN_SENDOFFER_RESULT = 2;
        public static final int GUBUN_SENDANSWER_RESULT = 3;
        public static final int GUBUN_SEDNCANDIDATE_RESULT = 4;
        public static final int GUBUN_FOUND_CONNECTOR = 5;
        public static final int GUBUN_FOUND_OFFER = 6;
        public static final int GUBUN_RECEIVER_SERVER = 7;
        public static final int GUBUN_FOUND_CANDIDATE = 8;
//        public static final int GUBUN_FOUND_CONNECTOR = 5;

        public static final String EXTRA_CODE = "code";
        public static final String EXTRA_GUBUN = "gubun";
        public static final String EXTRA_MESSAGE = "message";
        public static final String EXTRA_PARAMS = "params";
    }

    public interface WebRtcCallback {
        void onResult(WebRTCServerResult result);
    }

    private static final int STATUS_ON = 1;
    private static final int STATUS_OFF = -1;

    public int stat = STATUS_OFF;

    private static final int TURN_HTTP_TIMEOUT_MS = 5000;
    private static final String serverlist_url = "https://giants.co.kr/soundleader_webrtc/api/APIS0000.php";

    RoomConnectionParameters _parameter;
    WebRtcCallback _callback;

    Retrofit _httpObj;

    public WebRtcClient(WebRtcCallback callback) {
        _callback = callback;

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        _httpObj = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

    }



    public void connectRoom(RoomConnectionParameters parameter) {
        stat = STATUS_ON;
        _parameter = parameter;
        connectRoomInternal();
    }

    private void connectRoomInternal() {

        // get ICEServer
        Log.d(TAG, "[createRoomInternal] start AsynHttpUrlConnection");
        AsyncHttpURLConnection httpConnection =
                new AsyncHttpURLConnection("GET", serverlist_url, "", new AsyncHttpURLConnection.AsyncHttpEvents() {
                    @Override
                    public void onHttpError(String errorMessage) {
                        Log.d(TAG, "[createRoomInternal] onError : " + errorMessage);
                        if (_callback != null)
                            _callback.onResult(getErrorResult(errorMessage));
                    }

                    @Override
                    public void onHttpComplete(String response) {
                        Log.d(TAG, "[createRoomInternal] onComplete : " + response);
                        try {
                            List<PeerConnection.IceServer> turnServers = new ArrayList<>();
                            JSONObject responseJSON = new JSONObject(response);
                            JSONArray iceServers = responseJSON.getJSONArray("iceServers");
                            for (int i = 0; i < iceServers.length(); ++i) {
                                JSONObject server = iceServers.getJSONObject(i);
                                String serverUrl = server.getString("url");
                                serverUrl = serverUrl.replace("sturn:", "");
                                PeerConnection.IceServer sturnServer = new PeerConnection.IceServer(serverUrl);
                                turnServers.add(sturnServer);
//                                JSONArray turnUrls = server.getJSONArray("urls");
//                                String username = server.has("username") ? server.getString("username") : "";
//                                String credential = server.has("credential") ? server.getString("credential") : "";
//                                for (int j = 0; j < turnUrls.length(); j++) {
//                                    String turnUrl = turnUrls.getString(j);
//                                    PeerConnection.IceServer turnServer =
//                                            PeerConnection.IceServer.builder(turnUrl)
//                                                    .setUsername(username)
//                                                    .setPassword(credential)
//                                                    .createIceServer();
//                                    turnServers.add(turnServer);
//                                }
                            }
                            _parameter._servers = turnServers;
                        } catch (JSONException e) {
                            Log.d(TAG, "[createRoomInternal] JSONException : ");
                        }



                        WebRTCServerResult callbackData = new WebRTCServerResult();
                        callbackData.put(WebRTCServerResult.EXTRA_CODE, WebRTCServerResult.RESULT_CODE_SUCCESS);
                        callbackData.put(WebRTCServerResult.EXTRA_GUBUN, WebRTCServerResult.GUBUN_RECEIVER_SERVER);
                        callbackData.put(WebRTCServerResult.EXTRA_PARAMS, _parameter);
                        if (_callback != null)
                            _callback.onResult(callbackData);

                        RequestUrls request = _httpObj.create(RequestUrls.class);
                        request.foundAnswer(_parameter.roomId, "OFFER").enqueue(onFoundOfferResult);
                    }
                });
        httpConnection.send();
    }
    Callback<Result> onFoundOfferResult = new Callback<Result>() {
        @Override
        public void onResponse(Call<Result> call, Response<Result> response) {
            if (_parameter.initiator == RoomConnectionParameters.INITIATOR_CONNECTOR) {
                // OFFER
                Status status = response.body().getStatus();
                int result = status.getResult();
                Log.d(TAG, "onFoundOfferResult result : " + result);
                if (status.getCode() == 200) {
                    switch (result) {
                        case 0:
                            if (stat == STATUS_ON) {
                                // FOUND OFFER
                                WebRTCServerResult returnData = new WebRTCServerResult();
                                returnData.put(WebRTCServerResult.EXTRA_CODE, WebRTCServerResult.RESULT_CODE_SUCCESS);
                                returnData.put(WebRTCServerResult.EXTRA_GUBUN, WebRTCServerResult.GUBUN_FOUND_OFFER);
                                returnData.put(WebRTCServerResult.EXTRA_PARAMS, response.body().getSignalInfo());
                                if (_callback != null) {
                                    _callback.onResult(returnData);
                                }
                            }
                            break;
                        case 1:
                            if (stat == STATUS_ON) {
                                // NO OFFER RETRY
                                _handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        connectRoomInternal();
                                    }
                                }, 1000);
                            }
                            break;
                    }
                } else {
                    // failed
                }
            }
        }

        @Override
        public void onFailure(Call<Result> call, Throwable t) {
            Log.d(TAG, "onFoundAnswerResult error : " + t.getMessage());
        }
    };




    public void createRoom(RoomConnectionParameters parameter) {
        // create room
        stat = STATUS_ON;
        _parameter = parameter;
        createRoomInternal(_parameter.user.getIdx(), _parameter.roomTitle);
    }

    private void createRoomInternal(final int midx, final String title) {
        // get ICEServer
        Log.d(TAG, "[createRoomInternal] start AsynHttpUrlConnection");
        AsyncHttpURLConnection httpConnection =
                new AsyncHttpURLConnection("GET", serverlist_url, "", new AsyncHttpURLConnection.AsyncHttpEvents() {
                    @Override
                    public void onHttpError(String errorMessage) {
                        Log.d(TAG, "[createRoomInternal] onError : " + errorMessage);
                        if (_callback != null)
                            _callback.onResult(getErrorResult(errorMessage));
                    }

                    @Override
                    public void onHttpComplete(String response) {
                        Log.d(TAG, "[createRoomInternal] onComplete : " + response);
                        try {
                            List<PeerConnection.IceServer> turnServers = new ArrayList<>();
                            JSONObject responseJSON = new JSONObject(response);
                            JSONArray iceServers = responseJSON.getJSONArray("iceServers");
                            for (int i = 0; i < iceServers.length(); ++i) {
                                JSONObject server = iceServers.getJSONObject(i);
                                String serverUrl = server.getString("url");
                                serverUrl = serverUrl.replace("sturn:", "");
                                PeerConnection.IceServer sturnServer = new PeerConnection.IceServer(serverUrl);
                                turnServers.add(sturnServer);
                            }
                            _parameter._servers = turnServers;
                        } catch (JSONException e) {
                            Log.d(TAG, "[createRoomInternal] JSONException : ");
                        }
                        insertRoomId(title, _parameter.roomId, midx);
                    }
                });
        httpConnection.send();
    }



    public void insertRoomId(String title, String id, int midx) {
        Log.d(TAG, "[insertRoomId] : ");
        Retrofit _httpObj = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        HashMap<String, Object> postData = new HashMap<>();
        postData.put("title", title);
        postData.put("room_id", id);
        postData.put("midx", midx);
        RequestUrls request = _httpObj.create(RequestUrls.class);
        request.insertId(postData).enqueue(onInsertCallback);
    }



    Callback<RoomCheck> onInsertCallback = new Callback<RoomCheck>() {
        @Override
        public void onResponse(Call<RoomCheck> call, Response<RoomCheck> response) {
            Log.d(TAG, "[insertRoomId] : onInsertCallback " + response.body().toString());
            Status result = response.body().getStatus();
            if (result != null) {
                switch (result.getCode()) {
                    case 200:
                        WebRTCServerResult callbackData = new WebRTCServerResult();
                        callbackData.put(WebRTCServerResult.EXTRA_CODE, WebRTCServerResult.RESULT_CODE_SUCCESS);
                        callbackData.put(WebRTCServerResult.EXTRA_GUBUN, WebRTCServerResult.GUBUN_CREATE_RESULT);
                        callbackData.put(WebRTCServerResult.EXTRA_PARAMS, _parameter);
                        if (_callback != null)
                            _callback.onResult(callbackData);

                        break;
                    case 400:
                        if (_callback != null)
                            _callback.onResult(getErrorResult("Invalid Data"));
                        break;
                }
            }
        }

        @Override
        public void onFailure(Call<RoomCheck> call, Throwable t) {
            Log.d(TAG, "[insertRoomId] : onFailure ");
            if (_callback != null)
                _callback.onResult(getErrorResult(t.getMessage()));
        }
    };


    public WebRTCServerResult getErrorResult(String msg) {
        WebRTCServerResult result = new WebRTCServerResult();
        result.put(WebRTCServerResult.EXTRA_CODE, WebRTCServerResult.RESULT_CODE_ERROR);
        result.put(WebRTCServerResult.EXTRA_MESSAGE, msg);
        return result;
    }

    SessionDescription _sdp;



    public void sendSessionDescription(RoomConnectionParameters param, SessionDescription sdp) {
        _sdp = sdp;
        _parameter = param;
        try {
            String enData = Utils.Encrypt(sdp.description, _parameter.encr);
            HashMap<String, Object> data = new HashMap<>();
            data.put("encr", _parameter.encr);
            data.put("data", enData);
            data.put("userid", _parameter.user.getUserId());
            data.put("type", sdp.type);
            data.put("roomid", _parameter.roomId);
            RequestUrls request = _httpObj.create(RequestUrls.class);
            request.signalingSend(data).enqueue(signalingResult);
        } catch (Exception e) {
            // Error Occur
        }
    }


    Callback<Result> signalingResult = new Callback<Result>() {
        @Override
        public void onResponse(Call<Result> call, Response<Result> response) {
//            Log.d(TAG, "[signalingResult onResponse]");
            if (_parameter.initiator == RoomConnectionParameters.INITIATOR_NEW) {
                // OFFER
                Status status = response.body().getStatus();
                if (status.getCode() == 200) {
                    SignalingData sigData = response.body().getSignal();
//                    Log.d(TAG, "[signalingResult onResponse] onsuccess sig type : " + sigData.getType());
                    if (sigData.getType().equals("OFFER")) {
                        foundAnswer();
                    } else {
                    }
                } else {
                }
            }
        }

        @Override
        public void onFailure(Call<Result> call, Throwable t) {
            Log.d(TAG, "[signalingResult onFailure] : " + t.getMessage());
        }
    };



    public void sendCandidate(RoomConnectionParameters parameter, IceCandidate candidate) {
        _parameter = parameter;
        try {
            String sCandidate = candidate.sdpMid+"#"+candidate.sdpMLineIndex+"#"+candidate.sdp;
            String enData = Utils.Encrypt(sCandidate, _parameter.encr);
            HashMap<String, Object> data = new HashMap<>();
            data.put("encr", _parameter.encr);
            data.put("data", enData);
            data.put("userid", _parameter.user.getUserId());
            data.put("type", "CANDIDATE");
            data.put("roomid", _parameter.roomId);
            RequestUrls request = _httpObj.create(RequestUrls.class);
            request.signalingSend(data).enqueue(insertCandidateResult);
        } catch (Exception e) {
            // Error Occur
            // Please Retry
        }
    }

    Callback<Result> insertCandidateResult = new Callback<Result>() {
        @Override
        public void onResponse(Call<Result> call, Response<Result> response) {
        }

        @Override
        public void onFailure(Call<Result> call, Throwable t) {
        }
    };

    public void foundCandidate() {
        RequestUrls request = _httpObj.create(RequestUrls.class);
        request.foundCandidate(_parameter.roomId, "CANDIDATE", _parameter.user.getUserId()).enqueue(onFoundCandidate);
    }

    Callback<Result> onFoundCandidate = new Callback<Result>() {
        @Override
        public void onResponse(Call<Result> call, Response<Result> response) {

            // OFFER
            Status status = response.body().getStatus();
            int result = status.getResult();
            Log.d(TAG, "onFoundCandidate result : " + result);

            if (status.getCode() == 200) {
                switch (result) {
                    case 0:
                        if (stat == STATUS_ON) {                            // FOUND ANSWER

                            try {
                                List<IceCandidate> candidates = new ArrayList<>();

                                List<CandidateInfo> infos = response.body().getCandidateInfo();
                                for(int i = 0 ; i < infos.size() ; i++){
                                    CandidateInfo info = infos.get(i);
                                    String candidateVal = Utils.Decrypt(info.getData(), info.getEncr());
                                    Log.d(TAG, " sdp : "+ candidateVal);
                                    String[] tempVals = candidateVal.split("#");
                                    IceCandidate candidate = new IceCandidate(tempVals[0], Integer.parseInt(tempVals[1]), tempVals[2]);
                                    candidates.add(candidate);
                                }

                                WebRTCServerResult returnData = new WebRTCServerResult();
                                returnData.put(WebRTCServerResult.EXTRA_CODE, WebRTCServerResult.RESULT_CODE_SUCCESS);
                                returnData.put(WebRTCServerResult.EXTRA_GUBUN, WebRTCServerResult.GUBUN_FOUND_CANDIDATE);
                                returnData.put(WebRTCServerResult.EXTRA_PARAMS, candidates);
                                if (_callback != null) {
                                    _callback.onResult(returnData);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case 1:
                        if (stat == STATUS_ON) {
                            // NO ANSWER RETRY
                            _handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    foundAnswer();
                                }
                            }, 1000);
                        }
                        break;
                }
            } else {
                // failed
            }

        }

        @Override
        public void onFailure(Call<Result> call, Throwable t) {
            Log.d(TAG, "onFoundAnswerResult error : " + t.getMessage());
        }
    };




    private void foundAnswer() {
        RequestUrls request = _httpObj.create(RequestUrls.class);
        request.foundAnswer(_parameter.roomId, "ANSWER").enqueue(onFoundAnswerResult);
    }

    Callback<Result> onFoundAnswerResult = new Callback<Result>() {
        @Override
        public void onResponse(Call<Result> call, Response<Result> response) {
            if (_parameter.initiator == RoomConnectionParameters.INITIATOR_NEW) {
                // OFFER
                Status status = response.body().getStatus();
                int result = status.getResult();
                Log.d(TAG, "onFoundAnswerResult result : " + result);

                if (status.getCode() == 200) {
                    switch (result) {
                        case 0:
                            if (stat == STATUS_ON) {
                                // FOUND ANSWER
                                WebRTCServerResult returnData = new WebRTCServerResult();
                                returnData.put(WebRTCServerResult.EXTRA_CODE, WebRTCServerResult.RESULT_CODE_SUCCESS);
                                returnData.put(WebRTCServerResult.EXTRA_GUBUN, WebRTCServerResult.GUBUN_FOUND_CONNECTOR);
                                returnData.put(WebRTCServerResult.EXTRA_PARAMS, response.body().getSignalInfo());
                                if (_callback != null) {
                                    _callback.onResult(returnData);
                                }
                            }
                            break;
                        case 1:
                            if (stat == STATUS_ON) {
                                // NO ANSWER RETRY
                                _handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        foundAnswer();
                                    }
                                }, 1000);
                            }
                            break;
                    }
                } else {
                    // failed
                }
            }
        }

        @Override
        public void onFailure(Call<Result> call, Throwable t) {
            Log.d(TAG, "onFoundAnswerResult error : " + t.getMessage());
        }
    };





    public void removeSignaling(){
        HashMap<String, Object> data = new HashMap<>();
        data.put("room_id", _parameter.roomId);
        RequestUrls request = _httpObj.create(RequestUrls.class);
        request.removeSignaling(data).enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {

            }
        });
    }

    public void resetList(){
        RequestUrls request = _httpObj.create(RequestUrls.class);
        request.resetRoomList(_parameter.roomId).enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {

            }
        });
    }

    public void disconnect() {
        stat = STATUS_OFF;
    }


    // Requests & returns a TURN ICE Server based on a request URL.  Must be run
    // off the main thread!
    private List<PeerConnection.IceServer> requestTurnServers(String url)
            throws IOException, JSONException {
        List<PeerConnection.IceServer> turnServers = new ArrayList<>();
//        Log.d(TAG, "Request TURN from: " + url);
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty("REFERER", "https://appr.tc");
        connection.setConnectTimeout(TURN_HTTP_TIMEOUT_MS);
        connection.setReadTimeout(TURN_HTTP_TIMEOUT_MS);
        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new IOException("Non-200 response when requesting TURN server from " + url + " : "
                    + connection.getHeaderField(null));
        }
        InputStream responseStream = connection.getInputStream();
        String response = drainStream(responseStream);
        connection.disconnect();
//        Log.d(TAG, "TURN response: " + response);
        JSONObject responseJSON = new JSONObject(response);
        JSONArray iceServers = responseJSON.getJSONArray("iceServers");
        for (int i = 0; i < iceServers.length(); ++i) {
            JSONObject server = iceServers.getJSONObject(i);
            JSONArray turnUrls = server.getJSONArray("urls");
            String username = server.has("username") ? server.getString("username") : "";
            String credential = server.has("credential") ? server.getString("credential") : "";
            for (int j = 0; j < turnUrls.length(); j++) {
                String turnUrl = turnUrls.getString(j);
                PeerConnection.IceServer turnServer =
                        PeerConnection.IceServer.builder(turnUrl)
                                .setUsername(username)
                                .setPassword(credential)
                                .createIceServer();
                turnServers.add(turnServer);
            }
        }
        return turnServers;
    }

    private static String drainStream(InputStream in) {
        Scanner s = new Scanner(in, "UTF-8").useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

}
