package com.danilov.supermanga.core.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;

import com.danilov.supermanga.core.database.DatabaseAccessException;
import com.danilov.supermanga.core.database.MangaDAO;
import com.danilov.supermanga.core.model.Manga;
import com.danilov.supermanga.core.notification.UpdatesNotificationHelper;
import com.danilov.supermanga.core.util.Constants;
import com.danilov.supermanga.core.util.Logger;
import com.danilov.supermanga.core.util.ServiceContainer;

import java.util.List;

/**
 * Created by Semyon on 06.08.2015.
 */
public class StartUpdateService extends Service {

    private static final Logger LOGGER = new Logger(StartUpdateService.class);

    private MangaUpdateServiceNew service;

    private Handler handler = new UpdateHandler();

    private MangaUpdateServiceNew.MUpdateServiceNewConnection serviceConnection;


    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    public static void start(final Context context) {
        Intent intent = new Intent(context, StartUpdateService.class);
        context.startService(intent);
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        super.onStartCommand(intent, flags, startId);
        startUpdate();
        return START_NOT_STICKY;
    }

    private void startUpdate() {
        if (service != null) {
            onServiceBinded();
            return;
        }
        serviceConnection = new MangaUpdateServiceNew.MUpdateServiceNewConnection(new ServiceConnectionListener<MangaUpdateServiceNew>() {
            @Override
            public void onServiceConnected(final MangaUpdateServiceNew service) {
                StartUpdateService.this.service = service;
                onServiceBinded();
                service.addHandler(handler);
            }

            @Override
            public void onServiceDisconnected(final MangaUpdateServiceNew service) {
                StartUpdateService.this.service = null;
                service.removeHandler(handler);
            }
        });
        Intent intent = new Intent(this, MangaUpdateServiceNew.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void onServiceBinded() {
        MangaUpdateServiceNew.startUpdateList(StartUpdateService.this);
    }

    @Override
    public void onDestroy() {
        if (serviceConnection != null) {
            unbindService(serviceConnection);
        }
    }

    private class UpdateHandler extends Handler {

        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case MangaUpdateServiceNew.UPDATING_LIST:
                case MangaUpdateServiceNew.UPDATE_STARTED:
                    break;
                case MangaUpdateServiceNew.MANGA_UPDATE_FAILED:
                    break;
                case MangaUpdateServiceNew.MANGA_UPDATE_FINISHED:
                    break;
                case MangaUpdateServiceNew.UPDATE_FINISHED:
                    if (msg.obj != null) {
                        Context applicationContext = getApplicationContext();
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
                        sharedPreferences.edit().putLong(Constants.LAST_UPDATE_TIME, System.currentTimeMillis()).apply();

                        Integer quantity = (Integer) msg.obj;
                        if (quantity < 1) {
                            return;
                        }
                        new UpdatesNotificationHelper(applicationContext).buildNotification(quantity);
                        stopSelf();
                    }
                    break;
            }
        }

    }

}