package com.danilov.manga.core.view.library;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by Semyon Danilov on 02.08.2014.
 */
public class LayoutSuppressingImageView extends ImageView {

    /**
     * @param context The {@link android.content.Context} to use
     * @param attrs The attributes of the XML tag that is inflating the view
     */
    public LayoutSuppressingImageView(final Context context, final AttributeSet attrs) {
        super(context, attrs);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void requestLayout() {
        forceLayout();
    }

}