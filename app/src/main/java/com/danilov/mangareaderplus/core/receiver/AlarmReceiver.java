package com.danilov.mangareaderplus.core.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.danilov.mangareaderplus.core.service.StartUpdateService;
import com.danilov.mangareaderplus.core.util.Constants;
import com.danilov.mangareaderplus.core.util.Logger;

/**
 * Created by Semyon on 06.08.2015.
 */
public class AlarmReceiver extends BroadcastReceiver {

    private static final Logger LOGGER = new Logger(AlarmReceiver.class);

    @Override
    public void onReceive(final Context context, final Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        long lastUpdateTime = sharedPreferences.getLong(Constants.LAST_UPDATE_TIME, 0);
        if (lastUpdateTime + Constants.UPDATES_INTERVAL < System.currentTimeMillis()) {
            LOGGER.d("Starting update, because time is right!");
            StartUpdateService.start(context);
        }
    }

}
