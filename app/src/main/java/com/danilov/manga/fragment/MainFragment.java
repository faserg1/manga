package com.danilov.manga.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.danilov.manga.R;
import com.danilov.manga.core.database.DatabaseAccessException;
import com.danilov.manga.core.database.DownloadedMangaDAO;
import com.danilov.manga.core.model.LocalManga;
import com.danilov.manga.core.model.Manga;
import com.danilov.manga.core.service.MangaUpdateService;
import com.danilov.manga.core.util.ServiceContainer;

import java.util.List;

/**
 * Created by Semyon Danilov on 07.10.2014.
 */
public class MainFragment extends Fragment {

    private View view;

    private FragmentActivity activity;

    private UpdateBroadcastReceiver receiver;

    private DownloadedMangaDAO downloadedMangaDAO = ServiceContainer.getService(DownloadedMangaDAO.class);

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.manga_main_fragment, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        activity = getActivity();
        receiver = new UpdateBroadcastReceiver();
        activity.registerReceiver(receiver, new IntentFilter(MangaUpdateService.UPDATE));
        try {
            List<LocalManga> mangaList = downloadedMangaDAO.getAllManga();
            for (Manga manga : mangaList) {
                manga.setChaptersQuantity(1);
            }
            MangaUpdateService.startUpdateList(getActivity(), mangaList);
        } catch (DatabaseAccessException e) {
            e.printStackTrace();
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDetach() {
        activity.unregisterReceiver(receiver);
        super.onDetach();
    }

    private class UpdateBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {

        }

    }

    private void onUpdate(final Manga manga, final Integer difference) {

    }

}
