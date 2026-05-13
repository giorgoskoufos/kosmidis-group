package com.kosmidis.jarvis.data;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREFS_NAME = "AppPrefs";

    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_TOKEN = "userToken";
    private static final String KEY_GOOGLE_CONNECTED = "isGoogleConnected";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isLoggedIn() {
        return getUserId() != -1;
    }

    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    public String getUserToken() {
        return prefs.getString(KEY_USER_TOKEN, "");
    }

    public boolean isGoogleConnected() {
        return prefs.getBoolean(KEY_GOOGLE_CONNECTED, false);
    }

    public void saveSession(int userId, String email, String token) {
        prefs.edit()
                .putInt(KEY_USER_ID, userId)
                .putString(KEY_USER_EMAIL, email)
                .putString(KEY_USER_TOKEN, token)
                .apply();
    }

    public void setGoogleConnected(boolean connected) {
        prefs.edit()
                .putBoolean(KEY_GOOGLE_CONNECTED, connected)
                .apply();
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }
}