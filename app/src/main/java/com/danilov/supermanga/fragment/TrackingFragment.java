package com.danilov.supermanga.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.httpimage.HttpImageManager;
import com.danilov.supermanga.R;
import com.danilov.supermanga.activity.MainActivity;
import com.danilov.supermanga.activity.MangaInfoActivity;
import com.danilov.supermanga.core.adapter.BaseAdapter;
import com.danilov.supermanga.core.adapter.BaseGridAdapter;
import com.danilov.supermanga.core.adapter.ItemClickListener;
import com.danilov.supermanga.core.application.MangaApplication;
import com.danilov.supermanga.core.database.DatabaseAccessException;
import com.danilov.supermanga.core.database.HistoryDAO;
import com.danilov.supermanga.core.database.MangaDAO;
import com.danilov.supermanga.core.model.LocalManga;
import com.danilov.supermanga.core.model.Manga;
import com.danilov.supermanga.core.service.LocalImageManager;
import com.danilov.supermanga.core.util.Constants;
import com.danilov.supermanga.core.util.ServiceContainer;
import com.danilov.supermanga.core.util.Utils;
import com.danilov.supermanga.core.view.helper.MangaFilter;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by Semyon on 22.12.2014.
 */
public class TrackingFragment extends BaseFragmentNative {

    private static final String TAG = "TrackingFragment";

    private ProgressBar downloadedProgressBar;

    @Inject
    public LocalImageManager localImageManager = null;

    @Inject
    public HttpImageManager httpImageManager = null;

    private MangaDAO mangaDAO = null;
    private HistoryDAO historyDAO = null;

    private int sizeOfImage;

    private TrackingAdapter adapter = null;
    private RecyclerView recyclerView = null;
    private EditText filterEditText = null;

    public static TrackingFragment newInstance() {
        return new TrackingFragment();
    }

    public TrackingFragment() {
        MangaApplication.get().applicationComponent().inject(this);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.manga_favorites_fragment, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        sizeOfImage = getActivity().getResources().getDimensionPixelSize(R.dimen.grid_item_height);
        mangaDAO = ServiceContainer.getService(MangaDAO.class);
        historyDAO = ServiceContainer.getService(HistoryDAO.class);
        recyclerView = findViewById(R.id.recycler_view);
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
                List<Manga> _mangas = null;
                try {
                    _mangas = mangaDAO.getTracking();
                } catch (DatabaseAccessException e) {
                    _mangas = Collections.EMPTY_LIST;
                    _success = false;
                    _error = e.getMessage();
                    Log.e(TAG, "Failed to get favorite manga: " + _error);
                }
                final List<Manga> mangas = _mangas;
                final boolean success = _success;
                final String error = _error;
                handler.post(() -> {
                    MangaFilter filter = new MangaFilter(filterEditText, mangas);
                    adapter = new TrackingAdapter(recyclerView, mangas, filter);
                    adapter.setClickListener(listener);
                    filter.setAdapterAccessor(new MangaFilter.BaseAdapterAccessor() {
                        @Override
                        public void notifyDataSetInvalidated() {
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void notifyDataSetChanged(final List<Manga> mangaList) {
                            List<Manga> items = adapter.getItems();
                            items.clear();
                            items.addAll(mangaList);
                            adapter.notifyDataSetChanged();
                        }
                    });
                    downloadedProgressBar.setVisibility(View.INVISIBLE);
                    if (success) {
                        recyclerView.setAdapter(adapter);
                    } else {
                        String formedError = Utils.stringResource(getActivity(), R.string.p_failed_to_show_loaded);
                        Utils.showToast(getActivity(), formedError + error);
                    }
                });
            }

        };
        thread.start();
    }


    private ItemClickListener<Holder> listener = new ItemClickListener<Holder>() {

        @Override
        public void onItemClick(final int position, final Holder viewHolder) {
            Manga manga = adapter.getItem(position);

            Intent intent = new Intent(getActivity().getApplicationContext(), MangaInfoActivity.class);

            ImageView iv = viewHolder.mangaCover;
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
        }

        @Override
        public boolean onItemLongClick(final int position, final Holder viewHolder) {
            return false;
        }

    };

    private class TrackingAdapter extends BaseGridAdapter<Holder> {

        private MangaFilter filter;
        private List<Manga> items;

        public TrackingAdapter(final RecyclerView recyclerView, final List<Manga> objects,
                                final MangaFilter filter) {
            super(recyclerView);
            this.filter = filter;
            this.items = objects;
        }

        public Manga getItem(final int position) {
            return items.get(position);
        }

        public List<Manga> getItems() {
            return items;
        }

        @Override
        public Holder newViewHolder(final ViewGroup parent, final int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.favorites_grid_item, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(final Holder holder, final int position) {
            Manga manga = getItem(position);
            holder.title.setText(manga.getTitle());

            if (manga.isDownloaded()) {
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
                    //TODO: временный хак! Потом заблочить добавление в избранное если нет картинки (или придумать что-то ещё)
                    Uri coverUri = Uri.parse(manga.getCoverUri());
                    HttpImageManager.LoadRequest request = HttpImageManager.LoadRequest.obtain(coverUri, holder.mangaCover, manga.getRepository().getEngine().getRequestPreprocessor(), sizeOfImage);
                    Bitmap bitmap = httpImageManager.loadImage(request);
                    if (bitmap != null) {
                        holder.mangaCover.setImageBitmap(bitmap);
                    }
                }
            }

        }

        @Override
        public int getItemCount() {
            return items.size();
        }

    }

    private static class Holder extends RecyclerView.ViewHolder {

        public TextView title;
        public View isOnline;
        public ImageView mangaCover;

        protected Holder(final View view) {
            super(view);
            mangaCover = (ImageView) view.findViewById(R.id.manga_cover);
            isOnline = view.findViewById(R.id.is_online);
            title = (TextView) view.findViewById(R.id.manga_title);
        }

    }

}
