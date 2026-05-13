package com.kosmidis.jarvis.managers;

import android.net.Uri;

import com.kosmidis.jarvis.models.MessageModel;
import com.kosmidis.jarvis.network.ChatApiManager;
import com.kosmidis.jarvis.network.FileApiManager;
import com.kosmidis.jarvis.network.VisionApiManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ConversationManager {

    public interface ConversationCallback {
        void onConversationsLoaded(JSONArray jsonArray);
        void onConversationCreated(int conversationId);
        void onMessagesLoaded(List<MessageModel> messages);
        void onMessageReply(String reply);
        void onError(String error);
    }

    private final ChatApiManager chatApiManager;
    private final VisionApiManager visionApiManager;
    private final FileApiManager fileApiManager;

    public ConversationManager(
            ChatApiManager chatApiManager,
            VisionApiManager visionApiManager,
            FileApiManager fileApiManager
    ) {
        this.chatApiManager = chatApiManager;
        this.visionApiManager = visionApiManager;
        this.fileApiManager = fileApiManager;
    }

    public void fetchConversations(
            int userId,
            String token,
            ConversationCallback callback
    ) {
        chatApiManager.fetchConversations(
                userId,
                token,
                new ChatApiManager.ConversationsCallback() {
                    @Override
                    public void onSuccess(JSONArray conversations) {
                        callback.onConversationsLoaded(conversations);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        callback.onError(errorMessage);
                    }
                }
        );
    }

    public void createConversation(
            int userId,
            String token,
            ConversationCallback callback
    ) {
        chatApiManager.createConversation(
                userId,
                token,
                "New Conversation",
                new ChatApiManager.CreateConversationCallback() {
                    @Override
                    public void onSuccess(int conversationId) {
                        callback.onConversationCreated(conversationId);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        callback.onError(errorMessage);
                    }
                }
        );
    }

    public void fetchMessages(
            int conversationId,
            String token,
            ConversationCallback callback
    ) {
        chatApiManager.fetchMessages(
                conversationId,
                token,
                new ChatApiManager.MessagesCallback() {
                    @Override
                    public void onSuccess(JSONArray jsonArray) {

                        List<MessageModel> loadedMessages =
                                new ArrayList<>();

                        try {

                            for (int i = 0; i < jsonArray.length(); i++) {

                                JSONObject msgObj =
                                        jsonArray.getJSONObject(i);

                                String sender =
                                        msgObj.getString("sender");

                                String text =
                                        msgObj.getString("message_text");

                                boolean isUser =
                                        sender.equals("user");

                                loadedMessages.add(
                                        new MessageModel(
                                                text,
                                                isUser,
                                                false
                                        )
                                );
                            }

                            callback.onMessagesLoaded(loadedMessages);

                        } catch (Exception e) {
                            callback.onError("Message parse error");
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        callback.onError(errorMessage);
                    }
                }
        );
    }

    public void sendTextMessage(
            int conversationId,
            String message,
            String token,
            ConversationCallback callback
    ) {
        chatApiManager.sendTextMessage(
                conversationId,
                message,
                token,
                new ChatApiManager.TextMessageCallback() {
                    @Override
                    public void onSuccess(String reply) {
                        callback.onMessageReply(reply);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        callback.onError(errorMessage);
                    }
                }
        );
    }

    public void sendVisionMessage(
            int conversationId,
            String message,
            List<Uri> imageUris,
            String token,
            ConversationCallback callback
    ) {
        visionApiManager.sendVisionMessage(
                conversationId,
                message,
                imageUris,
                token,
                new VisionApiManager.VisionCallback() {
                    @Override
                    public void onSuccess(String reply) {
                        callback.onMessageReply(reply);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        callback.onError(errorMessage);
                    }
                }
        );
    }

    public void sendFileMessage(
            int conversationId,
            String message,
            List<Uri> fileUris,
            String token,
            ConversationCallback callback
    ) {
        fileApiManager.sendFiles(
                conversationId,
                message,
                fileUris,
                token,
                new FileApiManager.FileUploadCallback() {
                    @Override
                    public void onSuccess(String reply) {
                        callback.onMessageReply(reply);
                    }

                    @Override
                    public void onError(String error) {
                        callback.onError(error);
                    }
                }
        );
    }
}