package com.danilov.manga.test;

import com.danilov.manga.core.model.Manga;
import com.danilov.manga.core.repository.RepositoryEngine;

/**
 * Created by Semyon Danilov on 13.06.2014.
 */
public class Mock {

    public static Manga getMockManga() {
        return new Manga("Naruto", "/naruto_dj___kakurega_wa_narusasu_desu", RepositoryEngine.Repository.READMANGA);
    }

}
