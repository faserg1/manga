package com.danilov.manga.test;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ViewSwitcher;
import com.danilov.manga.R;
import com.danilov.manga.core.interfaces.MangaShowStrategy;
import com.danilov.manga.core.model.Manga;
import com.danilov.manga.core.model.MangaChapter;
import com.danilov.manga.core.repository.RepositoryEngine;
import com.danilov.manga.core.repository.RepositoryException;
import com.danilov.manga.core.view.InAndOutAnim;
import com.danilov.manga.core.view.MangaImageSwitcher;
import com.danilov.manga.core.view.SubsamplingScaleImageView;

import java.io.File;
import java.util.List;

/**
 * Created by Semyon Danilov on 20.06.2014.
 */
public class MangaViewTestActivity extends Activity implements View.OnClickListener{

    private MangaShowOfflineTest strategy;
    private View left;
    private View right;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_manga_view_activity);
        MangaImageSwitcher imageSwitcher = (MangaImageSwitcher) findViewById(R.id.imageSwitcher);
        strategy = new MangaShowOfflineTest(imageSwitcher, Mock.getOfflineMockManga());
        left = findViewById(R.id.left);
        right = findViewById(R.id.right);
        left.setOnClickListener(this);
        right.setOnClickListener(this);
        imageSwitcher.setFactory(new SubsamplingImageViewFactory());
        strategy.init();
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.left:
                strategy.previous();
                break;
            case R.id.right:
                strategy.next();
                break;
        }
    }

    private class SubsamplingImageViewFactory implements ViewSwitcher.ViewFactory {

        @Override
        public View makeView() {
            SubsamplingScaleImageView touchImageView = new SubsamplingScaleImageView(MangaViewTestActivity.this);

            touchImageView.setLayoutParams(new
                    ImageSwitcher.LayoutParams(
                    ImageSwitcher.LayoutParams.MATCH_PARENT, ImageSwitcher.LayoutParams.MATCH_PARENT));
            touchImageView.setVisibility(View.INVISIBLE);
            return touchImageView;
        }

    }

    private class MangaShowOfflineTest implements MangaShowStrategy {

        private MangaImageSwitcher imageSwitcher;
        private Manga manga;
        private MangaChapter currentChapter;
        private List<String> uris = null;

        private InAndOutAnim next;
        private InAndOutAnim prev;

        private int currentChapterNum;
        private int currentPictureNum = -1;

        public MangaShowOfflineTest(final MangaImageSwitcher imageSwitcher, final Manga manga) {
            this.imageSwitcher = imageSwitcher;
            this.manga = manga;
            this.currentChapterNum = 0;
            this.currentChapter = manga.getChapterByNumber(currentChapterNum);

            Animation nextInAnim = AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_in_right);
            Animation nextOutAnim = AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_out_left);
            Animation prevInAnim = AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_in_left);
            Animation prevOutAnim = AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_out_right);

            next = new InAndOutAnim(nextInAnim, nextOutAnim);
            next.setDuration(150);
            prev = new InAndOutAnim(prevInAnim, prevOutAnim);
            prev.setDuration(150);
        }

        public void init() {
            try {
                this.uris = RepositoryEngine.Repository.OFFLINE.getEngine().getChapterImages(currentChapter);
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
            showImage(0);
        }

        @Override
        public void showImage(final int i) {
            if (i == currentPictureNum) {
                return;
            }
            File imageFile = new File(uris.get(i));
            if (i < currentPictureNum) {
                imageSwitcher.setInAndOutAnim(prev);
                imageSwitcher.setPreviousImageDrawable(imageFile.getPath());
            } else if (i > currentPictureNum) {
                imageSwitcher.setInAndOutAnim(next);
                imageSwitcher.setNextImageDrawable(imageFile.getPath());
            }
            currentPictureNum = i;
        }

        @Override
        public void next() {
            showImage(currentPictureNum + 1);
        }

        @Override
        public void previous() {
            showImage(currentPictureNum - 1);
        }

    }

}