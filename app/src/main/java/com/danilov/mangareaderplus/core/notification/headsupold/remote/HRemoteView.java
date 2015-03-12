package com.danilov.mangareaderplus.core.notification.headsupold.remote;

/**
 * Created by semyon on 17.12.14.
 */
public abstract class HRemoteView<T> {

    protected int viewId;

    protected T view;

    public HRemoteView(int viewId) {
        this.viewId = viewId;
    }

    public int getViewId() {
        return viewId;
    }

    void hiddenApply(final T view) {
        this.view = view;
        apply(view);
    }

    public abstract void apply(final T view);

}
