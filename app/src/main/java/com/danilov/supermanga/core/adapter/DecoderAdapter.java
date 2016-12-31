package com.danilov.supermanga.core.adapter;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.danilov.supermanga.R;
import com.danilov.supermanga.core.util.Decoder;

/**
 * Created by Semyon on 31.12.2016.
 */
public class DecoderAdapter implements SpinnerAdapter {

    private Decoder[] values = Decoder.values();

    private Context context;

    public DecoderAdapter(final Context context) {
        this.context = context;
    }

    @Override
    public View getDropDownView(final int i, final View view, final ViewGroup viewGroup) {
        TextView textView = null;
        if (view != null) {
            textView = (TextView) view;
        } else {
            textView = (TextView) View.inflate(context, R.layout.spinner_menu_item_dropdown, null);
        }
        String text = values[i].toString();
        textView.setText(text);
        return textView;
    }

    public Decoder getElement(final int i) {
        return values[i];
    }

    @Override
    public void registerDataSetObserver(final DataSetObserver dataSetObserver) {

    }

    @Override
    public void unregisterDataSetObserver(final DataSetObserver dataSetObserver) {

    }

    @Override
    public int getCount() {
        return values.length;
    }

    @Override
    public Object getItem(final int i) {
        return values[i];
    }

    @Override
    public long getItemId(final int i) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(final int i, final View view, final ViewGroup viewGroup) {
        TextView textView = null;
        if (view != null) {
            textView = (TextView) view;
        } else {
            textView = (TextView) View.inflate(context, R.layout.spinner_menu_item_selected, null);
        }
        String text = values[i].toString();
        textView.setText(text);
        return textView;
    }

    @Override
    public int getItemViewType(final int i) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

}
