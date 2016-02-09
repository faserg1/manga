package com.danilov.supermanga.core.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Pair;

import com.danilov.supermanga.core.database.DatabaseAccessException;
import com.danilov.supermanga.core.database.MangaDAO;
import com.danilov.supermanga.core.database.UpdatesDAO;
import com.danilov.supermanga.core.model.Manga;
import com.danilov.supermanga.core.model.MangaChapter;
import com.danilov.supermanga.core.model.UpdatesElement;
import com.danilov.supermanga.core.repository.RepositoryEngine;
import com.danilov.supermanga.core.repository.RepositoryException;
import com.danilov.supermanga.core.util.Logger;
import com.danilov.supermanga.core.util.ServiceContainer;
import com.danilov.supermanga.core.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by Semyon on 28.06.2015.
 */
public class MangaUpdateServiceNew extends Service {

    private static final Logger LOGGER = new Logger(MangaUpdateServiceNew.class);

//    private static final String TAG = "MangaUpdateServiceNew";

    public static final int UPDATING_LIST = 0;
    public static final int MANGA_UPDATE_FINISHED = 1;
    public static final int UPDATE_STARTED = 2;
    public static final int UPDATE_FINISHED = 3;
    public static final int MANGA_UPDATE_FAILED = 4;

    private boolean hasError = false;
    private UpdateThread updateThread = null;

    private List<Handler> handlers = new ArrayList<>();

    public void addHandler(final Handler handler) {
        synchronized (handlers) {
            handlers.add(handler);
        }
        sendStatus();
    }

    public void removeHandler(final Handler handler) {
        synchronized (handlers) {
            handlers.remove(handler);
        }
    }

    public static void startUpdateList(Context context) {
        Intent intent = new Intent(context, MangaUpdateServiceNew.class);
        context.startService(intent);
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (intent != null) {
            if (updateThread == null) {
                updateThread = new UpdateThread();
                updateThread.start();
            }
        }
        return START_STICKY;
    }

    private class UpdateThread extends Thread {

        private List<Pair<Manga, UpdatesElement>> mangas = null;

        private UpdateThread() {
        }

        @Override
        public void run() {
            UpdatesDAO updatesDAO = ServiceContainer.getService(UpdatesDAO.class);
            MangaDAO mangaDAO = ServiceContainer.getService(MangaDAO.class);

            try {
                List<Manga> mangas  = Collections.synchronizedList(mangaDAO.getTracking());
                if (mangas.isEmpty()) {
                    return;
                }

                this.mangas = new ArrayList<>();
                for (Manga m : mangas) {
                    this.mangas.add(new Pair<Manga, UpdatesElement>(m, null));
                }
            } catch (DatabaseAccessException e) {
                LOGGER.e("Failed to get tracking: " + e.getMessage(), e);
                updateThread = null;
                return;
            }

            notifyHandlers(UPDATE_STARTED, getCurrentUpdating());

            int updatesCount = 0;

            while (!mangas.isEmpty()) {
                Pair<Manga, UpdatesElement> pair = mangas.remove(0);
                Manga manga = pair.first;
                int startValue = manga.getChaptersQuantity();
                Manga updatedManga = null;
                try {
                    updatedManga = updateInfo(manga);
                } catch (UpdateException e) {
                    LOGGER.i("Failed to update: " + e.getMessage(), e);
                    notifyHandlers(
                            MANGA_UPDATE_FAILED,
                            manga
                    );
                }
                if (updatedManga != null) {
                    try {
                        mangaDAO.update(updatedManga);
                    } catch (DatabaseAccessException e) {
                        LOGGER.e("Failed to update in database: " + e.getMessage(), e);
                    }
                }
                int diff = (updatedManga != null ? (updatedManga.getChaptersQuantity() - startValue) : -1);
                UpdatesElement el = null;
                if (diff > 0) {
                    try {
                        el = updatesDAO.updateInfo(manga, diff, new Date());
                    } catch (DatabaseAccessException e) {
                        LOGGER.e("Failed to update differences info in database: " + e.getMessage(), e);
                    }
                } else {
                    try {
                        el = updatesDAO.getUpdatesByManga(manga);
                    } catch (DatabaseAccessException e) {
                        LOGGER.e("Failed to get differences info from database: " + e.getMessage(), e);
                    }
                }
                notifyHandlers(
                        MANGA_UPDATE_FINISHED,
                        updatedManga != null ? updatedManga : manga,
                        el != null ? el.getId() : -1,
                        el != null ? el.getDifference() : -1
                );
                if (el != null && el.getDifference() > 0) {
                    updatesCount++;
                }
            }
            notifyHandlers(UPDATE_FINISHED, updatesCount);
            updateThread = null;
        }

    }

    private void sendStatus() {
        if (updateThread != null) {
            notifyHandlers(UPDATING_LIST,  getCurrentUpdating());
        }
    }

    private List<Pair<Manga, UpdatesElement>> getCurrentUpdating() {
        return new ArrayList<>(updateThread.mangas);
    }

    private void notifyHandlers(final int action, final Object object) {
        notifyHandlers(action, object, -1, -1);
    }

    private void notifyHandlers(final int action, final Object object, final int arg1, final int arg2) {
        Message message = Message.obtain();
        message.what = action;
        message.obj = object;
        message.arg1 = arg1;
        message.arg2 = arg2;
        synchronized (handlers) {
            for (Handler handler : handlers) {
                Message msg = Message.obtain();
                msg.copyFrom(message);
                handler.sendMessage(msg);
            }
        }
    }

    private Manga updateInfo(final Manga manga) throws UpdateException {
        final RepositoryEngine engine = manga.getRepository().getEngine();
        try {
            if (engine.queryForChapters(manga)) {
                List<MangaChapter> chapters = manga.getChapters();
                int quantity = 0;
                if (chapters != null) {
                    quantity = chapters.size();
                }
                manga.setChaptersQuantity(quantity);
                return manga;
            }
        } catch (RepositoryException e) {
            throw new UpdateException(e);
        } catch (Exception e) {
            throw new UpdateException(e);
        }
        return null;
    }



    @Override
    public IBinder onBind(final Intent intent) {
        return new ServiceBinder();
    }

    public static class MUpdateServiceNewConnection implements ServiceConnection {

        private ServiceConnectionListener<MangaUpdateServiceNew> listener;
        private MangaUpdateServiceNew service;


        public MUpdateServiceNewConnection(final ServiceConnectionListener<MangaUpdateServiceNew> listener) {
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

        public MangaUpdateServiceNew getService() {
            return MangaUpdateServiceNew.this;
        }

    }

}
