package com.soundleader.apprtctest.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.soundleader.apprtctest.R;
import com.soundleader.apprtctest.beans.Userdata;
import com.soundleader.apprtctest.utils.CSharedPref;

public class SplashActivity extends BaseActivity {

    Handler _handler = new Handler();

    CSharedPref _pref;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        _handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                _pref = new CSharedPref(SplashActivity.this);

                if(_pref.getString("user_id") != "null"){
                    _user = new Userdata();
                    _user.setIdx(_pref.getInt("midx"));
                    _user.setUserId(_pref.getString("user_id"));
                    _user.setUserType(_pref.getString("type"));
                    _user.setUserProfile(_pref.getString("profile"));

                    String msg = "";

                    if(_user.getUserType().equals("0")){
                        msg = _user.getUserId()+" 강사님 환영합니다.";
                    }else {
                        msg = _user.getUserId()+" 수강생님 환영합니다.";
                    }
                    Toast.makeText(SplashActivity.this, msg, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(SplashActivity.this, ListActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        },1500);
    }
}



