package com.danilov.mangareaderplus.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.danilov.mangareaderplus.R;
import com.danilov.mangareaderplus.activity.MainActivity;
import com.danilov.mangareaderplus.activity.MangaInfoActivity;
import com.danilov.mangareaderplus.core.adapter.BaseAdapter;
import com.danilov.mangareaderplus.core.database.DatabaseAccessException;
import com.danilov.mangareaderplus.core.database.MangaDAO;
import com.danilov.mangareaderplus.core.database.UpdatesDAO;
import com.danilov.mangareaderplus.core.model.Manga;
import com.danilov.mangareaderplus.core.model.UpdatesElement;
import com.danilov.mangareaderplus.core.service.MangaUpdateService;
import com.danilov.mangareaderplus.core.service.MangaUpdateServiceNew;
import com.danilov.mangareaderplus.core.service.ServiceConnectionListener;
import com.danilov.mangareaderplus.core.util.Constants;
import com.danilov.mangareaderplus.core.util.ServiceContainer;
import com.danilov.mangareaderplus.test.Mock;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Semyon Danilov on 07.10.2014.
 */
public class MainFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    private static final String FIRST_LAUNCH = "FIRST_LAUNCH";

    private Button update;
    private GridView updatesView;
    private List<Pair<Manga,UpdatesElement>> updates = new ArrayList<Pair<Manga,UpdatesElement>>();

    private UpdatesAdapterNew adapter;

    private MainActivity activity;
    private boolean firstLaunch;

    private MangaUpdateServiceNew service;

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
                try {
                    activity.changeUpdatesQuantity(updates.size());
                    List<Manga> mangaList = mangaDAO.getFavorite();
                    MangaUpdateServiceNew.startUpdateList(getActivity(), mangaList);
                } catch (DatabaseAccessException e) {
                    e.printStackTrace();
                }
            }
        });

        Intent intent = new Intent(getActivity(), MangaUpdateServiceNew.class);
        ServiceConnection serviceConnection = new MangaUpdateServiceNew.MUpdateServiceNewConnection(new ServiceConnectionListener<MangaUpdateServiceNew>() {
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
        getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        super.onActivityCreated(savedInstanceState);
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
        for (Pair<Manga, UpdatesElement> pair : pairs) {
            updates.add(pair);
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
                    updates.remove(i);
                }
                break;
            }
        }
        adapter.notifyDataSetChanged();
    }

    private class UpdatesAdapterNew extends BaseAdapter<Holder, Pair<Manga, UpdatesElement>> {


        public UpdatesAdapterNew(final Context context, final int resource, final List<Pair<Manga, UpdatesElement>> objects) {
            super(context, resource, objects);
        }

        @Override
        public void onBindViewHolder(final Holder holder, final int position) {
            Pair<Manga, UpdatesElement> item = getItem(position);
            Manga manga = item.first;
            UpdatesElement element = item.second;
            holder.title.setText(manga.getTitle());
            if (element == null) {
                holder.quantityNew.setVisibility(View.INVISIBLE);
                holder.okBtn.setVisibility(View.INVISIBLE);
                holder.progressBar.setVisibility(View.VISIBLE);
            } else {
                holder.okBtn.setVisibility(View.VISIBLE);
                holder.quantityNew.setVisibility(View.VISIBLE);
                holder.progressBar.setVisibility(View.INVISIBLE);
                holder.quantityNew.setText("" + element.getDifference());
            }
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
        public TextView quantityNew;

        protected Holder(final View v) {
            super(v);
            okBtn = (ImageButton) v.findViewById(R.id.ok_btn);
            title = (TextView) v.findViewById(R.id.title);
            quantityNew = (TextView) v.findViewById(R.id.quantity_new);
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
            updates.remove(element);
            adapter.notifyDataSetChanged();
            activity.changeUpdatesQuantity(updates.size());
        }
    }

}
