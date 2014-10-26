package com.danilov.manga.core.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.httpimage.HttpImageManager;
import com.danilov.manga.R;
import com.danilov.manga.core.model.LocalManga;
import com.danilov.manga.core.service.LocalImageManager;
import com.danilov.manga.core.util.ServiceContainer;

import java.util.List;

/**
 * Created by Semyon on 26.10.2014.
 */
public class DownloadedMangaAdapter extends ArrayAdapter<LocalManga> {

    private List<LocalManga> mangas = null;

    private LocalImageManager localImageManager = ServiceContainer.getService(LocalImageManager.class);

    private int sizeOfImage;

    private PopupButtonClickListener popupButtonClickListener;

    @Override
    public int getCount() {
        return mangas.size();
    }

    public DownloadedMangaAdapter(final Context context, final List<LocalManga> mangas, final PopupButtonClickListener listener) {
        super(context, 0, mangas);
        this.mangas = mangas;
        this.popupButtonClickListener = listener;
        sizeOfImage = context.getResources().getDimensionPixelSize(R.dimen.manga_list_image_height);
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
            holder.popupButton = (ImageButton) view.findViewById(R.id.popup_button);
        }
        final ImageButton popupButton = holder.popupButton;
        if (popupButtonClickListener != null) {
            holder.popupButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupButtonClickListener.onPopupButtonClick(popupButton, position);
                }
            });
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
        protected ImageButton popupButton;

    }

}