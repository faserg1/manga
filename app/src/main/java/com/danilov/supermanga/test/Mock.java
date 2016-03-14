package com.danilov.supermanga.test;

import android.content.Context;
import android.os.Environment;
import android.util.Pair;

import com.danilov.supermanga.R;
import com.danilov.supermanga.core.model.Manga;
import com.danilov.supermanga.core.model.MangaChapter;
import com.danilov.supermanga.core.model.UpdatesElement;
import com.danilov.supermanga.core.repository.RepositoryEngine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Semyon Danilov on 13.06.2014.
 */
public class Mock {

    public static Manga getMockManga() {
        return new Manga("Midori no Hibi", "/midori_s_days", RepositoryEngine.DefaultRepository.READMANGA);
    }

    public static Manga getOfflineMockManga() {
        File sdPath = Environment.getExternalStorageDirectory();
        String s = sdPath.getPath() + "/manga/download/fairytail";
        Manga manga = new Manga("Fairy tail", s, RepositoryEngine.DefaultRepository.OFFLINE);
        MangaChapter c1 = new MangaChapter("Uno", 0, s + "/0");
        MangaChapter c2 = new MangaChapter("Dos", 1, s + "/1");
        List<MangaChapter> chapters = new ArrayList<>();
        chapters.add(c1);
        chapters.add(c2);
        manga.setChapters(chapters);
        return manga;
    }



    public static Pair<Manga, UpdatesElement> getMockUpdate(final Context context) {
        UpdatesElement updatesElement = new UpdatesElement();
        Manga manga = new Manga(context.getString(R.string.p_manga_new_chapters), "", RepositoryEngine.DefaultRepository.OFFLINE);
        updatesElement.setManga(manga);
        updatesElement.setDifference(25);
        return new Pair<>(manga, updatesElement);
    }

}
