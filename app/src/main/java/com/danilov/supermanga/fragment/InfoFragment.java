package com.danilov.supermanga.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.httpimage.HttpImageManager;
import com.danilov.supermanga.R;
import com.danilov.supermanga.activity.MangaInfoActivity;
import com.danilov.supermanga.activity.MangaViewerActivity;
import com.danilov.supermanga.core.animation.OverXFlipper;
import com.danilov.supermanga.core.database.DatabaseAccessException;
import com.danilov.supermanga.core.database.HistoryDAO;
import com.danilov.supermanga.core.database.MangaDAO;
import com.danilov.supermanga.core.interfaces.RefreshableActivity;
import com.danilov.supermanga.core.model.HistoryElement;
import com.danilov.supermanga.core.model.Manga;
import com.danilov.supermanga.core.repository.RepositoryEngine;
import com.danilov.supermanga.core.repository.RepositoryException;
import com.danilov.supermanga.core.util.BitmapUtils;
import com.danilov.supermanga.core.util.Constants;
import com.danilov.supermanga.core.util.ServiceContainer;
import com.danilov.supermanga.core.util.Utils;
import com.danilov.supermanga.core.view.ScrollViewParallax;
import com.danilov.supermanga.core.view.ViewV16;
import com.danilov.supermanga.core.widget.ToggleImageButton;

/**
 * Created by Semyon on 09.11.2014.
 */
public class InfoFragment extends BaseFragmentNative implements View.OnClickListener, View.OnLongClickListener, CompoundButton.OnCheckedChangeListener {

    private final String TAG = "InfoFragment";

    private long ANIM_DURATION = 500l;

    private MangaInfoActivity activity;
    private RefreshableActivity refreshable;

    private HttpImageManager httpImageManager = null;

    private ScrollViewParallax scrollViewParallax;

    private TextView mangaDescriptionTextView = null;
    private TextView chaptersQuantityTextView = null;
    private TextView mangaTitle = null;
    private ImageButton repositoryLink = null;
    private ImageButton manageChapters = null;
    private TextView authors = null;
    private TextView genres = null;
    private TextView repositoryTitle = null;
    private ImageView mangaCover = null;

    private View downloadButton;
    private View readOnlineButton;

    private View addToTracking;
    private View removeFromTracking;

    private ToggleImageButton toggleFavorite;

    private MangaDAO mangaDAO = null;

    private Manga manga;

    private boolean isLoading = false;
    private boolean hasCoverLoaded = false;

    private OverXFlipper flipper;

    private int left;
    private int top;
    private int width;
    private int height;
    private boolean shown = false;
    private float widthScale;
    private float heightScale;
    private int leftDelta;
    private int topDelta;
    private boolean isDetached = false;

    public static InfoFragment newInstance(final Manga manga, final int left, final int top, final int width, final int height) {
        InfoFragment infoFragment = new InfoFragment();
        infoFragment.manga = manga;
        infoFragment.top = top;
        infoFragment.left = left;
        infoFragment.width = width;
        infoFragment.height = height;
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
        repositoryLink = findViewById(R.id.repository_link);
        mangaCover = (ImageView) view.findViewById(R.id.manga_cover);
        downloadButton = view.findViewById(R.id.download);
        readOnlineButton = view.findViewById(R.id.read_online);
        manageChapters = findViewById(R.id.chapters_list);
        scrollViewParallax = findViewById(R.id.scrollView);
        authors = findViewById(R.id.authors);
        genres = findViewById(R.id.genres);
        repositoryTitle = findViewById(R.id.repository_title);
        downloadButton.setOnClickListener(this);
        downloadButton.setOnLongClickListener(this);
        readOnlineButton.setOnClickListener(this);
        readOnlineButton.setOnLongClickListener(this);
        repositoryLink.setOnClickListener(this);
        repositoryLink.setOnLongClickListener(this);
        mangaDAO = ServiceContainer.getService(MangaDAO.class);
        httpImageManager = ServiceContainer.getService(HttpImageManager.class);
        addToTracking = view.findViewById(R.id.add_to_tracking);
        removeFromTracking = view.findViewById(R.id.remove_from_tracking);
        toggleFavorite = findViewById(R.id.toggle_favorite);
        toggleFavorite.setOnCheckedChangeListener(this);
        toggleFavorite.setOnLongClickListener(this);
        manageChapters.setOnLongClickListener(this);

        manageChapters.setOnClickListener(this);
        addToTracking.setOnClickListener(this);
        removeFromTracking.setOnClickListener(this);

        flipper = new OverXFlipper(addToTracking, removeFromTracking, 300);
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
        if (savedInstanceState == null && !shown) {
            mangaCover.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mangaCover.getViewTreeObserver().removeOnPreDrawListener(this);
                    int[] onScreenLocation = new int[2];
                    mangaCover.getLocationOnScreen(onScreenLocation);
                    leftDelta = left - onScreenLocation[0];
                    topDelta = top - onScreenLocation[1];
                    widthScale = (float) width / mangaCover.getWidth();
                    heightScale = (float) height / mangaCover.getHeight();
                    runAnim();
                    return true;
                }
            });
        } else {
            final MangaInfoActivity infoActivity = (MangaInfoActivity) getActivity();
            infoActivity.toggleOverlayBackground(true);
        }
        shown = true;
    }

    private void runAnim() {

        final long duration = (long) (ANIM_DURATION);

        final ViewV16 mangaCover = ViewV16.wrap(this.mangaCover);
        final ViewV16 addToFavorites = ViewV16.wrap(this.addToTracking);
        final ViewV16 removeFromFavorites = ViewV16.wrap(this.removeFromTracking);

        mangaCover.setPivotX(0);
        mangaCover.setPivotY(0);
        mangaCover.setScaleX(widthScale);
        mangaCover.setScaleY(heightScale);
        mangaCover.setTranslationX(leftDelta);
        mangaCover.setTranslationY(topDelta);

        addToFavorites.setScaleX(0);
        addToFavorites.setScaleY(0);
        removeFromFavorites.setScaleX(0);
        removeFromFavorites.setScaleY(0);

        final ViewV16 body = ViewV16.wrap(findViewById(R.id.body));
        body.setAlpha(0);
        body.setTranslationY(200);

        final MangaInfoActivity infoActivity = (MangaInfoActivity) getActivity();

        body.animate().setDuration(ANIM_DURATION).alpha(1).translationY(0).withEndAction(new Runnable() {
            @Override
            public void run() {
                addToFavorites.animate().setDuration(ANIM_DURATION).scaleY(1).scaleX(1);
                removeFromFavorites.animate().scaleY(1).scaleX(1);
                infoActivity.toggleOverlayBackground(true);
            }
        });

        final ViewV16 bigView = ViewV16.wrap(view.findViewById(R.id.very_big));
        if (bigView != null) {
            bigView.animate().setDuration(0).alpha(0).withEndAction(new Runnable() {
                @Override
                public void run() {
                    bigView.animate().setDuration(ANIM_DURATION).alpha(1);
                }
            });
        }

        ViewV16.ViewPropertyAnimator pa = mangaCover.animate().setDuration(duration).scaleX(1).scaleY(1).translationX(0).translationY(0)
                .setInterpolator(new DecelerateInterpolator());

        //TODO: android 2.3 fix
        final int version = Integer.valueOf(Build.VERSION.SDK);
        if (version < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            pa.withEndAction(new Runnable() {
                @Override
                public void run() {
                    ((ViewGroup) InfoFragment.this.mangaCover.getParent().getParent()).setClipChildren(true);
                }
            });
        }
    }

    private boolean backAccepted = false;

    @Override
    public boolean onBackPressed() {
        if (backAccepted) {
            return true;
        }
        backAccepted = true;
        final long duration = (long) (ANIM_DURATION);

        final ViewV16 mangaCover = ViewV16.wrap(this.mangaCover);
        final ViewV16 addToFavorites = ViewV16.wrap(this.addToTracking);
        final ViewV16 removeFromFavorites = ViewV16.wrap(this.removeFromTracking);
        final ViewV16 body = ViewV16.wrap(findViewById(R.id.body));

        mangaCover.animate().cancel();
        addToFavorites.animate().cancel();
        removeFromFavorites.animate().cancel();
        body.animate().cancel();

        mangaCover.setPivotX(0);
        mangaCover.setPivotY(0);

        final MangaInfoActivity infoActivity = (MangaInfoActivity) getActivity();

        addToFavorites.animate().setDuration(ANIM_DURATION).scaleY(0).scaleX(0).withEndAction(new Runnable() {
            @Override
            public void run() {
                body.animate().setDuration(ANIM_DURATION).alpha(0).translationY(200);

                infoActivity.toggleOverlayBackground(false);

                final ViewV16 bigView = ViewV16.wrap(view.findViewById(R.id.very_big));
                if (bigView != null) {
                    bigView.animate().setDuration(ANIM_DURATION).alpha(0);
                }

                //TODO: android 2.3 fix
                final int version = Integer.valueOf(Build.VERSION.SDK);
                if (version < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    ((ViewGroup) InfoFragment.this.mangaCover.getParent().getParent()).setClipChildren(false);
                }

                mangaCover.animate().setDuration(duration).scaleX(widthScale).scaleY(heightScale).translationX(leftDelta).translationY(topDelta)
                        .setInterpolator(new DecelerateInterpolator()).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        Activity activity = getActivity();
                        if (activity != null) {
                            activity.finish();
                        }
                    }
                });

            }
        });
        removeFromFavorites.animate().scaleY(0).scaleX(0);
        return true;
    }

    private void loadMangaInfo(final Manga manga) {
        String coverUrl = manga.getCoverUri();
        if (coverUrl != null) {
            hasCoverLoaded = true;
            Uri coverUri = Uri.parse(coverUrl);
            final int sizeOfImage = 2 * getResources().getDimensionPixelSize(R.dimen.info_parallax_image_height);

            Bitmap bitmap = httpImageManager.loadImage(new HttpImageManager.LoadRequest(coverUri, new LoadResponseListener(), manga.getRepository().getEngine().getRequestPreprocessor()));

            if (bitmap != null) {
                setCover(bitmap);
            }
        }
        mangaTitle.setText(manga.getTitle());


        String repoName = manga.getRepository().getName();
        repositoryTitle.setText(repoName);

        String mangaDescription = manga.getDescription();
        if (mangaDescription != null) {
            if (manga.isTracking()) {
                flipper.flip(2);
            }
            if (manga.isFavorite()) {
                toggleFavorite.setIsChecked(true);
            }
            mangaDescriptionTextView.setText(mangaDescription);
            authors.setText(manga.getAuthor());
            genres.setText(manga.getGenres());
            chaptersQuantityTextView.setText(String.valueOf(manga.getChaptersQuantity()));
            enableButtons();
        } else {
            isLoading = true;
            refreshable.startRefresh();
            MangaInfoQueryThread thread = new MangaInfoQueryThread(manga);
            thread.start();
        }
    }

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
        addToTracking.setEnabled(true);
        addToTracking.setClickable(true);
        removeFromTracking.setEnabled(true);
        removeFromTracking.setClickable(true);
    }

    private void disableButtons() {
        downloadButton.setEnabled(false);
        downloadButton.setClickable(false);
        readOnlineButton.setEnabled(false);
        readOnlineButton.setClickable(false);
        addToTracking.setEnabled(false);
        addToTracking.setClickable(false);
        removeFromTracking.setEnabled(false);
        removeFromTracking.setClickable(false);
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
            case R.id.add_to_tracking:
                addToTracking();
                break;
            case R.id.remove_from_tracking:
                removeFromTracking();
                break;
            case R.id.repository_link:
                openInBrowser();
                break;
            case R.id.chapters_list:
                activity.showChapterManagementFragment();
                break;
            case R.id.read_online:
                intent = new Intent(activity, MangaViewerActivity.class);

                HistoryDAO historyDAO = ServiceContainer.getService(HistoryDAO.class);
                HistoryElement historyElement = null;
                try {
                    Manga _manga = mangaDAO.getByLinkAndRepository(manga.getUri(), manga.getRepository());
                    if (_manga != null) {
                        historyElement = historyDAO.getHistoryByManga(_manga, true);
                    }
                } catch (DatabaseAccessException e) {
                }

                if (historyElement != null) {
                    intent.putExtra(Constants.FROM_CHAPTER_KEY, historyElement.getChapter());
                    intent.putExtra(Constants.FROM_PAGE_KEY, historyElement.getPage());
                }

                intent.putExtra(Constants.MANGA_PARCEL_KEY, manga);
                startActivity(intent);
                break;

        }
    }

    @Override
    public boolean onLongClick(final View v) {
        switch (v.getId()) {
            case R.id.download:
                Toast.makeText(getActivity(), "Скачать", Toast.LENGTH_SHORT).show();
                break;
            case R.id.toggle_favorite:
                Toast.makeText(getActivity(), "Добавить в любимое", Toast.LENGTH_SHORT).show();
                break;
            case R.id.repository_link:
                String repoName = manga.getRepository().getName();
                String openWith = String.format(activity.getString(R.string.open_in_repo), repoName);
                Toast.makeText(getActivity(), openWith, Toast.LENGTH_SHORT).show();
                break;
            case R.id.read_online:
                Toast.makeText(getActivity(), "Читать онлайн", Toast.LENGTH_SHORT).show();
                break;
            case R.id.chapters_list:
                Toast.makeText(getActivity(), "Главы", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

    private void openInBrowser() {
        String repoUri = manga.getRepository().getEngine().getBaseUri();
        String uri = manga.getUri();
        if (repoUri != null) { //for js engines
            if (!uri.contains(repoUri)) {
                uri = repoUri + uri;
            }
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(uri));
        startActivity(i);
    }

    private void addToTracking() {
        flipper.flip();
        try {
            manga.setTracking(true);
            mangaDAO.setTracking(manga, true);
        } catch (DatabaseAccessException e) {
            Context context = getActivity();
            String message = Utils.errorMessage(context, e.getMessage(), R.string.p_internet_error);
            Utils.showToast(context, message);
        }
    }

    private void removeFromTracking() {
        flipper.flip();
        try {
            manga.setTracking(false);
            mangaDAO.setTracking(manga, false);
        } catch (DatabaseAccessException e) {
            Context context = getActivity();
            String message = Utils.errorMessage(context, e.getMessage(), R.string.p_internet_error);
            Utils.showToast(context, message);
        }
    }

    @Override
    public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
        try {
            manga.setFavorite(isChecked);
            mangaDAO.setFavorite(manga, isChecked);
        } catch (DatabaseAccessException e) {
            Context context = getActivity();
            String message = Utils.errorMessage(context, e.getMessage(), R.string.p_internet_error);
            Utils.showToast(context, message);
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
                    manga.setTracking(_manga.isTracking());
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
                        if (manga.isTracking()) {
                            flipper.flip(2);
                        }
                        if (manga.isFavorite()) {
                            toggleFavorite.setIsChecked(true);
                        }
                        enableButtons();
                        String mangaDescription = manga.getDescription();
                        mangaDescriptionTextView.setText(mangaDescription);
                        authors.setText(manga.getAuthor());
                        genres.setText(manga.getGenres());
                        chaptersQuantityTextView.setText(String.valueOf(manga.getChaptersQuantity()));
                        if (!hasCoverLoaded) {
                            String coverUrl = manga.getCoverUri();
                            if (coverUrl != null) {
                                hasCoverLoaded = true;
                                Uri coverUri = Uri.parse(coverUrl);

                                final int sizeOfImage = getResources().getDimensionPixelSize(R.dimen.manga_info_height);

                                Bitmap bitmap = httpImageManager.loadImage(new HttpImageManager.LoadRequest(coverUri, new LoadResponseListener(), manga.getRepository().getEngine().getRequestPreprocessor(), sizeOfImage));

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

}
