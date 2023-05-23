package com.example.mifonelibproj.model.other;

public class CallLogState {
    public static final int OutGoing = 0;
    public static final int Incoming = 1;
    private int mValue;
    public CallLogState(int value){
        mValue = value;
    }

    public String getMessage(){
        switch (mValue){
            case 0: return "Calling out";
            case 1: return "Incoming call";
        }
        return "Invalid value int";
    }

    public int getInt(){
        return mValue;
    }

}
