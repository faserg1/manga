package com.danilov.supermanga.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.danilov.supermanga.R;
import com.danilov.supermanga.activity.MainActivity;
import com.danilov.supermanga.activity.MangaInfoActivity;
import com.danilov.supermanga.core.adapter.BaseAdapter;
import com.danilov.supermanga.core.database.DatabaseAccessException;
import com.danilov.supermanga.core.database.MangaDAO;
import com.danilov.supermanga.core.database.UpdatesDAO;
import com.danilov.supermanga.core.model.Manga;
import com.danilov.supermanga.core.model.UpdatesElement;
import com.danilov.supermanga.core.service.MangaUpdateServiceNew;
import com.danilov.supermanga.core.service.ServiceConnectionListener;
import com.danilov.supermanga.core.util.Constants;
import com.danilov.supermanga.core.util.ServiceContainer;
import com.danilov.supermanga.test.Mock;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Semyon Danilov on 07.10.2014.
 */
public class MainFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    private static final String FIRST_LAUNCH = "FIRST_LAUNCH";

    private View update;
    private GridView updatesView;
    private List<Pair<Manga,UpdatesElement>> updates = new ArrayList<Pair<Manga,UpdatesElement>>();

    private UpdatesAdapterNew adapter;

    private MainActivity activity;
    private boolean firstLaunch;

    private MangaUpdateServiceNew service;

    private ServiceConnection serviceConnection;

    private MangaDAO mangaDAO = ServiceContainer.getService(MangaDAO.class);
    private UpdatesDAO updatesDAO = ServiceContainer.getService(UpdatesDAO.class);

    public static MainFragment newInstance(final boolean firstLaunch) {
        MainFragment mainFragment = new MainFragment();
        mainFragment.setFirstLaunch(firstLaunch);
        return mainFragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.manga_main_fragment, container, false);
        return view;
    }

    public boolean isFirstLaunch() {
        return firstLaunch;
    }

    public void setFirstLaunch(final boolean firstLaunch) {
        this.firstLaunch = firstLaunch;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        updatesView = findViewById(R.id.updates);
        update = findViewById(R.id.update);

        findViewById(R.id.show_tracking).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                MainActivity activity = (MainActivity) getActivity();
                activity.showTrackingFragment();
            }
        });

        List<UpdatesElement> _updates = null;
        try {
            _updates = updatesDAO.getAllUpdates();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (_updates != null) {
            updates = new ArrayList<>();
            for (UpdatesElement updatesElement : _updates) {
                updates.add(new Pair<Manga, UpdatesElement>(updatesElement.getManga(), updatesElement));
            }
        }
        if (savedInstanceState != null) {
            firstLaunch = savedInstanceState.getBoolean(FIRST_LAUNCH, firstLaunch);
        }
        if (firstLaunch) {
            List<Pair<Manga, UpdatesElement>> mock = new ArrayList<>(1);
            mock.add(Mock.getMockUpdate(getActivity()));
            updates = mock;
            adapter = new UpdatesAdapterNew(getActivity(), 0, mock);
        } else {
            TextView usefulInfo = findViewById(R.id.useful_info);
            usefulInfo.setVisibility(View.GONE);
            adapter = new UpdatesAdapterNew(getActivity(), 0, updates);
        }
        updatesView.setAdapter(adapter);
        if (!firstLaunch) {
            updatesView.setOnItemClickListener(this);
        }
        activity = (MainActivity) getActivity();
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                activity.changeUpdatesQuantity(updates.size());
                MangaUpdateServiceNew.startUpdateList(getActivity());
            }
        });

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onPause() {
        getActivity().unbindService(serviceConnection);
        super.onPause();
    }

    @Override
    public void onResume() {
        serviceConnection = new MangaUpdateServiceNew.MUpdateServiceNewConnection(new ServiceConnectionListener<MangaUpdateServiceNew>() {
            @Override
            public void onServiceConnected(final MangaUpdateServiceNew service) {
                MainFragment.this.service = service;
                service.addHandler(handler);
            }

            @Override
            public void onServiceDisconnected(final MangaUpdateServiceNew service) {
                service.removeHandler(handler);
            }
        });
        Intent intent = new Intent(getActivity(), MangaUpdateServiceNew.class);
        getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        outState.putBoolean(FIRST_LAUNCH, firstLaunch);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onItemClick(final AdapterView<?> adapterView, final View view, final int i, final long l) {
        Pair<Manga, UpdatesElement> el = updates.get(i);
        Manga manga = el.first;
        Manga mangaToParcel = new Manga(manga.getTitle(), manga.getUri(), manga.getRepository());
        mangaToParcel.setAuthor(manga.getAuthor());
        Intent intent = new Intent(activity, MangaInfoActivity.class);
        intent.putExtra(Constants.MANGA_PARCEL_KEY, mangaToParcel);
        startActivity(intent);
    }

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case MangaUpdateServiceNew.UPDATING_LIST:
                case MangaUpdateServiceNew.UPDATE_STARTED:
                    updateUpdatingList((List<Pair<Manga, UpdatesElement>>) msg.obj);
                    break;
                case MangaUpdateServiceNew.MANGA_UPDATE_FAILED:
                    mangaUpdateError((Manga) msg.obj);
                    break;
                case MangaUpdateServiceNew.MANGA_UPDATE_FINISHED:
                    updateManga((Manga) msg.obj, msg.arg1, msg.arg2);
                    break;
                case MangaUpdateServiceNew.UPDATE_FINISHED:
                    break;
            }
        }
    };

    private void updateUpdatingList(final List<Pair<Manga, UpdatesElement>> pairs) {
        updates.clear();
        failedMangas.clear();
        List<UpdatesElement> _updates = null;
        try {
            _updates = updatesDAO.getAllUpdates();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (Pair<Manga, UpdatesElement> pair : pairs) {
            updates.add(pair);
        }
        if (_updates != null) {
            for (UpdatesElement updatesElement : _updates) {

                boolean isUpdating = false;

                for (Pair<Manga, UpdatesElement> pair : updates) {
                    if (pair.first != null) {
                        Manga manga1 = pair.first;
                        Manga manga2 = updatesElement.getManga();
                        if (manga1.getId() == manga2.getId()) {
                            isUpdating = true;
                            break;
                        }
                    }
                }
                if (!isUpdating) {
                    updates.add(new Pair<Manga, UpdatesElement>(updatesElement.getManga(), updatesElement));
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void updateManga(final Manga manga, final int id, final int diff) {
        for (int i = 0; i < updates.size(); i++) {
            Pair<Manga, UpdatesElement> pair = updates.get(i);
            Manga m = pair.first;
            if (manga.getId() == m.getId()) {
                if (diff > 0) {
                    Pair<Manga, UpdatesElement> newPair = new Pair<>(manga, new UpdatesElement(id, manga, diff));
                    updates.set(i, newPair);
                } else {
                    boolean hasError = failedMangas.contains(manga.getId());
                    if (!hasError) {
                        //если получение инфы о манге прошло нормально - удаляем из списка
                        updates.remove(i);
                    }
                }
                break;
            }
        }
        adapter.notifyDataSetChanged();
    }

    private Set<Integer> failedMangas = new HashSet<>();

    private void mangaUpdateError(final Manga manga) {
        if (manga != null) {
            failedMangas.add(manga.getId());
        }
    }

    private class UpdatesAdapterNew extends BaseAdapter<Holder, Pair<Manga, UpdatesElement>> {


        public UpdatesAdapterNew(final Context context, final int resource, final List<Pair<Manga, UpdatesElement>> objects) {
            super(context, resource, objects);
        }

        @Override
        public void onBindViewHolder(final Holder holder, final int position) {
            Pair<Manga, UpdatesElement> item = getItem(position);
            Manga manga = item.first;
            final UpdatesElement element = item.second;
            holder.title.setText(manga.getTitle());
            holder.repository.setText(manga.getRepository().getName());

            boolean hasError = failedMangas.contains(manga.getId());

            if (element == null) {
                holder.quantityNew.setVisibility(View.INVISIBLE);
                holder.okBtn.setVisibility(View.INVISIBLE);
                //если есть ошибка, значит это уже обновили (с ошибкой), убираем прогрессбар
                holder.progressBar.setVisibility(hasError ? View.INVISIBLE : View.VISIBLE);
            } else {
                holder.okBtn.setVisibility(View.VISIBLE);
                holder.okBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View view) {
                        deleteUpdate(element);
                    }
                });
                holder.quantityNew.setVisibility(View.VISIBLE);
                holder.progressBar.setVisibility(View.INVISIBLE);
                Resources res = getResources();
                int difference = element.getDifference();
                String diff = res.getQuantityString(R.plurals.updates_plural, difference, difference);
                holder.quantityNew.setText(diff);
            }
            holder.failed.setVisibility(hasError ? View.VISIBLE : View.INVISIBLE);
        }

        @Override
        public Holder onCreateViewHolder(final ViewGroup viewGroup, final int position) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.updates_item, viewGroup, false);
            return new Holder(view);
        }

    }

    private class Holder extends BaseAdapter.BaseHolder {

        private ProgressBar progressBar;
        public ImageButton okBtn;
        public TextView title;
        public TextView repository;
        public TextView quantityNew;
        public TextView failed;

        protected Holder(final View v) {
            super(v);
            okBtn = (ImageButton) v.findViewById(R.id.ok_btn);
            title = (TextView) v.findViewById(R.id.title);
            repository = (TextView) v.findViewById(R.id.repository);
            quantityNew = (TextView) v.findViewById(R.id.quantity_new);
            failed = (TextView) v.findViewById(R.id.failed);
            progressBar = (ProgressBar) v.findViewById(R.id.progress_bar);
        }
    }

    private void deleteUpdate(final UpdatesElement element) {
        firstLaunch = false; //supa hack
        try {
            updatesDAO.delete(element);
        } catch (DatabaseAccessException e) {
            //TODO: error handling
            e.printStackTrace();
        }
        if (updates != null) {
            for (int i = 0; i < updates.size(); i++) {
                Pair<Manga, UpdatesElement> pair = updates.get(i);
                if (element ==  pair.second) {
                    updates.remove(i);
                    break;
                }
            }
            adapter.notifyDataSetChanged();
            activity.changeUpdatesQuantity(updates.size());
        }
    }

}
