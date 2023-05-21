package com.example.mifonelibproj.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //Thực thi phương thức check expire tại đây
        Log.d("TAG", "Check Expire: Have checked expired access token");
    }
}
