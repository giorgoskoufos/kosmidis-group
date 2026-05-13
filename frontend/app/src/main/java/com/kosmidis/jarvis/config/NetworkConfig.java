package com.kosmidis.jarvis.config;

/**
 * Centralized network settings for easy switching between Local and Production
 * servers.
 */
public class NetworkConfig {

    // --- LOCAL DEBUGGING ---
    // Android Studio Emulator
    public static final String BASE_URL = "http://10.0.2.2:3000";

    // --- PHYSICAL DEVICE ---
    // public static final String BASE_URL = "http://192.168.1.5:3000";

    // --- PRODUCTION ---
    // public static final String BASE_URL =
    //         "https://app-kosmidis-solo-backend.xadp6y.easypanel.host";
}