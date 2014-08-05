package com.danilov.manga.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.httpimage.HttpImageManager;
import com.danilov.manga.R;
import com.danilov.manga.core.model.Manga;
import com.danilov.manga.core.repository.RepositoryEngine;
import com.danilov.manga.core.repository.RepositoryException;
import com.danilov.manga.core.util.Constants;
import com.danilov.manga.core.util.ServiceContainer;
import com.danilov.manga.core.util.Utils;
import com.danilov.manga.core.view.AnimatedActionView;

/**
 * Created by Semyon Danilov on 21.05.2014.
 */
public class MangaInfoActivity extends ActionBarActivity {

    private final String TAG = "MangaInfoActivity";

    private HttpImageManager httpImageManager = null;

    private TextView mangaDescriptionTextView = null;
    private TextView chaptersQuantityTextView = null;
    private TextView mangaTitle = null;
    private ImageView mangaCover = null;

    private AnimatedActionView refreshSign;

    private Button downloadButton;

    private Manga manga;

    private boolean isLoading = false;

    private boolean hasCoverLoaded = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manga_info_activity);
        mangaDescriptionTextView = (TextView) findViewById(R.id.manga_description);
        chaptersQuantityTextView = (TextView) findViewById(R.id.chapters_quantity);
        mangaTitle = (TextView) findViewById(R.id.manga_title);
        mangaCover = (ImageView) findViewById(R.id.manga_cover);
        downloadButton = (Button) findViewById(R.id.download);
        downloadButton.setOnClickListener(new ButtonClickListener());
        httpImageManager = ServiceContainer.getService(HttpImageManager.class);
        if (savedInstanceState == null) {
            Intent i = getIntent();
            manga = i.getParcelableExtra(Constants.MANGA_PARCEL_KEY);
        }
        if (manga != null) {
            loadMangaInfo(manga);
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
            MangaInfoQueryThread thread = new MangaInfoQueryThread(manga);
            thread.start();
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
            runOnUiThread(new Runnable() {

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
                        Context context = getApplicationContext();
                        String message = Utils.errorMessage(context, error, R.string.p_internet_error);
                        Utils.showToast(getApplicationContext(), message);
                    }
                    isLoading = false;
                    refreshSign.hide();
                    refreshSign.stopAnimation();
                }

            });
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        if (manga != null) {
            outState.putParcelable(Constants.MANGA_PARCEL_KEY, manga);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        manga = savedInstanceState.getParcelable(Constants.MANGA_PARCEL_KEY);
        if (manga != null) {
            loadMangaInfo(manga);
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.myactivity, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        refreshSign = new AnimatedActionView(this, menu, R.id.refresh, R.drawable.ic_action_refresh, R.anim.rotation);
        if (isLoading) {
            refreshSign.show();
            refreshSign.startAnimation();
        } else {
            refreshSign.hide();
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private class ButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(final View v) {
            switch (v.getId()) {
                case R.id.download:
                    Intent intent = new Intent(MangaInfoActivity.this, DownloadsActivity.class);
                    intent.putExtra(Constants.MANGA_PARCEL_KEY, manga);
                    startActivity(intent);
                    break;
                case R.id.add_to_favorites:
                    break;
            }
        }
    }

}