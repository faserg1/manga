package com.danilov.manga.test;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import com.danilov.manga.R;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Semyon Danilov on 11.06.2014.
 */
public class DownloadTestActivity extends Activity {

    private static final String TAG = "DownloadTestActivity";

    private GridView gridView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_download_activity);
        gridView = (GridView) findViewById(R.id.grid_view);
        gridView.setAdapter(new GridViewAdapter(getApplicationContext(), 0, new Object[15]));
    }

    private class GridViewAdapter extends ArrayAdapter {

        public GridViewAdapter(final Context context, final int resource, final Object[] objects) {
            super(context, resource, objects);
        }

        @Nullable
        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.manga_grid_item, parent, false);
            }
            return view;
        }
    }

}