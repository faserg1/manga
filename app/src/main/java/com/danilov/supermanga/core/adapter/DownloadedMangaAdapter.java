package com.danilov.supermanga.core.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.danilov.supermanga.R;
import com.danilov.supermanga.core.application.MangaApplication;
import com.danilov.supermanga.core.model.LocalManga;
import com.danilov.supermanga.core.model.Manga;
import com.danilov.supermanga.core.service.LocalImageManager;
import com.danilov.supermanga.core.view.helper.MangaFilter;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by Semyon on 26.10.2014.
 */
public class DownloadedMangaAdapter extends ArrayAdapter<LocalManga> {

    @Inject
    public LocalImageManager localImageManager;

    private int sizeOfImage;

    private boolean[] isPosSelected;

    private boolean isInMultiSelect = false;

    private PopupButtonClickListener popupButtonClickListener;

    private MangaFilter filter;

    public DownloadedMangaAdapter(final Context context, final List<LocalManga> mangas, final MangaFilter filter, final PopupButtonClickListener listener) {
        super(context, 0, mangas);
        MangaApplication.get().applicationComponent().inject(this);
        this.filter = filter;
        isPosSelected = new boolean[mangas.size()];
        for (int i = 0; i < mangas.size(); i++) {
            isPosSelected[i] = false;
        }
        this.popupButtonClickListener = listener;
        sizeOfImage = context.getResources().getDimensionPixelSize(R.dimen.grid_item_height);
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
            view.setTag(holder);
            holder.mangaCover = (ImageView) view.findViewById(R.id.manga_cover);
            holder.mangaTitle = (TextView) view.findViewById(R.id.manga_title);
            holder.popupButton = (ImageButton) view.findViewById(R.id.popup_button);
            holder.selector = view.findViewById(R.id.selectorBackground);
        }
        final ImageButton popupButton = holder.popupButton;
        if (popupButtonClickListener != null) {
            holder.popupButton.setOnClickListener(v -> popupButtonClickListener.onPopupButtonClick(popupButton, position));
        }
        LocalManga manga = getItem(position);
        holder.mangaTitle.setText(manga.getTitle());
        String mangaUri = manga.getLocalUri();
        Bitmap bitmap = localImageManager.loadBitmap(holder.mangaCover, mangaUri + "/cover", sizeOfImage);
        if (bitmap != null) {
            holder.mangaCover.setImageBitmap(bitmap);
        }
        if (isInMultiSelect) {
            view.findViewById(R.id.background).setVisibility(View.INVISIBLE);
        } else {
            view.findViewById(R.id.background).setVisibility(View.VISIBLE);
        }
        if (isPosSelected[position]) {
            holder.selector.setVisibility(View.VISIBLE);
        } else {
            holder.selector.setVisibility(View.INVISIBLE);
        }
        return view;
    }

    public void setIsInMultiSelect(final boolean isInMultiSelect) {
        this.isInMultiSelect = isInMultiSelect;
        notifyDataSetChanged();
    }

    public void onMultiSelectClick(final View view, final int position) {
        setPositionSelected(view, position, !isPosSelected[position]);
    }

    public void setPositionSelected(final View view, final int position, final boolean isSelected) {
        GridItemHolder holder = null;
        Object tag = view.getTag();
        if (tag instanceof GridItemHolder) {
            holder = (GridItemHolder) tag;
            holder.selector.setVisibility(isSelected ? View.VISIBLE : View.INVISIBLE);
        }
        isPosSelected[position] = isSelected;
    }

    public void deselectAll() {
        for (int i = 0; i < getCount(); i++) {
            isPosSelected[i] = false;
        }
        notifyDataSetChanged();
    }

    public List<LocalManga> getSelectedManga() {
        List<LocalManga> selected = new LinkedList<>();
        for (int i = 0; i < getCount(); i++) {
            if (isPosSelected[i]) {
                selected.add(getItem(i));
            }
        }
        return selected;
    }

    public int getSelectedQuantity() {
        int selected = 0;
        for (int i = 0; i < getCount(); i++) {
            if (isPosSelected[i]) {
                selected++;
            }
        }
        return selected;
    }

    private class GridItemHolder {

        public ImageView mangaCover;
        public TextView mangaTitle;
        public ImageButton popupButton;
        public View selector;

    }

    @Override
    public MangaFilter getFilter() {
        return filter;
    }

    public MangaFilter.BaseAdapterAccessor createAccessor() {
        return new MangaFilter.BaseAdapterAccessor() {

            @Override
            public void notifyDataSetInvalidated() {
                DownloadedMangaAdapter.this.notifyDataSetInvalidated();
            }

            @Override
            public void notifyDataSetChanged(final List<Manga> mangaList) {
                DownloadedMangaAdapter.this.clear();
                for (Manga manga : mangaList) {
                    DownloadedMangaAdapter.this.add((LocalManga) manga);
                }
                isPosSelected = new boolean[mangaList.size()];
                for (int i = 0; i < mangaList.size(); i++) {
                    isPosSelected[i] = false;
                }
            }

        };
    }

}