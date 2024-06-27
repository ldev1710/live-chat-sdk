package com.mitek.build.micall.sdk.core.account;

public class MiCallAccount {
    String domain;
    String proxy;
    String port;
    String username;
    String password;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public MiCallAccount(String domain, String proxy, String port, String username, String password) {
        this.domain = domain;
        this.proxy = proxy;
        this.port = port;
        this.username = username;
        this.password = password;
    }
}
