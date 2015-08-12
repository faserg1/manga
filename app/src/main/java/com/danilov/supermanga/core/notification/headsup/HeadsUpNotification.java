package com.danilov.supermanga.core.notification.headsup;


import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

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
