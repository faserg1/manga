package com.danilov.supermanga.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.danilov.supermanga.R;
import com.danilov.supermanga.activity.MainActivity;
import com.danilov.supermanga.activity.MangaQueryActivity;
import com.danilov.supermanga.core.adapter.BaseAdapter;
import com.danilov.supermanga.core.repository.RepositoryEngine;
import com.danilov.supermanga.core.repository.RepositoryHolder;
import com.danilov.supermanga.core.util.Constants;
import com.danilov.supermanga.core.util.ServiceContainer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Semyon Danilov on 07.10.2014.
 */
public class RepositoryPickerFragment extends BaseFragment {

    private RepositoryEngine.Repository[] repositories;
    private GridView repositoriesView;

    public static RepositoryPickerFragment newInstance() {
        return new RepositoryPickerFragment();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.manga_repository_picker_fragment, container, false);
        return view;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        repositoriesView = findViewById(R.id.repositories);

        SharedPreferences sharedPreferences = getDefaultSharedPreferences();
        String addedRepositoriesString = sharedPreferences.getString("ADDED_REPOSITORIES", "");
        boolean userMigrated = sharedPreferences.getBoolean("USER_MIGRATED", false);
        if (!userMigrated) {
            SharedPreferences.Editor edit = sharedPreferences.edit();
            if (addedRepositoriesString.length() > 0) {
                edit.putString("ADDED_REPOSITORIES", RepositoryEngine.DefaultRepository.READMANGA.toString() + "," + RepositoryEngine.DefaultRepository.MANGAREADERNET.toString() + "," + addedRepositoriesString).apply();
                addedRepositoriesString = sharedPreferences.getString("ADDED_REPOSITORIES", "");
            }
            edit.putBoolean("USER_MIGRATED", true).apply();
        }


        String[] addedRepositoriesStringArray = addedRepositoriesString.split(",");

        RepositoryHolder repositoryHolder = ServiceContainer.getService(RepositoryHolder.class);
//        repositories = RepositoryEngine.DefaultRepository.getBySettings(addedRepositoriesStringArray);
        repositories = repositoryHolder.getRepositories();;
        repositoriesView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                RepositoryEngine.Repository repository = repositories[position];
                String repoString = repository.toString();
                Intent intent = new Intent(getActivity(), MangaQueryActivity.class);
                intent.putExtra(Constants.REPOSITORY_KEY, repoString);
                startActivity(intent);
            }

        });


        RepositoryEngine.Repository[] _repositories = RepositoryEngine.DefaultRepository.getNotAdded(addedRepositoriesStringArray);

        final List<String> names = new ArrayList<>();
        names.add(getString(R.string.popular_sources));
        for (RepositoryEngine.Repository repository : _repositories) {
            names.add(repository.getName());
        }
        RepoSuggestAdapter adapter = new RepoSuggestAdapter(applicationContext, R.layout.repository_dropdown_item, names);
        repositoriesView.setAdapter(new RepoAdapter(this.getActivity(), R.layout.repository_item, repositories));
        super.onActivityCreated(savedInstanceState);
    }
    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private class RepoSuggestAdapter extends BaseAdapter<RepoSuggestHolder, String> {

        public RepoSuggestAdapter(final Context context, final int resource, final List<String> objects) {
            super(context, resource, objects);
        }

        @Override
        public void onBindViewHolder(final RepoSuggestHolder holder, final int position) {
            holder.text.setText(getItem(position));
        }

        @Override
        public RepoSuggestHolder onCreateViewHolder(final ViewGroup viewGroup, final int position) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.repository_dropdown_item, viewGroup, false); //attaching to parent == (false), because it attaching later by android
            return new RepoSuggestHolder(view);
        }

    }

    private class RepoSuggestHolder extends BaseAdapter.BaseHolder {

        private TextView text;

        protected RepoSuggestHolder(final View view) {
            super(view);
            text = findViewById(R.id.text1);
        }
    }

    private class RepoAdapter extends ArrayAdapter<RepositoryEngine.Repository> {

        private RepositoryEngine.Repository[] objects;
        private int resourceId;
        private Context context;

        @Override
        public int getCount() {
            return objects.length;
        }

        public RepoAdapter(final Context context, final int resource, final RepositoryEngine.Repository[] objects) {
            super(context, resource, objects);
            this.objects = objects;
            this.resourceId = resource;
            this.context = context;
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(resourceId, parent, false); //attaching to parent == (false), because it attaching later by android
            }
            Object tag = view.getTag();
            ViewHolder holder = null;
            TextView text;
            ImageView icon;
            if (tag != null) {
                holder = (ViewHolder) tag;
                text = holder.text;
                icon = holder.icon;
            } else {
                holder = new ViewHolder();
                text = (TextView) view.findViewById(R.id.repository_title);
                icon = (ImageView) view.findViewById(R.id.repository_cover);
                holder.text = text;
                holder.icon = icon;
            }
            RepositoryEngine.Repository repository = objects[position];
            text.setText(repository.getName());
            icon.setImageResource(repository.getCountryIconId());
            return view;
        }

        private class ViewHolder {
            public TextView text;
            public ImageView icon;
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.downloaded_manga_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_local_manga:
                MainActivity activity = (MainActivity) getActivity();
                activity.showAddJSRepositoryFragment();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
