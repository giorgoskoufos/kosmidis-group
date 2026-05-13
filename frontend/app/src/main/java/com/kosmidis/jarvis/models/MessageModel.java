package com.kosmidis.jarvis.models;

import android.net.Uri;

import java.util.List;

public class MessageModel {

    public String message;
    public boolean isUser;
    public boolean isTyping;
    public List<Uri> imageUris;
    public List<String> fileNames;

    public MessageModel(String message, boolean isUser, boolean isTyping) {
        this(message, isUser, isTyping, null, null);
    }

    public MessageModel(String message, boolean isUser, boolean isTyping, List<Uri> imageUris) {
        this(message, isUser, isTyping, imageUris, null);
    }

    public MessageModel(String message,
                        boolean isUser,
                        boolean isTyping,
                        List<Uri> imageUris,
                        List<String> fileNames) {

        this.message = message;
        this.isUser = isUser;
        this.isTyping = isTyping;
        this.imageUris = imageUris;
        this.fileNames = fileNames;
    }
}