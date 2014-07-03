package com.danilov.manga.core.view;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.MenuItemCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import com.danilov.manga.R;

/**
 * Created by Semyon Danilov on 04.07.2014.
 */
public class AnimatedActionView {

    private MenuItem item = null;
    private ImageView actionView = null;
    private Animation animation = null;

    private int drawableId;

    private boolean isAnimating = false;

    public AnimatedActionView(final Activity activity, final Menu menu, final int viewId, final int drawableId, final int animationId) {
        item = menu.findItem(viewId);
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        actionView = (ImageView) inflater.inflate(R.layout.image_action_view, null);
        this.drawableId = drawableId;
        if (actionView == null) {
            throw new NullPointerException("Action View is null");
        }
        animation = AnimationUtils.loadAnimation(activity, animationId);
        if (animation == null) {
            throw new NullPointerException("Animation is null");
        }
        animation.setRepeatCount(Animation.INFINITE);
    }

    public void startAnimation() {
        actionView.setImageResource(drawableId);
        MenuItemCompat.setActionView(item, actionView);
        isAnimating = true;
        actionView.startAnimation(animation);
    }

    public void stopAnimation() {
        isAnimating = false;
        actionView.clearAnimation();
    }

    public boolean isAnimating() {
        return isAnimating;
    }


}
