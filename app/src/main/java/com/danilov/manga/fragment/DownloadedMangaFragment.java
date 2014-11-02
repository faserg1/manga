package com.danilov.manga.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
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
import com.danilov.manga.core.database.HistoryDAO;
import com.danilov.manga.core.model.LocalManga;
import com.danilov.manga.core.service.LocalImageManager;
import com.danilov.manga.core.util.Constants;
import com.danilov.manga.core.util.IoUtils;
import com.danilov.manga.core.util.ServiceContainer;
import com.danilov.manga.core.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Semyon Danilov on 07.10.2014.
 */
public class DownloadedMangaFragment extends Fragment implements AdapterView.OnItemClickListener, PopupButtonClickListener, AdapterView.OnItemLongClickListener, ActionMode.Callback {

    private static final String TAG = "DownloadedMangaFragment";

    private boolean isInMultiChoice = false;

    private View view;
    private ProgressBar downloadedProgressBar;

    private LocalImageManager localImageManager = null;
    private DownloadedMangaDAO downloadedMangaDAO = null;
    private HistoryDAO historyDAO = null;

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
        historyDAO = ServiceContainer.getService(HistoryDAO.class);
        gridView = (GridView) view.findViewById(R.id.grid_view);
        downloadedProgressBar = (ProgressBar) view.findViewById(R.id.downloaded_progress_bar);
        gridView.setOnItemClickListener(this);
        gridView.setOnItemLongClickListener(this);
        loadDownloadedManga();
        super.onActivityCreated(savedInstanceState);
    }

    private void loadDownloadedManga() {
        downloadedProgressBar.setVisibility(View.VISIBLE);
        final Context context = getActivity();
        Thread thread = new Thread() {
            @Override
            public void run() {
                boolean _success = true;
                String _error = null;
                try {
                    List<LocalManga> localMangas = downloadedMangaDAO.getAllManga();
                    adapter = new DownloadedMangaAdapter(context, localMangas, DownloadedMangaFragment.this);
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
        if (isInMultiChoice) {
            adapter.onMultiSelectClick(view, position);
            return;
        }
        LocalManga manga = adapter.getMangas().get(position);
        Intent intent = new Intent(getActivity(), MangaViewerActivity.class);
        intent.putExtra(Constants.MANGA_PARCEL_KEY, manga);
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        ActionBarActivity actionBarActivity = (ActionBarActivity) getActivity();
        actionBarActivity.startSupportActionMode(this);
        adapter.setPositionSelected(view, position, true);
        adapter.setIsInMultiSelect(true);
        isInMultiChoice = true;
        return true;
    }

    //action mode callback
    @Override
    public boolean onCreateActionMode(final ActionMode actionMode, final Menu menu) {
        actionMode.getMenuInflater().inflate(R.menu.downloaded_manga_action_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(final ActionMode actionMode, final Menu menu) {
        actionMode.setTitle("working");
        return false;
    }

    @Override
    public boolean onActionItemClicked(final ActionMode actionMode, final MenuItem menuItem) {
        List<LocalManga> selected = adapter.getSelectedManga();
        switch (menuItem.getItemId()) {
            case R.id.delete:
                deleteManga(selected);
                actionMode.finish();
                return true;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(final ActionMode actionMode) {
        isInMultiChoice = false;
        adapter.setIsInMultiSelect(false);
        adapter.deselectAll();
    }

    @Override
    public void onPopupButtonClick(View popupButton, int listPosition) {
        final PopupMenu popup = new PopupMenu(getActivity(), popupButton);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.downloaded_manga_item_menu, popup.getMenu());
        final LocalManga manga = adapter.getMangas().get(listPosition);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(final MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.delete:
                        List<LocalManga> mangas = new ArrayList<LocalManga>(1);
                        mangas.add(manga);
                        deleteManga(mangas);
                        return true;
                }
                return false;
            }

        });
        popup.show();
    }

    private DialogFragment progressDialog = null;

    private void deleteManga(final List<LocalManga> mangas) {
        progressDialog = Utils.easyDialogProgress(getFragmentManager(), "Deleting", "Deleting manga");
        Thread thread = new Thread() {

            @Override
            public void run() {
                boolean _success = true;
                String _error = null;
                for (LocalManga localManga : mangas) {
                    IoUtils.deleteDirectory(new File(localManga.getLocalUri()));
                    try {
                        historyDAO.deleteManga(localManga);
                        downloadedMangaDAO.deleteManga(localManga);
                    } catch (DatabaseAccessException e) {
                        _success = false;
                        _error = e.getMessage();
                        Log.e(TAG, "Failed to delete downloaded manga: " + _error);
                    }
                    final boolean success = _success;
                    final String error = _error;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (success) {
                                loadDownloadedManga();
                            } else {
                                String formedError = Utils.stringResource(getActivity(), R.string.p_failed_to_delete);
                                Utils.showToast(getActivity(), formedError + error);
                            }
                        }
                    });
                    if (!success) {
                        break;
                    }
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                    }
                });
            }
        };
        thread.start();
    }
}