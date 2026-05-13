package com.kosmidis.jarvis.data;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class ApiClient {

    private static OkHttpClient client;

    private ApiClient() {}

    public static OkHttpClient getClient() {
        if (client == null) {
            client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();
        }

        return client;
    }
}