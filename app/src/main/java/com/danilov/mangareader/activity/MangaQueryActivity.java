package com.danilov.mangareader.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.PopupMenu;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.danilov.mangareader.R;
import com.danilov.mangareader.core.adapter.MangaListAdapter;
import com.danilov.mangareader.core.adapter.PopupButtonClickListener;
import com.danilov.mangareader.core.model.Manga;
import com.danilov.mangareader.core.repository.RepositoryEngine;
import com.danilov.mangareader.core.util.Constants;
import com.danilov.mangareader.core.widget.SlidingTabLayout;
import com.danilov.mangareader.fragment.FiltersFragment;
import com.danilov.mangareader.fragment.GenresFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Semyon Danilov on 26.07.2014.
 */
public class MangaQueryActivity extends BaseToolbarActivity implements View.OnClickListener,
                                                            AdapterView.OnItemClickListener,
        PopupButtonClickListener {

    private static final String TAG = "MangaQueryActivity";

    private static final String FOUND_MANGA_KEY = "FOUND_MANGA_KEY";
    private GridView searchResultsView;


    private RepositoryEngine.Repository repository;

    private RepositoryEngine engine = null;

    private MangaListAdapter adapter = null;

    private List<Manga> foundManga = null;

    private ViewPager viewPager;

    private SlidingTabLayout slidingTabLayout;

    private MenuItem searchBtn;
    private MenuItem cancelBtn;
    private TextView repositoryTitleTextView;

    private ProgressBar progressBar;

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
        repositoryTitleTextView = findViewWithId(R.id.repository_title);
        progressBar = findViewWithId(R.id.progress_bar);
        hideProgressBar();
        ActionBar actionBar = getSupportActionBar();
        repositoryTitleTextView.setText(repository.getName());
        repositoryTitleTextView.setVisibility(View.GONE);
        actionBar.setDisplayHomeAsUpEnabled(true);
        setupTabs(actionBar);
    }

    private void setupTabs(final ActionBar actionBar) {
        viewPager = findViewWithId(R.id.viewPager);
        viewPager.setAdapter(new SamplePagerAdapter(getSupportFragmentManager()));
        slidingTabLayout = findViewWithId(R.id.sliding_tabs);
        slidingTabLayout.setViewPager(viewPager);
    }

    private boolean shouldHideSearch = true;

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.query_activity_menu, menu);
        searchBtn = menu.findItem(R.id.search);
        cancelBtn = menu.findItem(R.id.cancel);
        searchBtn.setVisible(!shouldHideSearch);
        cancelBtn.setVisible(shouldHideSearch);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                searchBtn.setVisible(false);
                cancelBtn.setVisible(true);
                repositoryTitleTextView.setVisibility(View.GONE);
                slidingTabLayout.setVisibility(View.VISIBLE);
                viewPager.setVisibility(View.VISIBLE);
                return true;
            case R.id.cancel:
                hideViewPager();
                return true;
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(final View v) {

    }

    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onPopupButtonClick(final View popupButton, final int listPosition) {
        //TODO: consider rewriting
        if (true) {
            return;
        }
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



    @Override
    protected void onResume() {
        super.onResume();
    }

    public void showFoundMangaList(final List<Manga> manga) {
        this.foundManga = manga;
        if (this.foundManga == null) {
            return;
        }
        hideViewPager();
        adapter = new MangaListAdapter(this, R.layout.manga_list_item, foundManga, this);
        searchResultsView.setAdapter(adapter);
        searchResultsView.setOnItemClickListener(this);
    }

    public void hideViewPager() {
        viewPager.setVisibility(View.INVISIBLE);
        slidingTabLayout.setVisibility(View.GONE);
        shouldHideSearch = false;
        if (searchBtn != null) {
            searchBtn.setVisible(true);
        }
        if (cancelBtn != null) {
            cancelBtn.setVisible(false);
        }
        repositoryTitleTextView.setVisibility(View.VISIBLE);
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

    public void closeKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(findViewById(android.R.id.content).getWindowToken(), 0);
    }


    private class SamplePagerAdapter extends FragmentPagerAdapter {

        private String[] titles = {getString(R.string.sv_filters), getString(R.string.sv_genres)};

        public SamplePagerAdapter(final FragmentManager fm) {
            super(fm);
        }

        /**
         * @return the number of pages to display
         */
        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(final int position) {
            return titles[position];
        }

        @Override
        public Fragment getItem(final int position) {
            Bundle bundle = new Bundle();
            String repositoryString = repository.toString();
            bundle.putString(Constants.REPOSITORY_KEY, repositoryString);
            Fragment fragment = null;
            switch (position) {
                case 0:
                    fragment = new FiltersFragment();
                    break;
                case 1:
                    fragment = new GenresFragment();
                    break;
            }
            if (fragment != null) {
                fragment.setArguments(bundle);
            }
            return fragment;
        }

    }

}