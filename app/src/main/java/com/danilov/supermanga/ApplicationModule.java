package com.danilov.supermanga;

import android.support.annotation.NonNull;

import com.danilov.supermanga.core.application.MangaApplication;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Semyon on 18.03.2016.
 */
@Module
public class ApplicationModule {

    @NonNull
    private final MangaApplication application;

    public ApplicationModule(@NonNull final MangaApplication application) {
        this.application = application;
    }

    @Provides
    @NonNull
    @Singleton
    public MangaApplication provideMangaApplication() {
        return application;
    }

}
