package com.danilov.mangareaderplus.core.service;

import android.app.Service;

/**
 * Created by Semyon on 28.06.2015.
 */
public interface ServiceConnectionListener<T extends Service> {

    void onServiceConnected(final T service);

    void onServiceDisconnected(final T service);

}
