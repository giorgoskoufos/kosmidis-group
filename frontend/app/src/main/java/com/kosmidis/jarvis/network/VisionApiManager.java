package com.kosmidis.jarvis.network;

import android.content.Context;
import android.net.Uri;

import com.kosmidis.jarvis.config.NetworkConfig;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class VisionApiManager {

    public interface VisionCallback {
        void onSuccess(String reply);
        void onError(String errorMessage);
    }

    private final Context context;
    private final OkHttpClient client;

    public VisionApiManager(Context context, OkHttpClient client) {
        this.context = context;
        this.client = client;
    }

    public void sendVisionMessage(
            int conversationId,
            String message,
            List<Uri> imageUris,
            String userToken,
            VisionCallback callback
    ) {
        try {
            MultipartBody.Builder multipartBuilder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("conversationId", String.valueOf(conversationId))
                    .addFormDataPart("message", message);

            int count = Math.min(imageUris.size(), 5);

            for (int i = 0; i < count; i++) {
                byte[] imageBytes = readBytesFromUri(imageUris.get(i));

                RequestBody imageBody = RequestBody.create(
                        imageBytes,
                        MediaType.parse("image/jpeg")
                );

                multipartBuilder.addFormDataPart(
                        "images",
                        "upload_" + i + ".jpg",
                        imageBody
                );
            }

            Request request = new Request.Builder()
                    .url(NetworkConfig.BASE_URL + "/api/chat/vision")
                    .addHeader("Authorization", "Bearer " + userToken)
                    .post(multipartBuilder.build())
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, java.io.IOException e) {
                    callback.onError("[Vision Connection Error]");
                }

                @Override
                public void onResponse(Call call, Response response) throws java.io.IOException {
                    if (!response.isSuccessful() || response.body() == null) {
                        callback.onError("[Vision Server Error: " + response.code() + "]");
                        return;
                    }

                    try {
                        String responseData = response.body().string();
                        JSONObject json = new JSONObject(responseData);
                        callback.onSuccess(json.getString("reply"));
                    } catch (Exception e) {
                        callback.onError("[Vision Parse Error]");
                    }
                }
            });

        } catch (Exception e) {
            callback.onError("[Image Read Error]");
        }
    }

    private byte[] readBytesFromUri(Uri uri) throws Exception {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);

        if (inputStream == null) {
            throw new Exception("Could not open input stream");
        }

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        byte[] data = new byte[4096];
        int bytesRead;

        while ((bytesRead = inputStream.read(data)) != -1) {
            buffer.write(data, 0, bytesRead);
        }

        inputStream.close();

        return buffer.toByteArray();
    }
}