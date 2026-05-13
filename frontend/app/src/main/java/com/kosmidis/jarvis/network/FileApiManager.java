package com.kosmidis.jarvis.network;

import android.content.Context;
import android.net.Uri;

import com.kosmidis.jarvis.config.NetworkConfig;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class FileApiManager {

    private final Context context;
    private final OkHttpClient client;

    public FileApiManager(Context context, OkHttpClient client) {
        this.context = context;
        this.client = client;
    }

    public interface FileUploadCallback {
        void onSuccess(String reply);
        void onError(String error);
    }

    public void sendFiles(
            int conversationId,
            String message,
            List<Uri> fileUris,
            String token,
            FileUploadCallback callback
    ) {

        try {

            MultipartBody.Builder builder =
                    new MultipartBody.Builder()
                            .setType(MultipartBody.FORM);

            builder.addFormDataPart(
                    "conversationId",
                    String.valueOf(conversationId)
            );

            builder.addFormDataPart(
                    "message",
                    message
            );

            for (Uri uri : fileUris) {

                String fileName = "document";

                android.database.Cursor cursor =
                        context.getContentResolver().query(
                                uri,
                                null,
                                null,
                                null,
                                null
                        );

                if (cursor != null) {
                    int nameIndex =
                            cursor.getColumnIndex(
                                    android.provider.OpenableColumns.DISPLAY_NAME
                            );

                    if (nameIndex >= 0 && cursor.moveToFirst()) {
                        fileName = cursor.getString(nameIndex);
                    }

                    cursor.close();
                }

                InputStream inputStream =
                        context.getContentResolver().openInputStream(uri);

                java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();

                byte[] data = new byte[4096];
                int bytesRead;

                while ((bytesRead = inputStream.read(data)) != -1) {
                    buffer.write(data, 0, bytesRead);
                }

                inputStream.close();

                byte[] bytes = buffer.toByteArray();

                String mimeType = context.getContentResolver().getType(uri);

                if (mimeType == null) {
                    mimeType = "application/octet-stream";
                }

                RequestBody fileBody =
                        RequestBody.create(
                                bytes,
                                MediaType.parse(mimeType)
                        );
                builder.addFormDataPart(
                        "files",
                        fileName,
                        fileBody
                );
            }

            Request request =
                    new Request.Builder()
                            .url(NetworkConfig.BASE_URL + "/api/chat/files")
                            .addHeader("Authorization", "Bearer " + token)
                            .post(builder.build())
                            .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, java.io.IOException e) {
                    callback.onError("Upload failed");
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response)
                        throws java.io.IOException {

                    if (response.isSuccessful() && response.body() != null) {

                        String responseData = response.body().string();

                        try {

                            JSONObject jsonObject =
                                    new JSONObject(responseData);

                            callback.onSuccess(
                                    jsonObject.getString("reply")
                            );

                        } catch (Exception e) {
                            callback.onError("Invalid server response");
                        }

                    } else {
                        callback.onError("Server error");
                    }
                }
            });

        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }
}