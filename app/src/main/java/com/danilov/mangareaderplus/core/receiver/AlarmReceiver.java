package com.danilov.mangareaderplus.core.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.danilov.mangareaderplus.core.service.StartUpdateService;

/**
 * Created by Semyon on 06.08.2015.
 */
public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        StartUpdateService.start(context);
    }

}
