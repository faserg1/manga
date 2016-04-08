package com.danilov.supermanga.core.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by Semyon on 08.04.2016.
 */
public class AutoFitRecyclerView extends RecyclerView {

    private int columnWidth;

    private GridLayoutManager manager;

    private boolean customManagerSet = false;

    private boolean settingDefaultManager = true;

    public AutoFitRecyclerView(final Context context) {
        super(context);
        init(context, null);
    }

    public AutoFitRecyclerView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AutoFitRecyclerView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(final Context context, final AttributeSet attrs) {
        if (attrs != null) {
            int[] attrsArray = {
                    android.R.attr.columnWidth
            };
            TypedArray array = context.obtainStyledAttributes(
                    attrs, attrsArray);
            columnWidth = array.getDimensionPixelSize(0, -1);
            array.recycle();
        }
        manager = new GridLayoutManager(getContext(), 1);
        settingDefaultManager = true;
        setLayoutManager(manager);
        settingDefaultManager = false;
    }

    @Override
    public void setLayoutManager(final LayoutManager layout) {
        if (!settingDefaultManager) {
            customManagerSet = true;
            manager = null;
        }
        super.setLayoutManager(layout);
    }

    protected void onMeasure(final int widthSpec, final int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        if (columnWidth > 0 && !customManagerSet && manager != null) {
            int spanCount = Math.max(1, getMeasuredWidth() / columnWidth);
            manager.setSpanCount(spanCount);
        }
    }

}