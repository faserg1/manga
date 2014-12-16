package com.danilov.mangareader.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.httpimage.HttpImageManager;
import com.danilov.mangareader.R;
import com.danilov.mangareader.activity.MangaInfoActivity;
import com.danilov.mangareader.activity.MangaViewerActivity;
import com.danilov.mangareader.core.interfaces.RefreshableActivity;
import com.danilov.mangareader.core.model.Manga;
import com.danilov.mangareader.core.repository.RepositoryEngine;
import com.danilov.mangareader.core.repository.RepositoryException;
import com.danilov.mangareader.core.util.BitmapUtils;
import com.danilov.mangareader.core.util.Constants;
import com.danilov.mangareader.core.util.ServiceContainer;
import com.danilov.mangareader.core.util.Utils;

/**
 * Created by Semyon on 09.11.2014.
 */
public class InfoFragment extends Fragment {

    private final String TAG = "InfoFragment";

    private MangaInfoActivity activity;
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
        activity = (MangaInfoActivity) getActivity();
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

            Bitmap bitmap = httpImageManager.loadImage(new HttpImageManager.LoadRequest(coverUri, new LoadResponseListener(), sizeOfImage));

            if (bitmap != null) {
                setCover(bitmap);
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

    private boolean isDetached = false;

    @Override
    public void onDetach() {
        isDetached = true;
        super.onDetach();
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
                    if (isDetached) {
                        return;
                    }
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

                                Bitmap bitmap = httpImageManager.loadImage(new HttpImageManager.LoadRequest(coverUri, new LoadResponseListener(), sizeOfImage));

                                if (bitmap != null) {
                                    setCover(bitmap);
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

    private class LoadResponseListener implements HttpImageManager.OnLoadResponseListener {

        @Override
        public void beforeLoad(final HttpImageManager.LoadRequest r) {

        }

        @Override
        public void onLoadResponse(final HttpImageManager.LoadRequest r, final Bitmap data) {
            setCover(data);
        }

        @Override
        public void onLoadError(final HttpImageManager.LoadRequest r, final Throwable e) {

        }

    }

    private void setCover(final Bitmap bmp) {
        mangaCover.setImageBitmap(bmp);
        ImageView bigView = (ImageView) view.findViewById(R.id.very_big);
        if (bigView != null) {
            blur(bmp, bigView);
        }
    }

    private void blur(Bitmap bkg, ImageView view) {
        view.setImageBitmap(BitmapUtils.blur(bkg, 5));
    }

    private class ButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(final View v) {
            Intent intent = null;
            switch (v.getId()) {
                case R.id.download:
                    activity.showChaptersFragment();
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
