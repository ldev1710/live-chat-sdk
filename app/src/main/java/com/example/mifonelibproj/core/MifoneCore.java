package com.example.mifonelibproj.core;

import android.content.Context;

import com.example.mifonelibproj.listener.MifoneCoreListener;
import com.example.mifonelibproj.model.other.ConfigMifoneCore;

public interface MifoneCore {
    public void initMifoneCore(Context context, ConfigMifoneCore configMifoneCore);
    public void callOut(String phoneNumber);
    public void cancelCurrentCall();
    public void configMifoneCore();
    public void registerListener(MifoneCoreListener mifoneCoreListener);
}
