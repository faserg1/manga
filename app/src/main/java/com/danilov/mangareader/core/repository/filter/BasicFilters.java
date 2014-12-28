package com.danilov.mangareader.core.repository.filter;

import com.danilov.mangareader.core.repository.RepositoryEngine;
import com.danilov.mangareader.core.widget.TriStateCheckbox;

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

    }

}
