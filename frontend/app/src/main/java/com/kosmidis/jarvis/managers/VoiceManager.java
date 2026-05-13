package com.kosmidis.jarvis.managers;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Locale;

public class VoiceManager {

    public interface VoiceCallback {
        void onSpeechStart();
        void onSpeechEnd();
        void onSpeechResult(String text);
    }

    private final Activity activity;
    private final VoiceCallback callback;

    private SpeechRecognizer speechRecognizer;
    private Intent speechIntent;
    private TextToSpeech textToSpeech;

    private boolean isListening = false;

    public static final int RECORD_AUDIO_REQUEST_CODE = 1001;

    public VoiceManager(Activity activity, VoiceCallback callback) {
        this.activity = activity;
        this.callback = callback;

        setupTextToSpeech();
        setupSpeechRecognizer();
    }

    private void setupTextToSpeech() {
        textToSpeech = new TextToSpeech(activity, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(new Locale("el", "GR"));
            }
        });
    }

    private void setupSpeechRecognizer() {

        if (!SpeechRecognizer.isRecognitionAvailable(activity)) {
            Toast.makeText(activity, "Speech Recognition not supported", Toast.LENGTH_SHORT).show();
            return;
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(activity);

        speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        speechIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );

        speechIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                "el-GR"
        );

        speechIntent.putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                "Μίλα στον J.A.R.V.I.S."
        );

        speechRecognizer.setRecognitionListener(new RecognitionListener() {

            @Override
            public void onReadyForSpeech(Bundle params) {
                isListening = true;

                if (callback != null) {
                    callback.onSpeechStart();
                }

                Toast.makeText(activity, "Ακούω...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {
                isListening = false;

                if (callback != null) {
                    callback.onSpeechEnd();
                }
            }

            @Override
            public void onError(int error) {
                isListening = false;

                if (callback != null) {
                    callback.onSpeechEnd();
                }

                Toast.makeText(activity, "Δεν αναγνωρίστηκε φωνή", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResults(Bundle results) {
                isListening = false;

                ArrayList<String> matches =
                        results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                if (matches != null && !matches.isEmpty()) {
                    String spokenText = matches.get(0);

                    if (callback != null) {
                        callback.onSpeechResult(spokenText);
                    }
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
    }

    public void startListening() {

        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.RECORD_AUDIO
        ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    RECORD_AUDIO_REQUEST_CODE
            );

            return;
        }

        if (speechRecognizer != null && !isListening) {
            speechRecognizer.startListening(speechIntent);
        }
    }

    public void speak(String text) {

        if (textToSpeech != null && text != null && !text.trim().isEmpty()) {
            textToSpeech.speak(
                    text,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "jarvis_reply"
            );
        }
    }

    public void stopSpeaking() {
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
    }

    public void destroy() {

        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }

        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}