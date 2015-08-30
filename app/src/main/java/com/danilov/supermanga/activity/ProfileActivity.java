package com.danilov.supermanga.activity;

import android.content.IntentSender;
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
import android.widget.TextView;

import com.danilov.supermanga.R;
import com.danilov.supermanga.core.onlinestorage.GoogleDriveConnector;
import com.danilov.supermanga.core.onlinestorage.OnlineStorageConnector;
import com.danilov.supermanga.core.util.Utils;
import com.danilov.supermanga.core.view.UnderToolbarScrollView;
import com.danilov.supermanga.core.view.ViewV16;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.software.shell.fab.ActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by Semyon on 28.08.2015.
 */
public class ProfileActivity extends BaseToolbarActivity {

    private UnderToolbarScrollView scrollView;
    private ActionButton takePhoto;
    private ViewGroup fakeBar;
    private ViewGroup fakeBarInner;
    private TextView userNameTextView;

    private View googleSyncCard;
    private View googleSyncButton;
    private TextView googleAccountTextView;



    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        fakeBar = findViewWithId(R.id.fake_bar);
        takePhoto = findViewWithId(R.id.take_photo);
        userNameTextView = findViewWithId(R.id.user_name);
        googleSyncCard = findViewWithId(R.id.google_sync_card);
        googleAccountTextView = findViewWithId(R.id.google_account);
        googleSyncButton = findViewWithId(R.id.google_sync_button);

        final View rootView = getWindow().getDecorView().findViewById(android.R.id.content);

        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                final ViewV16 takePhotoView = ViewV16.wrap(takePhoto);
                takePhotoView.setPivotX(takePhoto.getWidth() / 2);
                takePhotoView.setPivotY(takePhoto.getHeight() / 2);
                scrollView.setListener(new UnderToolbarScrollView.UnderToolbarScrollListener() {

                    final int USER_NAME_MARGIN_BOTTOM = Utils.dpToPx(16);
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


                        RelativeLayout.LayoutParams userNameParams = (RelativeLayout.LayoutParams) userNameTextView.getLayoutParams();
                        int marginBottom = (int) (percentage * USER_NAME_MARGIN_BOTTOM);
                        userNameParams.bottomMargin = marginBottom;
                        userNameTextView.setLayoutParams(userNameParams);


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

        googleSyncCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                onlineStorageConnector = new GoogleDriveConnector(testListener);
                onlineStorageConnector.init();
            }
        });
        googleSyncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("TEST", true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                onlineStorageConnector.createFile("Title", jsonObject.toString(), OnlineStorageConnector.MimeType.TEXT_PLAIN, new OnlineStorageConnector.CommandCallback() {
                    @Override
                    public void onCommandSuccess() {
                        int a = 0;
                        a++;
                    }

                    @Override
                    public void onCommandError(final String message) {
                        if (message != null) {
                            boolean empty = message.isEmpty();
                        }
                    }
                });
            }
        });

    }

    private OnlineStorageConnector onlineStorageConnector = null;

    private OnlineStorageConnector.StorageConnectorListener testListener = new OnlineStorageConnector.StorageConnectorListener() {



        @Override
        public void onStorageConnected(final OnlineStorageConnector connector) {
            googleSyncButton.setVisibility(View.VISIBLE);
            googleAccountTextView.setText(onlineStorageConnector.getAccountName());
        }

        @Override
        public void onStorageDisconnected(final OnlineStorageConnector connector) {

        }

        @Override
        public void onConnectionFailed(final OnlineStorageConnector connector, final Object object) {
            ConnectionResult connectionResult = (ConnectionResult) object;
            if (!connectionResult.hasResolution()) {
                // show the localized error dialog.
                GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), ProfileActivity.this, 0).show();
                return;
            }

            try {
                connectionResult.startResolutionForResult(ProfileActivity.this, 1);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        }

    };

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