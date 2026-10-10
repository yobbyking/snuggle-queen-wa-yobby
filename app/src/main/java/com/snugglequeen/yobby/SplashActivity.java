package com.snugglequeen.yobby;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.widget.VideoView;
import android.net.Uri;
import android.widget.RelativeLayout;

/**
 * SplashActivity — sweet love splash screen with auto-play song.
 * Shows a romantic gradient background + love words, plays a dance RnB song
 * for 5 seconds, then transitions to PermissionsActivity.
 */
public class SplashActivity extends Activity {

    private MediaPlayer mediaPlayer;
    private static final int SPLASH_DURATION = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Full screen + no action bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        // Build the splash layout programmatically
        RelativeLayout root = new RelativeLayout(this);
        root.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));

        // Gradient background (pink → purple → dark)
        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{
                        Color.parseColor("#FF6B9D"),   // pink
                        Color.parseColor("#C44FE8"),    // purple
                        Color.parseColor("#1A1A2E"),    // dark
                }
        );
        root.setBackground(gradient);

        // Love heart emoji (large, centered)
        TextView heartText = new TextView(this);
        heartText.setText("❤️");
        heartText.setTextSize(80);
        RelativeLayout.LayoutParams heartParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        heartParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        heartParams.addRule(RelativeLayout.ABOVE, 1001);
        heartText.setLayoutParams(heartParams);
        heartText.setId(1000);
        root.addView(heartText);

        // App name
        TextView appName = new TextView(this);
        appName.setText("Snuggle Queen Wa Yobby");
        appName.setTextSize(28);
        appName.setTextColor(Color.WHITE);
        appName.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        appName.setShadowLayer(10, 2, 2, Color.parseColor("#80000000"));
        RelativeLayout.LayoutParams nameParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        nameParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        appName.setId(1001);
        appName.setLayoutParams(nameParams);
        root.addView(appName);

        // Sweet love words
        TextView loveWords = new TextView(this);
        loveWords.setText("Every moment with you feels like magic ✨\nYou are my heart, my soul, my everything 💕\nForever yours, always and always 💖");
        loveWords.setTextSize(15);
        loveWords.setTextColor(Color.parseColor("#FFE4EC"));
        loveWords.setGravity(android.view.Gravity.CENTER);
        loveWords.setLineSpacing(8, 1.2f);
        loveWords.setPadding(40, 0, 40, 0);
        RelativeLayout.LayoutParams loveParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        loveParams.addRule(RelativeLayout.BELOW, 1001);
        loveParams.setMargins(0, 40, 0, 0);
        loveWords.setLayoutParams(loveParams);
        root.addView(loveWords);

        // Loading text at bottom
        TextView loading = new TextView(this);
        loading.setText("Loading love... 💞");
        loading.setTextSize(14);
        loading.setTextColor(Color.parseColor("#FF6B9D"));
        loading.setGravity(android.view.Gravity.CENTER);
        RelativeLayout.LayoutParams loadingParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        loadingParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        loadingParams.setMargins(0, 0, 0, 80);
        loading.setLayoutParams(loadingParams);
        root.addView(loading);

        setContentView(root);

        // Play the auto RnB/dance song
        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.love_song);
            if (mediaPlayer != null) {
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
            }
        } catch (Exception e) {
            // If song file is missing, continue without it
        }

        // Transition after SPLASH_DURATION
        new Handler().postDelayed(() -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
            Intent intent = new Intent(SplashActivity.this, PermissionsActivity.class);
            startActivity(intent);
            finish();
            // Fade transition
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }, SPLASH_DURATION);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
