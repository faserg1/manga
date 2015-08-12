package com.danilov.supermanga.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.danilov.supermanga.R;
import com.danilov.supermanga.core.database.DatabaseAccessException;
import com.danilov.supermanga.core.database.HistoryDAO;
import com.danilov.supermanga.core.model.HistoryElement;
import com.danilov.supermanga.core.model.LocalManga;
import com.danilov.supermanga.core.service.LocalImageManager;
import com.danilov.supermanga.core.util.Constants;
import com.danilov.supermanga.core.util.ServiceContainer;

import java.util.List;

/**
 * Created by Semyon Danilov on 02.10.2014.
 */
public class HistoryActivity extends Activity implements AdapterView.OnItemClickListener {

    private static final String TAG = "HistoryActivity";

    private LocalImageManager localImageManager = null;
    private GridView gridView;
    int sizeOfImage = 0;

    private HistoryDAO historyDAO = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        localImageManager = ServiceContainer.getService(LocalImageManager.class);
        historyDAO = ServiceContainer.getService(HistoryDAO.class);
        setContentView(R.layout.history_activity);
        gridView = (GridView) findViewById(R.id.grid_view);
        try {
            List<HistoryElement> history = historyDAO.getMangaHistory();
            GridViewAdapter adapter = new GridViewAdapter(this, history);
            gridView.setAdapter(adapter);
        } catch (DatabaseAccessException e) {
            e.printStackTrace();
        }
        gridView.setOnItemClickListener(this);
        sizeOfImage = getApplicationContext().getResources().getDimensionPixelSize(R.dimen.manga_list_image_height);
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        GridViewAdapter adapter = (GridViewAdapter) parent.getAdapter();
        HistoryElement historyElement = adapter.getHistoryElements().get(position);
        Intent intent = new Intent(this, MangaViewerActivity.class);
        intent.putExtra(Constants.MANGA_PARCEL_KEY, historyElement.getManga());
        intent.putExtra(Constants.FROM_PAGE_KEY, historyElement.getPage());
        intent.putExtra(Constants.FROM_CHAPTER_KEY, historyElement.getChapter());
        startActivity(intent);
    }

    private class GridViewAdapter extends ArrayAdapter<HistoryElement> {

        private List<HistoryElement> history = null;

        @Override
        public int getCount() {
            return history.size();
        }

        public GridViewAdapter(final Context context, final List<HistoryElement> history) {
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