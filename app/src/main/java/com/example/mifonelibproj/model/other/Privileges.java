package com.example.mifonelibproj.model.other;

public class Privileges {
    private String page;
    private String permission;

    public Privileges() {}

    public Privileges(String page, String permission) {
        this.page = page;
        this.permission = permission;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }
}
