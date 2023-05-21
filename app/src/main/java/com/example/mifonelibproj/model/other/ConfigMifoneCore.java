package com.example.mifonelibproj.model.other;

public class ConfigMifoneCore {
    private long expire;
    private String accessToken;
    private String secret;

    public ConfigMifoneCore(long expire, String accessToken, String secret) {
        this.expire = expire;
        this.accessToken = accessToken;
        this.secret = secret;
    }

    public long getExpire() {
        return expire;
    }

    public void setExpire(long expire) {
        this.expire = expire;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
