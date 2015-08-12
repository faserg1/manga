package com.danilov.supermanga.core.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.danilov.supermanga.R;
import com.danilov.supermanga.core.view.library.OverScrollView;

/**
 * Created by Semyon on 14.12.2014.
 */
public class OverScrollViewParallax extends OverScrollView {

    private ParallaxView viewToParallax;
    private int offsetTop;
    private int startOffset;
    private int viewToParallaxId;

    public OverScrollViewParallax(final Context context) {
        super(context);
        init(null, 0);
    }

    public OverScrollViewParallax(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public OverScrollViewParallax(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.ScrollViewParallax, defStyle, 0);

        viewToParallaxId = a.getResourceId(
                R.styleable.ScrollViewParallax_viewToParallax,
                0);
        float oTop = a.getDimension(
                R.styleable.ScrollViewParallax_topOffset,
                0);
        startOffset = (int) a.getDimension(
                R.styleable.ScrollViewParallax_startOffset,
                0);
        offsetTop = (int) oTop;


        a.recycle();
    }

    private int initialPadding = -1;

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        View contentView = getChildAt(0);
        int lP = contentView.getPaddingLeft();
        int rP = contentView.getPaddingRight();
        int bP = contentView.getPaddingBottom();
        if (initialPadding == -1) {
            initialPadding = contentView.getPaddingTop();
        }
        if (viewToParallax == null) {
            View v = getRootView().findViewById(viewToParallaxId);
            if (Build.VERSION.SDK_INT >= 11) {
                viewToParallax = new ICSParallaxView(v);
            } else {
                viewToParallax = new PreICSParallaxView(v);
            }
        }
        scrollTo(0, startOffset);

        contentView.setPadding(lP, initialPadding + offsetTop, rP, bP);
    }

    @Override
    protected void onScrollChanged(final int horizontal, final int vertical, final int oldl, final int oldt) {
        super.onScrollChanged(horizontal, vertical, oldl, oldt);
        if (viewToParallax != null) {
            viewToParallax.setTranslationY(-(vertical / 2));
        }
    }

    private class PreICSParallaxView extends ParallaxView {

        private int lastOffset = 0;

        protected PreICSParallaxView(final View v) {
            super(v);
        }

        @Override
        public void setTranslationY(final float translate) {
            v.offsetTopAndBottom((int)translate - lastOffset);
            lastOffset = (int) translate;
        }

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private class ICSParallaxView extends ParallaxView {

        protected ICSParallaxView(final View v) {
            super(v);
        }

        @Override
        public void setTranslationY(final float translate) {
            v.setTranslationY(translate);
        }

    }

    private abstract class ParallaxView {

        protected View v;

        protected ParallaxView(final View v) {
            this.v = v;
        }

        public abstract void setTranslationY(final float translate);

    }

}