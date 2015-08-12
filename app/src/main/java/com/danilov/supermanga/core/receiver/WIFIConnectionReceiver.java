package com.danilov.supermanga.core.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;

import com.danilov.supermanga.core.service.StartUpdateService;
import com.danilov.supermanga.core.util.Constants;
import com.danilov.supermanga.core.util.Logger;

/**
 * Created by Semyon on 06.08.2015.
 */
public class WIFIConnectionReceiver extends BroadcastReceiver {

    private static final Logger LOGGER = new Logger(WIFIConnectionReceiver.class);

    @Override
    public void onReceive(final Context context, final Intent intent) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        LOGGER.d("Wifi state changed");
        if (wifi != null && wifi.isWifiEnabled()) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            long lastUpdateTime = sharedPreferences.getLong(Constants.LAST_UPDATE_TIME, 0);
            if (lastUpdateTime + Constants.UPDATES_INTERVAL < System.currentTimeMillis()) {
                LOGGER.d("Starting update, because time is right!");
                StartUpdateService.start(context);
            }
        }
    }
}
