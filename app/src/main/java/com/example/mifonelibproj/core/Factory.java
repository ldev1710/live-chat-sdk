package com.example.mifonelibproj.core;

import android.content.Context;

import com.example.mifonelibproj.listener.MifoneCoreListener;
import com.example.mifonelibproj.model.other.ConfigMifoneCore;

import org.linphone.core.Call;


public class Factory {

    public static void createMifoneCore(Context context, ConfigMifoneCore configMifoneCore){
        MifoneCoreHandle.initMifoneCore(context,configMifoneCore);
    }

    public static void registerListener(MifoneCoreListener mifoneCoreListener){
        MifoneCoreHandle.registerListener(mifoneCoreListener);
    }

    public static void configCore(){
        MifoneCoreHandle.configMifoneCore();
    }

    public static void makeCall(String numberPhone){
        MifoneCoreHandle.callOut(numberPhone);
    }

    public static void cancelCall(){
        MifoneCoreHandle.cancelCall();
    }

    public static void acceptCall(){
        MifoneCoreHandle.acceptCall();
    }

    public static void sendDtms(int numb){
        MifoneCoreHandle.sendDtms(numb);
    }
}