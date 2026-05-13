package com.kosmidis.jarvis.network;

import androidx.annotation.NonNull;

import com.kosmidis.jarvis.config.NetworkConfig;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GoogleIntegrationManager {

    public interface StatusCallback {
        void onSuccess(boolean isConnected);
        void onError(String errorMessage);
    }

    private final OkHttpClient client;

    public GoogleIntegrationManager(OkHttpClient client) {
        this.client = client;
    }

    public void checkStatus(
            int userId,
            String userToken,
            StatusCallback callback
    ) {
        String url = NetworkConfig.BASE_URL + "/api/users/" + userId + "/integrations/google/status";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + userToken)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError("[Google Status Connection Error]");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError("[Google Status Server Error: " + response.code() + "]");
                    return;
                }

                try {
                    String responseData = response.body().string();
                    JSONObject json = new JSONObject(responseData);
                    callback.onSuccess(json.getBoolean("isConnected"));
                } catch (Exception e) {
                    callback.onError("[Google Status Parse Error]");
                }
            }
        });
    }
}