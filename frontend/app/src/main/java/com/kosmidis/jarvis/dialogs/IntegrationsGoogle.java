package com.kosmidis.jarvis.dialogs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.kosmidis.jarvis.R;
import com.kosmidis.jarvis.config.NetworkConfig;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class IntegrationsGoogle extends BottomSheetDialogFragment {

    private SwitchMaterial switchRead, switchWrite;
    private Button btnSync, btnDisconnect;
    private final OkHttpClient client = new OkHttpClient();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Load your dynamic integrations layout
        View view = inflater.inflate(R.layout.dialog_integrations, container, false);

        // Initialize UI components from your XML IDs (Using your class-level variables)
        switchRead = view.findViewById(R.id.switch_read_perms);
        switchWrite = view.findViewById(R.id.switch_write_perms);
        btnSync = view.findViewById(R.id.btn_google_connect);
        btnDisconnect = view.findViewById(R.id.btn_google_disconnect);

        // Initialize the new views locally (only needed for the setup)
        LinearLayout layoutNotConnected = view.findViewById(R.id.layout_not_connected);
        LinearLayout layoutAlreadyConnected = view.findViewById(R.id.layout_already_connected);
        TextView tvWarning = view.findViewById(R.id.tv_gmail_warning);

        // Fetch user preferences
        SharedPreferences prefs = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String userEmail = prefs.getString("userEmail", "");
        boolean isGoogleConnected = prefs.getBoolean("isGoogleConnected", false);

        // --- 1. HANDLE CONNECTION STATE ---
        if (isGoogleConnected) {
            // User is already connected: Show success layout, hide toggles
            layoutNotConnected.setVisibility(View.GONE);
            layoutAlreadyConnected.setVisibility(View.VISIBLE);
        } else {
            // User is NOT connected: Show setup toggles, hide success layout
            layoutNotConnected.setVisibility(View.VISIBLE);
            layoutAlreadyConnected.setVisibility(View.GONE);

            // --- 2. GMAIL RESTRICTION ---
            if (userEmail != null && !userEmail.toLowerCase().endsWith("@gmail.com")) {
                switchRead.setEnabled(false);
                switchWrite.setEnabled(false);
                btnSync.setEnabled(false);
                btnSync.setAlpha(0.5f);

                if (tvWarning != null) {
                    tvWarning.setVisibility(View.VISIBLE);
                }
            }
        }

        // --- 3. Logic for Read Toggle ---
        switchRead.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                // If the user TURNS OFF Read, then Full MUST also be turned off
                // because there is no Write without Read.
                if (switchWrite.isChecked()) {
                    switchWrite.setChecked(false);
                }
            }
        });

        // --- 4. Logic for Full Access Toggle ---
        switchWrite.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // If the user TURNS ON Full, then Read MUST automatically turn on.
                if (!switchRead.isChecked()) {
                    switchRead.setChecked(true);
                }
            }
        });

        // --- 5. Sync Button Click ---
        btnSync.setOnClickListener(v -> {
            startGoogleSync(); // Calling your original method safely
        });

        // --- 6. Disconnect Button Click ---
        btnDisconnect.setOnClickListener(v -> {
            disconnectAccount();
        });

        return view;
    }

    private void disconnectAccount() {
        SharedPreferences prefs = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("userToken", "");

        if (token.isEmpty()) {
            Toast.makeText(requireContext(), "Auth session error. Re-login.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button to prevent double-clicks
        btnDisconnect.setEnabled(false);

        String url = NetworkConfig.BASE_URL + "/api/integrations/google/disconnect";
        
        // Empty POST body
        RequestBody body = RequestBody.create("", null);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        btnDisconnect.setEnabled(true);
                        Toast.makeText(requireContext(), "Network Error: Failed to disconnect.", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        btnDisconnect.setEnabled(true);
                        if (response.isSuccessful()) {
                            // Update local state
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean("isGoogleConnected", false);
                            editor.apply();

                            Toast.makeText(requireContext(), "Google Account Disconnected.", Toast.LENGTH_SHORT).show();
                            
                            // Close the dialog - user will see the setup screen next time they open it
                            dismiss();
                        } else {
                            Toast.makeText(requireContext(), "Error: Server failed to process disconnect.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
    private void startGoogleSync() {
        // 1. Get the current user ID and Email from SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        int userId = prefs.getInt("userId", -1);
        String userEmail = prefs.getString("userEmail", ""); // Fetch email here

        // Safety check: If we don't have userId or email, do not proceed
        if (userId == -1 || userEmail.isEmpty()) {
            Toast.makeText(requireContext(), "User session error. Please re-login.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Capture the state of the switches (Read/Write)
        boolean canRead = switchRead.isChecked();
        boolean canWrite = switchWrite.isChecked();

        // 3. Construct the authentication URL for your Node.js server
        // Add the &email=... parameter
        // Use Uri.encode to correctly handle special characters like @
        String baseUrl = NetworkConfig.BASE_URL + "/api/auth/google";
        String authUrl = baseUrl + "?userId=" + userId
                + "&email=" + Uri.encode(userEmail)
                + "&read=" + canRead
                + "&write=" + canWrite;

        // 4. Open the System Browser for Google OAuth
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl));
        startActivity(intent);

        dismiss(); // Close the bottom sheet after starting the flow
    }
}