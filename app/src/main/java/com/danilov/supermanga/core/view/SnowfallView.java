package com.danilov.supermanga.core.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;

import com.danilov.supermanga.R;
import com.danilov.supermanga.core.util.AnimateDrawable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Semyon on 31.12.2015.
 */
public class SnowFallView extends View {

    private int snow_flake_count = 10;
    private final List<Drawable> drawables = new ArrayList<Drawable>();
    private int[][] coords;
    private Drawable snowFlake;

    public SnowFallView(final Context context) {
        super(context);
        init(context);
    }

    public SnowFallView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SnowFallView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(final Context context) {
        setFocusable(true);
        setFocusableInTouchMode(true);

        snowFlake = context.getResources().getDrawable(R.drawable.snow_flake);
        snowFlake.setBounds(0, 0, snowFlake.getIntrinsicWidth(), snowFlake
                .getIntrinsicHeight());
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        super.onSizeChanged(width, height, oldw, oldh);
        Random random = new Random();
        Interpolator interpolator = new LinearInterpolator();

        snow_flake_count = Math.max(width, height) / 20;
        coords = new int[snow_flake_count][];
        drawables.clear();
        for (int i = 0; i < snow_flake_count; i++) {
            Animation animation = new TranslateAnimation(0, height / 10
                    - random.nextInt(height / 5), 0, height + 30);
            animation.setDuration(10 * height + random.nextInt(5 * height));
            animation.setRepeatCount(-1);
            animation.initialize(10, 10, 10, 10);
            animation.setInterpolator(interpolator);

            coords[i] = new int[] { random.nextInt(width - 30), -30 };

            drawables.add(new AnimateDrawable(snowFlake, animation));
            animation.setStartOffset(random.nextInt(20 * height));
            animation.startNow();
        }
    }

    private boolean shouldDraw = true;


    @Override
    protected void onDraw(final Canvas canvas) {
        if (!shouldDraw) {
            return;
        }
        if (System.currentTimeMillis() > 1451814221404L) {
            shouldDraw = false;
            drawables.clear();
            return;
        }
        for (int i = 0; i < snow_flake_count; i++) {
            Drawable drawable = drawables.get(i);
            canvas.save();
            canvas.translate(coords[i][0], coords[i][1]);
            drawable.draw(canvas);
            canvas.restore();
        }
        invalidate();
    }


}
