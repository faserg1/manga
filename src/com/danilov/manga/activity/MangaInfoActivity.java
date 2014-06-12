package com.danilov.manga.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.httpimage.HttpImageManager;
import com.danilov.manga.R;
import com.danilov.manga.core.http.HttpRequestException;
import com.danilov.manga.core.model.Manga;
import com.danilov.manga.core.repository.RepositoryEngine;
import com.danilov.manga.core.util.ServiceContainer;
import com.danilov.manga.core.util.Utils;

/**
 * Created by Semyon Danilov on 21.05.2014.
 */
public class MangaInfoActivity extends Activity {

    private final String TAG = "MangaInfoActivity";

    private HttpImageManager httpImageManager = null;

    private TextView mangaDescriptionTextView = null;
    private TextView mangaTitle = null;
    private ImageView mangaCover = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manga_info_activity);
        mangaDescriptionTextView = (TextView) findViewById(R.id.manga_description);
        mangaTitle = (TextView) findViewById(R.id.manga_title);
        mangaCover = (ImageView) findViewById(R.id.manga_cover);
        Intent i = getIntent();
        httpImageManager = ServiceContainer.getService(HttpImageManager.class);
        final Manga manga = i.getParcelableExtra("manga");
        final int sizeOfImage = getResources().getDimensionPixelSize(R.dimen.manga_info_height);
        if (manga != null) {
            Uri coverUri = Uri.parse(manga.getCoverUri());
            Bitmap bitmap = httpImageManager.loadImage(new HttpImageManager.LoadRequest(coverUri, mangaCover, sizeOfImage));
            if (bitmap != null) {
                mangaCover.setImageBitmap(bitmap);
            }
            mangaTitle.setText(manga.getTitle());
            String mangaDescription = manga.getDescription();
            if (mangaDescription != null) {
                mangaDescriptionTextView.setText(mangaDescription);
            } else {
                MangaInfoQueryThread thread = new MangaInfoQueryThread(manga);
                thread.start();
            }
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
            } catch (HttpRequestException e) {
                error = e.getMessage();
                Log.d(TAG, e.getMessage());
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (loaded) {
                        String mangaDescription = manga.getDescription();
                        mangaDescriptionTextView.setText(mangaDescription);
                    } else {
                        Context context = getApplicationContext();
                        String message = Utils.errorMessage(context, error, R.string.p_internet_error);
                        Utils.showToast(getApplicationContext(), message);
                    }
                }
            });
        }


    }

}