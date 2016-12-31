package com.danilov.supermanga.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.danilov.supermanga.R;
import com.danilov.supermanga.activity.MainActivity;
import com.danilov.supermanga.core.adapter.DecoderAdapter;
import com.danilov.supermanga.core.application.ApplicationSettings;
import com.danilov.supermanga.core.database.DatabaseAccessException;
import com.danilov.supermanga.core.database.MangaDAO;
import com.danilov.supermanga.core.model.Manga;
import com.danilov.supermanga.core.util.Decoder;
import com.danilov.supermanga.core.util.ServiceContainer;

import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Semyon on 23.02.2015.
 */
public class SettingsFragment extends BaseFragmentNative {

    private Spinner mainPageSelector;

    private Spinner decoderSpinner;

    private ApplicationSettings settings;

    @Bind(R.id.dark_theme)
    public CheckBox checkBox;

    @Bind(R.id.portrait_col_count)
    public SeekBar portraitColCount;

    @Bind(R.id.landscape_col_count)
    public SeekBar landscapeColCount;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.manga_settings_fragment, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        ButterKnife.bind(this, view);
        settings = ApplicationSettings.get(getActivity());
        mainPageSelector = findViewById(R.id.main_page_selector);
        decoderSpinner = findViewById(R.id.decoder_selector);
        final ApplicationSettings.UserSettings userSettings = settings.getUserSettings();
        final String path = userSettings.getDownloadPath();
        final Activity activity = getActivity(); //hack

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        boolean darkTheme = sharedPreferences.getBoolean("DARK_THEME", false);
        checkBox.setChecked(darkTheme);

        checkBox.setOnCheckedChangeListener((a, b) -> {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(applicationContext);
            sp.edit().putBoolean("DARK_THEME", b).apply();

            Intent intent = new Intent(activity, MainActivity.class);
            intent.putExtra(MainActivity.PAGE, MainActivity.MainMenuItem.SETTINGS.toString());
            activity.startActivity(intent);
            activity.finish();
        });


        int pColCount = sharedPreferences.getInt("P_COL_COUNT", 0);
        int lColCount = sharedPreferences.getInt("L_COL_COUNT", 0);

        portraitColCount.setProgress(pColCount);
        landscapeColCount.setProgress(lColCount);

        portraitColCount.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
                if (fromUser) {
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(applicationContext);
                    sp.edit().putInt("P_COL_COUNT", progress).apply();
                }
            }

            @Override
            public void onStartTrackingTouch(final SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {

            }
        });

        landscapeColCount.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
                if (fromUser) {
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(applicationContext);
                    sp.edit().putInt("L_COL_COUNT", progress).apply();
                }
            }

            @Override
            public void onStartTrackingTouch(final SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {

            }
        });

        final DecoderAdapter decoderAdapter = new DecoderAdapter(getActivity());
        decoderSpinner.setAdapter(decoderAdapter);
        decoderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
                Decoder element = decoderAdapter.getElement(position);
                settings.setDecoder(element);
                settings.update(getActivity());
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {

            }
        });
        Decoder decoder = settings.getDecoder();
        int decoderIdx = Arrays.binarySearch(Decoder.values(), decoder);
        decoderSpinner.setSelection(decoderIdx, false);

        final MainPageAdapter adapter = new MainPageAdapter();
        mainPageSelector.setAdapter(adapter);
        mainPageSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(final AdapterView<?> adapterView, final View view, final int i, final long l) {
                MainActivity.MainMenuItem item = adapter.getElement(i);
                settings.setMainMenuItem(item.toString());
                settings.update(getActivity());
            }

            @Override
            public void onNothingSelected(final AdapterView<?> adapterView) {

            }

        });
        String mainMenuItem = settings.getMainMenuItem();
        int idx = 0;
        MainActivity.MainMenuItem[] values = MainActivity.MainMenuItem.values();
        for (int i = 0; i < values.length; i++) {
            MainActivity.MainMenuItem menuItem = values[i];
            if (menuItem.toString().equals(mainMenuItem)) {
                idx = i;
            }
        }
        mainPageSelector.setSelection(idx, false);
        super.onActivityCreated(savedInstanceState);
    }

    private class MainPageAdapter implements SpinnerAdapter {

        private MainActivity.MainMenuItem[] items = MainActivity.MainMenuItem.values();

        @Override
        public View getDropDownView(final int i, final View view, final ViewGroup viewGroup) {
            TextView textView = null;
            if (view != null) {
                textView = (TextView) view;
            } else {
                textView = (TextView) View.inflate(getActivity(), R.layout.spinner_menu_item_dropdown, null);
            }
            String text = getActivity().getString(items[i].getStringId());
            textView.setText(text);
            return textView;
        }

        public MainActivity.MainMenuItem getElement(final int i) {
            return items[i];
        }

        @Override
        public void registerDataSetObserver(final DataSetObserver dataSetObserver) {

        }

        @Override
        public void unregisterDataSetObserver(final DataSetObserver dataSetObserver) {

        }

        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public Object getItem(final int i) {
            return items[i];
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
                textView = (TextView) View.inflate(getActivity(), R.layout.spinner_menu_item_selected, null);
            }
            String text = getActivity().getString(items[i].getStringId());
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


}
