package com.danilov.supermanga.core.animation;

import android.view.View;

/**
 * Created by Semyon on 30.12.2014.
 */
public class OverXFlipper {

    private View v1;
    private View v2;

    private View initialV1;
    private View initialV2;

    private int halfFlip;

    public OverXFlipper(final View v1, final View v2, final int timeToFlip) {
        this.v2 = v1;
        this.v1 = v2;
        this.initialV1 = v2;
        this.initialV2 = v1;
        this.halfFlip = timeToFlip / 2;
    }

    public void flip() {
        View tmp = v1;
        v1 = v2;
        v2 = tmp;

        v1.setVisibility(View.VISIBLE);
        v2.setVisibility(View.GONE);
        new XRotation(v1, 0, 90, halfFlip).start(() -> {
            v2.setVisibility(View.VISIBLE);
            v1.setVisibility(View.GONE);
            new XRotation(v2, -90, 0, halfFlip).start(()->{});
        });
    }

    public void flip(final int v) {
        boolean shouldFlip = false;
        switch (v) {
            case 1:
                if (v1 == initialV2) {
                    shouldFlip = true;
                }
                break;
            case 2:
                if (v1 == initialV1) {
                    shouldFlip = true;
                }
                break;
        }
        if (shouldFlip) {
            flip();
        }
    }

    public void flipNoAnim() {
        View tmp = v1;
        v1 = v2;
        v2 = tmp;
        v1.setVisibility(View.GONE);
        v2.setVisibility(View.VISIBLE);
    }

}
