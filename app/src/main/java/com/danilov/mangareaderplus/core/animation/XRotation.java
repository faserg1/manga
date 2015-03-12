package com.danilov.mangareaderplus.core.animation;

import android.view.View;

import com.danilov.mangareaderplus.core.util.Promise;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * Created by Semyon on 30.12.2014.
 */
public class XRotation {

    private View v;
    private float from;
    private float to;
    private int duration;

    public XRotation(final View v, final float from, final float to, final int duration) {
        this.v = v;
        this.from = from;
        this.to = to;
        this.duration = duration;
    }

    public Promise<Void> start() {
        final Promise<Void> promise = new Promise<>();
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(v, "rotationX", from, to);
        objectAnimator.setDuration(duration);
        objectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(final Animator animation) {

            }

            @Override
            public void onAnimationEnd(final Animator animation) {
                promise.finish(null, true);
            }

            @Override
            public void onAnimationCancel(final Animator animation) {

            }

            @Override
            public void onAnimationRepeat(final Animator animation) {

            }
        });
        objectAnimator.start();
        return promise;
    }

}
