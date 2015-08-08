package com.danilov.mangareaderplus.fragment;

import android.app.Activity;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.danilov.mangareaderplus.R;
import com.danilov.mangareaderplus.activity.FolderPickerActivity;
import com.danilov.mangareaderplus.activity.MainActivity;
import com.danilov.mangareaderplus.core.application.ApplicationSettings;

/**
 * Created by Semyon on 23.02.2015.
 */
public class SettingsFragment extends BaseFragment {

    private static final int FOLDER_PICKER_REQUEST = 1;

    private EditText downloadPath;
    private Button selectPath;
    private CheckBox showControls;
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
        downloadPath = findViewById(R.id.download_path);
        settings = ApplicationSettings.get(getActivity());
        showControls = findViewById(R.id.show_viewer_controls);
        mainPageSelector = findViewById(R.id.main_page_selector);
        final String path = settings.getMangaDownloadBasePath();

        downloadPath.setText(path);
        selectPath = findViewById(R.id.select_folder);
        selectPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Intent intent = new Intent(getActivity(), FolderPickerActivity.class);
                intent.putExtra(FolderPickerActivity.FOLDER_KEY, path);
                startActivityForResult(intent, FOLDER_PICKER_REQUEST);
            }
        });
        showControls.setChecked(settings.isShowViewerButtonsAlways());
        showControls.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton compoundButton, final boolean b) {
                settings.setShowViewerButtonsAlways(b);
                settings.update(getActivity());
            }
        });
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
                String path = data.getStringExtra(FolderPickerActivity.FOLDER_KEY);
                settings.setMangaDownloadBasePath(path);
                downloadPath.setText(path);
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
