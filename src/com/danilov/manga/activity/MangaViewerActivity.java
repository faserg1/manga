package com.danilov.manga.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import com.danilov.manga.R;
import com.danilov.manga.core.interfaces.MangaShowObserver;
import com.danilov.manga.core.interfaces.MangaShowStrategy;
import com.danilov.manga.core.model.LocalManga;
import com.danilov.manga.core.model.Manga;
import com.danilov.manga.core.strategy.OfflineManga;
import com.danilov.manga.core.strategy.OnlineManga;
import com.danilov.manga.core.strategy.ShowMangaException;
import com.danilov.manga.core.util.Constants;
import com.danilov.manga.core.view.InAndOutAnim;
import com.danilov.manga.core.view.MangaImageSwitcher;
import com.danilov.manga.core.view.SubsamplingScaleImageView;

/**
 * Created by Semyon Danilov on 06.08.2014.
 */
public class MangaViewerActivity extends Activity implements MangaShowObserver, MangaShowStrategy.MangaShowListener, View.OnClickListener {

    private static final String TAG = "MangaViewerActivity";

    private static final String CURRENT_CHAPTER_KEY = "CCK";
    private static final String CURRENT_IMAGE_KEY = "CIK";

    private MangaImageSwitcher imageSwitcher;
    private View nextBtn;
    private View prevBtn;
    private EditText currentImageTextView;
    private TextView totalImagesTextView;
    private EditText currentChapterTextView;
    private TextView totalChaptersTextView;

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
        this.currentImageTextView = (EditText) findViewById(R.id.imagePicker);
        this.totalImagesTextView = (TextView)findViewById(R.id.imageQuantity);
        this.currentChapterTextView = (EditText) findViewById(R.id.chapterPicker);
        this.totalChaptersTextView = (TextView) findViewById(R.id.chapterQuantity);
        nextBtn.setOnClickListener(this);
        prevBtn.setOnClickListener(this);
        Intent intent = getIntent();
        manga = intent.getParcelableExtra(Constants.MANGA_PARCEL_KEY);
        if (manga == null) {
            if (savedInstanceState != null) {
                manga = savedInstanceState.getParcelable(Constants.MANGA_PARCEL_KEY);
            }
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
        currentStrategy = new OnlineManga(manga, imageSwitcher, next, prev);
        currentStrategy.setOnInitListener(this);
        currentStrategy.setObserver(this);
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
        int currentChapterNumber = savedState.getInt(CURRENT_CHAPTER_KEY, 0);
        int currentImageNumber = savedState.getInt(CURRENT_IMAGE_KEY, 0);
        try {
            currentStrategy.initStrategy();
            currentStrategy.showChapter(currentChapterNumber);
            currentStrategy.showImage(currentImageNumber);
        } catch (ShowMangaException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void init() {
        try {
            currentStrategy.initStrategy();
        } catch (ShowMangaException e) {
            Log.e(TAG, e.getMessage());
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

    @Override
    public void onUpdate(final MangaShowStrategy strategy) {
        int currentChapter = strategy.getCurrentChapterNumber();
        int totalChapters = strategy.getTotalChaptersNumber();
        int currentImage = strategy.getCurrentImageNumber();
        int totalImages = strategy.getTotalImageNumber();
        currentChapterTextView.setText("" + (currentChapter + 1));
        currentImageTextView.setText("" + (currentImage + 1));
        totalImagesTextView.setText("" + totalImages);
        totalChaptersTextView.setText("" + totalChapters);
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

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        outState.putInt(CURRENT_CHAPTER_KEY, currentStrategy.getCurrentChapterNumber());
        outState.putInt(CURRENT_IMAGE_KEY, currentStrategy.getCurrentImageNumber());
        outState.putParcelable(Constants.MANGA_PARCEL_KEY, manga);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onInit(final MangaShowStrategy strategy) {
        if (manga.getChaptersQuantity() > 0) {
            try {
                currentStrategy.showChapter(fromChapter);
            } catch (ShowMangaException e) {
                Log.e(TAG, e.getMessage());
            }
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
            touchImageView.setMaxScale(4);
            touchImageView.setDebug(true);
            return touchImageView;
        }

    }
}