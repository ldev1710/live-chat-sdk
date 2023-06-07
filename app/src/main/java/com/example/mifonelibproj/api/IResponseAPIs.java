package com.example.mifonelibproj.api;

import com.example.mifonelibproj.model.other.UpdateTokenFirebase;
import com.example.mifonelibproj.model.response.APIsResponse;
import com.example.mifonelibproj.model.response.Logout;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface IResponseAPIs {
    @FormUrlEncoded
    @POST("v1/users/login")
    Call<APIsResponse> isLoginData(
            @Field("email") String email,
            @Field("password") String password,
            @Field("type") String type);

    @FormUrlEncoded
    @POST("v1/logout")
    Call<Logout> isLogout(
            @Field("id") String id,
            @Field("token_device") String token,
            @Field("type") String type);

    @FormUrlEncoded
    @POST("v1/mifone/updateFirebaseToken")
    Call<UpdateTokenFirebase> isUpdateTokenFirebase(
            @Field("token") String token,
            @Field("secret") String secret,
            @Field("extension") String extension,
            @Field("provider") String provider);
}