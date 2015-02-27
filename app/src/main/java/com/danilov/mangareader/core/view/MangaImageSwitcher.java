package com.danilov.mangareader.core.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.widget.ImageSwitcher;

/**
 * Created by Semyon Danilov on 21.06.2014.
 */
public class MangaImageSwitcher extends ImageSwitcher {

    private InAndOutAnim inAndOutAnim;

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
            if (state != null) {
                state.setCenter(10000, 0);
            }
            inAndOutAnim.getOut().setAnimationListener(new OutAnimationListener(prevImage));
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
            if (state != null) {
                state.setCenter(0, 10000);
            }
            inAndOutAnim.getOut().setAnimationListener(new OutAnimationListener(prevImage));
        }
        image.setImageFile(filePath, state);
        showPrevious();
    }

    public void setInAndOutAnim(final InAndOutAnim inAndOutAnim) {
        this.inAndOutAnim = inAndOutAnim;
        setInAnimation(inAndOutAnim.getIn());
        setOutAnimation(inAndOutAnim.getOut());
    }

    private class OutAnimationListener implements Animation.AnimationListener {

        private SubsamplingScaleImageView view;

        public OutAnimationListener(final SubsamplingScaleImageView view) {
            this.view = view;
        }

        @Override
        public void onAnimationStart(final Animation animation) {

        }

        @Override
        public void onAnimationEnd(final Animation animation) {
            view.reset();
        }

        @Override
        public void onAnimationRepeat(final Animation animation) {

        }

    }

}
