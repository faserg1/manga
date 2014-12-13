package com.danilov.manga.core.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.support.v4.app.NotificationCompat;
import android.widget.ImageView;
import android.widget.RemoteViews;

import com.danilov.manga.R;
import com.danilov.manga.core.service.MangaDownloadService;

/**
 * Created by Semyon on 13.12.2014.
 */
public class NotificationHelper {

    private Notification notification;

    private MangaDownloadService downloadService;

    private Context ctx;

    private ImageView fakeImageView;

    private static final int MANGA_DOWNLOAD_SERVICE = 1;

    private RemoteViews contentView;

    private NotificationManager notificationManager;

    public NotificationHelper(final MangaDownloadService downloadService) {
        this.downloadService = downloadService;
        ctx = downloadService.getApplicationContext();
        fakeImageView = new NImageView(ctx);
        notificationManager = (NotificationManager)downloadService
                .getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void buildNotification() {
        final Intent emptyIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, emptyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        contentView = new RemoteViews(downloadService.getPackageName(), R.layout.notification_layout);
        contentView.setImageViewResource(R.id.icon, R.drawable.ic_russia);
        contentView.setTextViewText(R.id.title, "My notification");
        contentView.setTextViewText(R.id.text, "Hello World!");

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(ctx)
                        .setContentIntent(pendingIntent)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContent(contentView);
        this.notification = mBuilder.build();
        this.notification.contentView = contentView;
        downloadService.startForeground(MANGA_DOWNLOAD_SERVICE, notification);
    }

    public void setIcon(final Bitmap bitmap) {
        RemoteViews views = contentView;
        views.setImageViewBitmap(R.id.icon, bitmap);
        notificationManager.notify(MANGA_DOWNLOAD_SERVICE, notification);
    }

    public ImageView getIconView() {
        return fakeImageView;
    }

    private class NImageView extends ImageView {

        public NImageView(final Context context) {
            super(context);
        }

        @Override
        public void setImageBitmap(final Bitmap bm) {
            setIcon(bm);
        }

    }

}
