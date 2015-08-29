package com.danilov.supermanga.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.danilov.supermanga.R;
import com.danilov.supermanga.core.util.Utils;
import com.danilov.supermanga.core.view.UnderToolbarScrollView;
import com.danilov.supermanga.core.view.ViewV16;
import com.software.shell.fab.ActionButton;

/**
 * Created by Semyon on 28.08.2015.
 */
public class ProfileActivity extends BaseToolbarActivity {

    private UnderToolbarScrollView scrollView;
    private ActionButton takePhoto;
    private ViewGroup fakeBar;
    private ViewGroup fakeBarInner;
    private MenuItem backButton;



    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        fakeBar = findViewWithId(R.id.fake_bar);
        takePhoto = findViewWithId(R.id.take_photo);

        final View rootView = getWindow().getDecorView().findViewById(android.R.id.content);

        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                final ViewV16 takePhotoView = ViewV16.wrap(takePhoto);
                takePhotoView.setPivotX(takePhoto.getWidth() / 2);
                takePhotoView.setPivotY(takePhoto.getHeight() / 2);
                scrollView.setListener(new UnderToolbarScrollView.UnderToolbarScrollListener() {

                    final int MARGIN_TOP = Utils.dpToPx(58);
                    final int MARGIN_LEFT = Utils.dpToPx(48);
                    final int MAX_HEIGHT = fakeBar.getHeight();
                    final int MIN_HEIGHT = Utils.dpToPx(58);
                    final int EXTRA_HEIGHT = MAX_HEIGHT - MIN_HEIGHT;

                    @Override
                    public int onScroll(final int x, final int y) {

                        int height = Math.max(MIN_HEIGHT, MAX_HEIGHT - y);
                        height = Math.min(MAX_HEIGHT, height);

                        RelativeLayout.LayoutParams fakeBarParameters = (RelativeLayout.LayoutParams) fakeBar.getLayoutParams();
                        fakeBarParameters.height = height;
                        fakeBar.setLayoutParams(fakeBarParameters);


                        int curExtra = height - MIN_HEIGHT;
                        float percentage = (float) curExtra / EXTRA_HEIGHT;
                        takePhotoView.setScaleX(percentage);
                        takePhotoView.setScaleY(percentage);
                        int marginTop = (int) (percentage * MARGIN_TOP);
                        int marginLeft = (int) ((1 - percentage) * MARGIN_LEFT);
                        RelativeLayout.LayoutParams fakeBarInnerParameters = (RelativeLayout.LayoutParams) fakeBarInner.getLayoutParams();
                        fakeBarInnerParameters.topMargin = marginTop;
                        fakeBarInnerParameters.leftMargin = marginLeft;
                        fakeBarInner.setLayoutParams(fakeBarInnerParameters);

                        return 0;
                    }
                });
                rootView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        fakeBarInner = findViewWithId(R.id.fake_bar_inner);
        scrollView = findViewWithId(R.id.scroll_view);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

}
