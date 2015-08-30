package com.danilov.supermanga.core.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.View;

import com.danilov.supermanga.core.application.MangaApplication;
import com.danilov.supermanga.core.onlinestorage.GoogleDriveConnector;
import com.danilov.supermanga.core.onlinestorage.OnlineStorageConnector;
import com.danilov.supermanga.core.util.Constants;
import com.danilov.supermanga.core.util.Logger;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Map;

/**
 * Created by Semyon on 30.08.2015.
 */
public class OnlineStorageProfileService extends Service {

    private static final Logger LOGGER = new Logger(OnlineStorageProfileService.class);

    public static final int GOOGLE_CONNECTED = 0;
    public static final int GOOGLE_NEED_CONFIRMATION = 1;
    public static final int GOOGLE_SENT_SUCCESS = 2;

    private Handler handler = null;

    private OnlineStorageConnector googleConnector = null;

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        return START_STICKY;
    }

    public static ServiceConnection bindService(final Context context, final ServiceConnectionListener<OnlineStorageProfileService> listener) {
        context.startService(new Intent(context, OnlineStorageProfileService.class));
        OnlineStorageServiceConnection connection = new OnlineStorageServiceConnection(listener);
        context.bindService(new Intent(context, OnlineStorageProfileService.class), connection, BIND_AUTO_CREATE);
        return connection;
    }

    public synchronized void setServiceHandler(final Handler handler) {
        this.handler = handler;
    }

    public synchronized void removeHandler() {
        this.handler = null;
    }

    public void connect() {
        googleConnector = new GoogleDriveConnector(googleConnectorListener);
        googleConnector.init();
    }

    private void notifyHandler(final int action, final Object object) {
        if (handler != null) {
            if (object == null) {
                handler.sendEmptyMessage(action);
            } else {
                Message message = handler.obtainMessage();
                message.what = action;
                message.obj = object;
                handler.sendMessage(message);
            }
        }
    }

    public OnlineStorageConnector getGoogleConnector() {
        return googleConnector;
    }

    public void sendDataViaGoogle() {
        final Context context = MangaApplication.getContext();
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        JSONObject settingsObject = new JSONObject();
        Map<String, ?> all = sharedPreferences.getAll();
        for (String fieldName : Constants.Settings.ALL_SETTINGS) {
            Object o = all.get(fieldName);
            try {
                settingsObject.put(fieldName, o);
            } catch (JSONException e) {
                //e.printStackTrace();
            }
        }
        googleConnector.createFile(Constants.Settings.ONLINE_SETTINGS_FILENAME, settingsObject.toString(), OnlineStorageConnector.MimeType.TEXT_PLAIN, new OnlineStorageConnector.CommandCallback() {
            @Override
            public void onCommandSuccess() {
                sharedPreferences.edit().putLong(Constants.Settings.LAST_UPDATE_PROFILE_TIME, System.currentTimeMillis()).apply();
                notifyHandler(GOOGLE_SENT_SUCCESS, null);
            }

            @Override
            public void onCommandError(final String message) {

            }
        });
    }

    private OnlineStorageConnector.StorageConnectorListener googleConnectorListener = new OnlineStorageConnector.StorageConnectorListener() {


        @Override
        public void onStorageConnected(final OnlineStorageConnector connector) {
            notifyHandler(GOOGLE_CONNECTED, null);
        }

        @Override
        public void onStorageDisconnected(final OnlineStorageConnector connector) {

        }

        @Override
        public void onConnectionFailed(final OnlineStorageConnector connector, final Object object) {
            ConnectionResult connectionResult = (ConnectionResult) object;
            notifyHandler(GOOGLE_NEED_CONFIRMATION, connectionResult);
        }

    };

    @Override
    public IBinder onBind(final Intent intent) {
        return new ServiceBinder();
    }

    public static class OnlineStorageServiceConnection implements ServiceConnection {

        private ServiceConnectionListener<OnlineStorageProfileService> listener;
        private OnlineStorageProfileService service;


        public OnlineStorageServiceConnection(final ServiceConnectionListener<OnlineStorageProfileService> listener) {
            this.listener = listener;
        }

        @Override
        public void onServiceConnected(final ComponentName componentName, final IBinder iBinder) {
            LOGGER.d("Service connected");
            if (listener != null) {
                ServiceBinder binder = (ServiceBinder) iBinder;
                service = binder.getService();
                listener.onServiceConnected(service);
            }
        }

        @Override
        public void onServiceDisconnected(final ComponentName componentName) {
            LOGGER.d("Service disconnected");
            if (listener != null) {
                listener.onServiceDisconnected(service);
            }
        }

    }

    private class ServiceBinder extends Binder {

        public OnlineStorageProfileService getService() {
            return OnlineStorageProfileService.this;
        }

    }

}
