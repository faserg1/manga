package com.danilov.manga.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.danilov.manga.R;
import com.danilov.manga.core.repository.RepositoryEngine;
import org.jetbrains.annotations.Nullable;

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
        final RepositoryEngine.Repository[] repositories = RepositoryEngine.Repository.getWithoutOffline();
        repositoriesView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                RepositoryEngine.Repository repository = repositories[position];
                String repoString = repository.toString();

            }

        });
        repositoriesView.setAdapter(new RepoAdapter(this.getActivity(), R.layout.repository_item, repositories));
        super.onActivityCreated(savedInstanceState);
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

        @Nullable
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
            ImageView country;
            if (tag != null) {
                holder = (ViewHolder) tag;
                text = holder.text;
                icon = holder.icon;
                country = holder.country;
            } else {
                holder = new ViewHolder();
                text = (TextView) view.findViewById(R.id.repository_title);
                icon = (ImageView) view.findViewById(R.id.repository_cover);
                country = (ImageView) view.findViewById(R.id.country_image);
                holder.text = text;
                holder.icon = icon;
                holder.country = country;
            }
            RepositoryEngine.Repository repository = objects[position];
            text.setText(repository.getName());
            icon.setImageResource(repository.getIconId());
            country.setImageResource(repository.getCountryIconId());
            return view;
        }

        private class ViewHolder {
            public TextView text;
            public ImageView icon;
            public ImageView country;
        }

    }

}
