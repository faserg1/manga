package com.danilov.mangareaderplus.core.view;

import android.view.animation.Animation;

/**
 * Created by Semyon Danilov on 21.06.2014.
 */
public class InAndOutAnim {

    private Animation in;
    private Animation out;

    public InAndOutAnim(final Animation in, final Animation out) {
        this.in = in;
        this.out = out;
    }

    public Animation getIn() {
        return in;
    }

    public void setIn(final Animation in) {
        this.in = in;
    }

    public Animation getOut() {
        return out;
    }

    public void setOut(final Animation out) {
        this.out = out;
    }

    public void setDuration(final int duration) {
        in.setDuration(duration);
        out.setDuration(duration);
    }

}
