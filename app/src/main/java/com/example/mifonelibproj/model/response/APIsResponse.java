package com.example.mifonelibproj.model.response;

import com.example.mifonelibproj.model.other.Privileges;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class APIsResponse {
    private int code = 0;
    private String message;
    private String data;
    private String groupId;
    private String user_log_id;
    private String secret;

    @SerializedName("privileges")
    private List<Privileges> privileges;

    @Override
    public String toString() {
        return "APIsResponse{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data='" + data + '\'' +
                ", groupId='" + groupId + '\'' +
                ", user_log_id='" + user_log_id + '\'' +
                ", secret='" + secret + '\'' +
                ", privileges=" + privileges +
                '}';
    }

    public APIsResponse(
            int code,
            String message,
            String data,
            String groupId,
            String user_log_id,
            String secret,
            List<Privileges> privileges) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.groupId = groupId;
        this.user_log_id = user_log_id;
        this.secret = secret;
        this.privileges = privileges;
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

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getUser_log_id() {
        return user_log_id;
    }

    public void setUser_log_id(String user_log_id) {
        this.user_log_id = user_log_id;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public List<Privileges> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(List<Privileges> privileges) {
        this.privileges = privileges;
    }
}