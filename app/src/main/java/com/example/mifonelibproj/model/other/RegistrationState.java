package com.example.mifonelibproj.model.other;

public class RegistrationState {
    public static int NONE = 0;
    public static int PROGRESS = 1;
    public static int OK = 2;
    public static int CLEARED = 3;
    public static int FAILED = 4;

    private int mValue;

    public RegistrationState(int value) {
        mValue = value;
    }

    public String toMessage(){
        switch (mValue){
            case 0:
                return "NONE";
            case 1:
                return "PROGRESS";
            case 2:
                return "OK";
            case 3:
                return "CLEARED";
            case 4:
                return "FAILED";
        }
        return "Invalid";
    }

    public int toInt(){
        return mValue;
    }

}
