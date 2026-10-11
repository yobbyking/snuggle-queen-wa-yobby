package com.snugglequeen.yobby;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

/**
 * Animated rainbow gradient background view — smooth shifting pink/violet/rose gradient.
 */
public class AnimatedBackgroundView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Handler h = new Handler(Looper.getMainLooper());
    private float shift = 0f;
    private boolean running = true;
    private final TickRunnable tick = new TickRunnable(this, h);

    public AnimatedBackgroundView(Context ctx) {
        super(ctx);
        h.post(tick);
    }

    public void stop() { running = false; h.removeCallbacks(tick); }

    @Override
    protected void onDraw(Canvas canvas) {
        float w = getWidth(), hgt = getHeight();
        if (w == 0 || hgt == 0) return;
        float p1 = shift;
        float p2 = (shift + 0.33f) % 1f;
        float p3 = (shift + 0.66f) % 1f;
        int c1 = blend(Color.parseColor("#FF5C8A"), Color.parseColor("#C44FE8"), p1);
        int c2 = blend(Color.parseColor("#C44FE8"), Color.parseColor("#FF5C8A"), p2);
        int c3 = blend(Color.parseColor("#FFC2D4"), Color.parseColor("#9D174D"), p3);
        LinearGradient grad = new LinearGradient(0, 0, w, hgt, new int[]{c1, c2, c3, c1}, null, Shader.TileMode.CLAMP);
        paint.setShader(grad);
        canvas.drawRect(0, 0, w, hgt, paint);
    }

    private int blend(int a, int b, float t) {
        int r = (int) (Color.red(a) + (Color.red(b) - Color.red(a)) * t);
        int g = (int) (Color.green(a) + (Color.green(b) - Color.green(a)) * t);
        int bl = (int) (Color.blue(a) + (Color.blue(b) - Color.blue(a)) * t);
        return Color.rgb(r, g, bl);
    }

    // Named static class — avoids d8 anonymous-class issue
    void onTick() {
        if (!running) return;
        shift = shift + 0.005f;
        if (shift > 1f) shift = 0f;
        invalidate();
    }

    static class TickRunnable implements Runnable {
        private final AnimatedBackgroundView view;
        private final Handler h;
        TickRunnable(AnimatedBackgroundView v, Handler h) { this.view = v; this.h = h; }
        @Override public void run() {
            view.onTick();
            h.postDelayed(this, 60);
        }
    }
}
