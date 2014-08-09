package com.danilov.manga.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.danilov.manga.R;
import com.danilov.manga.core.interfaces.MangaShowStrategy;
import com.danilov.manga.core.model.LocalManga;
import com.danilov.manga.core.model.Manga;
import com.danilov.manga.core.strategy.OfflineManga;
import com.danilov.manga.core.strategy.ShowMangaException;
import com.danilov.manga.core.util.Constants;
import com.danilov.manga.core.view.MangaImageSwitcher;

/**
 * Created by Semyon Danilov on 06.08.2014.
 */
public class MangaViewerActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "MangaViewerActivity";

    private MangaImageSwitcher imageSwitcher;
    private View nextBtn;
    private View prevBtn;

    private MangaShowStrategy currentStrategy = null;

    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manga_viewer_activity);
        this.imageSwitcher = (MangaImageSwitcher) findViewById(R.id.imageSwitcher);
        this.nextBtn = findViewById(R.id.nextBtn);
        this.prevBtn = findViewById(R.id.prevBtn);
        nextBtn.setOnClickListener(this);
        prevBtn.setOnClickListener(this);
        Manga manga = getIntent().getParcelableExtra(Constants.MANGA_PARCEL_KEY);
        if (manga instanceof LocalManga) {
            currentStrategy = new OfflineManga((LocalManga) manga, imageSwitcher, null, null);
        }
        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        }
    }

    /**
     * Not standard method, because I want to handle
     * restorations in OnCreate
     * @param savedState
     */
    private void restoreState(final Bundle savedState) {

    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.prevBtn:
                onPrevious();
                break;
            case R.id.nextBtn:
                onNext();
                break;
        }
    }

    private void onPrevious() {
        try {
            currentStrategy.previous();
        } catch (ShowMangaException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void onNext() {
        try {
            currentStrategy.next();
        } catch (ShowMangaException e) {
            Log.e(TAG, e.getMessage());
        }
    }

}