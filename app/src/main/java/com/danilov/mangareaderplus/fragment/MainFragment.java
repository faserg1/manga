package com.danilov.mangareaderplus.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.danilov.mangareaderplus.R;
import com.danilov.mangareaderplus.activity.MainActivity;
import com.danilov.mangareaderplus.activity.MangaInfoActivity;
import com.danilov.mangareaderplus.core.database.DatabaseAccessException;
import com.danilov.mangareaderplus.core.database.MangaDAO;
import com.danilov.mangareaderplus.core.database.UpdatesDAO;
import com.danilov.mangareaderplus.core.model.Manga;
import com.danilov.mangareaderplus.core.model.UpdatesElement;
import com.danilov.mangareaderplus.core.service.MangaUpdateService;
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
    private List<UpdatesElement> updates = new ArrayList<UpdatesElement>();

    private UpdatesAdapter adapter;

    private MainActivity activity;
    private boolean firstLaunch;

    private UpdateBroadcastReceiver receiver;

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
            updates = _updates;
        }
        if (savedInstanceState != null) {
            firstLaunch = savedInstanceState.getBoolean(FIRST_LAUNCH, firstLaunch);
        }
        if (firstLaunch) {
            List<UpdatesElement> mock = new ArrayList<>(1);
            mock.add(Mock.getMockUpdate(getActivity()));
            updates = mock;
            adapter = new UpdatesAdapter(getActivity(), 0, mock);
        } else {
            TextView usefulInfo = findViewById(R.id.useful_info);
            usefulInfo.setVisibility(View.GONE);
            adapter = new UpdatesAdapter(getActivity(), 0, updates);
        }
        updatesView.setAdapter(adapter);
        if (!firstLaunch) {
            updatesView.setOnItemClickListener(this);
        }
        activity = (MainActivity) getActivity();
        receiver = new UpdateBroadcastReceiver();
        activity.registerReceiver(receiver, new IntentFilter(MangaUpdateService.UPDATE));
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                try {
                    //TODO: update activity's [new quantity] value
                    updates.clear();
                    activity.changeUpdatesQuantity(updates.size());
                    updatesView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

                    List<Manga> mangaList = mangaDAO.getFavorite();
                    MangaUpdateService.startUpdateList(getActivity(), mangaList);
                } catch (DatabaseAccessException e) {
                    e.printStackTrace();
                }
            }
        });
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDetach() {
        activity.unregisterReceiver(receiver);
        super.onDetach();
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        outState.putBoolean(FIRST_LAUNCH, firstLaunch);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onItemClick(final AdapterView<?> adapterView, final View view, final int i, final long l) {
        UpdatesElement el = updates.get(i);
        Manga manga = el.getManga();
        Manga mangaToParcel = new Manga(manga.getTitle(), manga.getUri(), manga.getRepository());
        mangaToParcel.setAuthor(manga.getAuthor());
        Intent intent = new Intent(activity, MangaInfoActivity.class);
        intent.putExtra(Constants.MANGA_PARCEL_KEY, mangaToParcel);
        startActivity(intent);
    }

    private class UpdateBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            Manga manga = intent.getParcelableExtra(Constants.MANGA_PARCEL_KEY);
            int difference = intent.getIntExtra(Constants.MANGA_CHAPTERS_DIFFERENCE, 0);
            onUpdate(manga);
        }

    }

    private void onUpdate(final Manga manga) {
        try {
            synchronized (this) {
                Manga _manga = mangaDAO.getById(manga.getId());
                int oldQuantity = _manga.getChaptersQuantity();
                int newQuantity = manga.getChaptersQuantity();
                if (newQuantity != oldQuantity) {
                    mangaDAO.updateInfo(manga, newQuantity, manga.isDownloaded());
                    updatesDAO.updateInfo(manga, newQuantity - oldQuantity, new Date());
                    UpdatesElement element = updatesDAO.getUpdatesByManga(manga);
                    updates.remove(element);
                    updates.add(element);
                    adapter.notifyDataSetChanged();
                    activity.changeUpdatesQuantity(updates.size());
                }
            }
        } catch (DatabaseAccessException e) {
            e.printStackTrace();
        }
    }

    private class UpdatesAdapter extends ArrayAdapter<UpdatesElement> {

        private List<UpdatesElement> updates;

        @Override
        public int getCount() {
            if (updates == null) {
                return 0;
            }
            return updates.size();
        }

        public UpdatesAdapter(final Context context, final int resource, final List<UpdatesElement> objects) {
            super(context, resource, objects);
            updates = objects;
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            View v = convertView;
            Holder h = null;
            if (v != null) {
                Object tag = v.getTag();
                if (tag instanceof Holder) {
                    h = (Holder) tag;
                } else {
                    h = new Holder(v);
                    v.setTag(h);
                }
            } else {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.updates_item, null);
                h = new Holder(v);
                v.setTag(h);
            }

            final UpdatesElement element = updates.get(position);
            int difference = element.getDifference();
            Resources res = getResources();
            String diff = res.getQuantityString(R.plurals.updates_plural, difference, difference);

            h.title.setText(element.getManga().getTitle());
            h.quantityNew.setText(diff);
            h.okBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    deleteUpdate(element);
                }
            });
            return v;
        }

        private class Holder {

            public ImageButton okBtn;
            public TextView title;
            public TextView quantityNew;

            public Holder (final View v) {
                okBtn = (ImageButton) v.findViewById(R.id.ok_btn);
                title = (TextView) v.findViewById(R.id.title);
                quantityNew = (TextView) v.findViewById(R.id.quantity_new);
            }

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
