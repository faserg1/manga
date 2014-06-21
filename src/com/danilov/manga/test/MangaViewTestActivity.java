package com.danilov.manga.test;

import android.app.Activity;
import android.os.Bundle;
import com.danilov.manga.R;
import com.danilov.manga.core.interfaces.MangaShowStrategy;
import com.danilov.manga.core.view.MangaImageSwitcher;
import com.danilov.manga.core.view.TouchImageView;

/**
 * Created by Semyon Danilov on 20.06.2014.
 */
public class MangaViewTestActivity extends Activity {

    private MangaImageSwitcher imageSwitcher;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_manga_view_activity);
        imageSwitcher = (MangaImageSwitcher) findViewById(R.id.imageSwitcher);
    }

    private class MangaShowOfflineTest implements MangaShowStrategy {

        private MangaImageSwitcher imageSwitcher;

        public MangaShowOfflineTest(final MangaImageSwitcher imageSwitcher) {
            this.imageSwitcher = imageSwitcher;
        }

        @Override
        public TouchImageView showImage(final int i) {
            return null;
        }

    }

}