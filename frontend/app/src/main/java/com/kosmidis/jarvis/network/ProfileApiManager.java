package com.kosmidis.jarvis.network;

import androidx.annotation.NonNull;

import com.kosmidis.jarvis.config.NetworkConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProfileApiManager {

    public interface LoadProfileCallback {
        void onSuccess(JSONObject profileJson);
        void onError(String errorMessage);
    }

    public interface SaveProfileCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    private final OkHttpClient client;

    public static final MediaType JSON =
            MediaType.get("application/json; charset=utf-8");

    public ProfileApiManager(OkHttpClient client) {
        this.client = client;
    }

    public void loadProfile(int userId, String token, LoadProfileCallback callback) {
        String url = NetworkConfig.BASE_URL + "/api/profile/" + userId;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError("Connection error");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseData = response.body() != null ? response.body().string() : "";

                if (response.isSuccessful()) {
                    try {
                        callback.onSuccess(new JSONObject(responseData));
                    } catch (JSONException e) {
                        callback.onError("Profile parse error");
                    }
                } else {
                    callback.onError("Error loading profile");
                }
            }
        });
    }

    public void saveProfile(JSONObject profileBody, String token, SaveProfileCallback callback) {
        String url = NetworkConfig.BASE_URL + "/api/profile";

        RequestBody body = RequestBody.create(profileBody.toString(), JSON);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError("Connection error");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError("Error saving profile");
                }
            }
        });
    }
}