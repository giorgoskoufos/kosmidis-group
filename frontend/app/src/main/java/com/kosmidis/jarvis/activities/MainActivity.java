package com.kosmidis.jarvis.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.kosmidis.jarvis.R;
import com.kosmidis.jarvis.adapters.ChatAdapter;
import com.kosmidis.jarvis.data.SessionManager;
import com.kosmidis.jarvis.dialogs.IntegrationsGoogle;
import com.kosmidis.jarvis.dialogs.SettingsMenuDialog;
import com.kosmidis.jarvis.managers.AttachmentUiManager;
import com.kosmidis.jarvis.managers.ChatUiManager;
import com.kosmidis.jarvis.managers.FileManager;
import com.kosmidis.jarvis.managers.ImageManager;
import com.kosmidis.jarvis.managers.NavigationDrawerManager;
import com.kosmidis.jarvis.managers.VoiceManager;
import com.kosmidis.jarvis.managers.VoiceUiManager;
import com.kosmidis.jarvis.managers.ConversationManager;
import com.kosmidis.jarvis.models.MessageModel;
import com.kosmidis.jarvis.network.ChatApiManager;
import com.kosmidis.jarvis.network.FileApiManager;
import com.kosmidis.jarvis.network.GoogleIntegrationManager;
import com.kosmidis.jarvis.network.VisionApiManager;
import com.kosmidis.jarvis.utils.TextNormalizer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.noties.markwon.Markwon;
import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity {

    private EditText userInput;
    private Button sendButton;
    private ImageButton micButton;
    private ImageButton attachButton;

    private ImageManager imageManager;
    private FileManager fileManager;
    private VisionApiManager visionApiManager;
    private ChatApiManager chatApiManager;
    private GoogleIntegrationManager googleIntegrationManager;
    private VoiceManager voiceManager;
    private VoiceUiManager voiceUiManager;
    private FileApiManager fileApiManager;
    private ChatUiManager chatUiManager;
    private AttachmentUiManager attachmentUiManager;
    private NavigationDrawerManager navigationDrawerManager;
    private SessionManager sessionManager;
    private ConversationManager conversationManager;

    private LinearLayout selectedImagesPreviewList;
    private LinearLayout selectedImageContainer;
    private LinearLayout selectedFilesContainer;
    private ImageButton clearImageButton;

    private boolean shouldSpeakNextReply = false;
    private LinearLayout voiceOverlay;
    private ImageView voicePulseIcon;

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private NavigationView navigationView;

    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<MessageModel> messageList = new ArrayList<>();

    private LinearLayout welcomeLayout;
    private View cardGooglePrompt;

    private int currentConversationId = 1;
    private int currentUserId = -1;
    private String userToken = "";

    private Markwon markwon;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);

        userInput = findViewById(R.id.userInput);
        sendButton = findViewById(R.id.sendButton);
        micButton = findViewById(R.id.micButton);
        attachButton = findViewById(R.id.attachButton);

        selectedImagesPreviewList = findViewById(R.id.selectedImagesPreviewList);
        selectedImageContainer = findViewById(R.id.selectedImageContainer);
        selectedFilesContainer = findViewById(R.id.selectedFilesContainer);
        clearImageButton = findViewById(R.id.clearImageButton);

        voiceOverlay = findViewById(R.id.voiceOverlay);
        voicePulseIcon = findViewById(R.id.voicePulseIcon);
        voiceUiManager = new VoiceUiManager(voiceOverlay, voicePulseIcon);

        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.nav_view);

        navigationDrawerManager = new NavigationDrawerManager(
                drawerLayout,
                toolbar,
                navigationView
        );

        welcomeLayout = findViewById(R.id.welcomeLayout);
        cardGooglePrompt = findViewById(R.id.cardGooglePrompt);

        chatRecyclerView = findViewById(R.id.chatRecyclerView);

        imageManager = new ImageManager(
                this,
                selectedImageContainer,
                selectedImagesPreviewList,
                clearImageButton
        );

        fileManager = new FileManager(this);
        fileApiManager = new FileApiManager(this, client);

        attachmentUiManager = new AttachmentUiManager(
                this,
                imageManager,
                fileManager,
                selectedFilesContainer
        );

        visionApiManager = new VisionApiManager(this, client);
        chatApiManager = new ChatApiManager(client);
        googleIntegrationManager = new GoogleIntegrationManager(client);

        conversationManager = new ConversationManager(
                chatApiManager,
                visionApiManager,
                fileApiManager
        );

        currentUserId = sessionManager.getUserId();
        String userEmail = sessionManager.getUserEmail();
        userToken = sessionManager.getUserToken();

        if (currentUserId == -1) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        checkGoogleIntegrationStatus();

        markwon = Markwon.create(this);

        chatAdapter = new ChatAdapter(messageList, markwon);
        chatRecyclerView.setAdapter(chatAdapter);

        chatUiManager = new ChatUiManager(
                chatRecyclerView,
                welcomeLayout,
                cardGooglePrompt,
                messageList,
                chatAdapter
        );

        navigationDrawerManager.setup(
                userEmail,
                new NavigationDrawerManager.DrawerCallback() {
                    @Override
                    public void onNewChatClicked() {
                        createNewConversation();
                    }

                    @Override
                    public void onSettingsClicked() {
                        SettingsMenuDialog settingsDialog = new SettingsMenuDialog();
                        settingsDialog.show(getSupportFragmentManager(), "SettingsMenuDialog");
                    }

                    @Override
                    public void onLogoutClicked() {
                        sessionManager.clearSession();

                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        finish();
                    }

                    @Override
                    public void onConversationClicked(int conversationId) {
                        currentConversationId = conversationId;
                        fetchMessages(currentConversationId);
                    }
                }
        );

        sendButton.setOnClickListener(v -> processMessage(userInput.getText().toString(), false));

        cardGooglePrompt.setOnClickListener(v -> {
            IntegrationsGoogle integrationsGoogle = new IntegrationsGoogle();
            integrationsGoogle.show(getSupportFragmentManager(), "IntegrationsGoogle");
        });

        voiceManager = new VoiceManager(this, new VoiceManager.VoiceCallback() {
            @Override
            public void onSpeechStart() {
                voiceUiManager.show();
            }

            @Override
            public void onSpeechEnd() {
                voiceUiManager.hide();
            }

            @Override
            public void onSpeechResult(String spokenText) {
                voiceUiManager.hide();

                spokenText = TextNormalizer.normalizeJarvisName(spokenText);
                userInput.setText(spokenText);

                sendVoiceMessage(spokenText);
            }
        });

        micButton.setOnClickListener(v -> {
            if (voiceManager != null) {
                voiceManager.stopSpeaking();
            }

            voiceUiManager.show();
            voiceManager.startListening();
        });

        attachButton.setOnClickListener(v -> attachmentUiManager.showAttachmentDialog());

        fetchConversations();
        createNewConversation();
        handleDeepLink(getIntent());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FileManager.PICK_DOCUMENT_REQUEST && resultCode == RESULT_OK) {
            fileManager.handlePickerResult(data);
            attachmentUiManager.updateSelectedFilesPreview();

            Toast.makeText(
                    this,
                    fileManager.getSelectedFileUris().size() + " documents selected",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleDeepLink(intent);
    }

    private void handleDeepLink(Intent intent) {
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();

            if (uri != null && "jarvisapp".equals(uri.getScheme()) && "oauth2redirect".equals(uri.getHost())) {
                String status = uri.getQueryParameter("status");
                String isWriteEnabled = uri.getQueryParameter("write");

                if ("success".equals(status)) {
                    sessionManager.setGoogleConnected(true);

                    chatUiManager.updateWelcomeVisibility(
                            sessionManager.isGoogleConnected(),
                            sessionManager.getUserEmail()
                    );

                    String accessLevel = "true".equals(isWriteEnabled)
                            ? "Full Access (Read & Write)"
                            : "Read Only Access";

                    new MaterialAlertDialogBuilder(this)
                            .setTitle("Connection Successful")
                            .setMessage("J.A.R.V.I.S. is now connected to your Google Workspace with " + accessLevel + ".")
                            .setPositiveButton("Got it", (dialog, which) -> dialog.dismiss())
                            .show();
                } else {
                    Toast.makeText(this, "Failed to connect Google Account.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();

        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void lockChatInput() {
        userInput.setEnabled(false);
        sendButton.setEnabled(false);
        micButton.setEnabled(false);
        attachButton.setEnabled(false);
    }

    private void unlockChatInput() {
        userInput.setEnabled(true);
        sendButton.setEnabled(true);
        micButton.setEnabled(true);
        attachButton.setEnabled(true);
    }

    private void checkGoogleIntegrationStatus() {
        googleIntegrationManager.checkStatus(
                currentUserId,
                userToken,
                new GoogleIntegrationManager.StatusCallback() {
                    @Override
                    public void onSuccess(boolean isConnected) {
                        runOnUiThread(() -> {
                            sessionManager.setGoogleConnected(isConnected);

                            chatUiManager.updateWelcomeVisibility(
                                    sessionManager.isGoogleConnected(),
                                    sessionManager.getUserEmail()
                            );
                        });
                    }

                    @Override
                    public void onError(String errorMessage) {
                        // Silent fail
                    }
                }
        );
    }

    private void processMessage(String message, boolean speakReply) {
        boolean hasImage = imageManager != null && imageManager.hasSelectedImage();
        boolean hasFile = fileManager != null && fileManager.hasSelectedFiles();

        if (message.trim().isEmpty() && !hasImage && !hasFile) {
            return;
        }

        if (message.trim().isEmpty() && hasImage) {
            message = "Περιέγραψε αυτή την εικόνα.";
        }

        if (message.trim().isEmpty() && hasFile) {
            message = "Ανάλυσε αυτά τα αρχεία.";
        }

        shouldSpeakNextReply = speakReply;

        if (voiceManager != null) {
            voiceManager.stopSpeaking();
        }

        lockChatInput();
        hideKeyboard();

        final List<Uri> fileUrisToSend =
                fileManager != null && fileManager.hasSelectedFiles()
                        ? new ArrayList<>(fileManager.getSelectedFileUris())
                        : null;

        final List<String> fileNamesToShow = attachmentUiManager.getSelectedFileNames();

        final List<Uri> imageUrisToSend =
                imageManager != null && imageManager.hasSelectedImage()
                        ? imageManager.getSelectedImageUris()
                        : null;

        if (fileUrisToSend != null && !fileUrisToSend.isEmpty()) {
            chatUiManager.addFileMessage(message, true, false, fileNamesToShow);

            fileManager.clearFiles();
            attachmentUiManager.updateSelectedFilesPreview();

        } else if (imageUrisToSend != null && !imageUrisToSend.isEmpty()) {
            chatUiManager.addImageMessage(message, true, false, imageUrisToSend);
            imageManager.clearSelectedImages();

        } else {
            chatUiManager.addMessage(message, true, false);
        }

        userInput.setText("");

        final String finalMessage = message;

        new Handler().postDelayed(() -> {
            chatUiManager.addMessage("", false, true);

            if (fileUrisToSend != null && !fileUrisToSend.isEmpty()) {
                sendFileMessage(finalMessage, fileUrisToSend);
            } else if (imageUrisToSend != null && !imageUrisToSend.isEmpty()) {
                sendVisionMessage(finalMessage, imageUrisToSend);
            } else {
                sendMessageToServer(finalMessage);
            }
        }, 400);
    }

    private void sendVoiceMessage(String message) {
        processMessage(message, true);
    }

    private void fetchConversations() {

        conversationManager.fetchConversations(
                currentUserId,
                userToken,
                new ConversationManager.ConversationCallback() {

                    @Override
                    public void onConversationsLoaded(JSONArray jsonArray) {
                        runOnUiThread(() ->
                                navigationDrawerManager.updateConversationHistory(jsonArray)
                        );
                    }

                    @Override
                    public void onConversationCreated(int conversationId) {}

                    @Override
                    public void onMessagesLoaded(List<MessageModel> messages) {}

                    @Override
                    public void onMessageReply(String reply) {}

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() ->
                                Toast.makeText(
                                        MainActivity.this,
                                        error,
                                        Toast.LENGTH_SHORT
                                ).show()
                        );
                    }
                }
        );
    }

    private void createNewConversation() {

        conversationManager.createConversation(
                currentUserId,
                userToken,
                new ConversationManager.ConversationCallback() {

                    @Override
                    public void onConversationsLoaded(JSONArray jsonArray) {}

                    @Override
                    public void onConversationCreated(int conversationId) {
                        runOnUiThread(() -> {
                            currentConversationId = conversationId;

                            chatUiManager.clearMessages();

                            fetchConversations();

                            chatUiManager.updateWelcomeVisibility(
                                    sessionManager.isGoogleConnected(),
                                    sessionManager.getUserEmail()
                            );
                        });
                    }

                    @Override
                    public void onMessagesLoaded(List<MessageModel> messages) {}

                    @Override
                    public void onMessageReply(String reply) {}

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() ->
                                Toast.makeText(
                                        MainActivity.this,
                                        error,
                                        Toast.LENGTH_SHORT
                                ).show()
                        );
                    }
                }
        );
    }

    private void fetchMessages(int conversationId) {

        conversationManager.fetchMessages(
                conversationId,
                userToken,
                new ConversationManager.ConversationCallback() {

                    @Override
                    public void onConversationsLoaded(JSONArray jsonArray) {}

                    @Override
                    public void onConversationCreated(int conversationId) {}

                    @Override
                    public void onMessagesLoaded(List<MessageModel> messages) {
                        runOnUiThread(() -> {
                            chatUiManager.setMessagesFromServer(messages);

                            chatUiManager.updateWelcomeVisibility(
                                    sessionManager.isGoogleConnected(),
                                    sessionManager.getUserEmail()
                            );
                        });
                    }

                    @Override
                    public void onMessageReply(String reply) {}

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() ->
                                Toast.makeText(
                                        MainActivity.this,
                                        error,
                                        Toast.LENGTH_SHORT
                                ).show()
                        );
                    }
                }
        );
    }

    private void sendMessageToServer(String messageText) {

        conversationManager.sendTextMessage(
                currentConversationId,
                messageText,
                userToken,
                new ConversationManager.ConversationCallback() {

                    @Override
                    public void onConversationsLoaded(JSONArray jsonArray) {}

                    @Override
                    public void onConversationCreated(int conversationId) {}

                    @Override
                    public void onMessagesLoaded(List<MessageModel> messages) {}

                    @Override
                    public void onMessageReply(String reply) {
                        runOnUiThread(() -> {
                            chatUiManager.removeTypingIndicator();
                            chatUiManager.addMessage(reply, false, false);

                            if (shouldSpeakNextReply && voiceManager != null) {
                                voiceManager.speak(reply);
                            }

                            new Handler().postDelayed(
                                    () -> shouldSpeakNextReply = false,
                                    500
                            );

                            fetchConversations();
                            unlockChatInput();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            chatUiManager.removeTypingIndicator();
                            chatUiManager.addMessage(error, false, false);
                            unlockChatInput();
                        });
                    }
                }
        );
    }

    private void sendVisionMessage(String message, List<Uri> imageUris) {

        if (imageUris == null || imageUris.isEmpty()) {
            sendMessageToServer(message);
            return;
        }

        conversationManager.sendVisionMessage(
                currentConversationId,
                message,
                imageUris,
                userToken,
                new ConversationManager.ConversationCallback() {

                    @Override
                    public void onConversationsLoaded(JSONArray jsonArray) {}

                    @Override
                    public void onConversationCreated(int conversationId) {}

                    @Override
                    public void onMessagesLoaded(List<MessageModel> messages) {}

                    @Override
                    public void onMessageReply(String reply) {
                        runOnUiThread(() -> {
                            chatUiManager.removeTypingIndicator();
                            chatUiManager.addMessage(reply, false, false);

                            fetchConversations();
                            unlockChatInput();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            chatUiManager.removeTypingIndicator();
                            chatUiManager.addMessage(error, false, false);
                            unlockChatInput();
                        });
                    }
                }
        );
    }

    private void sendFileMessage(String message, List<Uri> fileUris) {

        if (fileUris == null || fileUris.isEmpty()) {
            sendMessageToServer(message);
            return;
        }

        conversationManager.sendFileMessage(
                currentConversationId,
                message,
                fileUris,
                userToken,
                new ConversationManager.ConversationCallback() {

                    @Override
                    public void onConversationsLoaded(JSONArray jsonArray) {}

                    @Override
                    public void onConversationCreated(int conversationId) {}

                    @Override
                    public void onMessagesLoaded(List<MessageModel> messages) {}

                    @Override
                    public void onMessageReply(String reply) {
                        runOnUiThread(() -> {
                            chatUiManager.removeTypingIndicator();
                            chatUiManager.addMessage(reply, false, false);

                            fetchConversations();
                            unlockChatInput();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            chatUiManager.removeTypingIndicator();
                            chatUiManager.addMessage(
                                    "[File Error] " + error,
                                    false,
                                    false
                            );

                            unlockChatInput();
                        });
                    }
                }
        );
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == VoiceManager.RECORD_AUDIO_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                voiceUiManager.show();
                voiceManager.startListening();
            } else {
                voiceUiManager.hide();

                Toast.makeText(
                        this,
                        "Χρειάζεται άδεια μικροφώνου για voice commands",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        voiceUiManager.hide();

        if (voiceManager != null) {
            voiceManager.destroy();
        }
    }
}