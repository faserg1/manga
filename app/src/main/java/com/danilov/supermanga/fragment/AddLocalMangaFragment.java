package com.danilov.supermanga.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.danilov.supermanga.R;
import com.danilov.supermanga.activity.FolderPickerActivity;
import com.danilov.supermanga.activity.MainActivity;
import com.danilov.supermanga.core.database.DatabaseAccessException;
import com.danilov.supermanga.core.database.MangaDAO;
import com.danilov.supermanga.core.model.LocalManga;
import com.danilov.supermanga.core.model.MangaChapter;
import com.danilov.supermanga.core.repository.OfflineEngine;
import com.danilov.supermanga.core.repository.RepositoryEngine;
import com.danilov.supermanga.core.repository.RepositoryException;
import com.danilov.supermanga.core.util.ServiceContainer;

import java.util.List;

/**
 * Created by Semyon on 30.12.2015.
 */
public class AddLocalMangaFragment extends BaseFragment implements View.OnClickListener {

    private static final int FOLDER_PICKER_REQUEST = 1;

    private Button selectPath;
    private Button ok;

    private EditText mangaPath;
    private EditText mangaTitle;
    private EditText mangaDescription;

    public static AddLocalMangaFragment newInstance() {
        return new AddLocalMangaFragment();
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.add_local_manga_fragment, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        selectPath = findViewById(R.id.select_path);
        ok = findViewById(R.id.ok);

        selectPath.setOnClickListener(this);
        ok.setOnClickListener(this);

        mangaPath = findViewById(R.id.manga_path);
        mangaTitle = findViewById(R.id.manga_title);
        mangaDescription = findViewById(R.id.manga_description);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case FOLDER_PICKER_REQUEST:
                if (resultCode != Activity.RESULT_OK) {
                    return;
                }
                String path = data.getStringExtra(FolderPickerActivity.FOLDER_KEY);
                mangaPath.setText(path);
                break;
        }
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.ok:
                tryBuildManga();
                break;
            case R.id.select_path:
                Intent intent = new Intent(this.getActivity(), FolderPickerActivity.class);
                startActivityForResult(intent, FOLDER_PICKER_REQUEST);
                break;
        }
    }

    private void tryBuildManga() {
        String title = mangaTitle.getText().toString();
        String description = mangaDescription.getText().toString();
        String mangaPath = this.mangaPath.getText().toString();
        LocalManga localManga = new LocalManga(title, mangaPath, RepositoryEngine.Repository.OFFLINE);
        localManga.setLocalUri(mangaPath);
        localManga.setDescription(description);
        localManga.setAuthor("");
        localManga.setGenres("");
        localManga.setFavorite(true);
        OfflineEngine engine = (OfflineEngine) RepositoryEngine.Repository.OFFLINE.getEngine();
        boolean success = false;
        try {
            success = engine.queryForChapters(localManga);
        } catch (RepositoryException e) {
            error(e.getMessage());
            return;
        }
        if (success) {
            List<MangaChapter> chapters = localManga.getChapters();
            if (chapters != null) {
                localManga.setChaptersQuantity(chapters.size());
            }
            MangaDAO mangaDAO = ServiceContainer.getService(MangaDAO.class);
            try {
                mangaDAO.addManga(localManga);
                Toast.makeText(getActivity(), "Манга успешно добавлена", Toast.LENGTH_LONG).show();
                finish();
            } catch (DatabaseAccessException e) {
                error(e.getMessage());
                return;
            }
        } else {
            error("Unknown error");
            return;
        }
    }

    private void error(final String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    private void finish() {
        MainActivity activity = (MainActivity) getActivity();
        activity.showDownloadedMangaFragment();
    }

}