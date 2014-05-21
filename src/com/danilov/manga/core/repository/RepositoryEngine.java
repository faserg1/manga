package com.danilov.manga.core.repository;

import com.danilov.manga.core.model.Manga;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Semyon Danilov on 17.05.2014.
 */
public interface RepositoryEngine {

    /**
     * Language of repository (e.g. Engurish, Russiano desu)
     * @return
     */
    String getLanguage();

    /**
     * Search for suggestions
     * @param query user input
     * @return json of suggestions
     */
    JSONObject getSuggestions(final String query);

    /**
     *
     * @param query user input
     * @return list of mangas matching query
     */
    List<Manga> queryRepository(final String query);

    /**
     * Getting info about manga (description and chapters)
     * @param manga
     * @return must return true if query was successful
     */
    boolean queryForMangaInfo(final Manga manga);

    String getBaseSearchUri();

}
