package com.danilov.supermanga.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ProgressBar;

import com.danilov.supermanga.R;
import com.danilov.supermanga.activity.MainActivity;
import com.danilov.supermanga.activity.MangaViewerActivity;
import com.danilov.supermanga.core.adapter.DownloadedMangaAdapter;
import com.danilov.supermanga.core.adapter.PopupButtonClickListener;
import com.danilov.supermanga.core.database.DatabaseAccessException;
import com.danilov.supermanga.core.database.HistoryDAO;
import com.danilov.supermanga.core.database.MangaDAO;
import com.danilov.supermanga.core.model.HistoryElement;
import com.danilov.supermanga.core.model.LocalManga;
import com.danilov.supermanga.core.model.Manga;
import com.danilov.supermanga.core.service.LocalImageManager;
import com.danilov.supermanga.core.util.Constants;
import com.danilov.supermanga.core.util.IoUtils;
import com.danilov.supermanga.core.util.ServiceContainer;
import com.danilov.supermanga.core.util.Utils;
import com.danilov.supermanga.core.view.helper.MangaFilter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Semyon Danilov on 07.10.2014.
 */
public class DownloadedMangaFragment extends BaseFragment implements AdapterView.OnItemClickListener, PopupButtonClickListener, AdapterView.OnItemLongClickListener, ActionMode.Callback {

    private static final String TAG = "DownloadedMangaFragment";

    private boolean isInMultiChoice = false;

    private ProgressBar downloadedProgressBar;

    private LocalImageManager localImageManager = null;
    private MangaDAO mangaDAO = null;
    private HistoryDAO historyDAO = null;

    private int sizeOfImage;

    private DownloadedMangaAdapter adapter = null;
    private GridView gridView = null;
    private EditText filterEditText = null;
    private ActionMode actionMode;

    public static DownloadedMangaFragment newInstance() {
        return new DownloadedMangaFragment();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.manga_downloaded_fragment, container, false);
        return view;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        sizeOfImage = getActivity().getResources().getDimensionPixelSize(R.dimen.manga_list_image_height);
        localImageManager = ServiceContainer.getService(LocalImageManager.class);
        mangaDAO = ServiceContainer.getService(MangaDAO.class);
        historyDAO = ServiceContainer.getService(HistoryDAO.class);
        gridView = findViewById(R.id.grid_view);
        filterEditText = findViewById(R.id.filter);
        findViewById(R.id.clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                filterEditText.setText("");
            }
        });
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
                List<LocalManga> _manga = null;
                try {
                    _manga = mangaDAO.getAllDownloaded();
                } catch (DatabaseAccessException e) {
                    _manga = Collections.EMPTY_LIST;
                    _success = false;
                    _error = e.getMessage();
                    Log.e(TAG, "Failed to get downloaded manga: " + _error);
                }
                final List<LocalManga> localMangas = _manga;
                final boolean success = _success;
                final String error = _error;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        MangaFilter mangaFilter = new MangaFilter(filterEditText, localMangas);
                        adapter = new DownloadedMangaAdapter(context, localMangas, mangaFilter, DownloadedMangaFragment.this);
                        mangaFilter.setAdapterAccessor(adapter.createAccessor());
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
            updateActionMode(actionMode);
            return;
        }
        LocalManga manga = adapter.getItem(position);

        HistoryElement historyElement = null;
        try {
            historyElement = historyDAO.getHistoryByManga(manga, false);
        } catch (DatabaseAccessException e) {
        }

        Intent intent = new Intent(getActivity(), MangaViewerActivity.class);


        intent.putExtra(Constants.MANGA_PARCEL_KEY, manga);
        intent.putExtra(Constants.SHOW_ONLINE, false);
        if (historyElement != null) {
            intent.putExtra(Constants.FROM_CHAPTER_KEY, historyElement.getChapter());
            intent.putExtra(Constants.FROM_PAGE_KEY, historyElement.getPage());
        }
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        ActionBarActivity actionBarActivity = (ActionBarActivity) getActivity();
        if (isInMultiChoice) {
            adapter.onMultiSelectClick(view, position);
            updateActionMode(actionMode);
            return true;
        }
        actionMode = actionBarActivity.startSupportActionMode(this);
        adapter.onMultiSelectClick(view, position);
        adapter.setIsInMultiSelect(true);
        isInMultiChoice = true;
        updateActionMode(actionMode);
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
        updateActionMode(actionMode);
        return true;
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

    private void updateActionMode(final ActionMode actionMode) {
        String selectedString = Utils.stringResource(getActivity(), R.string.sv_selected);
        int selected = adapter.getSelectedQuantity();
        actionMode.setTitle(selectedString + selected + "/" + adapter.getCount());
    }

    @Override
    public void onPopupButtonClick(View popupButton, int listPosition) {
        final PopupMenu popup = new PopupMenu(getActivity(), popupButton);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.downloaded_manga_item_menu, popup.getMenu());
        final LocalManga manga = adapter.getItem(listPosition);
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
//        progressDialog = Utils.easyDialogProgress(getFragmentManager(), "Deleting", "Deleting manga");
        Thread thread = new Thread() {

            @Override
            public void run() {
                boolean _success = true;
                String _error = null;
                Log.e(TAG, "A");
                for (LocalManga localManga : mangas) {
                    Log.e(TAG, "B");
                    try {
                        historyDAO.deleteManga(localManga, false);
                        Log.e(TAG, "C");
                        mangaDAO.setDownloaded(localManga, false);
                        Log.e(TAG, "D");
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
                            Log.e(TAG, "WW");
                            if (success) {
                                loadDownloadedManga();
                                Log.e(TAG, "XX");
                            } else {
                                String formedError = Utils.stringResource(getActivity(), R.string.p_failed_to_delete);
                                Utils.showToast(getActivity(), formedError + error);
                                Log.e(TAG, "YY");
                            }
                        }
                    });
                    if (!success) {
                        Log.e(TAG, "E");
                        break;
                    }
                }
                for (LocalManga localManga : mangas) {
                    IoUtils.deleteDirectory(new File(localManga.getLocalUri()));
                }
                Log.e(TAG, "F");
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "ZZ");
//                        progressDialog.dismiss();
                    }
                });
            }
        };
        thread.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.startHandling();
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        handler.stopHandling();
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        if (actionMode != null) {
            actionMode.finish();
        }
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.downloaded_manga_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_local_manga:
                MainActivity activity = (MainActivity) getActivity();
                activity.showAddLocalMangaFragment();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}