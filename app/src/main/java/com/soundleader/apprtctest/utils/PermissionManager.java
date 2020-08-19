package com.soundleader.apprtctest.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;

public class PermissionManager {

    String[] need_permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.INTERNET,Manifest.permission.WRITE_EXTERNAL_STORAGE};
    List<String> requirePermission;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean checkPermission(Context context){
        requirePermission = new ArrayList<>();
        boolean allgranted = true;
        // Here, thisActivity is the current activity
        for(String permission : need_permissions){
            if(context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED){

            }else{
                requirePermission.add(permission);
                allgranted = false;
            }
        }

        return allgranted;
    }

    public String[] getPermissionList(){

        String[] temp = new String[requirePermission.size()];
        int index = 0;
        for(String permission : requirePermission){
            temp[index] = permission;
            index++;
        }

        return temp;
    }
}
