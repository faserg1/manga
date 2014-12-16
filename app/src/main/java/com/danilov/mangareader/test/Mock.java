package com.danilov.mangareader.test;

import android.os.Environment;
import com.danilov.mangareader.core.model.Manga;
import com.danilov.mangareader.core.model.MangaChapter;
import com.danilov.mangareader.core.repository.RepositoryEngine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Semyon Danilov on 13.06.2014.
 */
public class Mock {

    public static Manga getMockManga() {
        return new Manga("Midori no Hibi", "/midori_s_days", RepositoryEngine.Repository.READMANGA);
    }

    public static Manga getOfflineMockManga() {
        File sdPath = Environment.getExternalStorageDirectory();
        String s = sdPath.getPath() + "/manga/download/fairytail";
        Manga manga = new Manga("Fairy tail", s, RepositoryEngine.Repository.OFFLINE);
        MangaChapter c1 = new MangaChapter("Uno", 0, s + "/0");
        MangaChapter c2 = new MangaChapter("Dos", 1, s + "/1");
        List<MangaChapter> chapters = new ArrayList<MangaChapter>();
        chapters.add(c1);
        chapters.add(c2);
        manga.setChapters(chapters);
        return manga;
    }

}
