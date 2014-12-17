package com.danilov.mangareader.core.notification.headsup;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.danilov.mangareader.R;
import com.danilov.mangareader.core.notification.headsup.remote.HRemoteViews;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by semyon on 17.12.14.
 */
public class HeadsUpNotification {


    private static Map<Integer, HRemoteViews> notificationViews = new HashMap<>();


    private Context context;

    private int notificationId = -1;
    private HRemoteViews content;

    public HeadsUpNotification(final Context context, final int notificationId, final HRemoteViews content) {
        this.notificationId = notificationId;
        this.content = content;
        this.context = context.getApplicationContext();
    }

    public void show() {
        Intent intent = new Intent(context, HeadsUpActivity.class);
        intent.putExtra(HeadsUpActivity.NOTIFICATION_ID, notificationId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notificationViews.put(notificationId, content);
        context.startActivity(intent);
    }

    public static class HeadsUpActivity extends FragmentActivity {

        static final String NOTIFICATION_ID = "NID";


        private int notificationId = -1;

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            super.onCreate(savedInstanceState);
            Intent intent = getIntent();
            notificationId = intent.getIntExtra(NOTIFICATION_ID, -1);
            if (notificationId == -1) {
                finish();
            }
            HRemoteViews remoteViews = notificationViews.remove(notificationId);
            View v = remoteViews.apply(getLayoutInflater());
            setContentView(v);
        }

        @Override
        public void onAttachedToWindow() {
            super.onAttachedToWindow();

            View view = getWindow().getDecorView();
            WindowManager.LayoutParams lp = (WindowManager.LayoutParams) view.getLayoutParams();
            lp.gravity = Gravity.LEFT | Gravity.TOP;
            lp.x = 10;
            lp.y = 10;
            lp.width = 300;
            lp.height = 300;
            getWindowManager().updateViewLayout(view, lp);
        }
    }

}
