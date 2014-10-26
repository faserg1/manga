package com.danilov.manga.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.support.v7.widget.PopupMenu;

import com.danilov.manga.R;
import com.danilov.manga.activity.MangaViewerActivity;
import com.danilov.manga.core.adapter.DownloadedMangaAdapter;
import com.danilov.manga.core.adapter.PopupButtonClickListener;
import com.danilov.manga.core.database.DatabaseAccessException;
import com.danilov.manga.core.database.DownloadedMangaDAO;
import com.danilov.manga.core.model.LocalManga;
import com.danilov.manga.core.service.LocalImageManager;
import com.danilov.manga.core.util.Constants;
import com.danilov.manga.core.util.ServiceContainer;
import com.danilov.manga.core.util.Utils;

import java.util.List;

/**
 * Created by Semyon Danilov on 07.10.2014.
 */
public class DownloadedMangaFragment extends Fragment implements AdapterView.OnItemClickListener, PopupButtonClickListener {

    private static final String TAG = "DownloadedMangaFragment";

    private View view;
    private ProgressBar downloadedProgressBar;

    private LocalImageManager localImageManager = null;
    private DownloadedMangaDAO downloadedMangaDAO = null;

    private int sizeOfImage;

    private DownloadedMangaAdapter adapter = null;
    private GridView gridView = null;

    private Handler handler = new Handler();

    public static DownloadedMangaFragment newInstance() {
        return new DownloadedMangaFragment();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.manga_downloaded_fragment, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        sizeOfImage = getActivity().getResources().getDimensionPixelSize(R.dimen.manga_list_image_height);
        localImageManager = ServiceContainer.getService(LocalImageManager.class);
        downloadedMangaDAO = ServiceContainer.getService(DownloadedMangaDAO.class);
        gridView = (GridView) view.findViewById(R.id.grid_view);
        downloadedProgressBar = (ProgressBar) view.findViewById(R.id.downloaded_progress_bar);
        gridView.setOnItemClickListener(this);
        loadDownloadedManga();
        super.onActivityCreated(savedInstanceState);
    }

    private void loadDownloadedManga() {
        downloadedProgressBar.setVisibility(View.VISIBLE);
        Thread thread = new Thread() {
            @Override
            public void run() {
                boolean _success = true;
                String _error = null;
                try {
                    List<LocalManga> localMangas = downloadedMangaDAO.getAllManga();
                    adapter = new DownloadedMangaAdapter(getActivity(), localMangas, DownloadedMangaFragment.this);
                } catch (DatabaseAccessException e) {
                    _success = false;
                    _error = e.getMessage();
                    Log.e(TAG, "Failed to get downloaded manga: " + _error);
                }
                final boolean success = _success;
                final String error = _error;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        downloadedProgressBar.setVisibility(View.INVISIBLE);
                        if (success) {
                            gridView.setAdapter(adapter);
                        } else {
                            String formedError = Utils.stringResource(getActivity(), R.string.p_failed_to_show_loaded);
                            Utils.showToast(getActivity(), formedError + error);
                        }
                    }
                });
            }

        };
        thread.start();
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        DownloadedMangaAdapter adapter = (DownloadedMangaAdapter) parent.getAdapter();
        LocalManga manga = adapter.getMangas().get(position);
        Intent intent = new Intent(getActivity(), MangaViewerActivity.class);
        intent.putExtra(Constants.MANGA_PARCEL_KEY, manga);
        startActivity(intent);
    }

    @Override
    public void onPopupButtonClick(View popupButton, int listPosition) {
        final PopupMenu popup = new PopupMenu(getActivity(), popupButton);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.downloaded_manga_item_menu, popup.getMenu());
        LocalManga manga = adapter.getMangas().get(listPosition);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(final MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.download:
//                        Intent intent = new Intent(MangaQueryActivity.this, DownloadsActivity.class);
//                        intent.putExtra(Constants.MANGA_PARCEL_KEY, manga);
//                        startActivity(intent);
                        return true;
                    case R.id.add_to_favorites:
                        return true;
                }
                return false;
            }

        });
        popup.show();
    }

}