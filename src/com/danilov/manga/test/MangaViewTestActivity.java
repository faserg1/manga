package com.danilov.manga.test;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.danilov.manga.R;
import com.danilov.manga.core.database.DatabaseAccessException;
import com.danilov.manga.core.database.DownloadedMangaDAO;
import com.danilov.manga.core.interfaces.MangaShowObserver;
import com.danilov.manga.core.interfaces.MangaShowStrategy;
import com.danilov.manga.core.model.LocalManga;
import com.danilov.manga.core.model.MangaChapter;
import com.danilov.manga.core.repository.RepositoryEngine;
import com.danilov.manga.core.repository.RepositoryException;
import com.danilov.manga.core.strategy.ShowMangaException;
import com.danilov.manga.core.util.ServiceContainer;
import com.danilov.manga.core.view.InAndOutAnim;
import com.danilov.manga.core.view.MangaImageSwitcher;
import com.danilov.manga.core.view.SlidingLayer;
import com.danilov.manga.core.view.SubsamplingScaleImageView;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

/**
 * Created by Semyon Danilov on 20.06.2014.
 */
public class MangaViewTestActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener{

    private ListView mangaList;

    private MangaShowOfflineTest strategy;
    private MangaImageSwitcher imageSwitcher;
    private View left;
    private View right;
    private View selectorStub;

    private int parentHeight;
    private int height;

    private SlidingLayer slidingLayer;

    private DownloadedMangaDAO downloadedMangaDAO;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_manga_view_activity);
        imageSwitcher = (MangaImageSwitcher) findViewById(R.id.imageSwitcher);
        slidingLayer = (SlidingLayer) findViewById(R.id.selector);
        left = findViewById(R.id.left);
        right = findViewById(R.id.right);
        selectorStub = findViewById(R.id.selectorStub);
        mangaList = (ListView) findViewById(R.id.mangaList);
        left.setOnClickListener(this);
        right.setOnClickListener(this);
        selectorStub.setOnClickListener(this);
        imageSwitcher.setFactory(new SubsamplingImageViewFactory());
        downloadedMangaDAO = ServiceContainer.getService(DownloadedMangaDAO.class);
        findViewById(R.id.goToChapter).setOnClickListener(this);
        try {
            List<LocalManga> localMangas = downloadedMangaDAO.getAllManga();
            TestMangaAdapter adapter = new TestMangaAdapter(this, android.R.layout.simple_list_item_1, localMangas);
            mangaList.setAdapter(adapter);
            mangaList.setOnItemClickListener(this);
        } catch (DatabaseAccessException e) {
            e.printStackTrace();
        }
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
            case R.id.selectorStub:
                if (slidingLayer.isOpened()) {
                    slidingLayer.closeLayer(true);
                } else {
                    slidingLayer.openLayer(true);
                }
                break;
            case R.id.goToChapter:
                findViewById(R.id.selectChapter).setVisibility(View.GONE);
                EditText text = (EditText) findViewById(R.id.chapter);
                Integer chapter = Integer.valueOf(text.getText().toString());
                strategy.init();
                strategy.goToChapter(chapter);
                break;
        }
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        mangaList.setVisibility(View.GONE);
        strategy = new MangaShowOfflineTest(imageSwitcher, (LocalManga) parent.getItemAtPosition(position));
        strategy.init();
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
        private LocalManga manga;
        private MangaChapter currentChapter;
        private List<String> uris = null;

        private InAndOutAnim next;
        private InAndOutAnim prev;

        private int currentChapterNum;
        private int currentPictureNum = -1;

        public MangaShowOfflineTest(final MangaImageSwitcher imageSwitcher, final LocalManga manga) {
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
                RepositoryEngine.Repository.OFFLINE.getEngine().queryForChapters(manga);
            } catch (RepositoryException e) {
                return;
            }
            goToChapter(0);
        }

        public void goToChapter(final int chapter) {
            currentPictureNum = -1;
            this.currentChapterNum = chapter;
            this.currentChapter = manga.getChapterByNumber(currentChapterNum);
            try {
                this.uris = RepositoryEngine.Repository.OFFLINE.getEngine().getChapterImages(currentChapter);
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
            showImage(0);
        }

        public void showChapter(final int i) {

        }

        @Override
        public void showImage(final int i) {
            if (i == currentPictureNum || i >= uris.size() || i < 0) {
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
            if (currentPictureNum + 1 >= uris.size()) {
                goToChapter(currentChapterNum + 1);
                return;
            }
            showImage(currentPictureNum + 1);
        }

        @Override
        public void previous() {
            showImage(currentPictureNum - 1);
        }

        @Override
        public void initStrategy() throws ShowMangaException {

        }

        @Override
        public int getCurrentImageNumber() {
            return 0;
        }

        @Override
        public int getTotalImageNumber() {
            return 0;
        }

        @Override
        public int getCurrentChapterNumber() {
            return 0;
        }

        @Override
        public int getTotalChaptersNumber() {
            return 0;
        }

        @Override
        public void setObserver(final MangaShowObserver observer) {

        }

    }

    class TestMangaAdapter extends ArrayAdapter<LocalManga> {

        private List<LocalManga> objects;

        @Override
        public int getCount() {
            return objects.size();
        }

        public TestMangaAdapter(final Context context, final int resource, final List<LocalManga> objects) {
            super(context, resource, objects);
            this.objects = objects;
        }

        @Nullable
        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                v = MangaViewTestActivity.this.getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null);
            }
            ((TextView) v).setText(objects.get(position).getTitle());
            return v;
        }
    }

}