package com.danilov.supermanga.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.danilov.supermanga.R;
import com.danilov.supermanga.core.interfaces.RefreshableActivity;
import com.danilov.supermanga.core.model.Manga;
import com.danilov.supermanga.core.model.MangaChapter;
import com.danilov.supermanga.core.util.Constants;
import com.danilov.supermanga.fragment.BaseFragmentNative;
import com.danilov.supermanga.fragment.ChapterManagementFragment;
import com.danilov.supermanga.fragment.ChaptersFragment;
import com.danilov.supermanga.fragment.InfoFragment;
import com.danilov.supermanga.fragment.WorldArtFragment;

/**
 * Created by Semyon Danilov on 21.05.2014.
 */
public class MangaInfoActivity extends BaseToolbarActivity implements RefreshableActivity, ChapterManagementFragment.Callback {

    public static final String EXTRA_LEFT = ".left";
    public static final String EXTRA_TOP = ".top";
    public static final String EXTRA_WIDTH = ".width";
    public static final String EXTRA_HEIGHT = ".height";

    private final String TAG = "MangaInfoActivity";

    private RelativeLayout frame;

    private View overlayBackground;

    private InfoFragment infoFragment;
    private ChaptersFragment chaptersFragment;

    private BaseFragmentNative currentFragment;

    private ProgressBar progressBar;

    private boolean isRefreshing = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manga_info_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        frame = (RelativeLayout) findViewById(R.id.frame);
        overlayBackground = findViewById(R.id.overlay_background);
        progressBar = findViewWithId(R.id.progress_bar);
        if (savedInstanceState == null) {
            top = getIntent().getIntExtra(EXTRA_TOP, 0);
            left = getIntent().getIntExtra(EXTRA_LEFT, 0);
            width = getIntent().getIntExtra(EXTRA_WIDTH, 0);
            height = getIntent().getIntExtra(EXTRA_HEIGHT, 0);
            showInfoFragment(true);
        } else {
            currentFragment = (BaseFragmentNative) getFragmentManager().findFragmentById(R.id.frame);
        }
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private int top;
    private int left;
    private int width;
    private int height;

    public void showInfoFragment(final boolean init) {
        if (infoFragment == null) {
            Manga manga = getIntent().getParcelableExtra(Constants.MANGA_PARCEL_KEY);
            infoFragment = InfoFragment.newInstance(manga, left, top, width, height);
        }
        currentFragment = infoFragment;
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (!init) {
            fragmentTransaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right);
        }
        fragmentTransaction.replace(R.id.frame, infoFragment).commit();
    }

    public void showChaptersFragment() {
        if (chaptersFragment == null) {
            Manga manga = getIntent().getParcelableExtra(Constants.MANGA_PARCEL_KEY);
            chaptersFragment = ChaptersFragment.newInstance(manga);
        }
        currentFragment = chaptersFragment;
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left);
        fragmentTransaction.replace(R.id.frame, chaptersFragment).commit();
    }

    public void showChapterManagementFragment() {
        Manga manga = getIntent().getParcelableExtra(Constants.MANGA_PARCEL_KEY);
        ChapterManagementFragment fragment = ChapterManagementFragment.newInstance(manga, true);
        currentFragment = fragment;
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.animator.card_flip_right_in,
                        R.animator.card_flip_right_out,
                        R.animator.card_flip_left_in,
                        R.animator.card_flip_left_out)
                .replace(R.id.frame, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void flipFromWorldArt() {
        if (infoFragment == null) {
            Manga manga = getIntent().getParcelableExtra(Constants.MANGA_PARCEL_KEY);
            infoFragment = InfoFragment.newInstance(manga, left, top, width, height);
        }
        currentFragment = infoFragment;
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.animator.card_flip_left_in,
                        R.animator.card_flip_left_out,
                        R.animator.card_flip_right_in,
                        R.animator.card_flip_right_out)
                .replace(R.id.frame, currentFragment)
                .commit();
    }

    private void flipToWorldArt() {
        WorldArtFragment fragment = new WorldArtFragment();
        currentFragment = fragment;
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.animator.card_flip_right_in,
                        R.animator.card_flip_right_out,
                        R.animator.card_flip_left_in,
                        R.animator.card_flip_left_out)
                .replace(R.id.frame, fragment)
                .commit();
    }

    @Override
    public void startRefresh() {
        isRefreshing = true;
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void stopRefresh() {
        isRefreshing = false;
        progressBar.setVisibility(View.GONE);
    }

    public void toggleOverlayBackground(final boolean enable) {
        overlayBackground.setVisibility(enable ? View.VISIBLE : View.GONE);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public void onBackPressed() {
        currentFragment = (BaseFragmentNative) getFragmentManager().findFragmentById(R.id.frame);
        if (currentFragment.onBackPressed()) {
            return;
        }
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onChapterSelected(final Manga manga, final MangaChapter chapter, final boolean isOnline) {
        Intent intent = new Intent(this, MangaViewerActivity.class);
        intent.putExtra(Constants.FROM_CHAPTER_KEY, chapter.getNumber());
        intent.putExtra(Constants.MANGA_PARCEL_KEY, manga);
        intent.putExtra(Constants.SHOW_ONLINE, isOnline);
        startActivity(intent);
    }

}