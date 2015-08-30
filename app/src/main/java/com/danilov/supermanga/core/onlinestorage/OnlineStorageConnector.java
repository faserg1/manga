package com.danilov.supermanga.core.onlinestorage;

import android.content.Context;

import com.danilov.supermanga.core.application.MangaApplication;

import java.io.File;

/**
 * Created by Semyon on 30.08.2015.
 */
public abstract class OnlineStorageConnector {

    protected final Context context = MangaApplication.getContext();
    protected final StorageConnectorListener connectorListener;

    public OnlineStorageConnector(final StorageConnectorListener connectorListener) {
        this.connectorListener = connectorListener;
    }

    public abstract void init();

    public abstract String getAccountName();

    public abstract void createFile(final String title, final File file, final MimeType mimeType, final CommandCallback commandCallback);

    public abstract void createFile(final String title, final String text, final MimeType mimeType, final CommandCallback commandCallback);

    public abstract void checkFileExist(final String title, final CommandCallback commandCallback);

    public static interface StorageConnectorListener {

        public void onStorageConnected(final OnlineStorageConnector connector);

        public void onStorageDisconnected(final OnlineStorageConnector connector);

        public void onConnectionFailed(final OnlineStorageConnector connector, final Object object);

    }

    public static interface CommandCallback {

        public void onCommandSuccess();

        public void onCommandError(final String message);

    }

    public enum MimeType {

        TEXT_PLAIN("text/plain");

        private String data;

        MimeType(final String data) {
            this.data = data;
        }

        public String getData() {
            return data;
        }
    }

}
