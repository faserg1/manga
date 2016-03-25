package com.danilov.supermanga.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.httpimage.HttpImageManager;
import com.danilov.supermanga.R;
import com.danilov.supermanga.activity.MangaInfoActivity;
import com.danilov.supermanga.activity.MangaViewerActivity;
import com.danilov.supermanga.core.application.MangaApplication;
import com.danilov.supermanga.core.database.DatabaseAccessException;
import com.danilov.supermanga.core.database.HistoryDAO;
import com.danilov.supermanga.core.model.HistoryElement;
import com.danilov.supermanga.core.model.LocalManga;
import com.danilov.supermanga.core.model.Manga;
import com.danilov.supermanga.core.service.LocalImageManager;
import com.danilov.supermanga.core.util.Constants;
import com.danilov.supermanga.core.util.ServiceContainer;
import com.danilov.supermanga.core.util.Utils;
import com.danilov.supermanga.core.view.helper.GridViewHelper;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Semyon Danilov on 07.10.2014.
 */
public class HistoryMangaFragment extends BaseFragmentNative implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private static final String TAG = "HistoryMangaFragment";

    @Bind(R.id.grid_view)
    public GridView gridView;

    @Bind(R.id.history_progress_bar)
    public ProgressBar historyProgressBar;
    @Inject
    public LocalImageManager localImageManager = null;
    @Inject
    public HttpImageManager httpImageManager = null;
    public float gridItemRatio;
    private HistoryMangaAdapter adapter = null;
    private HistoryDAO historyDAO = null;

    private int sizeOfImage;

    public HistoryMangaFragment() {
        MangaApplication.get().applicationComponent().inject(this);
    }

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
        ButterKnife.bind(this, view);
        historyDAO = ServiceContainer.getService(HistoryDAO.class);
        gridView.setOnItemClickListener(this);
        gridView.setOnItemLongClickListener(this);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int pColCount = sharedPreferences.getInt("P_COL_COUNT", 0);
        int lColCount = sharedPreferences.getInt("L_COL_COUNT", 0);
        int screenOrientation = getScreenOrientation();
        switch (screenOrientation) {
            case Configuration.ORIENTATION_PORTRAIT:
            case Configuration.ORIENTATION_SQUARE:
                if (pColCount != 0) {
                    gridView.setNumColumns(pColCount);
                }
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                if (lColCount != 0) {
                    gridView.setNumColumns(lColCount);
                }
                break;
        }

        sizeOfImage = getActivity().getResources().getDimensionPixelSize(R.dimen.grid_item_height);
        gridItemRatio = Utils.getFloatResource(R.dimen.grid_item_ratio, getActivity().getResources());
    }

    public int getScreenOrientation() {
        Display getOrient = getActivity().getWindowManager().getDefaultDisplay();
        int orientation = Configuration.ORIENTATION_UNDEFINED;
        if (getOrient.getWidth() == getOrient.getHeight()) {
            orientation = Configuration.ORIENTATION_SQUARE;
        } else {
            if (getOrient.getWidth() < getOrient.getHeight()) {
                orientation = Configuration.ORIENTATION_PORTRAIT;
            } else {
                orientation = Configuration.ORIENTATION_LANDSCAPE;
            }
        }
        return orientation;
    }

    @Override
    public void onStart() {
        super.onStart();
        loadHistory();
    }

    private void loadHistory() {
        if (adapter != null) {
            adapter.clearList();
        }
        historyProgressBar.setVisibility(View.VISIBLE);
        final Context context = getActivity();
        Thread thread = new Thread() {
            @Override
            public void run() {
                boolean _success = true;
                String _error = null;
                try {
                    List<HistoryElement> history = getHistorySorted();
                    if (!history.isEmpty()) {
                        adapter = new HistoryMangaAdapter(context, history);
                    }
                } catch (DatabaseAccessException e) {
                    _success = false;
                    _error = e.getMessage();
                    Log.e(TAG, "Failed to get history: " + _error);
                }
                final boolean success = _success;
                final String error = _error;
                handler.post(() -> {
                    historyProgressBar.setVisibility(View.INVISIBLE);
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

    @Override
    public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        Manga manga = adapter.getItem(position).getManga();

        Intent intent = new Intent(getActivity().getApplicationContext(), MangaInfoActivity.class);

        ImageView iv = (ImageView) view.findViewById(R.id.manga_cover);
        int[] onScreenLocation = new int[2];
        iv.getLocationOnScreen(onScreenLocation);

        intent.putExtra(MangaInfoActivity.EXTRA_LEFT, onScreenLocation[0]);
        intent.putExtra(MangaInfoActivity.EXTRA_TOP, onScreenLocation[1]);
        intent.putExtra(MangaInfoActivity.EXTRA_WIDTH, iv.getWidth());
        intent.putExtra(MangaInfoActivity.EXTRA_HEIGHT, iv.getHeight());
        intent.putExtra(MangaInfoActivity.EXTRA_HEIGHT, iv.getHeight());

        intent.putExtra(Constants.MANGA_PARCEL_KEY, manga);
        startActivity(intent);

        getActivity().overridePendingTransition(0, 0);
        return true;
    }

    private void removeHistory(final HistoryElement historyElement) {
        try {
            historyDAO.deleteManga(historyElement.getManga(), historyElement.isOnline());
            loadHistory();
        } catch (DatabaseAccessException e) {
            e.printStackTrace();
            Utils.showToast(getActivity(), getActivity().getString(R.string.e_failed_remove_history) + e.getMessage());
        }
    }

    @NonNull
    private List<HistoryElement> getHistorySorted() throws DatabaseAccessException {
        List<HistoryElement> history = historyDAO.getMangaHistory();
        Collections.sort(history, (l, r) -> {
            Date lDate = l.getDate();
            Date rDate = r.getDate();
            if (lDate.equals(rDate)) {
                return 0;
            }
            return lDate.after(rDate) ? -1 : 1;
        });
        return history;
    }

    private class HistoryMangaAdapter extends ArrayAdapter<HistoryElement> {

        private List<HistoryElement> history = null;

        public HistoryMangaAdapter(final Context context, final List<HistoryElement> history) {
            super(context, 0, history);
            this.history = history;
        }

        @Override
        public int getCount() {
            return history.size();
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
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();

                int colWidth = GridViewHelper.getColumnWidth(gridView);

                layoutParams.height = (int) (colWidth * gridItemRatio);
                view.setLayoutParams(layoutParams);
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
                holder.discardButton = (ImageButton) view.findViewById(R.id.discard_button);
                holder.isOnline = view.findViewById(R.id.is_online);
            }
            final HistoryElement historyElement = history.get(position);
            holder.discardButton.setOnClickListener(view1 -> removeHistory(historyElement));
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
                if (manga.getCoverUri() != null) {
                    Uri coverUri = Uri.parse(manga.getCoverUri());
                    HttpImageManager.LoadRequest request = HttpImageManager.LoadRequest.obtain(coverUri, holder.mangaCover, manga.getRepository().getEngine().getRequestPreprocessor(), sizeOfImage);
                    Bitmap bitmap = httpImageManager.loadImage(request);
                    if (bitmap != null) {
                        holder.mangaCover.setImageBitmap(bitmap);
                    }
                }
            }
            return view;
        }

        public void clearList() {
            if (history != null) {
                history.clear();
                notifyDataSetChanged();
            }
        }

        private class GridItemHolder {

            public ImageView mangaCover;
            public TextView mangaTitle;
            public ImageButton discardButton;
            private View isOnline;

        }

    }

}