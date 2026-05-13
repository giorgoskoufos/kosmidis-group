package com.kosmidis.jarvis.managers;

import android.net.Uri;
import android.view.View;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.kosmidis.jarvis.adapters.ChatAdapter;
import com.kosmidis.jarvis.models.MessageModel;

import java.util.ArrayList;
import java.util.List;

public class ChatUiManager {

    private final RecyclerView chatRecyclerView;
    private final LinearLayout welcomeLayout;
    private final View cardGooglePrompt;
    private final List<MessageModel> messageList;
    private final ChatAdapter chatAdapter;

    public ChatUiManager(
            RecyclerView chatRecyclerView,
            LinearLayout welcomeLayout,
            View cardGooglePrompt,
            List<MessageModel> messageList,
            ChatAdapter chatAdapter
    ) {
        this.chatRecyclerView = chatRecyclerView;
        this.welcomeLayout = welcomeLayout;
        this.cardGooglePrompt = cardGooglePrompt;
        this.messageList = messageList;
        this.chatAdapter = chatAdapter;
    }

    public void addMessage(String message, boolean isUser, boolean isTyping) {
        messageList.add(new MessageModel(message, isUser, isTyping));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        scrollToBottom();
        updateWelcomeVisibility(false, "");
    }

    public void addImageMessage(String message, boolean isUser, boolean isTyping, List<Uri> imageUris) {
        messageList.add(new MessageModel(message, isUser, isTyping, imageUris));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        scrollToBottom();
        updateWelcomeVisibility(false, "");
    }

    public void addFileMessage(String message, boolean isUser, boolean isTyping, List<String> fileNames) {
        messageList.add(new MessageModel(message, isUser, isTyping, null, fileNames));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        scrollToBottom();
        updateWelcomeVisibility(false, "");
    }

    public void removeTypingIndicator() {
        if (!messageList.isEmpty() && messageList.get(messageList.size() - 1).isTyping) {
            int removeIndex = messageList.size() - 1;
            messageList.remove(removeIndex);
            chatAdapter.notifyItemRemoved(removeIndex);
        }
    }

    public void clearMessages() {
        messageList.clear();
        chatAdapter.notifyDataSetChanged();
    }

    public void setMessagesFromServer(List<MessageModel> messages) {
        messageList.clear();
        messageList.addAll(messages);
        chatAdapter.notifyDataSetChanged();

        if (!messageList.isEmpty()) {
            chatRecyclerView.scrollToPosition(messageList.size() - 1);
        }
    }

    public boolean isEmpty() {
        return messageList.isEmpty();
    }

    public void updateWelcomeVisibility(boolean isGoogleConnected, String email) {
        if (messageList.isEmpty()) {
            welcomeLayout.setVisibility(View.VISIBLE);
            chatRecyclerView.setVisibility(View.GONE);

            if (email != null
                    && email.toLowerCase().endsWith("@gmail.com")
                    && !isGoogleConnected) {
                cardGooglePrompt.setVisibility(View.VISIBLE);
            } else {
                cardGooglePrompt.setVisibility(View.GONE);
            }
        } else {
            welcomeLayout.setVisibility(View.GONE);
            chatRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void scrollToBottom() {
        chatRecyclerView.smoothScrollToPosition(messageList.size() - 1);
    }
}