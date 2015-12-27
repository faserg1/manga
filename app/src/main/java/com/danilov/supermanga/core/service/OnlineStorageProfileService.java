package com.danilov.supermanga.core.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;

import com.danilov.supermanga.core.application.MangaApplication;
import com.danilov.supermanga.core.onlinestorage.GoogleDriveConnector;
import com.danilov.supermanga.core.onlinestorage.OnlineStorageConnector;
import com.danilov.supermanga.core.util.Constants;
import com.danilov.supermanga.core.util.Logger;
import com.google.android.gms.common.ConnectionResult;

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
    public static final int GOOGLE_FRESH_INIT_HAS_FILES_ON_DISK = 3;

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
    }

    public void save() {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final long lastSyncTime = sharedPreferences.getLong(Constants.Settings.LAST_UPDATE_PROFILE_TIME, -1);
        googleConnector.getExistingFile(Constants.Settings.ONLINE_SETTINGS_FILENAME, new OnlineStorageConnector.CommandCallback<OnlineStorageConnector.OnlineFile>() {
            @Override
            public void onCommandSuccess(final OnlineStorageConnector.OnlineFile onlineFile) {
                if (onlineFile != null) {
                    //заливаем на диск
                    replaceCurrent(onlineFile);
                } else {
                    //диск пустой, заливаем файл
                    createNew();
                }
            }

            @Override
            public void onCommandError(final String message) {

            }
        });
    }

    public void download() {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final long lastSyncTime = sharedPreferences.getLong(Constants.Settings.LAST_UPDATE_PROFILE_TIME, -1);
        googleConnector.getExistingFile(Constants.Settings.ONLINE_SETTINGS_FILENAME, new OnlineStorageConnector.CommandCallback<OnlineStorageConnector.OnlineFile>() {
            @Override
            public void onCommandSuccess(final OnlineStorageConnector.OnlineFile onlineFile) {
                if (onlineFile != null && lastSyncTime == -1) {
                    download(onlineFile);
                } else {
                    //alert-no-data
                }
            }

            @Override
            public void onCommandError(final String message) {

            }
        });
    }

    private class SyncUpCallback implements OnlineStorageConnector.CommandCallback<Boolean> {

        @Override
        public void onCommandSuccess(final Boolean object) {
            final OnlineStorageConnector.CommandCallback<Boolean> fileSendCallback = new OnlineStorageConnector.CommandCallback<Boolean>() {
                final int filesToSend = Constants.Settings.DB_FILES.length;
                int sent = 0;
                int callbacks = 0;
                @Override
                public synchronized void onCommandSuccess(final Boolean object) {
                    if (object) {
                        sent++;
                    }
                    callbacks++;
                    check();
                }

                @Override
                public synchronized void onCommandError(final String message) {
                    callbacks++;
                    check();
                }

                private void check() {
                    if (callbacks == filesToSend) {
                        if (sent != filesToSend) {

                        } else {

                        }
                    }
                }

            };

            for (int i = 0; i < Constants.Settings.DB_FILES.length; i++) {
                final String fileName = Constants.Settings.DB_FILES[i][0];
                final String localFilePath = Constants.Settings.DB_FILES[i][1];
                googleConnector.getExistingFile(fileName, new OnlineStorageConnector.CommandCallback<OnlineStorageConnector.OnlineFile>() {
                    @Override
                    public void onCommandSuccess(final OnlineStorageConnector.OnlineFile onlineFile) {
                        //TODO: remove and uncomment
                        googleConnector.createFile(fileName, new File(localFilePath), OnlineStorageConnector.MimeType.SQLITE, fileSendCallback);
//                        if (onlineFile != null) {
//                            onlineFile.rewriteWith(new File(localFilePath), fileSendCallback);
//                        } else {
//                            googleConnector.createFile(fileName, new File(localFilePath), OnlineStorageConnector.MimeType.SQLITE, fileSendCallback);
//                        }
                    }

                    @Override
                    public void onCommandError(final String message) {

                    }
                });
            }
        }

        @Override
        public void onCommandError(final String message) {

        }

    }

    public void replaceCurrent(final OnlineStorageConnector.OnlineFile onlineFile) {
        onlineFile.rewriteWith(getSettingsJsonString(), new SyncUpCallback());
    }

    public void createNew() {
        googleConnector.createFile(Constants.Settings.ONLINE_SETTINGS_FILENAME, getSettingsJsonString(), OnlineStorageConnector.MimeType.TEXT_PLAIN, new SyncUpCallback());
    }

    public void download(final OnlineStorageConnector.OnlineFile onlineFile) {
        onlineFile.download(new OnlineStorageConnector.CommandCallback<String>() {
            @Override
            public void onCommandSuccess(final String object) {


                //TODO: нужно предложить пользователю два варианта - скачать файл или залить новый
                //notifyHandler(GOOGLE_FRESH_INIT_HAS_FILES_ON_DISK, onlineFile);

                final OnlineStorageConnector.CommandCallback<Boolean> fileDownloadCallback = new OnlineStorageConnector.CommandCallback<Boolean>() {
                    final int filesToSend = Constants.Settings.DB_FILES.length;
                    int sent = 0;
                    int callbacks = 0;
                    @Override
                    public synchronized void onCommandSuccess(final Boolean object) {
                        if (object) {
                            sent++;
                        }
                        callbacks++;
                        check();
                    }

                    @Override
                    public synchronized void onCommandError(final String message) {
                        callbacks++;
                        check();
                    }

                    private void check() {
                        if (callbacks == filesToSend) {
                            if (sent != filesToSend) {

                            } else {

                            }
                        }
                    }

                };

                for (int i = 0; i < Constants.Settings.DB_FILES.length; i++) {
                    final String fileName = Constants.Settings.DB_FILES[i][0];
                    final String localFilePath = Constants.Settings.DB_FILES[i][1];
                    googleConnector.getExistingFile(fileName, new OnlineStorageConnector.CommandCallback<OnlineStorageConnector.OnlineFile>() {
                        @Override
                        public void onCommandSuccess(final OnlineStorageConnector.OnlineFile onlineFile) {
                            if (onlineFile != null) {
                                onlineFile.download(localFilePath, fileDownloadCallback);
                            } else {
                                fileDownloadCallback.onCommandSuccess(true);
                            }
                        }

                        @Override
                        public void onCommandError(final String message) {

                        }
                    });
                }
            }

            @Override
            public void onCommandError(final String message) {

            }
        });
    }

    public String getSettingsJsonString() {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final JSONObject settingsObject = new JSONObject();
        Map<String, ?> all = sharedPreferences.getAll();
        for (String fieldName : Constants.Settings.ALL_SETTINGS) {
            Object o = all.get(fieldName);
            try {
                settingsObject.put(fieldName, o);
            } catch (JSONException e) {
                //e.printStackTrace();
            }
        }
        return settingsObject.toString();
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
