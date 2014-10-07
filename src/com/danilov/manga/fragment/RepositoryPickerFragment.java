package com.danilov.manga.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import com.danilov.manga.R;
import com.danilov.manga.core.repository.RepositoryEngine;

/**
 * Created by Semyon Danilov on 07.10.2014.
 */
public class RepositoryPickerFragment extends Fragment {

    private View view;

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
        GridView repositoriesView = (GridView) view.findViewById(R.id.repositories);

        super.onActivityCreated(savedInstanceState);
    }

    private class RepoAdapter extends ArrayAdapter<RepositoryEngine.Repository> {

        private RepositoryEngine.Repository[] objects;
        private int resourceId;
        private Context context;

        public RepoAdapter(final Context context, final int resource, final RepositoryEngine.Repository[] objects) {
            super(context, resource, objects);
            this.objects = objects;
            this.resourceId = resource;
            this.context = context;
        }
    }

}
