package com.danilov.supermanga.core.view;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.view.animation.Interpolator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.view.ViewHelper;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

/**
 * Created by Semyon on 06.06.2015.
 */
public class ViewV16 {

    private View view;

    private ViewV16(final View view) {
        this.view = view;
    }

    public static ViewV16 wrap(final View v) {
        if (v == null) {
            return null;
        }
        return new ViewV16(v);
    }

    public ViewPropertyAnimator animate() {
        return ViewPropertyAnimator.animate(view);
    }

    public void setPivotX(final int i) {
        ViewHelper.setPivotX(view, i);
    }


    public void setPivotY(final int i) {
        ViewHelper.setPivotY(view, i);
    }

    public void setScaleX(final float widthScale) {
        ViewHelper.setScaleX(view, widthScale);
    }

    public void setScaleY(final float heightScale) {
        ViewHelper.setScaleY(view, heightScale);
    }

    public void setTranslationX(final int leftDelta) {
        ViewHelper.setTranslationX(view, leftDelta);
    }

    public void setTranslationY(final int topDelta) {
        ViewHelper.setTranslationY(view, topDelta);
    }

    public void setAlpha(final int i) {
        ViewHelper.setAlpha(view, i);
    }


    public static class ViewPropertyAnimator extends com.nineoldandroids.view.ViewPropertyAnimator {

        private com.nineoldandroids.view.ViewPropertyAnimator wrappedAnimator;

        private WeakReference<android.view.ViewPropertyAnimator> mNative;

        private View v;
        private ViewPropertyAnimator(final View v) {
            this.v = v;
        }

        public static ViewPropertyAnimator animate(final View v) {
            ViewPropertyAnimator viewPropertyAnimatorNew = new ViewPropertyAnimator(v);
            com.nineoldandroids.view.ViewPropertyAnimator wrappedAnimator = com.nineoldandroids.view.ViewPropertyAnimator.animate(v);
            viewPropertyAnimatorNew.wrappedAnimator = wrappedAnimator;

            final int version = Integer.valueOf(Build.VERSION.SDK);
            if (version >= Build.VERSION_CODES.JELLY_BEAN) {
                Class  aClass = wrappedAnimator.getClass();
                Field field = null;
                try {
                    field = aClass.getDeclaredField("mNative");
                    field.setAccessible(true);
                    viewPropertyAnimatorNew.mNative = (WeakReference<android.view.ViewPropertyAnimator>) field.get(wrappedAnimator);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            return viewPropertyAnimatorNew;
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        public ViewPropertyAnimator withEndAction(final Runnable runnable) {
            final int version = Integer.valueOf(Build.VERSION.SDK);
            boolean done = false;
            if (version >= Build.VERSION_CODES.JELLY_BEAN && mNative != null) {
                android.view.ViewPropertyAnimator n = mNative.get();
                if (n != null) {
                    done = true;
                    n.withEndAction(runnable);
                }
            }
            if (!done) {
                wrappedAnimator.setListener(new OneShotCancelableAnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEndOk(final Animator animation) {
                        runnable.run();
                    }
                });
            }
            return this;
        }

        @Override
        public ViewPropertyAnimator setDuration(final long duration) {
            wrappedAnimator.setDuration(duration);
            return this;
        }

        @Override
        public long getDuration() {
            return wrappedAnimator.getDuration();
        }

        @Override
        public long getStartDelay() {
            return wrappedAnimator.getStartDelay();
        }

        @Override
        public ViewPropertyAnimator setStartDelay(final long startDelay) {
            wrappedAnimator.setStartDelay(startDelay);
            return this;
        }

        @Override
        public ViewPropertyAnimator setInterpolator(final Interpolator interpolator) {
            wrappedAnimator.setInterpolator(interpolator);
            return this;
        }

        @Override
        public ViewPropertyAnimator setListener(final Animator.AnimatorListener listener) {
            wrappedAnimator.setListener(listener);
            return this;
        }

        @Override
        public void start() {
            wrappedAnimator.start();
        }

        @Override
        public void cancel() {
            wrappedAnimator.cancel();
        }

        @Override
        public ViewPropertyAnimator x(final float value) {
            wrappedAnimator.x(value);
            return this;
        }

        @Override
        public ViewPropertyAnimator xBy(final float value) {
            wrappedAnimator.xBy(value);
            return this;
        }

        @Override
        public ViewPropertyAnimator y(final float value) {
            wrappedAnimator.y(value);
            return this;
        }

        @Override
        public ViewPropertyAnimator yBy(final float value) {
            wrappedAnimator.yBy(value);
            return this;
        }

        @Override
        public ViewPropertyAnimator rotation(final float value) {
            wrappedAnimator.rotation(value);
            return this;
        }

        @Override
        public ViewPropertyAnimator rotationBy(final float value) {
            wrappedAnimator.rotationBy(value);
            return this;
        }

        @Override
        public ViewPropertyAnimator rotationX(final float value) {
            wrappedAnimator.rotationX(value);
            return this;
        }

        @Override
        public ViewPropertyAnimator rotationXBy(final float value) {
            wrappedAnimator.rotationXBy(value);
            return this;
        }

        @Override
        public ViewPropertyAnimator rotationY(final float value) {
            wrappedAnimator.rotationY(value);
            return this;
        }

        @Override
        public ViewPropertyAnimator rotationYBy(final float value) {
            wrappedAnimator.rotationYBy(value);
            return this;
        }

        @Override
        public ViewPropertyAnimator translationX(final float value) {
            wrappedAnimator.translationX(value);
            return this;
        }

        @Override
        public ViewPropertyAnimator translationXBy(final float value) {
            wrappedAnimator.translationX(value);
            return this;
        }

        @Override
        public ViewPropertyAnimator translationY(final float value) {
            wrappedAnimator.translationY(value);
            return this;
        }

        @Override
        public ViewPropertyAnimator translationYBy(final float value) {
            wrappedAnimator.translationYBy(value);
            return this;
        }

        @Override
        public ViewPropertyAnimator scaleX(final float value) {
            wrappedAnimator.scaleX(value);
            return this;
        }

        @Override
        public ViewPropertyAnimator scaleXBy(final float value) {
            wrappedAnimator.scaleXBy(value);
            return this;
        }

        @Override
        public ViewPropertyAnimator scaleY(final float value) {
            wrappedAnimator.scaleY(value);
            return this;
        }

        @Override
        public ViewPropertyAnimator scaleYBy(final float value) {
            wrappedAnimator.scaleYBy(value);
            return this;
        }

        @Override
        public ViewPropertyAnimator alpha(final float value) {
            wrappedAnimator.alpha(value);
            return this;
        }

        @Override
        public ViewPropertyAnimator alphaBy(final float value) {
            wrappedAnimator.alphaBy(value);
            return this;
        }

    }

    public static class OneShotCancelableAnimatorListenerAdapter extends com.nineoldandroids.animation.AnimatorListenerAdapter {

        /**
         * Created by Gustavo Claramunt (AnderWeb) on 2014/07/10
         *
         *
         *
         * This class extends android.animation.AnimatorListenerAdapter to bypass 2 issues:
         * Issue 1: on Android 4.0.x onAnimationStart/onAnimationEnd are called twice.
         *
         * Issue 2: when you cancel() an animation, there's no way for your listener to know
         * if the animation has been canceled before executing your code.
         *
         * This class is meant to be used by implementing 3 (optional) methods:
         * -onAnimationStartOk()
         * -onAnimationEndOk()
         * -onAnimationEndCanceled()
         *
         **/

        private boolean doneStart = false;
        private boolean doneEnd = false;
        private boolean canceled = false;

        @Override
        public final void onAnimationStart(Animator animation) {
            super.onAnimationStart(animation);
            if (!doneStart) {
                doneStart = true;
                onAnimationStartOk(animation);
            }
        }

        @Override
        public final void onAnimationCancel(Animator animation) {
            super.onAnimationCancel(animation);
            canceled = true;
        }

        @Override
        public final void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            if (!doneEnd) {
                doneEnd = true;
                if (canceled) {
                    onAnimationEndCanceled(animation);
                } else {
                    onAnimationEndOk(animation);
                }
            }
        }

        /**
         * This method will be called ONLY ONCE when your animation starts
         * @param animation
         */
        public void onAnimationStartOk(Animator animation) {

        }

        /**
         * This method will be called ONLY ONCE when your animation ends and ONLY if it wasn't previously canceled
         * @param animation
         */
        public void onAnimationEndOk(Animator animation) {

        }

        /**
         * This method will be called ONLY ONCE when your animation ends and ONLY because it was canceled
         * @param animation
         */
        public void onAnimationEndCanceled(Animator animation) {

        }

    }

    public static class OneShotAnimatorListener implements android.animation.Animator.AnimatorListener {


        private boolean doneStart = false;
        private boolean doneEnd = false;
        private boolean canceled = false;
        /**
         * This method will be called ONLY ONCE when your animation starts
         * @param animation
         */
        public void onAnimationStartOk(final android.animation.Animator animation) {

        }

        /**
         * This method will be called ONLY ONCE when your animation ends and ONLY if it wasn't previously canceled
         * @param animation
         */
        public void onAnimationEndOk(final android.animation.Animator animation) {

        }

        /**
         * This method will be called ONLY ONCE when your animation ends and ONLY because it was canceled
         * @param animation
         */
        public void onAnimationEndCanceled(final android.animation.Animator animation) {

        }

        @Override
        public void onAnimationStart(final android.animation.Animator animation) {
            if (!doneStart) {
                doneStart = true;
                onAnimationStartOk(animation);
            }
        }

        @Override
        public void onAnimationEnd(final android.animation.Animator animation) {
            if (!doneEnd) {
                doneEnd = true;
                if (canceled) {
                    onAnimationEndCanceled(animation);
                } else {
                    onAnimationEndOk(animation);
                }
            }
        }

        @Override
        public void onAnimationCancel(final android.animation.Animator animation) {
            canceled = true;
        }

        @Override
        public void onAnimationRepeat(final android.animation.Animator animation) {

        }

    }

}