package com.example.mifonelibproj.core;

import android.content.Context;

import com.example.mifonelibproj.listener.MifoneCoreListener;
import com.example.mifonelibproj.model.other.ConfigMifoneCore;
import com.example.mifonelibproj.model.other.User;

public class FactoryMifone {

    public static void createMifoneCore(Context context, ConfigMifoneCore configMifoneCore, User user){
        MifoneCoreHandle.initMifoneCore(context,configMifoneCore,user);
    }

    public static void registerListener(MifoneCoreListener mifoneCoreListener){
        MifoneCoreHandle.registerListener(mifoneCoreListener);
    }

    public static void holdCall(){
        MifoneCoreHandle.holdCall();
    }
    public static void resumeCall(){
        MifoneCoreHandle.resumeCall();
    }

    public  static String getNumbPhoneCallIn(){
        return MifoneCoreHandle.getNumbPhoneCallIn();
    }
    public static void transfer(String phoneNumber){
        MifoneCoreHandle.transfer(phoneNumber);
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
    public static void declineCall(){
        MifoneCoreHandle.declineCall();
    }
    public static void sendDtms(int numb){
        MifoneCoreHandle.sendDtms(numb);
    }
}