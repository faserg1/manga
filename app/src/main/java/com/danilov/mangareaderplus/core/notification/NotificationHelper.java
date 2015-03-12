package com.danilov.mangareaderplus.core.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;
import android.widget.ImageView;
import android.widget.RemoteViews;

import com.danilov.mangareaderplus.R;
import com.danilov.mangareaderplus.core.model.Manga;
import com.danilov.mangareaderplus.core.service.MangaDownloadService;

/**
 * Created by Semyon on 13.12.2014.
 */
public class NotificationHelper {

    private Notification notification;

    private MangaDownloadService downloadService;

    private Context ctx;

    private ImageView fakeImageView;

    private static final int MANGA_DOWNLOAD_SERVICE = 1;

    private static final int FINISHED_DOWNLOAD_ID = 2;

    private RemoteViews contentView;

    private NotificationManager notificationManager;

    private String title;

    public NotificationHelper(final MangaDownloadService downloadService) {
        this.downloadService = downloadService;
        ctx = downloadService.getApplicationContext();
        fakeImageView = new NImageView(ctx);
        notificationManager = (NotificationManager)downloadService
                .getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void buildNotification(final Manga manga) {
        final Intent emptyIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, emptyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        this.title = manga.getTitle();

        contentView = new RemoteViews(downloadService.getPackageName(), R.layout.notification_layout);
        contentView.setImageViewResource(R.id.icon, R.drawable.ic_launcher);
        contentView.setTextViewText(R.id.text, title);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(ctx)
                        .setContentIntent(pendingIntent)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContent(contentView);
        this.notification = mBuilder.build();
        this.notification.contentView = contentView;

        notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT;
        notification.flags = notification.flags | Notification.FLAG_INSISTENT;

        downloadService.startForeground(MANGA_DOWNLOAD_SERVICE, notification);
    }

    public void updateProgress(final int max, final int progress) {
        StringBuilder sb = new StringBuilder(title);
        sb.append(" (").append(progress + 1).append('/').append(max).append(')');
        contentView.setTextViewText(R.id.text, sb.toString());
        contentView.setProgressBar(R.id.progress_bar, max, progress, false);
        notificationManager.notify(MANGA_DOWNLOAD_SERVICE, notification);
    }

    public void finish() {

        notification.flags = notification.flags & (~Notification.FLAG_ONGOING_EVENT);

        contentView.setTextViewText(R.id.text, title);
        contentView.setProgressBar(R.id.progress_bar, 1, 1, false);
        notificationManager.notify(MANGA_DOWNLOAD_SERVICE, notification);
        downloadService.stopForeground(true);
        buildFinishedNotification();
    }

    private void buildFinishedNotification() {
        final Intent emptyIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, emptyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews contentView = new RemoteViews(downloadService.getPackageName(), R.layout.notification_layout);
        contentView.setImageViewResource(R.id.icon, R.drawable.ic_launcher);
        contentView.setTextViewText(R.id.text, title);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(ctx)
                        .setContentIntent(pendingIntent)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContent(contentView)
                        .setAutoCancel(true)
                        .setTicker("Download finished");

        Notification notification = mBuilder.build();
        notification.contentView = contentView;
        notification.flags = notification.flags | Notification.FLAG_INSISTENT;
        notificationManager.notify(FINISHED_DOWNLOAD_ID, notification);
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
