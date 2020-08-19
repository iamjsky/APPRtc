package com.soundleader.apprtctest.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import com.soundleader.apprtctest.R;
import com.soundleader.apprtctest.beans.Result;
import com.soundleader.apprtctest.beans.Status;
import com.soundleader.apprtctest.http.Constants;
import com.soundleader.apprtctest.http.RequestUrls;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegistActivity extends BaseActivity {

    final String TAG = "REGIST_A_L::";

    EditText _idTxt;
    EditText _passTxt;
    Spinner _typeSpinner;
    Retrofit _httpObj;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist);

        _idTxt = findViewById(R.id.edit_id);
        _passTxt = findViewById(R.id.edit_pass);
        _typeSpinner = findViewById(R.id.spinner_type);

        _httpObj = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

    }



    public void onRegist(View v){
        checkFields();
    }

    private void checkFields(){
        String id = _idTxt.getText().toString();
        id = id.trim().replace(" ", "");
        if(id.length() < 3){
            showMessageDialog("확인","ID는 3자리 이상으로 입력해주세요.", false);
            return;
        }

        String pass = _passTxt.getText().toString();
        if(pass.length() < 3){
            showMessageDialog("확인","비밀번호는 3자리 이상으로 입력해주세요.", false);
            return;
        }

        int typePos = _typeSpinner.getSelectedItemPosition();
        if(typePos == 0){
            showMessageDialog("확인","회원 구분을 선택해주세요.", false);
            return;
        }

        showProgressDialog();
        HashMap<String, Object> data = new HashMap<>();
        data.put("user_id", id);
        data.put("password", pass);
        data.put("type", (typePos-1));
        RequestUrls request = _httpObj.create(RequestUrls.class);
        request.requestRegist(data).enqueue(registResultCallback);
    }

    Callback<Result> registResultCallback = new Callback<Result>() {
        @Override
        public void onResponse(Call<Result> call, Response<Result> response) {
            hideProgressDialog();
            Log.d(TAG, response.raw().toString());
            Result result = response.body();
            Status staus = result.getStatus();
            if(result != null) {
                switch (staus.getCode()) {
                    case 200:
                        int gubun = staus.getResult();

                        if(gubun == 0){
                            // complete
                            setResult(RESULT_OK);
                            finish();
                        }else{
                            showMessageDialog("확인","이미 등록된 ID입니다. 다른 ID로 다시 시도해주세요",false);
                        }
                        break;
                }
            }
        }

        @Override
        public void onFailure(Call<Result> call, Throwable t) {
            hideProgressDialog();

            Log.d(TAG, t.getMessage());
            Log.d(TAG, t.toString());
        }
    };
}
