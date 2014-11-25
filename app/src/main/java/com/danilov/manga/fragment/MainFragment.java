package com.danilov.manga.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.danilov.manga.R;
import com.danilov.manga.core.database.DatabaseAccessException;
import com.danilov.manga.core.database.DownloadedMangaDAO;
import com.danilov.manga.core.database.UpdatesDAO;
import com.danilov.manga.core.model.LocalManga;
import com.danilov.manga.core.model.Manga;
import com.danilov.manga.core.model.UpdatesElement;
import com.danilov.manga.core.service.MangaUpdateService;
import com.danilov.manga.core.util.Constants;
import com.danilov.manga.core.util.ServiceContainer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Semyon Danilov on 07.10.2014.
 */
public class MainFragment extends BaseFragment {

    private Button update;
    private ListView updatesView;
    private List<UpdatesElement> updates = new ArrayList<UpdatesElement>();

    private UpdatesAdapter adapter;

    private FragmentActivity activity;

    private UpdateBroadcastReceiver receiver;

    private DownloadedMangaDAO downloadedMangaDAO = ServiceContainer.getService(DownloadedMangaDAO.class);
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
        activity = getActivity();
        receiver = new UpdateBroadcastReceiver();
        activity.registerReceiver(receiver, new IntentFilter(MangaUpdateService.UPDATE));
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                try {
                    //TODO: update activity's [new quantity] value
                    updates.clear();
                    updatesView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    List<LocalManga> mangaList = downloadedMangaDAO.getAllManga();
                    for (Manga manga : mangaList) {
                        updatesDAO.deleteManga((LocalManga) manga);
                    }
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

    private class UpdateBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            Manga manga = intent.getParcelableExtra(Constants.MANGA_PARCEL_KEY);
            int difference = intent.getIntExtra(Constants.MANGA_CHAPTERS_DIFFERENCE, 0);
            onUpdate(manga, difference);
        }

    }

    private void onUpdate(final Manga manga, final Integer difference) {
        try {
            updatesDAO.updateLocalInfo((LocalManga) manga, difference, new Date());
            downloadedMangaDAO.updateInfo(manga, manga.getChaptersQuantity());
            UpdatesElement element = updatesDAO.getUpdatesByManga((LocalManga) manga);
            updates.add(element);
            adapter.notifyDataSetChanged();
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

            UpdatesElement element = updates.get(position);
            int difference = element.getDifference();
            Resources res = getResources();
            String diff = res.getQuantityString(R.plurals.updates_plural, difference, difference);

            h.title.setText(element.getManga().getTitle());
            h.quantityNew.setText(diff);
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

}
