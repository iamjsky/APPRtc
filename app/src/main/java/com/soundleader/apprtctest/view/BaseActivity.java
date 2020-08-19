package com.soundleader.apprtctest.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.webkit.URLUtil;

import com.soundleader.apprtctest.R;
import com.soundleader.apprtctest.beans.Userdata;
import com.soundleader.apprtctest.dialog.DialogButtonCallback;
import com.soundleader.apprtctest.dialog.MessageDialog;
import com.soundleader.apprtctest.dialog.ProgressDialog;

import java.util.HashMap;

public class BaseActivity extends AppCompatActivity implements DialogButtonCallback {

    String TAG = "BASE_A_L::";

    ProgressDialog _progressDialog;

    static Userdata _user;
    public SharedPreferences sharedPref;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _progressDialog = new ProgressDialog(BaseActivity.this,android.R.style.Theme_Translucent_NoTitleBar);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    }


    public void showMessageDialog(String title, String msg, boolean hasCancel){
        showMessageDialog(title, msg, hasCancel, this);
    }

    public void showMessageDialog(String title, String msg, boolean hasCancel, DialogButtonCallback callback){
        MessageDialog dialog = new MessageDialog(BaseActivity.this, callback);
        dialog.show(title, msg, hasCancel);
    }

    public void showProgressDialog(){
        _progressDialog.show();
    }

    public void hideProgressDialog(){
        _progressDialog.dismiss();
    }

    @Override
    public void onDialogCallback(HashMap<String, Object> result) {

    }


    /**
     * Get a value from the shared preference or from the intent, if it does not
     * exist the default is used.
     */
    @Nullable
    public String sharedPrefGetString(
            int attributeId, String intentName, int defaultId, boolean useFromIntent) {
        String defaultValue = getString(defaultId);
        if (useFromIntent) {
            String value = getIntent().getStringExtra(intentName);
            if (value != null) {
                return value;
            }
            return defaultValue;
        } else {
            String attributeName = getString(attributeId);
            return sharedPref.getString(attributeName, defaultValue);
        }
    }

    /**
     * Get a value from the shared preference or from the intent, if it does not
     * exist the default is used.
     */
    public boolean sharedPrefGetBoolean(
            int attributeId, String intentName, int defaultId, boolean useFromIntent) {
        boolean defaultValue = Boolean.parseBoolean(getString(defaultId));
        if (useFromIntent) {
            return getIntent().getBooleanExtra(intentName, defaultValue);
        } else {
            String attributeName = getString(attributeId);
            return sharedPref.getBoolean(attributeName, defaultValue);
        }
    }

    /**
     * Get a value from the shared preference or from the intent, if it does not
     * exist the default is used.
     */
    public int sharedPrefGetInteger(
            int attributeId, String intentName, int defaultId, boolean useFromIntent) {
        String defaultString = getString(defaultId);
        int defaultValue = Integer.parseInt(defaultString);
        if (useFromIntent) {
            return getIntent().getIntExtra(intentName, defaultValue);
        } else {
            String attributeName = getString(attributeId);
            String value = sharedPref.getString(attributeName, defaultString);
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
//                Log.e(TAG, "Wrong setting for: " + attributeName + ":" + value);
                return defaultValue;
            }
        }
    }



    public boolean validateUrl(String url) {
        if (URLUtil.isHttpsUrl(url) || URLUtil.isHttpUrl(url)) {
            return true;
        }

        new AlertDialog.Builder(this)
                .setTitle(getText(R.string.invalid_url_title))
                .setMessage(getString(R.string.invalid_url_text, url))
                .setCancelable(false)
                .setNeutralButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                .create()
                .show();
        return false;
    }

}
