package com.danilov.manga.core.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import com.danilov.manga.core.model.Manga;
import com.danilov.manga.core.util.Constants;
import com.danilov.manga.core.util.Pair;
import com.danilov.manga.core.util.Utils;
import com.danilov.promise.Promise;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */

/**
 * TODO: normal JavaDoc
 */
public class MangaUpdateService extends IntentService {

    public static final String UPDATE = "com.danilov.manga.INTENTFILTER_ACTION_UPDATE";

    private static final String ACTION_UPDATE_ALL = "updateall";
    private static final String ACTION_UPDATE_LIST = "updatelist";

    private static final String EXTRA_MANGA_LIST = "mangas";


    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startUpdateList(Context context, final List<? extends Manga> mangas) {
        Intent intent = new Intent(context, MangaUpdateService.class);
        intent.setAction(ACTION_UPDATE_LIST);
        ArrayList<Manga> mangasArrayList = Utils.listToArrayList(mangas);
        intent.putParcelableArrayListExtra(EXTRA_MANGA_LIST, mangasArrayList);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startUpdateALl(Context context) {
        Intent intent = new Intent(context, MangaUpdateService.class);
        intent.setAction(ACTION_UPDATE_ALL);
        context.startService(intent);
    }

    public MangaUpdateService() {
        super("MangaUpdateService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPDATE_ALL.equals(action)) {
                handleUpdateAll();
            } else if (ACTION_UPDATE_LIST.equals(action)) {
                final List<Manga> mangas = intent.getParcelableArrayListExtra(EXTRA_MANGA_LIST);
                handleUpdateList(mangas);
            }
        }
    }

    private void handleUpdateList(final List<Manga> mangas) {
        UpdateManager updateManager = new UpdateManager(mangas);
        updateManager.checkUpdate(successHandler, errorHandler);
    }

    private void handleUpdateAll() {

    }

    private Promise.Action<Pair, Void> successHandler = new Promise.Action<Pair, Void>() {
        @Override
        public Void action(final Pair data, final boolean success) {
            Manga manga = (Manga) data.first;
            Integer diff = (Integer) data.second;
            sendUpdate(manga, diff);
            return null;
        }
    };

    private void sendUpdate(final Manga manga, final Integer diff) {
        Intent intent = new Intent(UPDATE);
        intent.putExtra(Constants.MANGA_PARCEL_KEY, manga);
        intent.putExtra(Constants.MANGA_CHAPTERS_DIFFERENCE, diff);
        sendBroadcast(intent);
    }

    private Promise.Action<Exception, Void> errorHandler = new Promise.Action<Exception, Void>() {
        @Override
        public Void action(final Exception data, final boolean success) {
            return null;
        }
    };

}
