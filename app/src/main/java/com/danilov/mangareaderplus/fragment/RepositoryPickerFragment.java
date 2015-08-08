package com.danilov.mangareaderplus.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.danilov.mangareaderplus.R;
import com.danilov.mangareaderplus.activity.MangaQueryActivity;
import com.danilov.mangareaderplus.core.repository.RepositoryEngine;
import com.danilov.mangareaderplus.core.util.Constants;
import com.danilov.mangareaderplus.core.util.Utils;
import com.danilov.mangareaderplus.core.view.ViewV16;

import java.util.Random;

/**
 * Created by Semyon Danilov on 07.10.2014.
 */
public class RepositoryPickerFragment extends BaseFragment {

    private static final String ADULT_SHOWN = "ADULT_SHOWN";

    private boolean adultShown = false;

    private RepositoryEngine.Repository[] repositories;
    private GridView repositoriesView;;
    private Button showAdultButton;

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
        if (savedInstanceState != null) {
            adultShown = savedInstanceState.getBoolean(ADULT_SHOWN, false);
        }
        repositories = adultShown ? RepositoryEngine.Repository.getWithAdult() : RepositoryEngine.Repository.getWithoutAdult();
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
        repositoriesView.setAdapter(new RepoAdapter(this.getActivity(), R.layout.repository_item, repositories));
        showAdultButton = findViewById(R.id.show_adult);
        showAdultButton.setVisibility(adultShown ? View.GONE : View.VISIBLE);
        showAdultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                showAdult();
            }
        });
        super.onActivityCreated(savedInstanceState);
    }

    private long ANIM_DURATION = 300l;
    private long WAIT_DURATION = 1200l;

    private void showAdult() {
        adultShown = true;
        showAdultButton.setVisibility(View.GONE);
        repositories = RepositoryEngine.Repository.getWithAdult();
        repositoriesView.setAdapter(new RepoAdapter(getActivity(), R.layout.repository_item, repositories));


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean shownHentaiAnim = sharedPreferences.getBoolean("SHOWN_HENTAI_ANIM", false);
        if (shownHentaiAnim) {
            Random random = new Random();
            if (random.nextInt(5) < 4) {
                return;
            }
        }
        sharedPreferences.edit().putBoolean("SHOWN_HENTAI_ANIM", true).apply();


        final View hentaiView = findViewById(R.id.hentai_animator);
        final ViewV16 hentaiAnimator = ViewV16.wrap(hentaiView);

        hentaiView.setVisibility(View.VISIBLE);
        final int translation = Utils.dpToPx(150);
        hentaiAnimator.setTranslationY(translation);
        hentaiAnimator.setTranslationX(translation);
        hentaiAnimator.animate().setDuration(ANIM_DURATION).translationY(0).translationX(0).withEndAction(new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        hentaiAnimator.animate().setDuration(ANIM_DURATION).translationY(translation).translationX(translation).withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                hentaiView.setVisibility(View.GONE);
                            }
                        });
                    }
                }, WAIT_DURATION);
            }
        });


    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(ADULT_SHOWN, adultShown);
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
