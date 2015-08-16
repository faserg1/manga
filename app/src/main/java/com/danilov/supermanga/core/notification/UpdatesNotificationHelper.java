package com.danilov.supermanga.core.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.danilov.supermanga.R;
import com.danilov.supermanga.activity.MainActivity;

/**
 * Created by Semyon on 06.08.2015.
 */
public class UpdatesNotificationHelper {

    private static final int GOT_UPDATES_ID = 1235367;

    private Context context;
    private Notification notification;
    private NotificationManager notificationManager;

    public UpdatesNotificationHelper(final Context context) {
        this.context = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void buildNotification(final int quantity) {
        final Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(MainActivity.PAGE, MainActivity.MainMenuItem.UPDATES.toString());
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_layout);
        contentView.setImageViewResource(R.id.icon, R.drawable.ic_launcher);
        String message = context.getString(R.string.got_updates) + quantity;
        contentView.setTextViewText(R.id.text, context.getString(R.string.got_updates) + " " + quantity);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setContentIntent(pendingIntent)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setTicker(message)
                        .setContent(contentView);
        this.notification = mBuilder.build();
        this.notification.contentView = contentView;

        notification.flags = notification.flags | Notification.FLAG_INSISTENT;
        notificationManager.notify(GOT_UPDATES_ID, notification);
    }

}
