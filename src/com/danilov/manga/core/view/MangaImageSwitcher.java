package com.danilov.manga.core.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageSwitcher;

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
        TouchImageView prevImage = (TouchImageView)getCurrentView();
        TouchImageView image = (TouchImageView)this.getNextView();
        image.setImageDrawable(drawable);
        showNext();
    }

    public void setPreviousImageDrawable(final Drawable drawable) {
        TouchImageView prevImage = (TouchImageView)getCurrentView();
        TouchImageView image = (TouchImageView) this.getNextView();
        image.setImageDrawable(drawable);
        showPrevious();
    }

    public void setInAndOutAnim(final InAndOutAnim inAndOutAnim) {
        setInAnimation(inAndOutAnim.getIn());
        setOutAnimation(inAndOutAnim.getOut());
    }

}
