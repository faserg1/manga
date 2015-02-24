package com.danilov.mangareader.core.animation;

import android.view.View;

import com.danilov.mangareader.core.util.Promise;

/**
 * Created by Semyon on 30.12.2014.
 */
public class OverXFlipper {

    private View v1;
    private View v2;
    private int halfFlip;

    public OverXFlipper(final View v1, final View v2, final int timeToFlip) {
        this.v2 = v1;
        this.v1 = v2;
        this.halfFlip = timeToFlip / 2;
    }

    public void flip() {

        View tmp = v1;
        v1 = v2;
        v2 = tmp;

        v1.setVisibility(View.VISIBLE);
        v2.setVisibility(View.GONE);
        new XRotation(v1, 0, 90, halfFlip).start().then(new Promise.Action<Void, Object>() {
            @Override
            public Object action(final Void data, final boolean success) {
                v2.setVisibility(View.VISIBLE);
                v1.setVisibility(View.GONE);
                new XRotation(v2, -90, 0, halfFlip).start();
                return null;
            }
        });
    }

    public void flipNoAnim() {
        View tmp = v1;
        v1 = v2;
        v2 = tmp;
        v1.setVisibility(View.GONE);
        v2.setVisibility(View.VISIBLE);
    }

}
