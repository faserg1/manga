package com.danilov.manga.test;

import com.danilov.manga.core.model.Manga;
import com.danilov.manga.core.repository.RepositoryEngine;

/**
 * Created by Semyon Danilov on 13.06.2014.
 */
public class Mock {

    public static Manga getMockManga() {
        return new Manga("Midori no Hibi", "/midori_s_days", RepositoryEngine.Repository.READMANGA);
    }

}
