package com.danilov.manga.core.repository;

import com.danilov.manga.R;
import com.danilov.manga.core.model.Manga;
import com.danilov.manga.core.model.MangaChapter;
import com.danilov.manga.core.model.MangaSuggestion;

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
     * @return suggestions
     */
    List<MangaSuggestion> getSuggestions(final String query) throws RepositoryException;

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
    boolean queryForMangaDescription(final Manga manga) throws RepositoryException;

    boolean queryForChapters(final Manga manga) throws RepositoryException;

    List<String> getChapterImages(final MangaChapter chapter) throws RepositoryException;

    String getBaseSearchUri();

    String getBaseUri();

    //enum of names containing matched engines
    public enum Repository {

        READMANGA(new ReadmangaEngine(), "ReadManga", R.drawable.ic_readmanga, R.drawable.ic_russia),
        ADULTMANGA(new AdultmangaEngine(), "AdultManga", R.drawable.ic_adultmanga, R.drawable.ic_russia),
        OFFLINE(new OfflineEngine(), "", 0, -1);

        private static Repository[] withoutOffline = {READMANGA, ADULTMANGA};

        private RepositoryEngine engine;
        private String name;
        private int iconId;
        private int countryIconId;

        Repository(final RepositoryEngine engine, final String name, final int iconId, final int countryIconId) {
            this.engine = engine;
            this.name = name;
            this.iconId = iconId;
            this.countryIconId = countryIconId;
        }

        public int getCountryIconId() {
            return countryIconId;
        }

        public String getName() {
            return name;
        }

        public int getIconId() {
            return iconId;
        }

        public static Repository[] getWithoutOffline() {
            return withoutOffline;
        }

        public RepositoryEngine getEngine() {
            return engine;
        }

    }

}
