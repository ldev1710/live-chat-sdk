package com.mitek.build.micall.sdk.model;

public class Call {
    int id;
    String remoteExtension;

    public int getId() {
        return id;
    }

    public Call(int id, String remoteExtension) {
        this.id = id;
        this.remoteExtension = remoteExtension;
    }

    public String getRemoteExtension() {
        return remoteExtension;
    }
}
