package com.example.mifonelibproj.model.response;

import androidx.annotation.Keep;
import com.google.gson.annotations.SerializedName;

@Keep
public class Logout {
    @SerializedName("code")
    private int code;

    @SerializedName("message")
    private String message;

    public Logout(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
