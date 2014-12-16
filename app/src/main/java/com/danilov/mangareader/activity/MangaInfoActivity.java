package com.danilov.mangareader.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.RelativeLayout;

import com.danilov.mangareader.R;
import com.danilov.mangareader.core.interfaces.RefreshableActivity;
import com.danilov.mangareader.core.model.Manga;
import com.danilov.mangareader.core.util.Constants;
import com.danilov.mangareader.core.view.AnimatedActionView;
import com.danilov.mangareader.fragment.ChaptersFragment;
import com.danilov.mangareader.fragment.InfoFragment;

/**
 * Created by Semyon Danilov on 21.05.2014.
 */
public class MangaInfoActivity extends BaseToolbarActivity implements RefreshableActivity {

    private final String TAG = "MangaInfoActivity";
    private AnimatedActionView refreshSign;

    private RelativeLayout frame;

    private InfoFragment infoFragment;
    private ChaptersFragment chaptersFragment;

    private boolean isRefreshing = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manga_info_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        frame = (RelativeLayout) findViewById(R.id.frame);
        if (savedInstanceState == null) {
            showInfoFragment();
        }
    }

    public void showInfoFragment() {
        Manga manga = getIntent().getParcelableExtra(Constants.MANGA_PARCEL_KEY);
        infoFragment = InfoFragment.newInstance(manga);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
        fragmentTransaction.replace(R.id.frame, infoFragment).commit();
    }

    public void showChaptersFragment() {
        Manga manga = getIntent().getParcelableExtra(Constants.MANGA_PARCEL_KEY);
        chaptersFragment = ChaptersFragment.newInstance(manga);
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

}