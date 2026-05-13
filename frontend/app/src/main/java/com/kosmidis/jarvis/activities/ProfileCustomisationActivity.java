package com.kosmidis.jarvis.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.kosmidis.jarvis.R;
import com.kosmidis.jarvis.data.ApiClient;
import com.kosmidis.jarvis.data.SessionManager;
import com.kosmidis.jarvis.network.ProfileApiManager;

import org.json.JSONException;
import org.json.JSONObject;

public class ProfileCustomisationActivity extends AppCompatActivity {

    private EditText etFirstNamePC, etLastNamePC, etAgePC, etProfessionPC, etInterestsPC;
    private Button btnSaveProfilePC, btnBackPC;

    private SessionManager sessionManager;
    private ProfileApiManager profileApiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_customisation);

        sessionManager = new SessionManager(this);
        profileApiManager = new ProfileApiManager(ApiClient.getClient());

        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Error: User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindViews();
        setupListeners();
        loadProfile();
    }

    private void bindViews() {
        etFirstNamePC = findViewById(R.id.etPCFirstName);
        etLastNamePC = findViewById(R.id.etPCLastName);
        etAgePC = findViewById(R.id.etPCAge);
        etProfessionPC = findViewById(R.id.etPCProfession);
        etInterestsPC = findViewById(R.id.etPCInterests);
        btnSaveProfilePC = findViewById(R.id.btnSaveProfilePC);
        btnBackPC = findViewById(R.id.btnBackPC);
    }

    private void setupListeners() {
        btnBackPC.setOnClickListener(v -> finish());
        btnSaveProfilePC.setOnClickListener(v -> saveProfile());
    }

    private void loadProfile() {
        profileApiManager.loadProfile(
                sessionManager.getUserId(),
                sessionManager.getUserToken(),
                new ProfileApiManager.LoadProfileCallback() {
                    @Override
                    public void onSuccess(JSONObject profileJson) {
                        runOnUiThread(() -> fillProfileFields(profileJson));
                    }

                    @Override
                    public void onError(String errorMessage) {
                        runOnUiThread(() ->
                                Toast.makeText(
                                        ProfileCustomisationActivity.this,
                                        errorMessage,
                                        Toast.LENGTH_SHORT
                                ).show()
                        );
                    }
                }
        );
    }

    private void fillProfileFields(JSONObject json) {
        etFirstNamePC.setText(json.optString("first_name", ""));
        etLastNamePC.setText(json.optString("last_name", ""));

        String ageStr = json.optString("age", "");

        if (!ageStr.equals("null") && !ageStr.isEmpty()) {
            etAgePC.setText(ageStr);
        } else {
            etAgePC.setText("");
        }

        etProfessionPC.setText(json.optString("profession", ""));
        etInterestsPC.setText(json.optString("interests", ""));
    }

    private void saveProfile() {
        try {
            JSONObject profileBody = new JSONObject();

            profileBody.put("userId", sessionManager.getUserId());
            profileBody.put("firstName", etFirstNamePC.getText().toString().trim());
            profileBody.put("lastName", etLastNamePC.getText().toString().trim());

            String ageStr = etAgePC.getText().toString().trim();
            profileBody.put("age", ageStr.isEmpty() ? JSONObject.NULL : Integer.parseInt(ageStr));

            profileBody.put("profession", etProfessionPC.getText().toString().trim());
            profileBody.put("interests", etInterestsPC.getText().toString().trim());

            profileApiManager.saveProfile(
                    profileBody,
                    sessionManager.getUserToken(),
                    new ProfileApiManager.SaveProfileCallback() {
                        @Override
                        public void onSuccess() {
                            runOnUiThread(() -> {
                                Toast.makeText(
                                        ProfileCustomisationActivity.this,
                                        "Profile saved!",
                                        Toast.LENGTH_SHORT
                                ).show();

                                finish();
                            });
                        }

                        @Override
                        public void onError(String errorMessage) {
                            runOnUiThread(() ->
                                    Toast.makeText(
                                            ProfileCustomisationActivity.this,
                                            errorMessage,
                                            Toast.LENGTH_SHORT
                                    ).show()
                            );
                        }
                    }
            );

        } catch (JSONException | NumberFormatException e) {
            Toast.makeText(this, "Please check your input", Toast.LENGTH_SHORT).show();
        }
    }
}