package com.danilov.supermanga;

import android.support.annotation.NonNull;

import com.danilov.supermanga.core.adapter.DownloadedMangaAdapter;
import com.danilov.supermanga.core.adapter.MangaListAdapter;
import com.danilov.supermanga.core.application.MangaApplication;
import com.danilov.supermanga.core.module.CacheModule;
import com.danilov.supermanga.core.module.NetworkModule;
import com.danilov.supermanga.core.repository.AdultmangaEngine;
import com.danilov.supermanga.core.repository.AllHentaiEngine;
import com.danilov.supermanga.core.repository.HentaichanEngine;
import com.danilov.supermanga.core.repository.MangaReaderNetEngine;
import com.danilov.supermanga.core.repository.MangachanEngine;
import com.danilov.supermanga.core.repository.ReadmangaEngine;
import com.danilov.supermanga.core.service.MangaDownloadService;
import com.danilov.supermanga.core.view.MangaViewPager;
import com.danilov.supermanga.fragment.FavoritesFragment;
import com.danilov.supermanga.fragment.HistoryMangaFragment;
import com.danilov.supermanga.fragment.InfoFragment;
import com.danilov.supermanga.fragment.TrackingFragment;
import com.danilov.supermanga.fragment.WorldArtFragment;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by Semyon on 18.03.2016.
 */
@Singleton
@Component(modules = {
        ApplicationModule.class,
        CacheModule.class,
        NetworkModule.class
})
//Это штука, которая умеет инжектить зависимости
public interface ApplicationComponent {

    void inject(@NonNull MangaApplication mangaApplication);

    void inject(@NonNull DownloadedMangaAdapter downloadedMangaAdapter);

    void inject(@NonNull MangaListAdapter mangaListAdapter);

    void inject(@NonNull AdultmangaEngine adultmangaEngine);

    void inject(@NonNull ReadmangaEngine readmangaEngine);

    void inject(@NonNull AllHentaiEngine allHentaiEngine);

    void inject(@NonNull MangachanEngine mangachanEngine);

    void inject(@NonNull MangaReaderNetEngine mangaReaderNetEngine);

    void inject(@NonNull MangaDownloadService mangaDownloadService);

    void inject(@NonNull MangaViewPager mangaViewPager);

    void inject(@NonNull FavoritesFragment favoritesFragment);

    void inject(@NonNull WorldArtFragment worldArtFragment);

    void inject(@NonNull HistoryMangaFragment historyMangaFragment);

    void inject(@NonNull InfoFragment infoFragment);

    void inject(@NonNull TrackingFragment trackingFragment);

    void inject(@NonNull WorldArtFragment.ImagesFragment imagesFragment);

    void inject(@NonNull HentaichanEngine hentaichanEngine);
}