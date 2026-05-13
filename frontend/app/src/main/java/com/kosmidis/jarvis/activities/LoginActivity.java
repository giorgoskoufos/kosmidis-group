package com.kosmidis.jarvis.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.kosmidis.jarvis.R;
import com.kosmidis.jarvis.data.ApiClient;
import com.kosmidis.jarvis.data.SessionManager;
import com.kosmidis.jarvis.network.AuthApiManager;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvGoToRegister;

    private AuthApiManager authApiManager;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        if (sessionManager.isLoggedIn()) {
            openMainScreen();
            return;
        }

        setContentView(R.layout.activity_login);

        authApiManager = new AuthApiManager(ApiClient.getClient());

        bindViews();
        setupListeners();
    }

    private void bindViews() {
        etEmail = findViewById(R.id.etLoginEmail);
        etPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);
    }

    private void setupListeners() {
        tvGoToRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );

        btnLogin.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        authApiManager.login(email, password, new AuthApiManager.LoginCallback() {
            @Override
            public void onSuccess(int userId, String token) {
                runOnUiThread(() -> {
                    sessionManager.saveSession(userId, email, token);
                    openMainScreen();
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() ->
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show()
                );
            }
        });
    }

    private void openMainScreen() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}