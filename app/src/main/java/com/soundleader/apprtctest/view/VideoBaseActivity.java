package com.soundleader.apprtctest.view;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.URLUtil;

import com.soundleader.apprtctest.R;
import com.soundleader.apprtctest.beans.Userdata;
import com.soundleader.apprtctest.dialog.MessageDialog;
import com.soundleader.apprtctest.dialog.ProgressDialog;

import java.util.ArrayList;
import java.util.HashMap;

public class VideoBaseActivity extends BaseActivity {

    private static final int PERMISSION_REQUEST = 2;

    String TAG = "BASE_A_L::";


    @TargetApi(Build.VERSION_CODES.M)
    private String[] getMissingPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return new String[0];
        }

        PackageInfo info;
        try {
            info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Failed to retrieve permissions.");
            return new String[0];
        }

        if (info.requestedPermissions == null) {
            Log.w(TAG, "No requested permissions.");
            return new String[0];
        }

        ArrayList<String> missingPermissions = new ArrayList<>();
        for (int i = 0; i < info.requestedPermissions.length; i++) {
            if ((info.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) == 0) {
                missingPermissions.add(info.requestedPermissions[i]);
            }
        }
        Log.d(TAG, "Missing permissions: " + missingPermissions);

        return missingPermissions.toArray(new String[missingPermissions.size()]);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST) {
            String[] missingPermissions = getMissingPermissions();
            if (missingPermissions.length != 0) {
                // User didn't grant all the permissions. Warn that the application might not work
                // correctly.
                new AlertDialog.Builder(this)
                        .setMessage(R.string.missing_permissions_try_again)
                        .setPositiveButton(R.string.yes,
                                (dialog, id) -> {
                                    // User wants to try giving the permissions again.
                                    dialog.cancel();
                                    requestPermissions();
                                })
                        .setNegativeButton(R.string.no,
                                (dialog, id) -> {
                                    // User doesn't want to give the permissions.
                                    dialog.cancel();
                                    onPermissionsGranted();
                                })
                        .show();
            } else {
                // All permissions granted.
                onPermissionsGranted();
            }
        }
    }

    private void onPermissionsGranted() {
        // If an implicit VIEW intent is launching the app, go directly to that URL.
//        final Intent intent = getIntent();
//        if ("android.intent.action.VIEW".equals(intent.getAction()) && !commandLineRun) {
//            boolean loopback = intent.getBooleanExtra(CallActivity.EXTRA_LOOPBACK, false);
//            int runTimeMs = intent.getIntExtra(CallActivity.EXTRA_RUNTIME, 0);
//            boolean useValuesFromIntent =
//                    intent.getBooleanExtra(CallActivity.EXTRA_USE_VALUES_FROM_INTENT, false);
//            String room = sharedPref.getString(keyprefRoom, "");
//            connectToRoom(room, true, loopback, useValuesFromIntent, runTimeMs);
//        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // Dynamic permissions are not required before Android M.
            onPermissionsGranted();
            return;
        }

        String[] missingPermissions = getMissingPermissions();
        if (missingPermissions.length != 0) {
            requestPermissions(missingPermissions, PERMISSION_REQUEST);
        } else {
            onPermissionsGranted();
        }
    }




}
