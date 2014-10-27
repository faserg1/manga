package com.danilov.manga.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.danilov.manga.R;
import com.danilov.manga.activity.MangaViewerActivity;
import com.danilov.manga.core.adapter.DownloadedMangaAdapter;
import com.danilov.manga.core.database.DatabaseAccessException;
import com.danilov.manga.core.database.DownloadedMangaDAO;
import com.danilov.manga.core.database.HistoryDAO;
import com.danilov.manga.core.model.HistoryElement;
import com.danilov.manga.core.model.LocalManga;
import com.danilov.manga.core.service.LocalImageManager;
import com.danilov.manga.core.util.Constants;
import com.danilov.manga.core.util.ServiceContainer;
import com.danilov.manga.core.util.Utils;

import java.util.List;

/**
 * Created by Semyon Danilov on 07.10.2014.
 */
public class HistoryMangaFragment extends Fragment implements AdapterView.OnItemClickListener {

    private static final String TAG = "HistoryMangaFragment";

    private View view;
    private GridView gridView;
    private ProgressBar historyProgressBar;

    private Handler handler = new Handler();

    private HistoryMangaAdapter adapter = null;

    private LocalImageManager localImageManager = null;
    private HistoryDAO historyDAO = null;

    private int sizeOfImage;

    public static HistoryMangaFragment newInstance() {
        return new HistoryMangaFragment();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.manga_history_fragment, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        localImageManager = ServiceContainer.getService(LocalImageManager.class);
        historyProgressBar = (ProgressBar) view.findViewById(R.id.history_progress_bar);
        historyDAO = ServiceContainer.getService(HistoryDAO.class);
        gridView = (GridView) view.findViewById(R.id.grid_view);
        loadHistory();
        gridView.setOnItemClickListener(this);
        sizeOfImage = getActivity().getResources().getDimensionPixelSize(R.dimen.manga_list_image_height);
    }

    private void loadHistory() {
        historyProgressBar.setVisibility(View.VISIBLE);
        final Context context = getActivity();
        Thread thread = new Thread() {
            @Override
            public void run() {
                boolean _success = true;
                String _error = null;
                try {
                    List<HistoryElement> history = historyDAO.getAllLocalMangaHistory();
                    if (history != null && !history.isEmpty()) {
                        Log.d(TAG, "Context is " + context);
                        Log.d(TAG, "HistoryDAO is " + history);
                        adapter = new HistoryMangaAdapter(context, history);
                    }
                } catch (DatabaseAccessException e) {
                    _success = false;
                    _error = e.getMessage();
                    Log.e(TAG, "Failed to get history: " + _error);
                }
                final boolean success = _success;
                final String error = _error;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        historyProgressBar.setVisibility(View.INVISIBLE);
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
        HistoryMangaAdapter adapter = (HistoryMangaAdapter) parent.getAdapter();
        HistoryElement historyElement = adapter.getHistoryElements().get(position);
        Intent intent = new Intent(this.getActivity(), MangaViewerActivity.class);
        intent.putExtra(Constants.MANGA_PARCEL_KEY, historyElement.getManga());
        intent.putExtra(Constants.FROM_PAGE_KEY, historyElement.getPage());
        intent.putExtra(Constants.FROM_CHAPTER_KEY, historyElement.getChapter());
        startActivity(intent);
    }



    private class HistoryMangaAdapter extends ArrayAdapter<HistoryElement> {

        private List<HistoryElement> history = null;

        @Override
        public int getCount() {
            return history.size();
        }

        public HistoryMangaAdapter(final Context context, final List<HistoryElement> history) {
            super(context, 0, history);
            this.history = history;
        }

        public List<HistoryElement> getHistoryElements() {
            return history;
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.history_grid_item, parent, false);
            }
            GridItemHolder holder = null;
            Object tag = view.getTag();
            if (tag instanceof GridItemHolder) {
                holder = (GridItemHolder) tag;
            }
            if (holder == null) {
                holder = new GridItemHolder();
                holder.mangaCover = (ImageView) view.findViewById(R.id.manga_cover);
                holder.mangaTitle = (TextView) view.findViewById(R.id.manga_title);
            }
            LocalManga manga = (LocalManga) history.get(position).getManga();
            holder.mangaTitle.setText(manga.getTitle());
            String mangaUri = manga.getLocalUri();
            Bitmap bitmap = localImageManager.loadBitmap(holder.mangaCover, mangaUri + "/cover", sizeOfImage);
            if (bitmap != null) {
                holder.mangaCover.setImageBitmap(bitmap);
            }
            return view;
        }

        private class GridItemHolder {

            public ImageView mangaCover;
            public TextView mangaTitle;

        }

    }

}