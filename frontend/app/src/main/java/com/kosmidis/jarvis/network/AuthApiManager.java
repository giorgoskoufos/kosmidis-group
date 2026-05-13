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

public class AuthApiManager {

    public interface LoginCallback {
        void onSuccess(int userId, String token);
        void onError(String errorMessage);
    }

    public interface RegisterCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    private final OkHttpClient client;

    public static final MediaType JSON =
            MediaType.get("application/json; charset=utf-8");

    public AuthApiManager(OkHttpClient client) {
        this.client = client;
    }

    public void login(String email, String password, LoginCallback callback) {
        String url = NetworkConfig.BASE_URL + "/api/login";

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("email", email);
            jsonBody.put("password", password);

            RequestBody body = RequestBody.create(jsonBody.toString(), JSON);

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
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
                            JSONObject json = new JSONObject(responseData);
                            int userId = json.getInt("userId");
                            String token = json.getString("token");

                            callback.onSuccess(userId, token);
                        } catch (JSONException e) {
                            callback.onError("Login parse error");
                        }
                    } else {
                        try {
                            JSONObject errorJson = new JSONObject(responseData);
                            callback.onError(errorJson.optString("error", "Invalid credentials"));
                        } catch (JSONException e) {
                            callback.onError("Invalid credentials");
                        }
                    }
                }
            });

        } catch (JSONException e) {
            callback.onError("Request error");
        }
    }

    public void register(String email, String password, RegisterCallback callback) {
        String url = NetworkConfig.BASE_URL + "/api/register";

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("email", email);
            jsonBody.put("password", password);

            RequestBody body = RequestBody.create(jsonBody.toString(), JSON);

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    callback.onError("Connection error with Server");
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String responseData = response.body() != null ? response.body().string() : "";

                    if (response.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        try {
                            JSONObject errorJson = new JSONObject(responseData);
                            callback.onError(errorJson.optString("error", "Registration failed"));
                        } catch (JSONException e) {
                            callback.onError("Registration failed");
                        }
                    }
                }
            });

        } catch (JSONException e) {
            callback.onError("Request error");
        }
    }
}