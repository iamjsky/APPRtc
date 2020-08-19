package com.soundleader.apprtctest.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.soundleader.apprtctest.R;

public class CSharedPref {

    private final String PREFERENCES_NAME = "webrtc_pref";

    SharedPreferences _pref;

    public static void setDefaultVal(Context context){
        PreferenceManager.setDefaultValues(context, R.xml.preferences, false);
    }


    public CSharedPref(Context context){
        _pref = getPreferences(context);
    }

    private SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public void saveString(String key, String val){
        SharedPreferences.Editor editor = _pref.edit();
        editor.putString(key, val);
        editor.commit();
    }

    public String getString(String key){
        return _pref.getString(key, "null");
    }

    public void saveInt(String key, int val){
        SharedPreferences.Editor editor = _pref.edit();
        editor.putInt(key, val);
        editor.commit();
    }

    public int getInt(String key){ return _pref.getInt(key, -1);}


    public void resetLogin(){
        SharedPreferences.Editor editor = _pref.edit();
        editor.remove("midx");
        editor.remove("user_id");
        editor.remove("type");
        editor.commit();
    }
}
