package com.example.mifonelibproj.listener;

import com.example.mifonelibproj.model.other.RegistrationState;
import com.example.mifonelibproj.model.other.State;


public interface MifoneCoreListener {
    public void onResultConfigAccount(boolean isSuccess,String message);
    public void onCallStateChanged(State state, String message);
    public void onRegistrationStateChanged(RegistrationState state, String message);
    public void onError(String message);
    public void onExpiredAccessToken();
    public void onResultConfigProxy(boolean isSuccess);
//    public void onGlobalStateChanged(GlobalState gstate, String message);
//    public void onLogCollectionUploadStateChanged(Core.LogCollectionUploadState state, String info);
}