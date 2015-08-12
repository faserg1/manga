package com.danilov.supermanga.core.view;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by Semyon on 05.08.2015.
 */
public class CompatPager extends RTLSupportPager {


    public CompatPager(final Context context) {
        super(context);
    }

    public CompatPager(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnPageChangeListener(final CompatPager.OnPageChangeListener listener) {
        super.setOnPageChangeListener(listener);
    }

    public interface OnPageChangeListener extends RTLSupportPager.OnPageChangeListener {}

    public static class SimpleOnPageChangeListener implements OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            // This space for rent
        }

        @Override
        public void onPageSelected(int position) {
            // This space for rent
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            // This space for rent
        }
    }

}
