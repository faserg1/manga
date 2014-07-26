package com.danilov.manga.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import com.danilov.manga.R;
import com.danilov.manga.core.adapter.MangaListAdapter;
import com.danilov.manga.core.model.Manga;
import com.danilov.manga.core.repository.ReadmangaEngine;
import com.danilov.manga.core.repository.RepositoryEngine;
import com.danilov.manga.core.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Semyon Danilov on 26.07.2014.
 */
public class MangaQueryActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static final String FOUND_MANGA_KEY = "FOUND_MANGA_KEY";
    private static final String BRAND_HIDDEN = "BRAND_HIDDEN";

    private EditText query;
    private ListView searchResultsView;

    private View brand;

    private MangaListAdapter adapter = null;

    private List<Manga> foundManga = null;

    private boolean brandHidden = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manga_query_activity);
        query = (EditText) findViewById(R.id.query);
        searchResultsView = (ListView) findViewById(R.id.search_results);
        brand = findViewById(R.id.brand_container);
        ImageButton btn = (ImageButton) findViewById(R.id.search_button);
        btn.setOnClickListener(this);
    }

    @Override
    public void onClick(final View v) {
        QueryTask task = new QueryTask();
        String textQuery = query.getText().toString();
        task.execute(textQuery);
    }

    private class QueryTask extends AsyncTask<String, Void, List<Manga>> {

        @Override
        protected void onPreExecute() {
            hideBrand();
        }

        @Override
        protected List<Manga> doInBackground(final String... params) {
            if (params == null || params.length < 1) {
                return null;
            }
            RepositoryEngine repositoryEngine = new ReadmangaEngine();
            final List<Manga> mangaList = repositoryEngine.queryRepository(params[0]);
            return mangaList;
        }

        @Override
        protected void onPostExecute(final List<Manga> foundManga) {
            if (foundManga == null) {
                return;
            }
            setFoundMangaList(foundManga);
        }

    }

    @Override
    protected void onResume() {
        setFoundMangaList(foundManga);
        if (brandHidden) {
            brand.setVisibility(View.GONE);
        }
        super.onResume();
    }

    private void setFoundMangaList(final List<Manga> manga) {
        this.foundManga = manga;
        if (this.foundManga == null) {
            return;
        }
        adapter = new MangaListAdapter(this, R.layout.manga_list_item, foundManga);
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

    private void hideBrand() {
        if (brandHidden) {
            return;
        }
        brandHidden = true;
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setDuration(1000);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(final Animation animation) {

            }

            @Override
            public void onAnimationEnd(final Animation animation) {
                brand.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(final Animation animation) {

            }

        });
        brand.startAnimation(fadeOut);
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        foundManga = savedInstanceState.getParcelableArrayList(FOUND_MANGA_KEY);
        brandHidden = savedInstanceState.getBoolean(BRAND_HIDDEN);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        ArrayList<Manga> mangas = null;
        if (!(foundManga instanceof ArrayList)) {
            mangas.addAll(foundManga);
        } else {
            mangas = (ArrayList<Manga>) foundManga;
        }
        outState.putParcelableArrayList(FOUND_MANGA_KEY, mangas);
        outState.putBoolean(BRAND_HIDDEN, brandHidden);
        super.onSaveInstanceState(outState);
    }

}