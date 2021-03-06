package com.danilov.supermanga.core.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.httpimage.HttpImageManager;
import com.danilov.supermanga.R;
import com.danilov.supermanga.core.application.MangaApplication;
import com.danilov.supermanga.core.model.Manga;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by Semyon Danilov on 18.05.2014.
 */
public class MangaListAdapter extends ArrayAdapter<Manga> {

    private final String TAG = "MangaListAdapter";

    @Inject
    public HttpImageManager httpImageManager;

    private final int resourceId;
    private int sizeOfImage;
    private PopupButtonClickListener popupButtonClickListener;

    public MangaListAdapter(final Context context, final int resource, final List<Manga> objects, final PopupButtonClickListener popupButtonClickListener) {
        super(context, resource, objects);
        MangaApplication.get().applicationComponent().inject(this);
        resourceId = resource;
        sizeOfImage = context.getResources().getDimensionPixelSize(R.dimen.manga_list_image_height);
        this.popupButtonClickListener = popupButtonClickListener;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(resourceId, parent, false); //attaching to parent == (false), because it attaching later by android
        }
        Manga manga = getItem(position);
        Object tag = view.getTag();
        final MangaViewBag viewBag;

        if (tag != null && tag instanceof MangaViewBag) {
            viewBag = (MangaViewBag) tag;
        } else {
            viewBag = new MangaViewBag();
            TextView titleView = (TextView) view.findViewById(R.id.manga_title);
            ImageView coverView = (ImageView) view.findViewById(R.id.manga_cover);
            ImageButton popupButton = (ImageButton) view.findViewById(R.id.popup_button);
            viewBag.titleView = titleView;
            viewBag.coverView = coverView;
            viewBag.popupButton = popupButton;
            view.setTag(viewBag);
        }
        viewBag.titleView.setText(manga.getTitle());
        if (popupButtonClickListener != null) {
            viewBag.popupButton.setOnClickListener(v -> popupButtonClickListener.onPopupButtonClick(viewBag.popupButton, position));
        }
        if (manga.getCoverUri() != null) {
            //TODO: временный хак! Потом заблочить добавление в избранное если нет картинки (или придумать что-то ещё)
            Uri coverUri = Uri.parse(manga.getCoverUri());
            HttpImageManager.LoadRequest request = HttpImageManager.LoadRequest.obtain(coverUri, viewBag.coverView, manga.getRepository().getEngine().getRequestPreprocessor(), sizeOfImage);
            Bitmap bitmap = httpImageManager.loadImage(request);
            if (bitmap != null) {
                viewBag.coverView.setImageBitmap(bitmap);
            }
        }
        return view;
    }

    public class MangaViewBag {
        protected TextView titleView;
        protected ImageView coverView;
        protected ImageButton popupButton;
        //TODO: add everything else
    }

}