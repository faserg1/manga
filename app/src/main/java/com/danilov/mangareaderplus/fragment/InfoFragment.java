package com.danilov.mangareaderplus.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.httpimage.HttpImageManager;
import com.danilov.mangareaderplus.R;
import com.danilov.mangareaderplus.activity.MangaInfoActivity;
import com.danilov.mangareaderplus.activity.MangaViewerActivity;
import com.danilov.mangareaderplus.core.animation.OverXFlipper;
import com.danilov.mangareaderplus.core.database.DatabaseAccessException;
import com.danilov.mangareaderplus.core.database.MangaDAO;
import com.danilov.mangareaderplus.core.interfaces.RefreshableActivity;
import com.danilov.mangareaderplus.core.model.Manga;
import com.danilov.mangareaderplus.core.repository.RepositoryEngine;
import com.danilov.mangareaderplus.core.repository.RepositoryException;
import com.danilov.mangareaderplus.core.util.BitmapUtils;
import com.danilov.mangareaderplus.core.util.Constants;
import com.danilov.mangareaderplus.core.util.ServiceContainer;
import com.danilov.mangareaderplus.core.util.Utils;
import com.danilov.mangareaderplus.core.view.ScrollViewParallax;

/**
 * Created by Semyon on 09.11.2014.
 */
public class InfoFragment extends BaseFragment implements View.OnClickListener {

    private final String TAG = "InfoFragment";

    private MangaInfoActivity activity;
    private RefreshableActivity refreshable;

    private HttpImageManager httpImageManager = null;

    private ScrollViewParallax scrollViewParallax;

    private TextView mangaDescriptionTextView = null;
    private TextView chaptersQuantityTextView = null;
    private TextView mangaTitle = null;
    private ImageView mangaCover = null;

    private View downloadButton;
    private View readOnlineButton;

    private View addToFavorites;
    private View removeFromFavorites;

    private MangaDAO mangaDAO = null;

    private Manga manga;

    private boolean isLoading = false;
    private boolean hasCoverLoaded = false;

    private OverXFlipper flipper;

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
        downloadButton = view.findViewById(R.id.download);
        readOnlineButton = view.findViewById(R.id.read_online);
        scrollViewParallax = findViewById(R.id.scrollView);
        downloadButton.setOnClickListener(this);
        readOnlineButton.setOnClickListener(this);
        mangaDAO = ServiceContainer.getService(MangaDAO.class);
        httpImageManager = ServiceContainer.getService(HttpImageManager.class);
        addToFavorites = view.findViewById(R.id.add_to_favorites);
        removeFromFavorites = view.findViewById(R.id.remove_from_favorites);

        addToFavorites.setOnClickListener(this);
        removeFromFavorites.setOnClickListener(this);

        flipper = new OverXFlipper(addToFavorites, removeFromFavorites, 300);
        disableButtons();
        if (savedInstanceState == null) {
            Intent i = activity.getIntent();
            manga = i.getParcelableExtra(Constants.MANGA_PARCEL_KEY);
            if (manga != null) {
                loadMangaInfo(manga);
            }
        } else {
            restoreInstanceState(savedInstanceState);
        }
        final int baseColor = getResources().getColor(R.color.color_primary);
        final float size = getResources().getDimension(R.dimen.info_parallax_image_height);
        activity.getToolbar().setBackgroundColor(Utils.getColorWithAlpha(.0f, baseColor));
        scrollViewParallax.setScrollListener(new ScrollViewParallax.ScrollListener() {
            @Override
            public void onScroll(final int horizontal, final int vertical, final int oldl, final int oldt) {
                float alpha = 1 - (float) Math.max(0, size - vertical) / size;
                activity.getToolbar().setBackgroundColor(Utils.getColorWithAlpha(alpha, baseColor));
            }
        });
    }

    private void loadMangaInfo(final Manga manga) {
        String coverUrl = manga.getCoverUri();
        if (coverUrl != null) {
            hasCoverLoaded = true;
            Uri coverUri = Uri.parse(coverUrl);
            final int sizeOfImage = 2 * getResources().getDimensionPixelSize(R.dimen.info_parallax_image_height);

            Bitmap bitmap = httpImageManager.loadImage(new HttpImageManager.LoadRequest(coverUri, new LoadResponseListener()));

            if (bitmap != null) {
                setCover(bitmap);
            }
        }
        mangaTitle.setText(manga.getTitle());
        String mangaDescription = manga.getDescription();
        if (mangaDescription != null) {
            if (manga.isFavorite()) {
                flipper.flip(2);
            }
            mangaDescriptionTextView.setText(mangaDescription);
            chaptersQuantityTextView.setText(String.valueOf(manga.getChaptersQuantity()));
            enableButtons();
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

    private void enableButtons() {
        downloadButton.setEnabled(true);
        downloadButton.setClickable(true);
        readOnlineButton.setEnabled(true);
        readOnlineButton.setClickable(true);
        addToFavorites.setEnabled(true);
        addToFavorites.setClickable(true);
        removeFromFavorites.setEnabled(true);
        removeFromFavorites.setClickable(true);
    }

    private void disableButtons() {
        downloadButton.setEnabled(false);
        downloadButton.setClickable(false);
        readOnlineButton.setEnabled(false);
        readOnlineButton.setClickable(false);
        addToFavorites.setEnabled(false);
        addToFavorites.setClickable(false);
        removeFromFavorites.setEnabled(false);
        removeFromFavorites.setClickable(false);
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
            try {
                Manga _manga = mangaDAO.getByLinkAndRepository(manga.getUri(), manga.getRepository());
                if (_manga != null) {
                    manga.setId(_manga.getId());
                    manga.setFavorite(_manga.isFavorite());
                }
            } catch (DatabaseAccessException e) {
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
                        if (manga.isFavorite()) {
                            flipper.flip(2);
                        }
                        enableButtons();
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

    @Override
    public void onClick(final View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.download:
                activity.showChaptersFragment();
                break;
            case R.id.add_to_favorites:
                addToFavorites();
                break;
            case R.id.remove_from_favorites:
                removeFromFavorites();
                break;
            case R.id.read_online:
                intent = new Intent(activity, MangaViewerActivity.class);
                intent.putExtra(Constants.MANGA_PARCEL_KEY, manga);
                startActivity(intent);
                break;

        }
    }

    private void addToFavorites() {
        flipper.flip();
        try {
            manga.setFavorite(true);
            mangaDAO.setFavorite(manga, true);
        } catch (DatabaseAccessException e) {
            Context context = getActivity();
            String message = Utils.errorMessage(context, e.getMessage(), R.string.p_internet_error);
            Utils.showToast(context, message);
        }
    }

    private void removeFromFavorites() {
        flipper.flip();
        try {
            manga.setFavorite(false);
            mangaDAO.setFavorite(manga, false);
        } catch (DatabaseAccessException e) {
            Context context = getActivity();
            String message = Utils.errorMessage(context, e.getMessage(), R.string.p_internet_error);
            Utils.showToast(context, message);
        }
    }

}
