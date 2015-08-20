package com.danilov.supermanga.core.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.danilov.supermanga.core.service.StartUpdateService;
import com.danilov.supermanga.core.util.Constants;
import com.danilov.supermanga.core.util.Logger;

import java.util.Calendar;

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

    public static void setUpdateAlarm(final Context context) {

        Calendar cal= Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 12);

        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1123, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), Constants.UPDATES_INTERVAL, pendingIntent);
    }

}
