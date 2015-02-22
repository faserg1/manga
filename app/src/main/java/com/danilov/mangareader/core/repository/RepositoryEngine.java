package com.danilov.mangareader.core.repository;

import com.danilov.mangareader.R;
import com.danilov.mangareader.core.model.Manga;
import com.danilov.mangareader.core.model.MangaChapter;
import com.danilov.mangareader.core.model.MangaSuggestion;

import java.util.ArrayList;
import java.util.Iterator;
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
    List<Manga> queryRepository(final String query, final List<Filter.FilterValue> filterValues);

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

    public List<FilterGroup> getFilters();

    //enum of names containing matched engines
    public enum Repository {

        READMANGA(new ReadmangaEngine(), "ReadManga", R.drawable.ic_readmanga, R.drawable.ic_russia),
        ADULTMANGA(new AdultmangaEngine(), "AdultManga", R.drawable.ic_adultmanga, R.drawable.ic_russia),
        MANGAREADERNET(new MangaReaderNetEngine(), "MangaReader", R.drawable.ic_mangareadernet, R.drawable.ic_english),
        OFFLINE(new OfflineEngine(), "", 0, -1);

        private static Repository[] withoutOffline = {READMANGA, ADULTMANGA, MANGAREADERNET};

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

    public class FilterGroup implements Iterable<Filter> {

        private List<Filter> filters;

        private String name;

        public FilterGroup(final String name, final int size) {
            this.name = name;
            filters = new ArrayList<>(size);
        }

        public int size() {
            return filters.size();
        }

        public String getName() {
            return name;
        }

        public void add(final Filter filter) {
            this.filters.add(filter);
        }

        public Filter get(final int i) {
            return filters.get(i);
        }

        @Override
        public Iterator<Filter> iterator() {
            return filters.iterator();
        }

    }

    public abstract class Filter<T> {

        private String name;

        public Filter(final String name) {
            this.name = name;
        }

        public abstract FilterType getType();

        public String getName() {
            return name;
        }

        public FilterValue newValue() {
            return new FilterValue();
        }

        public abstract String apply(final String uri, final FilterValue value);

        public abstract T getDefault();

        public enum FilterType {
            TRI_STATE,
            TWO_STATE;
        }

        public class FilterValue {

            private T value;

            public FilterValue() {
            }

            public void setValue(final T value) {
                this.value = value;
            }

            public T getValue() {
                return value;
            }

            public String apply(final String uri) {
                return Filter.this.apply(uri, this);
            }

        }

    }

}
