package com.danilov.manga.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.httpimage.HttpImageManager;
import com.danilov.manga.R;
import com.danilov.manga.activity.DownloadsActivity;
import com.danilov.manga.activity.MangaViewerActivity;
import com.danilov.manga.core.interfaces.RefreshableActivity;
import com.danilov.manga.core.model.Manga;
import com.danilov.manga.core.repository.RepositoryEngine;
import com.danilov.manga.core.repository.RepositoryException;
import com.danilov.manga.core.util.Constants;
import com.danilov.manga.core.util.ServiceContainer;
import com.danilov.manga.core.util.Utils;
import com.danilov.manga.core.view.AnimatedActionView;

/**
 * Created by Semyon on 09.11.2014.
 */
public class InfoFragment extends Fragment {

    private final String TAG = "InfoFragment";

    private ActionBarActivity activity;
    private RefreshableActivity refreshable;

    private HttpImageManager httpImageManager = null;

    private TextView mangaDescriptionTextView = null;
    private TextView chaptersQuantityTextView = null;
    private TextView mangaTitle = null;
    private ImageView mangaCover = null;

    private Button downloadButton;
    private Button readOnlineButton;

    private View view;

    private Manga manga;

    private boolean isLoading = false;
    private boolean hasCoverLoaded = false;

    public static InfoFragment newInstance(final Manga manga) {
        InfoFragment infoFragment = new InfoFragment();
        infoFragment.manga = manga;
        return infoFragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.manga_info_item, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = (ActionBarActivity) getActivity();
        refreshable = (RefreshableActivity) getActivity();
        mangaDescriptionTextView = (TextView) view.findViewById(R.id.manga_description);
        chaptersQuantityTextView = (TextView) view.findViewById(R.id.chapters_quantity);
        mangaTitle = (TextView) view.findViewById(R.id.manga_title);
        mangaCover = (ImageView) view.findViewById(R.id.manga_cover);
        downloadButton = (Button) view.findViewById(R.id.download);
        readOnlineButton = (Button) view.findViewById(R.id.read_online);
        ButtonClickListener buttonClickListener = new ButtonClickListener();
        downloadButton.setOnClickListener(buttonClickListener);
        readOnlineButton.setOnClickListener(buttonClickListener);
        httpImageManager = ServiceContainer.getService(HttpImageManager.class);
        if (savedInstanceState == null) {
            Intent i = activity.getIntent();
            manga = i.getParcelableExtra(Constants.MANGA_PARCEL_KEY);
            if (manga != null) {
                loadMangaInfo(manga);
            }
        } else {
            restoreInstanceState(savedInstanceState);
        }
    }

    private void loadMangaInfo(final Manga manga) {
        String coverUrl = manga.getCoverUri();
        if (coverUrl != null) {
            hasCoverLoaded = true;
            Uri coverUri = Uri.parse(coverUrl);
            final int sizeOfImage = getResources().getDimensionPixelSize(R.dimen.manga_info_height);
            HttpImageManager.LoadRequest request = HttpImageManager.LoadRequest.obtain(coverUri, mangaCover, sizeOfImage);
            Bitmap bitmap = httpImageManager.loadImage(request);
            if (bitmap != null) {
                mangaCover.setImageBitmap(bitmap);
            }
        }
        mangaTitle.setText(manga.getTitle());
        String mangaDescription = manga.getDescription();
        if (mangaDescription != null) {
            mangaDescriptionTextView.setText(mangaDescription);
            chaptersQuantityTextView.setText(String.valueOf(manga.getChaptersQuantity()));
        } else {
            isLoading = true;
            refreshable.startRefresh();
            MangaInfoQueryThread thread = new MangaInfoQueryThread(manga);
            thread.start();
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        if (manga != null) {
            outState.putParcelable(Constants.MANGA_PARCEL_KEY, manga);
        }
        super.onSaveInstanceState(outState);
    }

    public void restoreInstanceState(final Bundle savedInstanceState) {
        manga = savedInstanceState.getParcelable(Constants.MANGA_PARCEL_KEY);
        if (manga != null) {
            loadMangaInfo(manga);
        }
        if (isLoading) {
            refreshable.startRefresh();
        } else {
            refreshable.stopRefresh();
        }
    }

    private class MangaInfoQueryThread extends Thread {

        private boolean loaded = false;
        private Manga manga;
        private String error = null;

        public MangaInfoQueryThread(final Manga manga) {
            this.manga = manga;
        }

        @Override
        public void run() {
            RepositoryEngine repositoryEngine = manga.getRepository().getEngine();
            try {
                loaded = repositoryEngine.queryForMangaDescription(manga);
            } catch (RepositoryException e) {
                error = e.getMessage();
                Log.d(TAG, e.getMessage());
            }
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (loaded) {
                        String mangaDescription = manga.getDescription();
                        mangaDescriptionTextView.setText(mangaDescription);
                        chaptersQuantityTextView.setText(String.valueOf(manga.getChaptersQuantity()));
                        if (!hasCoverLoaded) {
                            String coverUrl = manga.getCoverUri();
                            if (coverUrl != null) {
                                hasCoverLoaded = true;
                                Uri coverUri = Uri.parse(coverUrl);
                                final int sizeOfImage = getResources().getDimensionPixelSize(R.dimen.manga_info_height);
                                HttpImageManager.LoadRequest request = HttpImageManager.LoadRequest.obtain(coverUri, mangaCover, sizeOfImage);
                                Bitmap bitmap = httpImageManager.loadImage(request);
                                if (bitmap != null) {
                                    mangaCover.setImageBitmap(bitmap);
                                }
                            }
                        }
                    } else {
                        Context context = getActivity();
                        String message = Utils.errorMessage(context, error, R.string.p_internet_error);
                        Utils.showToast(context, message);
                    }
                    isLoading = false;
                    refreshable.stopRefresh();
                }

            });
        }

    }

    private class ButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(final View v) {
            Intent intent = null;
            switch (v.getId()) {
                case R.id.download:
                    intent = new Intent(activity, DownloadsActivity.class);
                    intent.putExtra(Constants.MANGA_PARCEL_KEY, manga);
                    startActivity(intent);
                    break;
                case R.id.add_to_favorites:
                    break;
                case R.id.read_online:
                    intent = new Intent(activity, MangaViewerActivity.class);
                    intent.putExtra(Constants.MANGA_PARCEL_KEY, manga);
                    startActivity(intent);
                    break;
            }
        }
    }
}
