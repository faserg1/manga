package com.danilov.manga.core.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageSwitcher;
import android.widget.ImageView;

/**
 * Created by Semyon Danilov on 21.06.2014.
 */
public class MangaImageSwitcher extends ImageSwitcher {


    public MangaImageSwitcher(final Context context) {
        super(context);
    }

    public MangaImageSwitcher(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public void setNextImageDrawable(final Drawable drawable) {
        super.setImageDrawable(drawable);
    }

    public void setPreviousImageDrawable(final Drawable drawable) {
        ImageView image = (ImageView) this.getNextView();
        image.setImageDrawable(drawable);
        showPrevious();
    }

}
