package com.danilov.supermanga.test;

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
import com.danilov.supermanga.activity.MangaViewerActivity;
import com.danilov.supermanga.core.model.LocalManga;
import com.danilov.supermanga.core.service.LocalImageManager;
import com.danilov.supermanga.core.util.Constants;
import com.danilov.supermanga.core.util.ServiceContainer;

import java.util.List;

/**
 * Created by Semyon Danilov on 11.06.2014.
 */
public class LocalMangaActivity extends Activity implements AdapterView.OnItemClickListener {

    private static final String TAG = "DownloadTestActivity";

    private GridView gridView;
    int sizeOfImage = 0;

    private LocalImageManager localImageManager = null;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        localImageManager = ServiceContainer.getService(LocalImageManager.class);
        setContentView(R.layout.test_local_manga_activity);
        gridView = (GridView) findViewById(R.id.grid_view);
        GridViewAdapter adapter = new GridViewAdapter(this, null);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(this);
        sizeOfImage = getApplicationContext().getResources().getDimensionPixelSize(R.dimen.manga_list_image_height);
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        GridViewAdapter adapter = (GridViewAdapter) parent.getAdapter();
        LocalManga manga = adapter.getMangas().get(position);
        Intent intent = new Intent(this, MangaViewerActivity.class);
        intent.putExtra(Constants.MANGA_PARCEL_KEY, manga);
        startActivity(intent);
    }

    private class GridViewAdapter extends ArrayAdapter<LocalManga> {

        private List<LocalManga> mangas = null;

        @Override
        public int getCount() {
            return mangas.size();
        }

        public GridViewAdapter(final Context context, final List<LocalManga> mangas) {
            super(context, 0, mangas);
            this.mangas = mangas;
        }

        public List<LocalManga> getMangas() {
            return mangas;
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.manga_grid_item, parent, false);
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
            LocalManga manga = mangas.get(position);
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