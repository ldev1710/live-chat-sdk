package com.example.mifonelibproj.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.ContactsContract;

import com.example.mifonelibproj.config.AppConfig;
import com.example.mifonelibproj.model.other.ProfileUser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class SharePrefUtils {

    private static SharePrefUtils sharePrefUtils;
    private static SharedPreferences pref;

    private SharePrefUtils() {
        pref = AppConfig.getContext().getSharedPreferences("Database", Context.MODE_PRIVATE);
    }

    public static SharePrefUtils getInstance() {
        if (sharePrefUtils == null) {
            sharePrefUtils = new SharePrefUtils();
        }
        return sharePrefUtils;
    }

    public void put(String key, Object value) {
        put(key, new Gson().toJson(value));
    }

    public void put(String key, String value) {
        pref.edit().putString(key, value).apply();
    }

}
