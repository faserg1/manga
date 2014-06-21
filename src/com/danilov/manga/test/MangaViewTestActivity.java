package com.danilov.manga.test;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import com.danilov.manga.R;
import com.danilov.manga.core.interfaces.MangaShowStrategy;
import com.danilov.manga.core.model.Manga;
import com.danilov.manga.core.model.MangaChapter;
import com.danilov.manga.core.view.MangaImageSwitcher;

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
        strategy = new MangaShowOfflineTest(imageSwitcher, Mock.getMockManga());
        left = findViewById(R.id.left);
        right = findViewById(R.id.right);
        left.setOnClickListener(this);
        right.setOnClickListener(this);
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

    private class MangaShowOfflineTest implements MangaShowStrategy {

        private MangaImageSwitcher imageSwitcher;
        private Manga manga;
        private MangaChapter currentChapter;

        private int currentChapterNum;
        private int currentPictureNum;

        public MangaShowOfflineTest(final MangaImageSwitcher imageSwitcher, final Manga manga) {
            this.imageSwitcher = imageSwitcher;
            this.manga = manga;
        }

        @Override
        public void showImage(final int i) {
            if (i == currentPictureNum) {
                return;
            }
            if (i < currentPictureNum) {
                imageSwitcher.setNextImageDrawable(null);
            } else if (i > currentPictureNum) {
                imageSwitcher.setPreviousImageDrawable(null);
            }
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