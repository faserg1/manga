package com.danilov.mangareader.core.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.danilov.mangareader.R;
import com.danilov.mangareader.core.repository.RepositoryEngine;
import com.danilov.mangareader.core.widget.TriStateCheckbox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Semyon on 22.12.2014.
 */

public class FilterQueryAdapter extends BaseAdapter<FilterQueryAdapter.Holder, RepositoryEngine.FilterGroup> {

    private Map<RepositoryEngine.Filter, Object> values = new HashMap<>();

    private int elementsInRow = 0;
    private List<RepositoryEngine.FilterGroup> filterGroups;

    public FilterQueryAdapter(final Context context, final int elementsInRow, final List<RepositoryEngine.FilterGroup> filterGroups) {
        super(context, 0);
        this.elementsInRow = elementsInRow;
        this.filterGroups = filterGroups;
        init();
    }

    private int totalSize = 0;

    //размеры груп фильтров
    private int[] sizes;

    //номер фильтра по позиции в списке (пустота тоже считается)
    private int[] posWithFilters;

    private void init() {
        sizes = new int[filterGroups.size()];
        for (int i = 0; i < filterGroups.size(); i++) {
            int filterGroupSize = filterGroups.get(i).size();
            int left = filterGroupSize % elementsInRow;

            int sz = filterGroupSize + (left != 0 ? elementsInRow - left : 0) + elementsInRow;
            totalSize += sz;

            sizes[i] = sz;
        }
        posWithFilters = new int[totalSize];
        int a = 0;
        for (int i = 0; i < sizes.length; i++) {
            for (int j = 0; j < sizes[i]; j++) {
                posWithFilters[a] = i;
                a++;
            }
        }
        setDefaults();
    }

    private void setDefaults() {
        for (RepositoryEngine.FilterGroup filterGroup : filterGroups) {
            for (RepositoryEngine.Filter filter : filterGroup) {
                values.put(filter, filter.getDefault());
            }
        }
    }

    @Override
    public int getCount() {
        return totalSize;
    }

    private RepositoryEngine.Filter getFilterByPos(final int pos) {
        int filterGroupNum = posWithFilters[pos];
        RepositoryEngine.FilterGroup filterGroup = filterGroups.get(filterGroupNum);
        int szToRemove = 0;
        for (int i = 0; i < filterGroupNum; i++) {
            szToRemove += sizes[i];
        }
        int inGroupPos = pos - szToRemove;

        szToRemove = elementsInRow;
        inGroupPos -= szToRemove;

        if (inGroupPos < 0) {
            //это название или пустота
            return null;
        }

        return filterGroup.size() > inGroupPos ? filterGroup.get(inGroupPos) : null;
    }

    private String getFilterGroupNameOrTitleByPos(final int pos) {
        int filterGroupNum = posWithFilters[pos];
        RepositoryEngine.FilterGroup filterGroup = filterGroups.get(filterGroupNum);
        int szToRemove = 0;
        for (int i = 0; i < filterGroupNum; i++) {
            szToRemove += sizes[i];
        }
        int inGroupPos = pos - szToRemove;

        szToRemove = elementsInRow;
        inGroupPos -= szToRemove;

        if (inGroupPos < 0) {
            //это название или пустота
            if (Math.abs(inGroupPos) == elementsInRow) {
                return filterGroup.getName();
            } else {
                return null;
            }
        }

        return filterGroup.size() > inGroupPos ? filterGroup.get(inGroupPos).getName() : null;
    }

    @Override
    public void onBindViewHolder(final Holder holder, final int position) {
        final RepositoryEngine.Filter filter = getFilterByPos(position);
        String filterGroupName = getFilterGroupNameOrTitleByPos(position);
        if (filterGroupName == null) {
            holder.hide();
            return;
        }
        holder.unhide();
        if (filter == null) {
            holder.setTitle(filterGroupName);
        } else {
            Integer value = (Integer) values.get(filter);
            switch (filter.getType()) {
                case TRI_STATE:
                    holder.setTriStateCheckboxText(filter.getName());
                    holder.triStateCheckbox.setTriStateListener(null);
                    if (value != null) {
                        holder.triStateCheckbox.setState(value);
                    } else {
                        holder.triStateCheckbox.setState(TriStateCheckbox.UNCHECKED);
                    }
                    holder.triStateCheckbox.setTriStateListener(new TriStateCheckbox.TriStateListener() {
                        @Override
                        public void onStateChanged(final int state) {
                            values.put(filter, state);
                        }
                    });
                    break;
                case TWO_STATE:
                    holder.setCheckboxText(filter.getName());
                    if (value != null) {
                        holder.checkBox.setChecked(value != 0);
                    } else {
                        holder.checkBox.setChecked(false);
                    }
                    holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(final CompoundButton compoundButton, final boolean b) {
                            values.put(filter, b ? 1 : 0);
                        }
                    });
                    break;
            }
        }
    }

    public List<RepositoryEngine.Filter.FilterValue> getFilterValues() {
        List<RepositoryEngine.Filter.FilterValue> filterValues = new ArrayList<>(values.size());
        for (RepositoryEngine.FilterGroup filterGroup : filterGroups) {
            for (RepositoryEngine.Filter filter : filterGroup) {
                Object val = values.get(filter);
                RepositoryEngine.Filter.FilterValue filterValue = filter.newValue();
                filterValue.setValue(val);
                filterValues.add(filterValue);
            }
        }
        return filterValues;
    }

    @Override
    public Holder onCreateViewHolder(final ViewGroup viewGroup, final int position) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.filter_item, viewGroup, false);
        return new Holder(v);
    }

    public class Holder extends BaseAdapter.BaseHolder {

        public TriStateCheckbox triStateCheckbox;
        public TextView title;
        public CheckBox checkBox;

        protected Holder(final View view) {
            super(view);
            triStateCheckbox = findViewById(R.id.triStateCheckbox);
            checkBox = findViewById(R.id.checkbox);
            title = findViewById(R.id.title);
        }

        void hide() {
            view.setVisibility(View.INVISIBLE);
        }

        void unhide() {
            view.setVisibility(View.VISIBLE);
        }

        void setTriStateCheckboxText(final String text) {
            triStateCheckbox.setText(text);
            triStateCheckbox.setVisibility(View.VISIBLE);
            checkBox.setVisibility(View.GONE);
            title.setVisibility(View.GONE);
        }

        void setCheckboxText(final String text) {
            checkBox.setText(text);
            checkBox.setVisibility(View.VISIBLE);
            triStateCheckbox.setVisibility(View.GONE);
            title.setVisibility(View.GONE);
        }

        void setTitle(final String text) {
            title.setText(text);
            title.setVisibility(View.VISIBLE);
            triStateCheckbox.setVisibility(View.GONE);
            checkBox.setVisibility(View.GONE);
        }

    }

}
