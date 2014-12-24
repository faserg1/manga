package com.danilov.mangareader.core.notification.headsup;


import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.danilov.mangareader.core.notification.headsupold.remote.HRemoteViews;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by semyon on 17.12.14.
 */
public class HeadsUpNotification {

    private Context context;

    private int notificationId = -1;
    private int layoutId;
    private View contentView;

    public HeadsUpNotification(final Context context, final int notificationId, final int layoutId) {
        this.notificationId = notificationId;
        this.context = context.getApplicationContext();

        LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentView = inflater.inflate(layoutId, null, false);
    }

    public View getContentView() {
        return contentView;
    }

    public void show() {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PRIORITY_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        lp.y = 50;
        wm.addView(contentView, lp);
    }

}
