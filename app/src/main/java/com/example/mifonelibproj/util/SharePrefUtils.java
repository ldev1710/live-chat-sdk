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

    public void put(String key, boolean v) {
        pref.edit().putBoolean(key, v).apply();
    }

    public void put(String key, List<Object> list) {
        put(key, new Gson().toJson(list));
    }

    // save local List<T> ====================================

    public <T> void setList(String key, List<T> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        set(key, json);
    }

    public void set(String key, String value) {
        if (pref != null) {
            pref.edit().putString(key, value).apply();
        }
    }

    public List<ContactsContract.Data> getCompaniesList(String key) {
        if (pref != null) {

            Gson gson = new Gson();
            List<ContactsContract.Data> companyList;

            String string = pref.getString(key, null);
            Type type = new TypeToken<List<ContactsContract.Data>>() {}.getType();
            companyList = gson.fromJson(string, type);
            return companyList;
        }
        return null;
    }

    // =========================================================

    public String getString(String key) {
        return pref.getString(key, null);
    }

    public int getInt(String key) {
        return pref.getInt(key, 0);
    }

    public Boolean getBoolean(String key) {
        return pref.getBoolean(key, false);
    }

    public <T> T getObject(String key, Class<T> object) {
        String json = getString(key);
        if (json == null) {
            return null;
        }
        return new Gson().fromJson(json, object);
    }

    @SuppressLint({"CommitPrefEdits", "ApplySharedPref"})
    public void removeKey(String key) {
        pref.edit().remove(key);
        pref.edit().apply();
    }

    @SuppressLint("ApplySharedPref")
    public void remove() {
        pref.edit().clear().apply();
        pref.edit().commit();
        pref.edit().apply();
    }

    public boolean isUserLoggedIn() {
        ProfileUser profileUser = getObject("ProfileUser", ProfileUser.class);
        if (profileUser != null) {
            return true;
        }
        return false;
    }

    public boolean isSaveData(String key) {
        return getCompaniesList(key) != null;
    }
}