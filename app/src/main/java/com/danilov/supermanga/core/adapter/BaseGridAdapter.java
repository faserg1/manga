package com.danilov.supermanga.core.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.danilov.supermanga.R;
import com.danilov.supermanga.core.util.Utils;

/**
 * Created by Semyon on 08.04.2016.
 */
public abstract class BaseGridAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private final Context context;
    private RecyclerView recyclerView;
    private float gridItemRatio;
    private ItemClickListener<VH> clickListener;

    public BaseGridAdapter(@NonNull final RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        this.gridItemRatio = Utils.getFloatResource(R.dimen.grid_item_ratio, recyclerView.getResources());
        this.context = recyclerView.getContext();
        init();
    }

    private void init() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int pColCount = sharedPreferences.getInt("P_COL_COUNT", 0);
        int lColCount = sharedPreferences.getInt("L_COL_COUNT", 0);
        int screenOrientation = getScreenOrientation();
        switch (screenOrientation) {
            case Configuration.ORIENTATION_PORTRAIT:
            case Configuration.ORIENTATION_SQUARE:
                if (pColCount != 0) {
                    GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 3);
                    gridLayoutManager.setSpanCount(pColCount);
                    recyclerView.setLayoutManager(gridLayoutManager);
                }
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                if (lColCount != 0) {
                    GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 3);
                    gridLayoutManager.setSpanCount(lColCount);
                    recyclerView.setLayoutManager(gridLayoutManager);
                }
                break;
        }

    }

    public void setClickListener(final ItemClickListener<VH> clickListener) {
        this.clickListener = clickListener;
    }

    public abstract VH newViewHolder(final ViewGroup parent, final int viewType);

    @Override
    public final VH onCreateViewHolder(final ViewGroup parent, final int viewType) {
        VH viewHolder = newViewHolder(parent, viewType);

        View view = viewHolder.itemView;
        GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
        int spanCount = layoutManager.getSpanCount();
        int width = layoutManager.getWidth();
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        int colWidth = width / spanCount;
        layoutParams.height = (int) (colWidth * gridItemRatio);
        view.setLayoutParams(layoutParams);

        ViewHolderClickListener listener = new ViewHolderClickListener(viewHolder);
        view.setOnClickListener(listener);
        view.setOnLongClickListener(listener);
        return viewHolder;
    }

    private int getScreenOrientation() {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display getOrient = windowManager.getDefaultDisplay();
        int orientation;
        if (getOrient.getWidth() == getOrient.getHeight()) {
            orientation = Configuration.ORIENTATION_SQUARE;
        } else {
            if (getOrient.getWidth() < getOrient.getHeight()) {
                orientation = Configuration.ORIENTATION_PORTRAIT;
            } else {
                orientation = Configuration.ORIENTATION_LANDSCAPE;
            }
        }
        return orientation;
    }

    private class ViewHolderClickListener
            implements View.OnClickListener, View.OnLongClickListener {

        private VH vh;

        public ViewHolderClickListener(final VH vh) {
            this.vh = vh;
        }

        @Override
        public void onClick(final View v) {
            if (clickListener != null) {
                clickListener.onItemClick(vh.getAdapterPosition(), vh);
            }
        }

        @Override
        public boolean onLongClick(final View v) {
            if (clickListener != null) {
                return clickListener.onItemLongClick(vh.getAdapterPosition(), vh);
            }
            return false;
        }

    }

}
