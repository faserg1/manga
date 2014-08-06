package com.danilov.manga.test;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import com.danilov.manga.R;
import com.danilov.manga.core.database.DatabaseAccessException;
import com.danilov.manga.core.database.DownloadedMangaDAO;
import com.danilov.manga.core.model.LocalManga;
import com.danilov.manga.core.service.LocalImageManager;
import com.danilov.manga.core.util.ServiceContainer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by Semyon Danilov on 11.06.2014.
 */
public class LocalMangaActivity extends Activity {

    private static final String TAG = "DownloadTestActivity";

    private LocalImageManager localImageManager = null;
    private GridView gridView;
    int sizeOfImage = 0;

    private DownloadedMangaDAO downloadedMangaDAO = null;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        localImageManager = ServiceContainer.getService(LocalImageManager.class);
        downloadedMangaDAO = ServiceContainer.getService(DownloadedMangaDAO.class);
        setContentView(R.layout.test_local_manga_activity);
        gridView = (GridView) findViewById(R.id.grid_view);
        try {
            List<LocalManga> localMangas = downloadedMangaDAO.getAllManga();
            GridViewAdapter adapter = new GridViewAdapter(this, localMangas);
            gridView.setAdapter(adapter);
        } catch (DatabaseAccessException e) {
            e.printStackTrace();
        }
        sizeOfImage = getApplicationContext().getResources().getDimensionPixelSize(R.dimen.manga_list_image_height);
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

        @Nullable
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