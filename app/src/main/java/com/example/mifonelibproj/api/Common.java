package com.example.mifonelibproj.api;


import com.example.mifonelibproj.model.other.ProfileUser;

public class Common {
    public static ProfileUser curentUser;
    public static String groupId;
    public static int id_user;
    public static String mAddress;
    public static String BASE_URL = "https://api-prod.mipbx.vn/api/";
    public static String BASE_URL_LIST_USER =
            "https://api-prod.mipbx.vn/api/dev/getListUserByMiFone/";

    public static IResponseAPIs getAPIs() {
        return RetrofitClient.getClient(BASE_URL).create(IResponseAPIs.class);

    }
}