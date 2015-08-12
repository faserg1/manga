package com.danilov.supermanga.core.notification.headsupold.remote.impl;

import android.widget.TextView;

import com.danilov.supermanga.core.notification.headsupold.remote.HRemoteView;

/**
 * Created by semyon on 17.12.14.
 */
public class RemoteTextView extends HRemoteView<TextView> {

    private String text;

    public RemoteTextView(final int viewId) {
        super(viewId);
    }

    public void setText(final String text) {
        this.text = text;
        apply(view);
    }

    @Override
    public void apply(final TextView view) {
        if (view == null) {
            return;
        }
        view.setText(text);
    }



}
