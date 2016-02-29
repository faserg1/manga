package com.danilov.supermanga.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.danilov.supermanga.R;
import com.danilov.supermanga.core.interfaces.RefreshableActivity;
import com.danilov.supermanga.core.model.Manga;
import com.danilov.supermanga.core.util.Constants;
import com.danilov.supermanga.core.util.transition.ActivitySwitcher;
import com.danilov.supermanga.core.view.AnimatedActionView;
import com.danilov.supermanga.fragment.BaseFragment;
import com.danilov.supermanga.fragment.BaseFragmentNative;
import com.danilov.supermanga.fragment.ChaptersFragment;
import com.danilov.supermanga.fragment.InfoFragment;
import com.danilov.supermanga.fragment.WorldArtFragment;

import java.util.List;

/**
 * Created by Semyon Danilov on 21.05.2014.
 */
public class MangaInfoActivity extends BaseToolbarActivity implements RefreshableActivity {

    public static final String EXTRA_LEFT = ".left";
    public static final String EXTRA_TOP = ".top";
    public static final String EXTRA_WIDTH = ".width";
    public static final String EXTRA_HEIGHT = ".height";

    private final String TAG = "MangaInfoActivity";
    private AnimatedActionView refreshSign;

    private RelativeLayout frame;

    private View overlayBackground;

    private InfoFragment infoFragment;
    private ChaptersFragment chaptersFragment;

    private BaseFragmentNative currentFragment;

    private boolean isRefreshing = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manga_info_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        frame = (RelativeLayout) findViewById(R.id.frame);
        overlayBackground = findViewById(R.id.overlay_background);
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
            fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
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
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
        fragmentTransaction.replace(R.id.frame, chaptersFragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.myactivity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
//                onBackPressed();
                flipToWorldArt();
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
    public boolean onPrepareOptionsMenu(final Menu menu) {
        refreshSign = new AnimatedActionView(this, menu, R.id.refresh, R.drawable.ic_action_refresh, R.anim.rotation);
        if (isRefreshing) {
            refreshSign.show();
            refreshSign.startAnimation();
        } else {
            refreshSign.stopAnimation();
            refreshSign.hide();
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void startRefresh() {
        isRefreshing = true;
        if (refreshSign != null) {
            refreshSign.show();
            refreshSign.startAnimation();
        }
    }

    @Override
    public void stopRefresh() {
        isRefreshing = false;
        if (refreshSign != null) {
            refreshSign.stopAnimation();
            refreshSign.hide();
        }
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
        if (currentFragment.onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }
}