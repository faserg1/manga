package com.danilov.mangareaderplus.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.danilov.mangareaderplus.R;
import com.danilov.mangareaderplus.activity.FolderPickerActivity;
import com.danilov.mangareaderplus.core.application.ApplicationSettings;

/**
 * Created by Semyon on 23.02.2015.
 */
public class SettingsFragment extends BaseFragment {

    private static final int FOLDER_PICKER_REQUEST = 1;

    private EditText downloadPath;
    private Button selectPath;
    private CheckBox showControls;

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


}
