package com.danilov.supermanga.core.adapter;

import android.support.v7.widget.RecyclerView;

/**
 * Created by Semyon on 09.04.2016.
 */
public interface ItemClickListener<VH extends RecyclerView.ViewHolder> {

    void onItemClick(final int position, final VH viewHolder);

    boolean onItemLongClick(final int position, final VH viewHolder);

}
