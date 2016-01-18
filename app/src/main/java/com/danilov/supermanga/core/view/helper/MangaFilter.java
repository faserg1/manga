package com.danilov.supermanga.core.view.helper;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.GridView;

import com.danilov.supermanga.core.adapter.BaseAdapter;
import com.danilov.supermanga.core.model.Manga;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Semyon on 18.01.2016.
 */
public class MangaFilter extends Filter implements TextWatcher {

    private EditText editText;

    private List<? extends Manga> data;

    private BaseAdapterAccessor adapterAccessor;

    public MangaFilter(final EditText editText, final List<? extends Manga> data) {
        this.editText = editText;
        this.data = new ArrayList<>(data); //сохраняем исходные значения
        editText.addTextChangedListener(this);
    }


    @Override
    public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

    }

    @Override
    public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
        filter(s);
    }

    @Override
    public void afterTextChanged(final Editable s) {

    }

    @Override
    protected FilterResults performFiltering(final CharSequence constraint) {
        FilterResults filterResults = new FilterResults();
        if (constraint == null || constraint.length() == 0) {
            filterResults.count = data.size();
            filterResults.values = data;
        } else {
            List<Manga> mangas = new ArrayList<>();
            for (Manga manga : data) {
                if (mangaMatch(manga, "" + constraint)) {
                    mangas.add(manga);
                }
            }
            filterResults.count = mangas.size();
            filterResults.values = mangas;
        }
        return filterResults;
    }

    public boolean mangaMatch(final Manga manga, final String val) {
        String mangaTitle = manga.getTitle();
        mangaTitle = mangaTitle.replace(" ", "").toLowerCase();
        String constraint = val.replace(" ", "").toLowerCase();

        if (mangaTitle.contains(constraint)) {
            return true;
        }
        return false;
    }

    public void setAdapterAccessor(final BaseAdapterAccessor adapterAccessor) {
        this.adapterAccessor = adapterAccessor;
    }

    @Override
    protected void publishResults(final CharSequence constraint, final FilterResults results) {
        if (adapterAccessor == null) {
            return;
        }
        if (results.count == 0) {
            adapterAccessor.notifyDataSetInvalidated();
        } else {
            adapterAccessor.notifyDataSetChanged((List<Manga>) results.values);
        }
    }

    public interface  BaseAdapterAccessor {
        void notifyDataSetInvalidated();
        void notifyDataSetChanged(final List<Manga> mangaList);
    }

    public class AdapterAccessor implements BaseAdapterAccessor {

        private BaseAdapter<?, Manga> adapter;

        public AdapterAccessor(final BaseAdapter<?, Manga> adapter) {
            this.adapter = adapter;
        }

        public void notifyDataSetInvalidated() {
            adapter.notifyDataSetInvalidated();
        }

        public void notifyDataSetChanged(final List<Manga> mangaList) {
            adapter.clear();
            for (Manga manga : mangaList) {
                adapter.add(manga);
            }
            adapter.notifyDataSetChanged();
        }

    }

}