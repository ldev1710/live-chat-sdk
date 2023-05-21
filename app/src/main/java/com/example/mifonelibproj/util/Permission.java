package com.example.mifonelibproj.util;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class Permission {
    private Context context;
    public static final int REQUEST_CODE_AUDIO_PERMISSION = 0;
    public static final int REQUEST_CODE_READ_PHONE_STATE = 1;
    public Permission(Context context) {
        this.context = context;
    }

    public boolean checkPermissions(String[] permissions){
        boolean res = true;
        for(String permission: permissions){
            res &= ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
        }
        return res;
    }

    public void requestPermissions(String[] permissions,int requestCode){
        ((AppCompatActivity) context).requestPermissions(permissions,requestCode);
    }

}
