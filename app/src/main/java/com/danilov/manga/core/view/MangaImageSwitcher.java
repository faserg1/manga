package com.danilov.manga.core.view;

import android.content.Context;
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

    public void setNextImageDrawable(final String filePath) {
        SubsamplingScaleImageView prevImage = (SubsamplingScaleImageView) getCurrentView();
        SubsamplingScaleImageView image = (SubsamplingScaleImageView) this.getNextView();
        ImageViewState state = null;
        if (prevImage != null) {
            state = prevImage.getState();
        }
        image.setImageFile(filePath, state);
        showNext();
    }

    public void setPreviousImageDrawable(final String filePath) {
        SubsamplingScaleImageView prevImage = (SubsamplingScaleImageView) getCurrentView();
        SubsamplingScaleImageView image = (SubsamplingScaleImageView) this.getNextView();
        ImageViewState state = null;
        if (prevImage != null) {
            state = prevImage.getState();
        }
        image.setImageFile(filePath, state);
        showPrevious();
    }

    public void setInAndOutAnim(final InAndOutAnim inAndOutAnim) {
        setInAnimation(inAndOutAnim.getIn());
        setOutAnimation(inAndOutAnim.getOut());
    }

}
