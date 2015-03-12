package com.danilov.mangareaderplus.test;

import android.content.Context;
import android.os.Environment;

import com.danilov.mangareaderplus.R;
import com.danilov.mangareaderplus.core.model.Manga;
import com.danilov.mangareaderplus.core.model.MangaChapter;
import com.danilov.mangareaderplus.core.model.UpdatesElement;
import com.danilov.mangareaderplus.core.repository.RepositoryEngine;

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



    public static UpdatesElement getMockUpdate(final Context context) {
        UpdatesElement updatesElement = new UpdatesElement();
        Manga manga = new Manga(context.getString(R.string.p_manga_new_chapters), "", RepositoryEngine.Repository.OFFLINE);
        updatesElement.setManga(manga);
        updatesElement.setDifference(25);
        return updatesElement;
    }

}
