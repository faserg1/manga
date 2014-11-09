package com.danilov.manga.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.httpimage.HttpImageManager;
import com.danilov.manga.R;
import com.danilov.manga.core.interfaces.RefreshableActivity;
import com.danilov.manga.core.model.Manga;
import com.danilov.manga.core.repository.RepositoryEngine;
import com.danilov.manga.core.repository.RepositoryException;
import com.danilov.manga.core.util.Constants;
import com.danilov.manga.core.util.ServiceContainer;
import com.danilov.manga.core.util.Utils;
import com.danilov.manga.core.view.AnimatedActionView;
import com.danilov.manga.fragment.ChaptersFragment;
import com.danilov.manga.fragment.InfoFragment;

/**
 * Created by Semyon Danilov on 21.05.2014.
 */
public class MangaInfoActivity extends ActionBarActivity implements RefreshableActivity {

    private final String TAG = "MangaInfoActivity";
    private AnimatedActionView refreshSign;

    private RelativeLayout frame;

    private InfoFragment infoFragment;
    private ChaptersFragment chaptersFragment;

    private boolean isRefreshing = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manga_info_activity);
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