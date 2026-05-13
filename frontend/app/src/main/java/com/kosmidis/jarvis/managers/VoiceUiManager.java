package com.kosmidis.jarvis.managers;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class VoiceUiManager {

    private final LinearLayout voiceOverlay;
    private final ImageView voicePulseIcon;
    private final Animation voicePulseAnimation;

    public VoiceUiManager(
            LinearLayout voiceOverlay,
            ImageView voicePulseIcon
    ) {
        this.voiceOverlay = voiceOverlay;
        this.voicePulseIcon = voicePulseIcon;
        this.voicePulseAnimation = createPulseAnimation();
    }

    private Animation createPulseAnimation() {
        ScaleAnimation animation = new ScaleAnimation(
                1.0f,
                1.18f,
                1.0f,
                1.18f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
        );

        animation.setDuration(650);
        animation.setRepeatMode(Animation.REVERSE);
        animation.setRepeatCount(Animation.INFINITE);

        return animation;
    }

    public void show() {
        voiceOverlay.setVisibility(View.VISIBLE);
        voicePulseIcon.startAnimation(voicePulseAnimation);
    }

    public void hide() {
        voicePulseIcon.clearAnimation();
        voiceOverlay.setVisibility(View.GONE);
    }
}