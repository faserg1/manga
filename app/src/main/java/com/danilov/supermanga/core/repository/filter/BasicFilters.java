package com.danilov.supermanga.core.repository.filter;

import com.danilov.supermanga.core.repository.RepositoryEngine;

/**
 * Created by Semyon on 22.12.2014.
 */
public class BasicFilters {

    public static class RABaseTriState extends RepositoryEngine.Filter<Integer> {

        private String paramName;

        private static String[] vals = new String[]{"", "in", "ex"};

        public RABaseTriState(final String name, final String paramName) {
            super(name);
            this.paramName = paramName;
        }

        @Override
        public FilterType getType() {
            return FilterType.TRI_STATE;
        }

        @Override
        public String apply(final String uri, final FilterValue value) {
            Integer val = value.getValue();
            String strVal = vals[val];
            return uri + "&" + paramName + "=" + strVal;
        }

        @Override
        public Integer getDefault() {
            return 0;
        }

    }

    public static class MangaReaderTriState extends RepositoryEngine.Filter<Integer> {

        private static String[] vals = new String[]{"0", "1", "2"};

        public MangaReaderTriState(final String name) {
            super(name);
        }

        @Override
        public FilterType getType() {
            return FilterType.TRI_STATE;
        }

        @Override
        public String apply(final String uri, final FilterValue value) {
            Integer val = value.getValue();
            String strVal = vals[val];
            return uri + strVal;
        }

        @Override
        public Integer getDefault() {
            return 0;
        }

    }

}
