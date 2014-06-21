package com.danilov.manga.core.repository;

import com.danilov.manga.core.model.Manga;
import com.danilov.manga.core.model.MangaChapter;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Semyon Danilov on 21.06.2014.
 */
public class OfflineEngine implements RepositoryEngine {

    @Override
    public String getLanguage() {
        return null;
    }

    @Override
    public JSONObject getSuggestions(final String query) {
        return null;
    }

    @Override
    public List<Manga> queryRepository(final String query) {
        return null;
    }

    @Override
    public boolean queryForMangaDescription(final Manga manga) throws RepositoryException {
        return false;
    }

    @Override
    public boolean queryForChapters(final Manga manga) throws RepositoryException {
        return false;
    }

    @Override
    public List<String> getChapterImages(final MangaChapter chapter) throws RepositoryException {
        return null;
    }

    @Override
    public String getBaseSearchUri() {
        return null;
    }

    @Override
    public String getBaseUri() {
        return null;
    }

}
