package com.danilov.supermanga.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.danilov.supermanga.R;
import com.danilov.supermanga.activity.MangaQueryActivity;
import com.danilov.supermanga.core.adapter.BaseAdapter;
import com.danilov.supermanga.core.repository.ReadmangaEngine;
import com.danilov.supermanga.core.repository.RepositoryEngine;
import com.danilov.supermanga.core.util.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Semyon Danilov on 07.10.2014.
 */
public class RepositoryPickerFragment extends BaseFragment {

    private RepositoryEngine.Repository[] repositories;
    private GridView repositoriesView;

    private View addRepositoryWrapper;
    private Button showAddForm;
    private Button addRepo;
    private TextView noSources;
    private AutoCompleteTextView repositoryUrl;

    public static RepositoryPickerFragment newInstance() {
        return new RepositoryPickerFragment();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.manga_repository_picker_fragment, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        repositoriesView = findViewById(R.id.repositories);
        addRepositoryWrapper = findViewById(R.id.add_repository_wrapper);
        showAddForm = findViewById(R.id.show_add_form);
        addRepo = findViewById(R.id.add);
        noSources = findViewById(R.id.no_sources);
        repositoryUrl = findViewById(R.id.repository_url);

        SharedPreferences sharedPreferences = getDefaultSharedPreferences();
        String addedRepositoriesString = sharedPreferences.getString("ADDED_REPOSITORIES", "");
        boolean userMigrated = sharedPreferences.getBoolean("USER_MIGRATED", false);
        if (!userMigrated) {
            SharedPreferences.Editor edit = sharedPreferences.edit();
            if (addedRepositoriesString.length() > 0) {
                edit.putString("ADDED_REPOSITORIES", RepositoryEngine.Repository.READMANGA.toString() + "," + RepositoryEngine.Repository.MANGAREADERNET.toString() + "," + addedRepositoriesString).apply();
                addedRepositoriesString = sharedPreferences.getString("ADDED_REPOSITORIES", "");
            }
            edit.putBoolean("USER_MIGRATED", true).apply();
        }


        String[] addedRepositoriesStringArray = addedRepositoriesString.split(",");

        repositories = RepositoryEngine.Repository.getBySettings(addedRepositoriesStringArray);
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

        RepositoryEngine.Repository[] _repositories = RepositoryEngine.Repository.getNotAdded(addedRepositoriesStringArray);
        final List<String> names = new ArrayList<>();
        names.add(getString(R.string.popular_sources));
        for (RepositoryEngine.Repository repository : _repositories) {
            names.add(repository.getName());
        }
        RepoSuggestAdapter adapter = new RepoSuggestAdapter(applicationContext, R.layout.repository_dropdown_item, names);
        repositoryUrl.setAdapter(adapter);
        repositoryUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                repositoryUrl.showDropDown();
            }
        });
        repositoryUrl.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> adapterView, final View view, final int i, final long l) {
                String selected = (String) adapterView.getItemAtPosition(i);
                int pos = names.indexOf(selected);
                if (pos == 0) {
                    repositoryUrl.showDropDown();
                    return;
                } else {
                    repositoryUrl.setText(selected);
                }
            }
        });
        repositoryUrl.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (view.getWindowVisibility() != View.VISIBLE) {
                    return;
                }
                if(hasFocus){
                    repositoryUrl.showDropDown();
                }
            }
        });

        showAddForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                showAddForm();
            }
        });
        addRepo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                String userInput = repositoryUrl.getText().toString();
                if (userInput != null && !userInput.isEmpty()) {
                    userInput = userInput.toLowerCase();
                    for (RepositoryEngine.Repository repository : RepositoryEngine.Repository.getWithoutOffline()) {
                        String repoName = repository.getName().toLowerCase();
                        if (userInput.contains(repoName)) {
                            SharedPreferences sharedPreferences = getDefaultSharedPreferences();
                            String addedRepositoriesString = sharedPreferences.getString("ADDED_REPOSITORIES", "");
                            if (addedRepositoriesString.contains(repository.toString())) {
                                Toast.makeText(applicationContext, getString(R.string.source_already_added), Toast.LENGTH_LONG).show();
                                return;
                            }
                            if (addedRepositoriesString.length() == 0) {
                                addedRepositoriesString += repository.toString();
                            } else {
                                addedRepositoriesString += "," + repository.toString();
                            }
                            sharedPreferences.edit().putString("ADDED_REPOSITORIES", addedRepositoriesString).apply();
                            update();
                            hideAddForm();
                            return;
                        }
                    }
                    Toast.makeText(applicationContext, getString(R.string.source_not_supported), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(applicationContext, getString(R.string.add_source), Toast.LENGTH_LONG).show();
                }
            }
        });
        noSources.setVisibility(repositories.length < 1 ? View.VISIBLE : View.GONE);
        repositoriesView.setAdapter(new RepoAdapter(this.getActivity(), R.layout.repository_item, repositories));
        super.onActivityCreated(savedInstanceState);
    }

    private long ANIM_DURATION = 300l;
    private long WAIT_DURATION = 1200l;

    private void update() {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences();
        String addedRepositoriesString = sharedPreferences.getString("ADDED_REPOSITORIES", "");
        String[] addedRepositoriesStringArray = addedRepositoriesString.split(",");
        repositories = RepositoryEngine.Repository.getBySettings(addedRepositoriesStringArray);
        noSources.setVisibility(repositories.length < 1 ? View.VISIBLE : View.GONE);
        repositoriesView.setAdapter(new RepoAdapter(getActivity(), R.layout.repository_item, repositories));
    }

    private void showAddForm() {
        showAddForm.setVisibility(View.GONE);
        addRepositoryWrapper.setVisibility(View.VISIBLE);
    }

    private void hideAddForm() {
        showAddForm.setVisibility(View.VISIBLE);
        addRepositoryWrapper.setVisibility(View.GONE);
        repositoryUrl.setText("");
        InputMethodManager imm = (InputMethodManager) applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(repositoryUrl.getWindowToken(), 0);
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

}
