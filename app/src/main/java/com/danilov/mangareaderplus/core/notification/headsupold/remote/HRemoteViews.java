package com.danilov.mangareaderplus.core.notification.headsupold.remote;

import android.view.LayoutInflater;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by semyon on 17.12.14.
 */
public class HRemoteViews {

    private int layout;

    private List<HRemoteView> views = new ArrayList<>();

    private View mainView;

    public HRemoteViews(final int layout) {
        this.layout = layout;
    }

    public void addView(final HRemoteView remoteView) {
        views.add(remoteView);
    }

    public View apply(final LayoutInflater inflater) {
        mainView = inflater.inflate(layout, null);

        for (HRemoteView view : views) {
            int viewId = view.getViewId();
            View v = mainView.findViewById(viewId);
            view.hiddenApply(v);
        }

        return mainView;
    }

}
