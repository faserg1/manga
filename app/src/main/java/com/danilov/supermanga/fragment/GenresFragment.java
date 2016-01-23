package com.danilov.supermanga.fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.danilov.supermanga.R;
import com.danilov.supermanga.activity.MangaQueryActivity;
import com.danilov.supermanga.core.adapter.BaseAdapter;
import com.danilov.supermanga.core.application.MangaApplication;
import com.danilov.supermanga.core.model.Manga;
import com.danilov.supermanga.core.repository.RepositoryEngine;
import com.danilov.supermanga.core.repository.RepositoryException;
import com.danilov.supermanga.core.repository.RepositoryHolder;
import com.danilov.supermanga.core.util.Constants;
import com.danilov.supermanga.core.util.ServiceContainer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Semyon on 01.03.2015.
 */
public class GenresFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    private GridView genresView;

    private RepositoryEngine.Repository repository;

    private RepositoryEngine engine = null;

    private MangaQueryActivity queryActivity;

    private Context context = null;

    private List<RepositoryEngine.Genre> genres;

    private boolean hasNoGenres = false;

    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.genres_fragment, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            String repositoryString = getArguments().getString(Constants.REPOSITORY_KEY);

            RepositoryHolder repositoryHolder = ServiceContainer.getService(RepositoryHolder.class);
            repository = repositoryHolder.valueOf(repositoryString);

            engine = repository.getEngine();
        } else {
            //why I do not use getString with default value? Because it's API 12 :(
            String repositoryString = savedInstanceState.getString(Constants.REPOSITORY_KEY);
            if (repositoryString == null) {
                repository = RepositoryEngine.DefaultRepository.READMANGA;
            } else {
                RepositoryHolder repositoryHolder = ServiceContainer.getService(RepositoryHolder.class);
                repository = repositoryHolder.valueOf(repositoryString);
            }
            engine = repository.getEngine();
        }

        genresView = findViewById(R.id.genres);
        queryActivity = (MangaQueryActivity) getActivity();
        context = queryActivity.getApplicationContext();
        genres = engine.getGenres();
        if (genres.isEmpty()) {
            genres = new ArrayList<>();
            hasNoGenres = true;
            genresView.setClickable(false);
            genres.add(new RepositoryEngine.Genre(getString(R.string.sv_genre_search_not_supported)));
        }
        genresView.setAdapter(new GenresAdapter(context, -1, genres));
        genresView.setOnItemClickListener(this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        String repositoryString = repository.toString();
        outState.putString(Constants.REPOSITORY_KEY, repositoryString);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onItemClick(final AdapterView<?> adapterView, final View view, final int i, final long l) {
        if (hasNoGenres) {
            return;
        }
        RepositoryEngine.Genre genre = genres.get(i);
        QueryTask task = new QueryTask();
        task.execute(genre);
    }

    private class QueryTask extends AsyncTask<RepositoryEngine.Genre, Void, List<Manga>> {

        private String error = null;

        @Override
        protected void onPreExecute() {
            queryActivity.hideViewPager();
            queryActivity.showProgressBar();
        }

        @Override
        protected List<Manga> doInBackground(final RepositoryEngine.Genre... params) {
            if (params == null || params.length < 1) {
                return null;
            }
            try {
                return engine.queryRepository(params[0]);
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

    public class GenresAdapter extends BaseAdapter<Holder, RepositoryEngine.Genre> {

        private List<RepositoryEngine.Genre> genres = null;

        public GenresAdapter(final Context context, final int resource, final List<RepositoryEngine.Genre> objects) {
            super(context, resource, objects);
            this.genres = objects;
        }

        @Override
        public void onBindViewHolder(final Holder holder, final int position) {
            holder.getTextView().setText(genres.get(position).getName());
        }

        @Override
        public Holder onCreateViewHolder(final ViewGroup viewGroup, final int position) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.genre_item, viewGroup, false);
            return new Holder(v);
        }

    }

    public class Holder extends BaseAdapter.BaseHolder {

        private TextView textView;

        protected Holder(final View view) {
            super(view);
            textView = (TextView) view;
        }

        public TextView getTextView() {
            return textView;
        }

        public void setTextView(final TextView textView) {
            this.textView = textView;
        }

    }

}
