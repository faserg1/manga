package com.danilov.supermanga.fragment;

import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.danilov.supermanga.R;
import com.danilov.supermanga.activity.BaseToolbarActivity;
import com.danilov.supermanga.activity.MainActivity;
import com.danilov.supermanga.core.adapter.BaseGridAdapter;
import com.danilov.supermanga.core.adapter.ItemClickListener;
import com.danilov.supermanga.core.application.MangaApplication;
import com.danilov.supermanga.core.database.DatabaseAccessException;
import com.danilov.supermanga.core.database.HistoryDAO;
import com.danilov.supermanga.core.database.MangaDAO;
import com.danilov.supermanga.core.model.LocalManga;
import com.danilov.supermanga.core.model.Manga;
import com.danilov.supermanga.core.repository.RepositoryEngine;
import com.danilov.supermanga.core.repository.RepositoryException;
import com.danilov.supermanga.core.service.LocalImageManager;
import com.danilov.supermanga.core.util.IoUtils;
import com.danilov.supermanga.core.util.ServiceContainer;
import com.danilov.supermanga.core.util.Utils;
import com.danilov.supermanga.core.view.helper.MangaFilter;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

/**
 * Created by Semyon Danilov on 07.10.2014.
 */
public class DownloadedMangaFragment extends BaseFragmentNative implements ActionMode.Callback {

    private static final String TAG = "DownloadedMangaFragment";

    private boolean isInMultiChoice = false;

    private ProgressBar downloadedProgressBar;

    @Inject
    LocalImageManager localImageManager = null;

    private MangaDAO mangaDAO = null;

    private HistoryDAO historyDAO = null;

    private int sizeOfImage;

    private NewDownloadedMangaAdapter adapter = null;
    private RecyclerView gridView = null;
    private EditText filterEditText = null;
    private ActionMode actionMode;
    private ItemClickListener<Holder> listener = new ItemClickListener<Holder>() {

        @Override
        public void onItemClick(final int position, final Holder viewHolder) {
            LocalManga manga = adapter.getItem(position);

            if (isInMultiChoice) {
                adapter.onMultiSelectClick(view, position);
                updateActionMode(actionMode);
                return;
            }
            MainActivity activity = (MainActivity) getActivity();
            activity.showChaptersFragment(manga);
        }

        @Override
        public boolean onItemLongClick(final int position, final Holder viewHolder) {
            BaseToolbarActivity baseToolbarActivity = (BaseToolbarActivity) getActivity();
            if (isInMultiChoice) {
                adapter.onMultiSelectClick(view, position);
                updateActionMode(actionMode);
                return true;
            }
            actionMode = baseToolbarActivity.startSupportActionMode(DownloadedMangaFragment.this);
            adapter.onMultiSelectClick(view, position);
            adapter.setIsInMultiSelect(true);
            isInMultiChoice = true;
            updateActionMode(actionMode);
            return true;
        }

    };
    private DialogFragment progressDialog = null;

    public DownloadedMangaFragment() {
        MangaApplication.get().applicationComponent().inject(this);
    }

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
        mangaDAO = ServiceContainer.getService(MangaDAO.class);
        historyDAO = ServiceContainer.getService(HistoryDAO.class);
        gridView = findViewById(R.id.recycler_view);
        filterEditText = findViewById(R.id.filter);
        findViewById(R.id.clear).setOnClickListener(v -> filterEditText.setText(""));
        downloadedProgressBar = (ProgressBar) view.findViewById(R.id.downloaded_progress_bar);
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
                handler.post(() -> {
                    MangaFilter mangaFilter = new MangaFilter(filterEditText, localMangas);
                    adapter = new NewDownloadedMangaAdapter(gridView, localMangas);
                    adapter.setClickListener(listener);
                    mangaFilter.setAdapterAccessor(new MangaFilter.BaseAdapterAccessor() {
                        @Override
                        public void notifyDataSetInvalidated() {
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void notifyDataSetChanged(final List<Manga> mangaList) {
                            List<? super LocalManga> items = adapter.getItems();
                            items.clear();
                            for (Manga manga : mangaList) {
                                items.add((LocalManga) manga);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    });
                    downloadedProgressBar.setVisibility(View.INVISIBLE);
                    if (success) {
                        gridView.setAdapter(adapter);
                    } else {
                        String formedError = Utils.stringResource(getActivity(), R.string.p_failed_to_show_loaded);
                        Utils.showToast(getActivity(), formedError + error);
                    }
                });
            }

        };
        thread.start();
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
        actionMode.setTitle(selectedString + selected + "/" + adapter.getItemCount());
    }

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
                    handler.post(() -> {
                        Log.e(TAG, "WW");
                        if (success) {
                            loadDownloadedManga();
                            Log.e(TAG, "XX");
                        } else {
                            String formedError = Utils.stringResource(getActivity(), R.string.p_failed_to_delete);
                            Utils.showToast(getActivity(), formedError + error);
                            Log.e(TAG, "YY");
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
                handler.post(() -> {
                    Log.e(TAG, "ZZ");
//                        progressDialog.dismiss();
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

    private static class Holder extends RecyclerView.ViewHolder {

        public ImageView mangaCover;
        public TextView mangaTitle;
        public TextView chaptersDownloaded;
        public View selector;
        public View background;

        public View rootView;

        protected Holder(final View view) {
            super(view);
            this.rootView = view;
            mangaCover = (ImageView) view.findViewById(R.id.manga_cover);
            mangaTitle = (TextView) view.findViewById(R.id.manga_title);
            chaptersDownloaded = (TextView) view.findViewById(R.id.chapters_downloaded);
            selector = view.findViewById(R.id.selectorBackground);
            background = view.findViewById(R.id.background);
        }

        public View getRootView() {
            return rootView;
        }
    }

    private class NewDownloadedMangaAdapter extends BaseGridAdapter<Holder> {

        private List<LocalManga> mangas;

        private boolean[] isPosSelected;

        private boolean isInMultiSelect = false;

        private Executor downloadedChaptersQueryExecutor = Executors.newSingleThreadExecutor();

        public NewDownloadedMangaAdapter(final RecyclerView recyclerView, final List<LocalManga> objects) {
            super(recyclerView);
            this.mangas = objects;

            isPosSelected = new boolean[mangas.size()];
            for (int i = 0; i < mangas.size(); i++) {
                isPosSelected[i] = false;
            }
        }

        public LocalManga getItem(final int position) {
            return mangas.get(position);
        }

        public List<? super LocalManga> getItems() {
            return mangas;
        }

        @Override
        public Holder newViewHolder(final ViewGroup parent, final int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.manga_grid_item_new, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(final Holder holder, final int position) {
            LocalManga manga = getItem(position);
            if (manga.getChapters() == null) {
                // to avoid multiple tasks for single manga
                manga.setChapters(Collections.emptyList());
                downloadedChaptersQueryExecutor.execute(() -> {
                    try {
                        RepositoryEngine.DefaultRepository.OFFLINE.getEngine().queryForChapters(manga);
                        handler.post(this::notifyDataSetChanged);
                    } catch (RepositoryException e) {
                        //meh
                    }
                });
            } else {
                holder.chaptersDownloaded.setText("" + manga.getChapters().size());
            }

            holder.mangaTitle.setText(manga.getTitle());
            String mangaUri = manga.getLocalUri();
            Bitmap bitmap = localImageManager.loadBitmap(holder.mangaCover, mangaUri + "/cover", sizeOfImage);
            if (bitmap != null) {
                holder.mangaCover.setImageBitmap(bitmap);
            }
            if (isInMultiSelect) {
                holder.background.setVisibility(View.INVISIBLE);
            } else {
                holder.background.setVisibility(View.VISIBLE);
            }
            if (isPosSelected[position]) {
                holder.selector.setVisibility(View.VISIBLE);
            } else {
                holder.selector.setVisibility(View.INVISIBLE);
            }

        }

        @Override
        public int getItemCount() {
            return mangas.size();
        }

        public void setIsInMultiSelect(final boolean isInMultiSelect) {
            this.isInMultiSelect = isInMultiSelect;
            notifyDataSetChanged();
        }

        public void onMultiSelectClick(final View view, final int position) {
            setPositionSelected(view, position, !isPosSelected[position]);
        }

        public void setPositionSelected(final View view, final int position, final boolean isSelected) {
            isPosSelected[position] = isSelected;
            notifyItemChanged(position);
        }

        public void deselectAll() {
            for (int i = 0; i < getItemCount(); i++) {
                isPosSelected[i] = false;
            }
            notifyDataSetChanged();
        }

        public List<LocalManga> getSelectedManga() {
            List<LocalManga> selected = new LinkedList<>();
            for (int i = 0; i < getItemCount(); i++) {
                if (isPosSelected[i]) {
                    selected.add(getItem(i));
                }
            }
            return selected;
        }

        public int getSelectedQuantity() {
            int selected = 0;
            for (int i = 0; i < getItemCount(); i++) {
                if (isPosSelected[i]) {
                    selected++;
                }
            }
            return selected;
        }


    }

}