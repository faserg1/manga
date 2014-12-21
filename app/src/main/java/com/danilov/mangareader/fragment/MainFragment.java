package com.danilov.mangareader.fragment;

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

import com.danilov.mangareader.R;
import com.danilov.mangareader.activity.MainActivity;
import com.danilov.mangareader.activity.MangaInfoActivity;
import com.danilov.mangareader.core.database.DatabaseAccessException;
import com.danilov.mangareader.core.database.MangaDAO;
import com.danilov.mangareader.core.database.UpdatesDAO;
import com.danilov.mangareader.core.model.Manga;
import com.danilov.mangareader.core.model.UpdatesElement;
import com.danilov.mangareader.core.service.MangaUpdateService;
import com.danilov.mangareader.core.util.Constants;
import com.danilov.mangareader.core.util.ServiceContainer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Semyon Danilov on 07.10.2014.
 */
public class MainFragment extends BaseFragment implements AdapterView.OnItemClickListener{

    private Button update;
    private GridView updatesView;
    private List<UpdatesElement> updates = new ArrayList<UpdatesElement>();

    private UpdatesAdapter adapter;

    private MainActivity activity;

    private UpdateBroadcastReceiver receiver;

    private MangaDAO mangaDAO = ServiceContainer.getService(MangaDAO.class);
    private UpdatesDAO updatesDAO = ServiceContainer.getService(UpdatesDAO.class);

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.manga_main_fragment, container, false);
        return view;
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
        adapter = new UpdatesAdapter(getActivity(), 0, updates);
        updatesView.setAdapter(adapter);
        updatesView.setOnItemClickListener(this);
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
