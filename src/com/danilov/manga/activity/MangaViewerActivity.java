package com.danilov.manga.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ViewSwitcher;
import com.danilov.manga.R;
import com.danilov.manga.core.interfaces.MangaShowStrategy;
import com.danilov.manga.core.model.LocalManga;
import com.danilov.manga.core.model.Manga;
import com.danilov.manga.core.strategy.OfflineManga;
import com.danilov.manga.core.strategy.ShowMangaException;
import com.danilov.manga.core.util.Constants;
import com.danilov.manga.core.view.InAndOutAnim;
import com.danilov.manga.core.view.MangaImageSwitcher;
import com.danilov.manga.core.view.SubsamplingScaleImageView;

/**
 * Created by Semyon Danilov on 06.08.2014.
 */
public class MangaViewerActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "MangaViewerActivity";

    private MangaImageSwitcher imageSwitcher;
    private View nextBtn;
    private View prevBtn;

    private MangaShowStrategy currentStrategy;
    private Manga manga;
    private int fromChapter;

    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manga_viewer_activity);
        this.imageSwitcher = (MangaImageSwitcher) findViewById(R.id.imageSwitcher);
        imageSwitcher.setFactory(new SubsamplingImageViewFactory());
        this.nextBtn = findViewById(R.id.nextBtn);
        this.prevBtn = findViewById(R.id.prevBtn);
        nextBtn.setOnClickListener(this);
        prevBtn.setOnClickListener(this);
        Intent intent = getIntent();
        manga = intent.getParcelableExtra(Constants.MANGA_PARCEL_KEY);
        if (manga == null) {
            return;
        }
        fromChapter = intent.getIntExtra(Constants.FROM_CHAPTER_KEY, 0);


        //loading anims
        Animation nextInAnim = AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_in_right);
        Animation nextOutAnim = AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_out_left);
        Animation prevInAnim = AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_in_left);
        Animation prevOutAnim = AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_out_right);

        InAndOutAnim next = new InAndOutAnim(nextInAnim, nextOutAnim);
        next.setDuration(150);
        InAndOutAnim prev = new InAndOutAnim(prevInAnim, prevOutAnim);
        prev.setDuration(150);

        if (manga instanceof LocalManga) {
            currentStrategy = new OfflineManga((LocalManga) manga, imageSwitcher, next, prev);
        }
        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        } else {
            init();
        }
    }

    /**
     * Not standard method, because I want to handle
     * restorations in OnCreate
     * @param savedState
     */
    private void restoreState(final Bundle savedState) {

    }

    private void init() {
        try {
            currentStrategy.initStrategy();
        } catch (ShowMangaException e) {
            Log.e(TAG, e.getMessage());
        }
        if (manga.getChaptersQuantity() > 0) {
            try {
                currentStrategy.showChapter(fromChapter);
            } catch (ShowMangaException e) {
                Log.e(TAG, e.getMessage());
            }
        }
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

    private class SubsamplingImageViewFactory implements ViewSwitcher.ViewFactory {

        @Override
        public View makeView() {
            SubsamplingScaleImageView touchImageView = new SubsamplingScaleImageView(MangaViewerActivity.this);

            touchImageView.setLayoutParams(new
                    ImageSwitcher.LayoutParams(
                    ImageSwitcher.LayoutParams.MATCH_PARENT, ImageSwitcher.LayoutParams.MATCH_PARENT));
            touchImageView.setVisibility(View.INVISIBLE);
            return touchImageView;
        }

    }
}