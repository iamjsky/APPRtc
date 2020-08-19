package com.soundleader.apprtctest.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.soundleader.apprtctest.R;
import com.soundleader.apprtctest.beans.Result;
import com.soundleader.apprtctest.beans.Status;
import com.soundleader.apprtctest.beans.Userdata;
import com.soundleader.apprtctest.http.Constants;
import com.soundleader.apprtctest.http.RequestUrls;
import com.soundleader.apprtctest.utils.CSharedPref;

import java.util.HashMap;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends BaseActivity {

    final String TAG = "LOGIN_A_L::";
    EditText _idEdit;
    EditText _passEdit;

    Retrofit _httpObj;

    CSharedPref _pref;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CSharedPref.setDefaultVal(LoginActivity.this);

        setContentView(R.layout.activity_login);

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        _httpObj = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        _pref = new CSharedPref(LoginActivity.this);

        _idEdit = findViewById(R.id.edit_id);
        _passEdit = findViewById(R.id.edit_pass);

    }




    private boolean checkFields(){
        String id = _idEdit.getText().toString();
        String pass = _passEdit.getText().toString();

        id = id.trim().replace(" ","");

        if(id.length() < 3){
            showMessageDialog("확인"," ID는 3자 이상 입력해주세요.", false);
            return false;
        }
        if(pass.length() < 3){
            showMessageDialog("확인"," 비밀번호는 3자 이상 입력해주세요.", false);
            return false;
        }

        return true;
    }


    public void onLogin(View v){
        if(checkFields()){
            showProgressDialog();
            // Next Step
            String id = _idEdit.getText().toString();
            String pass = _passEdit.getText().toString();
            id = id.trim().replace(" ","");
            HashMap<String, Object> data = new HashMap<>();
            data.put("user_id", id);
            data.put("password", pass);
            RequestUrls request = _httpObj.create(RequestUrls.class);
            request.requestLogin(data).enqueue(registLoginCallback);
        }
    }






    Callback<Result> registLoginCallback = new Callback<Result>() {
        @Override
        public void onResponse(Call<Result> call, Response<Result> response) {
            Log.d(TAG,response.raw().toString());
            hideProgressDialog();

            Status status = response.body().getStatus();
            int code = status.getCode();
            int result = status.getResult();

            switch (code){
                case 200:
                    if(result == 0){
                        Userdata user = response.body().getUserdata();
                        // complete -> GO to Next Step
                        _pref.saveInt("midx", user.getIdx());
                        _pref.saveString("user_id", user.getUserId());
                        _pref.saveString("type", user.getUserType());
                        _pref.saveString("profile", user.getUserProfile());
                        _user = user;

                        String msg = "";
                        if(_user.getUserType().equals("0")){
                            msg = _user.getUserId()+" 강사님 환영합니다.";
                        }else {
                            msg = _user.getUserId()+" 수강생님 환영합니다.";
                        }
                        Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(LoginActivity.this, ListActivity.class);
                        startActivity(intent);
                        finish();
                    }else{
                        showMessageDialog("확인","ID 또는 비밀번호를 확인해주세요.", false);
                    }

                    break;
                case 300:
                    showMessageDialog("확인","가입된 회원정보를 찾을 수 없습니.", false);
                    break;
                case 400:
                    // 잘못된 접근
                    showMessageDialog("확인","잘못된 접근입니다. 잠시후 다시 시도해주세요.", false);
                    break;
            }

        }

        @Override
        public void onFailure(Call<Result> call, Throwable t) {

            Log.d(TAG,t.getMessage());
            hideProgressDialog();
            showMessageDialog("확인","통신 중 에러가 발생하였습니다. 잠시후 다시 시도해주세요.", false);
        }
    };






    public void onRegist(View v){
        Intent intent = new Intent(LoginActivity.this, RegistActivity.class);
        startActivityForResult(intent, 200);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 200:
                if(resultCode == RESULT_OK){
                    showMessageDialog("확인", "성공적으로 가입되었습니다.", false);
                }
                break;
        }
    }





}
