package com.snugglequeen.yobby;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

/**
 * Floating hearts overlay — random love emojis drift up from the bottom of the screen.
 */
public class HeartsView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Handler h = new Handler(Looper.getMainLooper());
    private final ArrayList<float[]> hearts = new ArrayList<>();
    private final ArrayList<String> emojis = new ArrayList<>();
    private final String[] options = {"💕", "💖", "💗", "💞", "🌹", "✨", "💝", "❤️"};
    private final Random rnd = new Random();
    private boolean running = true;
    private final TickRunnable tick = new TickRunnable(this, h);

    public HeartsView(Context ctx) {
        super(ctx);
        h.post(tick);
    }

    public void stop() { running = false; h.removeCallbacks(tick); }

    void onTick() {
        if (!running) return;
        if (rnd.nextInt(6) == 0 && hearts.size() < 30) {
            float x = rnd.nextFloat() * getWidth();
            float y = getHeight() + 40;
            hearts.add(new float[]{x, y, 0.5f + rnd.nextFloat(), rnd.nextFloat() * 2f + 0.5f});
            emojis.add(options[rnd.nextInt(options.length)]);
        }
        for (int i = hearts.size() - 1; i >= 0; i--) {
            float[] hp = hearts.get(i);
            hp[1] = hp[1] - hp[3] * 2;
            hp[2] = hp[2] + 0.005f;
            if (hp[1] < -60) {
                hearts.remove(i);
                emojis.remove(i);
            }
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (int i = 0; i < hearts.size(); i++) {
            float[] hp = hearts.get(i);
            paint.setTextSize(28 + hp[2] * 12);
            paint.setAlpha(Math.min(255, (int)(200 * Math.min(1f, hp[2]))));
            canvas.drawText(emojis.get(i), hp[0], hp[1], paint);
        }
    }

    static class TickRunnable implements Runnable {
        private final HeartsView view;
        private final Handler h;
        TickRunnable(HeartsView v, Handler h) { this.view = v; this.h = h; }
        @Override public void run() {
            view.onTick();
            h.postDelayed(this, 40);
        }
    }
}
