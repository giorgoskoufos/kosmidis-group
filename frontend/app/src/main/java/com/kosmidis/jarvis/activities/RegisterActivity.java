package com.kosmidis.jarvis.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.kosmidis.jarvis.R;
import com.kosmidis.jarvis.data.ApiClient;
import com.kosmidis.jarvis.network.AuthApiManager;

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnRegister;
    private TextView tvGoToLogin;

    private AuthApiManager authApiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authApiManager = new AuthApiManager(ApiClient.getClient());

        bindViews();
        setupListeners();
    }

    private void bindViews() {
        etEmail = findViewById(R.id.etRegisterEmail);
        etPassword = findViewById(R.id.etRegisterPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);
    }

    private void setupListeners() {
        tvGoToLogin.setOnClickListener(v -> finish());
        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        authApiManager.register(email, password, new AuthApiManager.RegisterCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(
                            RegisterActivity.this,
                            "Registration successful! Please login.",
                            Toast.LENGTH_LONG
                    ).show();

                    finish();
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() ->
                        Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show()
                );
            }
        });
    }
}