package com.danilov.mangareader.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SearchView;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import com.danilov.mangareader.R;
import com.danilov.mangareader.core.adapter.FilterQueryAdapter;
import com.danilov.mangareader.core.adapter.MangaListAdapter;
import com.danilov.mangareader.core.adapter.PopupButtonClickListener;
import com.danilov.mangareader.core.model.Manga;
import com.danilov.mangareader.core.model.MangaSuggestion;
import com.danilov.mangareader.core.repository.RepositoryEngine;
import com.danilov.mangareader.core.repository.RepositoryException;
import com.danilov.mangareader.core.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Semyon Danilov on 26.07.2014.
 */
public class MangaQueryActivity extends BaseToolbarActivity implements View.OnClickListener,
                                                            AdapterView.OnItemClickListener,
        PopupButtonClickListener {

    private static final String FOUND_MANGA_KEY = "FOUND_MANGA_KEY";

    public static final String CURSOR_ID = BaseColumns._ID;
    public static final String CURSOR_NAME = SearchManager.SUGGEST_COLUMN_TEXT_1;
    public static final String CURSOR_LINK = "LINK";

    public static final String[] COLUMNS = {CURSOR_ID, CURSOR_NAME, CURSOR_LINK};

    private GridView searchResultsView;

    private GridView filters;

    private SearchView searchView;

    private MangaListAdapter adapter = null;

    private FilterQueryAdapter filterQueryAdapter = null;

    private List<Manga> foundManga = null;

    private RepositoryEngine.Repository repository;

    private RepositoryEngine engine = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            String repositoryString = getIntent().getStringExtra(Constants.REPOSITORY_KEY);
            repository = RepositoryEngine.Repository.valueOf(repositoryString);
            engine = repository.getEngine();
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
        setContentView(R.layout.manga_query_activity);

        searchResultsView = (GridView) findViewById(R.id.search_results);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(repository.getName());
        actionBar.setDisplayHomeAsUpEnabled(true);

        filters = (GridView) findViewById(R.id.filters);
        Integer numCols = Integer.valueOf(filters.getTag().toString());
        filterQueryAdapter = new FilterQueryAdapter(getApplicationContext(), numCols, engine.getFilters());
        filters.setAdapter(filterQueryAdapter);
    }

    @Override
    public void onClick(final View v) {

    }

    @Override
    public void onPopupButtonClick(final View popupButton, final int listPosition) {
        final CustomPopup popup = new CustomPopup(this, popupButton, listPosition);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.queried_manga_item_menu, popup.getMenu());
        final Manga manga = foundManga.get(listPosition);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(final MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.download:
                        Intent intent = new Intent(MangaQueryActivity.this, DownloadsActivity.class);
                        intent.putExtra(Constants.MANGA_PARCEL_KEY, manga);
                        startActivity(intent);
                        return true;
                    case R.id.add_to_favorites:
                        return true;
                }
                return false;
            }

        });
        popup.show();
    }

    private class CustomPopup extends PopupMenu {

        private int position;

        public CustomPopup(final Context context, final View anchor, final int position) {
            super(context, anchor);
            this.position = position;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(final int position) {
            this.position = position;
        }
    }

    private class QueryTask extends AsyncTask<String, Void, List<Manga>> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected List<Manga> doInBackground(final String... params) {
            if (params == null || params.length < 1) {
                return null;
            }
            return engine.queryRepository(params[0], filterQueryAdapter.getFilterValues());
        }

        @Override
        protected void onPostExecute(final List<Manga> foundManga) {
            if (foundManga == null) {
                return;
            }
            showFoundMangaList(foundManga);
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
                adapter = new MangaSuggestionsAdapter(MangaQueryActivity.this, cursor);
                searchView.setSuggestionsAdapter(adapter);
            } else {
                adapter.changeCursor(cursor);
            }
            adapter.setSuggestions(mangaSuggestions);
            searchView.setQueryRefinementEnabled(true);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void showFoundMangaList(final List<Manga> manga) {
        this.foundManga = manga;
        if (this.foundManga == null) {
            return;
        }
        adapter = new MangaListAdapter(this, R.layout.manga_list_item, foundManga, this);
        searchResultsView.setAdapter(adapter);
        searchResultsView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(final AdapterView<?> adapterView, final View view, final int i, final long l) {
        Manga manga = adapter.getItem(i);
        Intent intent = new Intent(this, MangaInfoActivity.class);
        intent.putExtra(Constants.MANGA_PARCEL_KEY, manga);
        startActivity(intent);
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        foundManga = savedInstanceState.getParcelableArrayList(FOUND_MANGA_KEY);
        showFoundMangaList(foundManga);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        ArrayList<Manga> mangas = null;
        if (foundManga != null) {
            if (!(foundManga instanceof ArrayList)) {
                mangas = new ArrayList<Manga>(foundManga.size());
                mangas.addAll(foundManga);
            } else {
                mangas = (ArrayList<Manga>) foundManga;
            }
            outState.putParcelableArrayList(FOUND_MANGA_KEY, mangas);
        }
        String repositoryString = repository.toString();
        outState.putString(Constants.REPOSITORY_KEY, repositoryString);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.manga_search_menu, menu);
        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search));
        searchView.setSubmitButtonEnabled(true);
        searchView.setIconifiedByDefault(true);
        searchView.setFocusable(false);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            private long lastSuggestionUpdateTime = 0;
            private SuggestionsTask suggestionsTask = null;

            private int DELAY = 300;

            @Override
            public boolean onQueryTextSubmit(final String query) {
                filters.setVisibility(View.INVISIBLE);
                QueryTask task = new QueryTask();
                task.execute(query);
                closeKeyboard();
                MangaSuggestionsAdapter adapter = (MangaSuggestionsAdapter) searchView.getSuggestionsAdapter();
                if (adapter == null) {
                    adapter = new MangaSuggestionsAdapter(MangaQueryActivity.this, new MatrixCursor(COLUMNS));
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
        searchView.setSuggestionsAdapter(new MangaSuggestionsAdapter(this, matrixCursor));
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
                Intent intent = new Intent(MangaQueryActivity.this, MangaInfoActivity.class);
                intent.putExtra(Constants.MANGA_PARCEL_KEY, manga);
                startActivity(intent);
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
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

    private void closeKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(findViewById(android.R.id.content).getWindowToken(), 0);
    }

}