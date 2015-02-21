package com.danilov.mangareader.core.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.AccelerateInterpolator;
import android.widget.Checkable;
import android.widget.RelativeLayout;

import com.danilov.mangareader.R;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: document your custom view class.
 */
public class RippleView extends RelativeLayout implements Checkable {

    private static final String TAG = "RippleView";
    private int rippleColor = Color.RED;

    private int rippleColorOne = Color.RED;
    private int rippleColorTwo = Color.WHITE;
    private int backgroundColor = Color.WHITE;

    private boolean fillOnEnd = false;

    private float mDownX;
    private float mDownY;

    private Paint paintOne;

    private Paint paintTwo;

    private List<InkSpot> spots = new ArrayList<InkSpot>();

    public RippleView(Context context) {
        super(context);
        init(null, 0);
    }

    public RippleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public RippleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.RippleView, defStyle, 0);

        rippleColorOne = a.getColor(
                R.styleable.RippleView_rippleColorOne,
                rippleColorOne);

        rippleColorTwo = a.getColor(
                R.styleable.RippleView_rippleColorTwo,
                rippleColorTwo);

        fillOnEnd = a.getBoolean(
                R.styleable.RippleView_fillOnEnd,
                fillOnEnd);

        paintOne = new Paint();
        paintTwo = new Paint();

        backgroundColor = a.getColor(
                R.styleable.RippleView_backgroundColor,
                backgroundColor);

        setBackgroundColor(backgroundColor);

        rippleColor = rippleColorOne;
        paintOne.setColor(rippleColorOne);
        paintTwo.setColor(rippleColorTwo);
        currentPaint = paintOne;
        setDrawingCacheEnabled(false);
        a.recycle();
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            mDownX = event.getX();
            mDownY = event.getY();

            float max = Math.max(getWidth(), getHeight()) * 3.0f;
            InkSpot inkSpot = new InkSpot(currentPaint, mDownX, mDownY, max);
            ObjectAnimator animator = ObjectAnimator.ofFloat(inkSpot, "radius", 0,
                    max);
            animator.setInterpolator(new AccelerateInterpolator());
            animator.setDuration(400);
            animator.start();
            animator.addListener(inkSpot);
            spots.add(inkSpot);
            if (fillOnEnd) {
                changeColor();
            }
            Log.d(TAG, "Current spots quantity: " + spots.size());
        }
        return false;
    }

    private Paint currentPaint = paintOne;

    private void changeColor() {
        if (rippleColor == rippleColorOne) {
            rippleColor = rippleColorTwo;
            currentPaint = paintTwo;
        } else {
            rippleColor = rippleColorOne;
            currentPaint = paintOne;
        }
    }

    @Override
    protected void onDraw(@NonNull final Canvas canvas) {
        super.onDraw(canvas);
        for (InkSpot inkSpot : spots) {
            Paint paint = inkSpot.getPaint();
            float radius = inkSpot.getRadius();
            if (!fillOnEnd) {
                float maxRadius = inkSpot.getMaxRadius();
                paint.setAlpha((int) ((1 - (radius / maxRadius)) * 255));
            }
            canvas.drawCircle(inkSpot.getCenterX(), inkSpot.getCenterY(), radius, paint);
        }

    }

    @Override
    public boolean isActivated() {
        if (Build.VERSION.SDK_INT >= 11) {
            return super.isActivated();
        } else {
            return isChecked;
        }
    }

    private boolean isChecked;

    @Override
    public void setChecked(final boolean b) {
        isChecked = b;
        invalidate();
        refreshDrawableState();
    }

    @Override
    public boolean isChecked() {
        return isChecked;
    }

    @Override
    public void toggle() {
        setChecked(!isChecked);
    }

    private static final int[] mCheckedStateSet = {
            android.R.attr.state_checked,
    };

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, mCheckedStateSet);
        }
        return drawableState;
    }

    public void clear() {
        setChecked(false);
        spots.clear();
    }

    private class InkSpot implements Animator.AnimatorListener {

        public float centerX;

        public float centerY;

        private Paint paint;

        protected float radius;

        private float maxRadius;

        private InkSpot(final Paint paint, final float centerX, final float centerY, final float maxRadius) {
            this.paint = paint;
            this.centerY = centerY;
            this.centerX = centerX;
            this.maxRadius = maxRadius;
        }

        public Paint getPaint() {
            return paint;
        }

        public float getRadius() {
            return radius;
        }

        public void setRadius(final float radius) {
            this.radius = radius;
//            if (radius > 0 && !fillOnEnd) {
//                RadialGradient radialGradient = new RadialGradient(mDownX, mDownY,
//                        radius, Color.TRANSPARENT, rippleColor,
//                        Shader.TileMode.MIRROR);
//                paintOne.setShader(radialGradient);
//            }
            invalidate();
        }

        public float getCenterX() {
            return centerX;
        }

        public float getCenterY() {
            return centerY;
        }

        public float getMaxRadius() {
            return maxRadius;
        }

        @Override
        public void onAnimationStart(final Animator animator) {

        }

        @Override
        public void onAnimationEnd(final Animator animator) {
            spots.remove(this);
            setRadius(0.0f);
            if (fillOnEnd) {
                setBackgroundColor(paint.getColor());
            }
        }

        @Override
        public void onAnimationCancel(final Animator animator) {

        }

        @Override
        public void onAnimationRepeat(final Animator animator) {

        }
    }

}
