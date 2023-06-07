package com.example.mifonelibproj.api;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    public static Retrofit retrofit = null;

    public static Retrofit getClient(String baseURL) {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(
                chain -> {
                    Request original = chain.request();
                    Request.Builder builder = original.newBuilder();
                    builder.header("Content-type", "application/json");
                    builder.method(original.method(), original.body()).build();
                    Request request = builder.build();
                    return chain.proceed(request);
                });
        httpClient.connectTimeout(60, TimeUnit.SECONDS);
        httpClient.readTimeout(60, TimeUnit.SECONDS);
        httpClient.writeTimeout(60, TimeUnit.SECONDS);

//        if (BuildConfig.DEBUG) {
//            HttpLoggingInterceptor interceptorBody = new HttpLoggingInterceptor();
//            interceptorBody.setLevel(HttpLoggingInterceptor.Level.BODY);
//            httpClient.addInterceptor(interceptorBody);
//        }

        if (retrofit == null) {
            retrofit =
                    new Retrofit.Builder()
                            .addConverterFactory(GsonConverterFactory.create())
                            .baseUrl(baseURL)
                            .client(httpClient.build())
                            .build();
        }
        return retrofit;
    }
}