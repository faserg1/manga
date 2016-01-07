package com.danilov.supermanga.core.repository;

import android.support.annotation.NonNull;

import com.danilov.supermanga.core.http.RequestPreprocessor;
import com.danilov.supermanga.core.model.Manga;
import com.danilov.supermanga.core.model.MangaChapter;
import com.danilov.supermanga.core.model.MangaSuggestion;
import com.danilov.supermanga.core.repository.special.AuthorizableEngine;

import java.util.Collections;
import java.util.List;

/**
 * Created by Semyon on 07.01.2016.
 */
public class MangachanEngine extends AuthorizableEngine {

    @Override
    public String getLanguage() {
        return "Русский";
    }

    @Override
    public List<MangaSuggestion> getSuggestions(final String query) throws RepositoryException {
        return null;
    }

    @Override
    public List<Manga> queryRepository(final String query, final List<Filter.FilterValue> filterValues) throws RepositoryException {
        return null;
    }

    @Override
    public List<Manga> queryRepository(final Genre genre) throws RepositoryException {
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

    @NonNull
    @Override
    public List<FilterGroup> getFilters() {
        return Collections.emptyList();
    }

    @NonNull
    @Override
    public List<Genre> getGenres() {
        return Collections.emptyList();
    }

    @Override
    public RequestPreprocessor getRequestPreprocessor() {
        return null;
    }

    @Override
    public String getLogin() {
        return null;
    }

    @Override
    public boolean isAuthorized() {
        return false;
    }

    @Override
    public String authPageUrl() {
        return "http://mangachan.ru/";
    }

    @Override
    public String authSuccessUrl() {
        return null;
    }

    @Override
    public String authSuccessMethod() {
        return null;
    }

}