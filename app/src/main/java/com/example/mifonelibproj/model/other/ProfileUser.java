package com.example.mifonelibproj.model.other;

public class ProfileUser {
    private String extension;
    private String password;
    private String domain;
    private String proxy;
    private String port;
    private String transport;

    public ProfileUser() {}

    public ProfileUser(
            String extension,
            String password,
            String domain,
            String proxy,
            String port,
            String transport) {
        this.extension = extension;
        this.password = password;
        this.domain = domain;
        this.proxy = proxy;
        this.port = port;
        this.transport = transport;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

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

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport;
    }
}
