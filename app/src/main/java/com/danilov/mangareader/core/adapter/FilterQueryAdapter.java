package com.danilov.mangareader.core.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.danilov.mangareader.R;
import com.danilov.mangareader.core.repository.RepositoryEngine;
import com.danilov.mangareader.core.widget.TriStateCheckbox;

import java.util.List;

/**
 * Created by Semyon on 22.12.2014.
 */

public class FilterQueryAdapter extends BaseAdapter<FilterQueryAdapter.Holder, RepositoryEngine.FilterGroup> {


    private int elementsInRow = 0;
    private List<RepositoryEngine.FilterGroup> filterGroups;

    public FilterQueryAdapter(final Context context, final int elementsInRow, final List<RepositoryEngine.FilterGroup> filterGroups) {
        super(context, 0);
        this.elementsInRow = elementsInRow;
        this.filterGroups = filterGroups;
        init();
    }

    private int totalSize = 0;

    private int[] sizes;

    private int[] posWithFilters;

    private void init() {
        sizes = new int[filterGroups.size()];
        for (int i = 0; i < filterGroups.size(); i++) {
            int filterGroupSize = filterGroups.get(i).size();
            int left = filterGroupSize % elementsInRow;

            int sz = filterGroupSize + (left != 0 ? elementsInRow - left : 0) + (elementsInRow - 1);
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
            //это название
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
            //это название
            return filterGroup.getName();
        }

        return filterGroup.size() > inGroupPos ? filterGroup.get(inGroupPos).getName() : null;
    }



    @Override
    public void onBindViewHolder(final Holder holder, final int position) {
        RepositoryEngine.Filter filter = getFilterByPos(position);
        String filterGroupName = getFilterGroupNameOrTitleByPos(position);
        if (filterGroupName == null) {
            holder.hide();
            return;
        }
        holder.unhide();
        if (filter == null) {
            holder.setTitle(filterGroupName);
        } else {
            switch (filter.getType()) {
                case TRI_STATE:
                    holder.setTriStateCheckboxText(filter.getName());
                    break;
                case TWO_STATE:
                    holder.setCheckboxText(filter.getName());
                    break;
            }
        }
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
