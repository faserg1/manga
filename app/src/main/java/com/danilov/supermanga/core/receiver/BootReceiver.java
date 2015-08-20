package com.danilov.supermanga.core.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.danilov.supermanga.core.util.Constants;

import java.util.Calendar;

/**
 * Created by Semyon on 20.08.2015.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            AlarmReceiver.setUpdateAlarm(context);
        }
    }



}