package com.danilov.supermanga.core.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Semyon Danilov on 07.10.2014.
 */
public class DrawerStub extends DrawerLayout {

    public DrawerStub(final Context context) {
        super(context);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected Parcelable onSaveInstanceState() {
        return null;
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onRestoreInstanceState(final Parcelable state) {

    }

    @Override
    public boolean onKeyUp(final int keyCode, final KeyEvent event) {
        return false;
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        return false;
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(final AttributeSet attrs) {
        return null;
    }

    @Override
    protected boolean checkLayoutParams(final ViewGroup.LayoutParams p) {
        return false;
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(final ViewGroup.LayoutParams p) {
        return null;
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return null;
    }

    @Override
    public boolean isDrawerVisible(final int drawerGravity) {
        return true;
    }

    @Override
    public boolean isDrawerVisible(final View drawer) {
        return true;
    }

    @Override
    public boolean isDrawerOpen(final int drawerGravity) {
        return false;
    }

    @Override
    public boolean isDrawerOpen(final View drawer) {
        return false;
    }

    @Override
    public void closeDrawer(final int gravity) {
    }

    @Override
    public void closeDrawer(final View drawerView) {
    }

    @Override
    public void openDrawer(final int gravity) {
    }

    @Override
    public void openDrawer(final View drawerView) {
    }

    @Override
    public void closeDrawers() {
    }

    @Override
    public void requestDisallowInterceptTouchEvent(final boolean disallowIntercept) {
    }

    @Override
    public boolean onTouchEvent(final MotionEvent ev) {
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent ev) {
        return true;
    }

    @Override
    protected boolean drawChild(final Canvas canvas, final View child, final long drawingTime) {
        return true;
    }

    @Override
    public void computeScroll() {
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void requestLayout() {
    }

    @Override
    protected void onLayout(final boolean changed, final int l, final int t, final int r, final int b) {
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onAttachedToWindow() {
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onDetachedFromWindow() {
    }

    @Override
    public int getDrawerLockMode(final View drawerView) {
        return 0;
    }

    @Override
    public int getDrawerLockMode(final int edgeGravity) {
        return 0;
    }

    @Override
    public void setDrawerLockMode(final int lockMode, final View drawerView) {
    }

    @Override
    public void setDrawerLockMode(final int lockMode, final int edgeGravity) {
    }

    @Override
    public void setDrawerLockMode(final int lockMode) {
    }

    @Override
    public void setDrawerListener(final DrawerListener listener) {
    }

    @Override
    public void setScrimColor(final int color) {
    }

    @Override
    public void setDrawerShadow(final int resId, final int gravity) {
    }

    @Override
    public void setDrawerShadow(final Drawable shadowDrawable, final int gravity) {
    }

}
