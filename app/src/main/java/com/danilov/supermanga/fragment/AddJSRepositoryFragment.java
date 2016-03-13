package com.danilov.supermanga.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.danilov.supermanga.R;
import com.danilov.supermanga.activity.FilePickerActivity;
import com.danilov.supermanga.activity.MainActivity;
import com.danilov.supermanga.core.repository.RepositoryHolder;
import com.danilov.supermanga.core.repository.special.JSCrud;
import com.danilov.supermanga.core.repository.special.JavaScriptRepository;
import com.danilov.supermanga.core.util.ServiceContainer;

/**
 * Created by Semyon on 30.12.2015.
 */
public class AddJSRepositoryFragment extends BaseFragmentNative implements View.OnClickListener {

    private static final int FILE_PICKER_REQUEST = 1;

    private Button selectPath;
    private Button ok;

    private EditText repositoryTitle;
    private EditText repositoryPath;

    public static AddJSRepositoryFragment newInstance() {
        return new AddJSRepositoryFragment();
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.add_js_repository_fragment, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        selectPath = findViewById(R.id.select_path);
        ok = findViewById(R.id.ok);

        selectPath.setOnClickListener(this);
        ok.setOnClickListener(this);

        repositoryTitle = findViewById(R.id.repository_title);
        repositoryPath = findViewById(R.id.repository_path);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case FILE_PICKER_REQUEST:
                if (resultCode != Activity.RESULT_OK) {
                    return;
                }
                String path = data.getStringExtra(FilePickerActivity.FILE_KEY);
                repositoryPath.setText(path);
                break;
        }
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.ok:
                tryBuildRepository();
                break;
            case R.id.select_path:
                Intent intent = new Intent(this.getActivity(), FilePickerActivity.class);
                startActivityForResult(intent, FILE_PICKER_REQUEST);
                break;
        }
    }

    private void tryBuildRepository() {
        String repositoryPath = this.repositoryPath.getText().toString();
        String repositoryTitle = this.repositoryTitle.getText().toString();

        JavaScriptRepository javaScriptRepository = new JavaScriptRepository(repositoryPath, repositoryTitle);
        JSCrud jsCrud = ServiceContainer.getService(JSCrud.class);
        jsCrud.create(javaScriptRepository);
        ServiceContainer.getService(RepositoryHolder.class).init();
        MainActivity activity = (MainActivity) getActivity();
        activity.showRepositoryPickerFragment();
    }

    private void error(final String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    private void finish() {
        MainActivity activity = (MainActivity) getActivity();
        activity.showDownloadedMangaFragment();
    }

}