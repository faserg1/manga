package com.danilov.manga.core.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.httpimage.HttpImageManager;
import com.danilov.manga.R;
import com.danilov.manga.core.model.Manga;
import com.danilov.manga.core.util.ServiceContainer;

import java.util.List;

/**
 * Created by Semyon Danilov on 18.05.2014.
 */
public class MangaListAdapter extends ArrayAdapter<Manga> {

    private final String TAG = "MangaListAdapter";

    private final int resourceId;
    private int sizeOfImage;

    public MangaListAdapter(final Context context, final int resource, final List<Manga> objects) {
        super(context, resource, objects);
        resourceId = resource;
        sizeOfImage = context.getResources().getDimensionPixelSize(R.dimen.manga_list_image_height);
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
        MangaViewBag viewBag;

        Log.d(TAG, "Position = " + position);
        Log.d(TAG, "Has convertView = " + (convertView != null));

        if (tag != null && tag instanceof MangaViewBag) {
            viewBag = (MangaViewBag) tag;
        } else {
            viewBag = new MangaViewBag();
            TextView titleView = (TextView) view.findViewById(R.id.manga_title);
            ImageView coverView = (ImageView) view.findViewById(R.id.manga_cover);
            viewBag.titleView = titleView;
            viewBag.coverView = coverView;
            view.setTag(viewBag);
        }
        viewBag.titleView.setText(manga.getTitle());
        HttpImageManager httpImageManager = ServiceContainer.getService(HttpImageManager.class);
        Uri coverUri = Uri.parse(manga.getCoverUri());
        Bitmap bitmap = httpImageManager.loadImage(new HttpImageManager.LoadRequest(coverUri, viewBag.coverView, sizeOfImage));
        if (bitmap != null) {
            viewBag.coverView.setImageBitmap(bitmap);
        }
        return view;
    }

    public class MangaViewBag {
        protected TextView titleView;
        protected ImageView coverView;
        //TODO: add everything else
    }

}
