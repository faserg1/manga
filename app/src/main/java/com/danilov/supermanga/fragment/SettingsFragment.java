package com.danilov.supermanga.fragment;

import android.app.Activity;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.danilov.supermanga.R;
import com.danilov.supermanga.activity.FolderPickerActivity;
import com.danilov.supermanga.activity.MainActivity;
import com.danilov.supermanga.core.application.ApplicationSettings;
import com.danilov.supermanga.core.database.DatabaseAccessException;
import com.danilov.supermanga.core.database.MangaDAO;
import com.danilov.supermanga.core.model.Manga;
import com.danilov.supermanga.core.util.ServiceContainer;

import java.util.List;

/**
 * Created by Semyon on 23.02.2015.
 */
public class SettingsFragment extends BaseFragmentNative {

    private static final int FOLDER_PICKER_REQUEST = 1;

//    private EditText downloadPath;
//    private Button selectPath;
//    private CheckBox showControls;
    private Spinner mainPageSelector;

    private ApplicationSettings settings;

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
//        downloadPath = findViewById(R.id.download_path);
        settings = ApplicationSettings.get(getActivity());
//        showControls = findViewById(R.id.show_viewer_controls);
        mainPageSelector = findViewById(R.id.main_page_selector);
        final ApplicationSettings.UserSettings userSettings = settings.getUserSettings();
        final String path = userSettings.getDownloadPath();
        final Activity activity = getActivity(); //hack
        findViewById(R.id.transfer).setOnClickListener(v -> {
            final MangaDAO mangaDAO = ServiceContainer.getService(MangaDAO.class);
            Thread t = new Thread() {
                @Override
                public void run() {
                    try {
                        List<Manga> favorite = mangaDAO.getFavorite();
                        for (Manga manga : favorite) {
                            manga.setTracking(true);
                            mangaDAO.setTracking(manga, true);
                        }
                        activity.runOnUiThread(() -> Toast.makeText(activity, "Готово!", Toast.LENGTH_LONG).show());
                    } catch (DatabaseAccessException e) {
                        activity.runOnUiThread(() -> Toast.makeText(activity, "Ошибка при переносе, пишите разработчику", Toast.LENGTH_LONG).show());
                    }
                }
            };
            t.start();
        });
//        downloadPath.setText(path);
//        selectPath = findViewById(R.id.select_folder);
//        selectPath.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(final View view) {
//                Intent intent = new Intent(getActivity(), FolderPickerActivity.class);
//                intent.putExtra(FolderPickerActivity.FOLDER_KEY, path);
//                startActivityForResult(intent, FOLDER_PICKER_REQUEST);
//            }
//        });
        /*showControls.setChecked(userSettings.isAlwaysShowButtons());
        showControls.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton compoundButton, final boolean b) {
                userSettings.setAlwaysShowButtons(b);
                settings.update(getActivity());
            }
        });*/
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


    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case FOLDER_PICKER_REQUEST:
                final ApplicationSettings.UserSettings userSettings = settings.getUserSettings();
                String path = data.getStringExtra(FolderPickerActivity.FOLDER_KEY);
                userSettings.setDownloadPath(path);
//                downloadPath.setText(path);
                settings.update(getActivity());
                break;
        }
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
