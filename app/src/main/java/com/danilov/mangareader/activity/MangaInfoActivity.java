package com.danilov.mangareader.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import com.danilov.mangareader.R;
import com.danilov.mangareader.core.interfaces.RefreshableActivity;
import com.danilov.mangareader.core.model.Manga;
import com.danilov.mangareader.core.util.Constants;
import com.danilov.mangareader.core.view.AnimatedActionView;
import com.danilov.mangareader.fragment.BaseFragment;
import com.danilov.mangareader.fragment.ChaptersFragment;
import com.danilov.mangareader.fragment.InfoFragment;

import java.util.List;

/**
 * Created by Semyon Danilov on 21.05.2014.
 */
public class MangaInfoActivity extends BaseToolbarActivity implements RefreshableActivity {

    private final String TAG = "MangaInfoActivity";
    private AnimatedActionView refreshSign;

    private RelativeLayout frame;

    private InfoFragment infoFragment;
    private ChaptersFragment chaptersFragment;

    private BaseFragment currentFragment;

    private boolean isRefreshing = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manga_info_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        frame = (RelativeLayout) findViewById(R.id.frame);
        if (savedInstanceState == null) {
            showInfoFragment();
        } else {
            currentFragment = (BaseFragment) getSupportFragmentManager().findFragmentById(R.id.frame);
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            if (fragments != null) {
                for (Fragment fragment : fragments) {
                    if (fragment instanceof InfoFragment) {
                        infoFragment = (InfoFragment) fragment;
                    } else if (fragment instanceof ChaptersFragment) {
                        chaptersFragment = (ChaptersFragment) fragment;
                    }
                }
            }
        }
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    public void showInfoFragment() {
        Manga manga = getIntent().getParcelableExtra(Constants.MANGA_PARCEL_KEY);
        if (infoFragment == null) {
            infoFragment = InfoFragment.newInstance(manga);
        }
        currentFragment = infoFragment;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
        fragmentTransaction.replace(R.id.frame, infoFragment).commit();
    }

    public void showChaptersFragment() {
        Manga manga = getIntent().getParcelableExtra(Constants.MANGA_PARCEL_KEY);
        if (chaptersFragment == null) {
            chaptersFragment = ChaptersFragment.newInstance(manga);
        }
        currentFragment = chaptersFragment;
        FragmentManager fragmentManager = getSupportFragmentManager();
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
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    public void onBackPressed() {
        if (currentFragment.onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }
}