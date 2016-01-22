package com.danilov.supermanga.fragment;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.danilov.supermanga.R;
import com.danilov.supermanga.activity.MangaInfoActivity;
import com.danilov.supermanga.activity.MangaQueryActivity;
import com.danilov.supermanga.core.adapter.FilterQueryAdapter;
import com.danilov.supermanga.core.application.MangaApplication;
import com.danilov.supermanga.core.model.Manga;
import com.danilov.supermanga.core.model.MangaSuggestion;
import com.danilov.supermanga.core.repository.RepositoryEngine;
import com.danilov.supermanga.core.repository.RepositoryException;
import com.danilov.supermanga.core.repository.special.test.JSTestEngine;
import com.danilov.supermanga.core.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Semyon on 01.03.2015.
 */
public class FiltersFragment extends BaseFragment implements Toolbar.OnMenuItemClickListener {

    public static final String CURSOR_ID = BaseColumns._ID;
    public static final String CURSOR_NAME = SearchManager.SUGGEST_COLUMN_TEXT_1;
    public static final String CURSOR_LINK = "LINK";

    public static final String[] COLUMNS = {CURSOR_ID, CURSOR_NAME, CURSOR_LINK};

    private Toolbar toolbar;

    private GridView filters;

    private SearchView searchView;

    private RepositoryEngine.Repository repository;

    private RepositoryEngine engine = null;

    private FilterQueryAdapter filterQueryAdapter = null;

    private MangaQueryActivity queryActivity;

    private Context context = null;

    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.filters_fragment, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            String repositoryString = getArguments().getString(Constants.REPOSITORY_KEY);
            repository = RepositoryEngine.Repository.valueOf(repositoryString);
            engine = repository.getEngine();
            /*TODO: remove*/ engine = new JSTestEngine(getActivity(), "", "");
        } else {
            //why I do not use getString with default value? Because it's API 12 :(
            String repositoryString = savedInstanceState.getString(Constants.REPOSITORY_KEY);
            if (repositoryString == null) {
                repository = RepositoryEngine.Repository.READMANGA;
            } else {
                repository = RepositoryEngine.Repository.valueOf(repositoryString);
            }
            engine = repository.getEngine();
        }

        queryActivity = (MangaQueryActivity) getActivity();
        context = queryActivity.getApplicationContext();
        filters = findViewById(R.id.filters);
        toolbar = findViewById(R.id.toolbar);
        Integer numCols = Integer.valueOf(filters.getTag().toString());
        filterQueryAdapter = new FilterQueryAdapter(context, numCols, engine.getFilters());
        filters.setAdapter(filterQueryAdapter);
        setupToolbar();
        super.onActivityCreated(savedInstanceState);
    }

    private void setupToolbar() {
        toolbar.inflateMenu(R.menu.manga_search_menu);
        final Menu menu = toolbar.getMenu();
        toolbar.setOnMenuItemClickListener(this);
        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search));
        searchView.setSubmitButtonEnabled(true);
        searchView.setIconifiedByDefault(false);
        searchView.setFocusable(false);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            private long lastSuggestionUpdateTime = 0;
            private SuggestionsTask suggestionsTask = null;

            private int DELAY = 300;

            @Override
            public boolean onQueryTextSubmit(final String query) {
                QueryTask task = new QueryTask();
                task.execute(query);
                queryActivity.closeKeyboard();
                queryActivity.hideViewPager();
                MangaSuggestionsAdapter adapter = (MangaSuggestionsAdapter) searchView.getSuggestionsAdapter();
                if (adapter == null) {
                    adapter = new MangaSuggestionsAdapter(context, new MatrixCursor(COLUMNS));
                    searchView.setSuggestionsAdapter(adapter);
                } else {
                    adapter.changeCursor(new MatrixCursor(COLUMNS));
                }
                adapter.setSuggestions(new ArrayList<MangaSuggestion>());
                return true;
            }

            @Override
            public boolean onQueryTextChange(final String query) {
                long curTime = System.currentTimeMillis();
                if (curTime - lastSuggestionUpdateTime < DELAY) {
                    return false;
                }
                lastSuggestionUpdateTime = curTime;
                if (suggestionsTask != null) {
                    suggestionsTask.cancel(true);
                }
                suggestionsTask = new SuggestionsTask();
                suggestionsTask.execute(query);
                return true;
            }

        });
        searchView.setQueryRefinementEnabled(true);
        MatrixCursor matrixCursor = new MatrixCursor(COLUMNS);
        searchView.setSuggestionsAdapter(new MangaSuggestionsAdapter(context, matrixCursor));
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {

            @Override
            public boolean onSuggestionSelect(final int i) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(final int i) {
                MangaSuggestionsAdapter adapter = (MangaSuggestionsAdapter) searchView.getSuggestionsAdapter();
                MangaSuggestion suggestion = adapter.getSuggestions().get(i);
                Manga manga = new Manga(suggestion.getTitle(), suggestion.getUrl(), suggestion.getRepository());
                Intent intent = new Intent(context, MangaInfoActivity.class);
                intent.putExtra(Constants.MANGA_PARCEL_KEY, manga);
                startActivity(intent);
                return true;
            }
        });
    }

    @Override
    public boolean onMenuItemClick(final MenuItem menuItem) {
        return false;
    }

    private class QueryTask extends AsyncTask<String, Void, List<Manga>> {

        private String error = null;

        @Override
        protected void onPreExecute() {
            queryActivity.showProgressBar();
        }

        @Override
        protected List<Manga> doInBackground(final String... params) {
            if (params == null || params.length < 1) {
                return null;
            }
            try {
                return engine.queryRepository(params[0], filterQueryAdapter.getFilterValues());
            } catch (RepositoryException e) {
                error = e.getMessage();
                return null;
            }
        }

        @Override
        protected void onPostExecute(final List<Manga> foundManga) {
            queryActivity.hideProgressBar();
            if (foundManga == null) {
                final Context context = MangaApplication.getContext();
                Toast.makeText(context, context.getString(R.string.p_internet_error) + ": " + error, Toast.LENGTH_SHORT).show();
                return;
            }
            queryActivity.showFoundMangaList(foundManga);
        }

    }

    private class SuggestionsTask extends AsyncTask<String, Void, List<MangaSuggestion>> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected List<MangaSuggestion> doInBackground(final String... params) {
            try {
                return engine.getSuggestions(params[0]);
            } catch (RepositoryException e) {
                //can't load suggestions, nevermind
            }
            return null;
        }

        @Override
        protected void onPostExecute(final List<MangaSuggestion> mangaSuggestions) {
            if (mangaSuggestions == null) {
                return;
            }
            MatrixCursor cursor = new MatrixCursor(COLUMNS);
            int idx = 0;
            for (MangaSuggestion suggestion : mangaSuggestions) {
                String[] row = new String[3];
                row[0] = String.valueOf(idx);
                row[1] = suggestion.getTitle();
                row[2] = suggestion.getUrl();
                cursor.addRow(row);
                idx++;
            }
            MangaSuggestionsAdapter adapter = (MangaSuggestionsAdapter) searchView.getSuggestionsAdapter();
            if (adapter == null) {
                adapter = new MangaSuggestionsAdapter(context, cursor);
                searchView.setSuggestionsAdapter(adapter);
            } else {
                adapter.changeCursor(cursor);
            }
            adapter.setSuggestions(mangaSuggestions);
            searchView.setQueryRefinementEnabled(true);
        }

    }

    private class MangaSuggestionsAdapter extends CursorAdapter {

        private List<MangaSuggestion> suggestions = null;

        public MangaSuggestionsAdapter(final Context context, final Cursor c) {
            super(context, c, 0);
        }

        public List<MangaSuggestion> getSuggestions() {
            return suggestions;
        }

        public void setSuggestions(final List<MangaSuggestion> suggestions) {
            this.suggestions = suggestions;
        }

        @Override
        public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(context);
            return inflater.inflate(R.layout.suggestions_list_item, parent, false);
        }

        @Override
        public void bindView(final View view, final Context context, final Cursor cursor) {
            TextView tv = (TextView) view.findViewById(R.id.text1);
            int textIndex = cursor.getColumnIndex(CURSOR_NAME);
            final String value = cursor.getString(textIndex);
            ImageButton btn = (ImageButton) view.findViewById(R.id.suggestion_merge);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    searchView.setQuery(value, false);
                }
            });
            tv.setText(value);
        }

    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        String repositoryString = repository.toString();
        outState.putString(Constants.REPOSITORY_KEY, repositoryString);
        super.onSaveInstanceState(outState);
    }

}
