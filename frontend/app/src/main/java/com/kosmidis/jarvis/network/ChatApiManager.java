package com.kosmidis.jarvis.network;

import androidx.annotation.NonNull;

import com.kosmidis.jarvis.config.NetworkConfig;

import org.json.JSONArray;
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

public class ChatApiManager {

    public interface TextMessageCallback {
        void onSuccess(String reply);
        void onError(String errorMessage);
    }

    public interface MessagesCallback {
        void onSuccess(JSONArray messages);
        void onError(String errorMessage);
    }

    public interface ConversationsCallback {
        void onSuccess(JSONArray conversations);
        void onError(String errorMessage);
    }

    public interface CreateConversationCallback {
        void onSuccess(int conversationId);
        void onError(String errorMessage);
    }

    private final OkHttpClient client;

    public static final MediaType JSON =
            MediaType.get("application/json; charset=utf-8");

    public ChatApiManager(OkHttpClient client) {
        this.client = client;
    }

    public void sendTextMessage(
            int conversationId,
            String messageText,
            String userToken,
            TextMessageCallback callback
    ) {
        String url = NetworkConfig.BASE_URL + "/api/chat";

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("conversationId", conversationId);
            jsonBody.put("message", messageText);

            RequestBody body = RequestBody.create(jsonBody.toString(), JSON);

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + userToken)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    callback.onError("[Connection Error]");
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (!response.isSuccessful() || response.body() == null) {
                        callback.onError("[Server Error: " + response.code() + "]");
                        return;
                    }

                    try {
                        String responseData = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseData);
                        callback.onSuccess(jsonResponse.getString("reply"));
                    } catch (JSONException e) {
                        callback.onError("[Parse Error]");
                    }
                }
            });

        } catch (JSONException e) {
            callback.onError("[Request Error]");
        }
    }

    public void fetchMessages(
            int conversationId,
            String userToken,
            MessagesCallback callback
    ) {
        String url = NetworkConfig.BASE_URL + "/api/messages/" + conversationId;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + userToken)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError("[Messages Connection Error]");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError("[Messages Server Error: " + response.code() + "]");
                    return;
                }

                try {
                    String responseData = response.body().string();
                    callback.onSuccess(new JSONArray(responseData));
                } catch (Exception e) {
                    callback.onError("[Messages Parse Error]");
                }
            }
        });
    }

    public void fetchConversations(
            int userId,
            String userToken,
            ConversationsCallback callback
    ) {
        String url = NetworkConfig.BASE_URL + "/api/conversations/" + userId;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + userToken)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError("[Conversations Connection Error]");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError("[Conversations Server Error: " + response.code() + "]");
                    return;
                }

                try {
                    String responseData = response.body().string();
                    callback.onSuccess(new JSONArray(responseData));
                } catch (Exception e) {
                    callback.onError("[Conversations Parse Error]");
                }
            }
        });
    }

    public void createConversation(
            int userId,
            String userToken,
            String title,
            CreateConversationCallback callback
    ) {
        String url = NetworkConfig.BASE_URL + "/api/conversations";

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("userId", userId);
            jsonBody.put("title", title);

            RequestBody body = RequestBody.create(jsonBody.toString(), JSON);

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + userToken)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    callback.onError("[Create Conversation Connection Error]");
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (!response.isSuccessful() || response.body() == null) {
                        callback.onError("[Create Conversation Server Error: " + response.code() + "]");
                        return;
                    }

                    try {
                        String responseData = response.body().string();
                        JSONObject json = new JSONObject(responseData);
                        callback.onSuccess(json.getInt("id"));
                    } catch (Exception e) {
                        callback.onError("[Create Conversation Parse Error]");
                    }
                }
            });

        } catch (JSONException e) {
            callback.onError("[Create Conversation Request Error]");
        }
    }
}