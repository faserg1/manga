package com.danilov.mangareader.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
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

import com.android.httpimage.HttpImageManager;
import com.danilov.mangareader.R;
import com.danilov.mangareader.activity.MangaViewerActivity;
import com.danilov.mangareader.core.database.DatabaseAccessException;
import com.danilov.mangareader.core.database.HistoryDAO;
import com.danilov.mangareader.core.model.HistoryElement;
import com.danilov.mangareader.core.model.LocalManga;
import com.danilov.mangareader.core.model.Manga;
import com.danilov.mangareader.core.service.LocalImageManager;
import com.danilov.mangareader.core.util.Constants;
import com.danilov.mangareader.core.util.ServiceContainer;
import com.danilov.mangareader.core.util.Utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by Semyon Danilov on 07.10.2014.
 */
public class HistoryMangaFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    private static final String TAG = "HistoryMangaFragment";

    private GridView gridView;
    private ProgressBar historyProgressBar;

    private HistoryMangaAdapter adapter = null;

    private LocalImageManager localImageManager = null;
    private HttpImageManager httpImageManager = null;
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
        httpImageManager = ServiceContainer.getService(HttpImageManager.class);
        historyProgressBar = (ProgressBar) view.findViewById(R.id.history_progress_bar);
        historyDAO = ServiceContainer.getService(HistoryDAO.class);
        gridView = (GridView) view.findViewById(R.id.grid_view);
        gridView.setOnItemClickListener(this);
        sizeOfImage = getActivity().getResources().getDimensionPixelSize(R.dimen.grid_item_height);
    }

    @Override
    public void onStart() {
        super.onStart();
        loadHistory();
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
                    List<HistoryElement> history = getHistorySorted();
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
        intent.putExtra(Constants.SHOW_ONLINE, historyElement.isOnline());
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
                holder.isOnline = view.findViewById(R.id.is_online);
            }
            HistoryElement historyElement = history.get(position);
            Manga manga = historyElement.getManga();
            holder.mangaTitle.setText(manga.getTitle());
            if (!historyElement.isOnline()) {
                holder.isOnline.setVisibility(View.INVISIBLE);
                LocalManga localManga = (LocalManga) manga;
                String mangaUri = localManga.getLocalUri();
                Bitmap bitmap = localImageManager.loadBitmap(holder.mangaCover, mangaUri + "/cover", sizeOfImage);
                if (bitmap != null) {
                    holder.mangaCover.setImageBitmap(bitmap);
                }
            } else {
                holder.isOnline.setVisibility(View.VISIBLE);
                Uri coverUri = Uri.parse(manga.getCoverUri());
                HttpImageManager.LoadRequest request = HttpImageManager.LoadRequest.obtain(coverUri, holder.mangaCover, sizeOfImage);
                Bitmap bitmap = httpImageManager.loadImage(request);
                if (bitmap != null) {
                    holder.mangaCover.setImageBitmap(bitmap);
                }
            }
            return view;
        }

        private class GridItemHolder {

            public ImageView mangaCover;
            public TextView mangaTitle;
            private View isOnline;

        }

    }

    private List<HistoryElement> getHistorySorted() throws DatabaseAccessException {
        List<HistoryElement> history = historyDAO.getMangaHistory();
        if (history == null) {
            return null;
        }
        Collections.sort(history, new Comparator<HistoryElement>() {
            @Override
            public int compare(final HistoryElement l, final HistoryElement r) {
                Date lDate = l.getDate();
                Date rDate = r.getDate();
                if (lDate.equals(rDate)) {
                    return 0;
                }
                return lDate.after(rDate) ? -1 : 1;
            }
        });
        return history;
    }

}