package com.danilov.supermanga.core.util;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;

/**
 * Created by Semyon on 31.12.2015.
 */
class ProxyDrawable extends Drawable {

    private Drawable mProxy;

    public ProxyDrawable(Drawable target) {
        mProxy = target;
    }

    public Drawable getProxy() {
        return mProxy;
    }

    public void setProxy(Drawable proxy) {
        if (proxy != this) {
            mProxy = proxy;
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if (mProxy != null) {
            mProxy.draw(canvas);
        }
    }

    @Override
    public int getIntrinsicWidth() {
        return mProxy != null ? mProxy.getIntrinsicWidth() : -1;
    }

    @Override
    public int getIntrinsicHeight() {
        return mProxy != null ? mProxy.getIntrinsicHeight() : -1;
    }

    @Override
    public int getOpacity() {
        return mProxy != null ? mProxy.getOpacity() : PixelFormat.TRANSPARENT;
    }

    @Override
    public void setFilterBitmap(boolean filter) {
        if (mProxy != null) {
            mProxy.setFilterBitmap(filter);
        }
    }

    @Override
    public void setDither(boolean dither) {
        if (mProxy != null) {
            mProxy.setDither(dither);
        }
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        if (mProxy != null) {
            mProxy.setColorFilter(colorFilter);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        if (mProxy != null) {
            mProxy.setAlpha(alpha);
        }
    }
}

public class AnimateDrawable extends ProxyDrawable {

    private Animation mAnimation;
    private Transformation mTransformation = new Transformation();

    public AnimateDrawable(Drawable target) {
        super(target);
    }

    public AnimateDrawable(Drawable target, Animation animation) {
        super(target);
        mAnimation = animation;
    }

    public Animation getAnimation() {
        return mAnimation;
    }

    public void setAnimation(Animation anim) {
        mAnimation = anim;
    }

    public boolean hasStarted() {
        return mAnimation != null && mAnimation.hasStarted();
    }

    public boolean hasEnded() {
        return mAnimation == null || mAnimation.hasEnded();
    }

    @Override
    public void draw(Canvas canvas) {
        Drawable dr = getProxy();
        if (dr != null) {
            int sc = canvas.save();
            Animation anim = mAnimation;
            if (anim != null) {
                anim.getTransformation(
                        AnimationUtils.currentAnimationTimeMillis(),
                        mTransformation);
                canvas.concat(mTransformation.getMatrix());
            }
            dr.draw(canvas);
            canvas.restoreToCount(sc);
        }
    }
}