package com.danilov.supermanga.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.app.DialogFragment;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.danilov.supermanga.R;
import com.danilov.supermanga.core.application.ApplicationSettings;
import com.danilov.supermanga.core.application.MangaApplication;
import com.danilov.supermanga.core.dialog.CustomDialog;
import com.danilov.supermanga.core.service.OnlineStorageProfileService;
import com.danilov.supermanga.core.service.ServiceConnectionListener;
import com.danilov.supermanga.core.util.Constants;
import com.danilov.supermanga.core.util.Utils;
import com.danilov.supermanga.core.view.UnderToolbarScrollView;
import com.danilov.supermanga.core.view.ViewV16;
import com.danilov.supermanga.core.widget.RelativeTimeTextView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.software.shell.fab.ActionButton;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Semyon on 28.08.2015.
 */
public class ProfileActivity extends BaseToolbarActivity {

    @Bind(R.id.scroll_view)
    public UnderToolbarScrollView scrollView;

    @Bind(R.id.take_photo)
    public ActionButton takePhoto;

    @Bind(R.id.fake_bar)
    public ViewGroup fakeBar;

    @Bind(R.id.fake_bar_inner)
    public ViewGroup fakeBarInner;

    @Bind(R.id.user_name)
    public TextView userNameTextView;

    @Bind(R.id.google_sync_card)
    public View googleSyncCard;

    @Bind(R.id.yandex_sync_card)
    public View yandexSyncCard;

    @Bind(R.id.user_name_card)
    public View userNameCard;

    @Bind(R.id.google_sync_button)
    public View googleSyncButton;

    @Bind(R.id.google_download_button)
    public View googleDownloadButton;

    @Bind(R.id.yandex_sync_button)
    public View yandexSyncButton;

    @Bind(R.id.yandex_download_button)
    public View yandexDownloadButton;

    @Bind(R.id.email_card)
    public View emailCard;

    @Bind(R.id.always_show_buttons_card)
    public View alwaysShowButtonsCard;

    @Bind(R.id.download_path_card)
    public View downloadPathCard;

    @Bind(R.id.google_account)
    public RelativeTimeTextView googleAccountTextView;

    @Bind(R.id.yandex_account)
    public RelativeTimeTextView yandexAccountTextView;

    @Bind(R.id.user_name_small)
    public TextView userNameSmall;

    @Bind(R.id.email)
    public TextView email;

    @Bind(R.id.time_read)
    public TextView timeRead;

    @Bind(R.id.download_path)
    public TextView downloadPath;

    @Bind(R.id.mangas_complete)
    public TextView mangasComplete;

    @Bind(R.id.megabytes_downloaded)
    public TextView megabytesDownloaded;

    @Bind(R.id.app_version)
    public TextView appVersion;

    @Bind(R.id.show_btns_switch)
    public SwitchCompat showBtnsSwitch;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

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
                        userNameParams.bottomMargin = (int) (percentage * USER_NAME_MARGIN_BOTTOM);
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

        googleSyncCard.setOnClickListener(v -> {
            if (service != null) {
                service.connect();
            }
        });
        googleSyncButton.setOnClickListener(v -> {
            if (service == null) {
                return;
            }
            service.save();
        });
        googleDownloadButton.setOnClickListener(v -> {
            if (service == null) {
                return;
            }
            service.download();
        });
        yandexSyncCard.setOnClickListener(v -> {
            if (service != null) {
                service.connectYandex();
            }
        });
        yandexSyncButton.setOnClickListener(v -> {
            if (service == null) {
                return;
            }
            service.saveYandex();
        });
        yandexDownloadButton.setOnClickListener(v -> {
            if (service == null) {
                return;
            }
            service.downloadYandex();
        });

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        long lastUpdateTimeGoogle = sharedPreferences.getLong(Constants.Settings.LAST_UPDATE_PROFILE_TIME_GOOGLE, -1L);
        long lastUpdateTimeYandex = sharedPreferences.getLong(Constants.Settings.LAST_UPDATE_PROFILE_TIME_YANDEX, -1L);

        String yandexProfileName = sharedPreferences.getString("YA_USERNAME", null);
        if (yandexProfileName == null) {
            yandexProfileName = "Нажмите, чтобы подключить";
            yandexAccountTextView.setPrefix(yandexProfileName);
            yandexAccountTextView.setReferenceTime(RelativeTimeTextView.ONLY_PREFIX);
        } else {
            String prefix =  yandexProfileName + " (" + getString(R.string.sv_synchronized) + " ";
            yandexAccountTextView.setPrefix(prefix);
            yandexAccountTextView.setReferenceTime(lastUpdateTimeYandex);
            yandexAccountTextView.setSuffix(")");
        }

        String googleProfileName = sharedPreferences.getString(Constants.Settings.GOOGLE_PROFILE_NAME, null);
        if (googleProfileName == null) {
            googleProfileName = "Нажмите, чтобы подключить";
            googleAccountTextView.setPrefix(googleProfileName);
            googleAccountTextView.setReferenceTime(RelativeTimeTextView.ONLY_PREFIX);
        } else {
            String prefix =  googleProfileName + " (" + getString(R.string.sv_synchronized) + " ";
            googleAccountTextView.setPrefix(prefix);
            googleAccountTextView.setReferenceTime(lastUpdateTimeGoogle);
            googleAccountTextView.setSuffix(")");
        }

        init();

        serviceConnection = OnlineStorageProfileService.bindService(this, new ServiceConnectionListener<OnlineStorageProfileService>() {
            @Override
            public void onServiceConnected(final OnlineStorageProfileService service) {
                ProfileActivity.this.service = service;
                service.setServiceHandler(handler);
            }

            @Override
            public void onServiceDisconnected(final OnlineStorageProfileService service) {

            }
        });
    }


    private void init() {
        final ApplicationSettings.UserSettings userSettings = ApplicationSettings.get(this).getUserSettings();

        final String userNameString = userSettings.getUserName();
        final String emailString = userSettings.getEmail();
        long timeReadLong = userSettings.getTimeRead();
        final String downloadPathString = userSettings.getDownloadPath();
        int mangasCompleteInt = userSettings.getMangasComplete();
        long megabytesDownloadedLong = userSettings.getBytesDownloaded() / (1024 * 1024);
        boolean alwaysShowButtons = userSettings.isAlwaysShowButtons();

        long seconds = timeReadLong / 1000;
        long hours = seconds / 3600;
        long secondsDelta = seconds - (hours * 3600);

        long minutes = secondsDelta / 60;
        long secs = secondsDelta - (minutes * 60);

        PackageManager manager = getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            int versionCode = info.versionCode;
            String versionName = info.versionName;
            appVersion.setText(new StringBuilder().append(versionName).append(" (").append(versionCode).append(")").toString());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        userNameTextView.setText(userNameString);
        userNameSmall.setText(userNameString);
        email.setText(emailString);
        timeRead.setText(hours + " " + getString(R.string.hrs) + " " + minutes + " " + getString(R.string.min) + " " +secs + " " + getString(R.string.sec));
        downloadPath.setText(downloadPathString);
        mangasComplete.setText(mangasCompleteInt + "");
        megabytesDownloaded.setText(megabytesDownloadedLong + "");
        showBtnsSwitch.setChecked(alwaysShowButtons);
        alwaysShowButtonsCard.setOnClickListener(v -> {
            boolean isChecked = !showBtnsSwitch.isChecked();
            showBtnsSwitch.setChecked(isChecked);
            userSettings.setAlwaysShowButtons(isChecked);
            Context context = getApplicationContext();
            ApplicationSettings.get(context).update(context);
        });
        showBtnsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            userSettings.setAlwaysShowButtons(isChecked);
            Context context = getApplicationContext();
            ApplicationSettings.get(context).update(context);
        });

        userNameCard.setOnClickListener(v -> {
            ValueDialogFragment dialogFragment = ValueDialogFragment.createDialog(getString(R.string.username), userNameString, Constants.Settings.USER_NAME);
            dialogFragment.show(getFragmentManager(), ValueDialogFragment.TAG);
        });
        emailCard.setOnClickListener(v -> {
            ValueDialogFragment dialogFragment = ValueDialogFragment.createDialog(getString(R.string.email), emailString, Constants.Settings.EMAIL);
            dialogFragment.show(getFragmentManager(), ValueDialogFragment.TAG);
        });
        downloadPathCard.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, FolderPickerActivity.class);
            intent.putExtra(FolderPickerActivity.FOLDER_KEY, downloadPathString);
            startActivityForResult(intent, FOLDER_PICKER_REQUEST);
        });
        if (getIntent() != null && getIntent().getData() != null) {
            onYandexLogin();
        }
    }

    private void onYandexLogin () {
        Uri data = getIntent().getData();
        setIntent(null);
        Pattern pattern = Pattern.compile("access_token=(.*?)(&|$)");
        Matcher matcher = pattern.matcher(data.toString());
        if (matcher.find()) {
            final String token = matcher.group(1);
            if (!TextUtils.isEmpty(token)) {
                Log.d("ProfileActivity", "onLogin: token: "+token);
                saveToken(token);
            } else {
                Log.w("ProfileActivity", "onYandexLogin: empty token");
            }
        } else {
            Log.w("ProfileActivity", "onYandexLogin: token not found in return url");
        }
    }

    private void saveToken(String token) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString("YA_USERNAME", "YaUser");
        editor.putString("YA_TOKEN", token);
        editor.apply();
        handler.sendEmptyMessage(OnlineStorageProfileService.YANDEX_CONNECTED);
    }

    private ServiceConnection serviceConnection;
    private OnlineStorageProfileService service;

//    @SuppressWarnings()
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case OnlineStorageProfileService.GOOGLE_CONNECTED:
                    googleSyncButton.setVisibility(View.VISIBLE);
                    googleDownloadButton.setVisibility(View.VISIBLE);
                    if (service != null) {
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        long lastUpdateTime = sharedPreferences.getLong(Constants.Settings.LAST_UPDATE_PROFILE_TIME_GOOGLE, -1L);
                        String accountName = service.getGoogleConnector().getAccountName();
                        String prefix =  accountName + " (" + getString(R.string.sv_synchronized) + " ";
                        googleAccountTextView.setPrefix(prefix);
                        googleAccountTextView.setReferenceTime(lastUpdateTime);
                        googleAccountTextView.setSuffix(")");

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(Constants.Settings.GOOGLE_PROFILE_NAME, accountName).apply();
                    }
                    break;
                case OnlineStorageProfileService.GOOGLE_NEED_CONFIRMATION:
                    ConnectionResult connectionResult = (ConnectionResult) msg.obj;
                    if (!connectionResult.hasResolution()) {
                        // show the localized error dialog.
                        GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), ProfileActivity.this, 0).show();
                        return;
                    }

                    try {
                        connectionResult.startResolutionForResult(ProfileActivity.this, GOOGLE_AUTH_REQUEST_CODE);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                    break;
                case OnlineStorageProfileService.GOOGLE_SENT_SUCCESS:
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                    long lastUpdateTime = System.currentTimeMillis();
                    sharedPreferences.edit().putLong(Constants.Settings.LAST_UPDATE_PROFILE_TIME_GOOGLE, lastUpdateTime).apply();
                    String accountName = service.getGoogleConnector().getAccountName();
                    String prefix =  accountName + " (" + getString(R.string.sv_synchronized) + " ";
                    googleAccountTextView.setPrefix(prefix);
                    googleAccountTextView.setReferenceTime(lastUpdateTime);
                    googleAccountTextView.setSuffix(")");
                    break;
                case OnlineStorageProfileService.GOOGLE_DOWNLOADED:
                    init();
                    break;
                case OnlineStorageProfileService.YANDEX_NEED_CONFIRMATION:
                    String yandexAuthURL = "https://oauth.yandex.ru/authorize?response_type=token&client_id=" + getString(R.string.yandex_client_id);
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(yandexAuthURL)));
                    break;
                case OnlineStorageProfileService.YANDEX_CONNECTED:
                    yandexSyncButton.setVisibility(View.VISIBLE);
                    yandexDownloadButton.setVisibility(View.VISIBLE);
                    if (service != null) {
                        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        lastUpdateTime = sharedPreferences.getLong(Constants.Settings.LAST_UPDATE_PROFILE_TIME_YANDEX, -1L);
                        accountName = sharedPreferences.getString("YA_USERNAME", "");
                        prefix =  accountName + " (" + getString(R.string.sv_synchronized) + " ";
                        yandexAccountTextView.setPrefix(prefix);
                        yandexAccountTextView.setReferenceTime(lastUpdateTime);
                        yandexAccountTextView.setSuffix(")");
                    }
                    break;
                case OnlineStorageProfileService.YANDEX_SENT_SUCCESS:
                    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                    lastUpdateTime = System.currentTimeMillis();
                    sharedPreferences.edit().putLong(Constants.Settings.LAST_UPDATE_PROFILE_TIME_YANDEX, lastUpdateTime).apply();
                    accountName = service.getYandexConnector().getAccountName();
                    prefix =  accountName + " (" + getString(R.string.sv_synchronized) + " ";
                    yandexAccountTextView.setPrefix(prefix);
                    yandexAccountTextView.setReferenceTime(lastUpdateTime);
                    yandexAccountTextView.setSuffix(")");
                    break;
                case OnlineStorageProfileService.YANDEX_DOWNLOADED:
                    init();
                    break;
            }
        }
    };

    private static final int GOOGLE_AUTH_REQUEST_CODE = 1;
    private static final int FOLDER_PICKER_REQUEST = 2;

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GOOGLE_AUTH_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    service.connect();
                }
                break;
            case FOLDER_PICKER_REQUEST:
                if (resultCode != Activity.RESULT_OK) {
                    return;
                }
                ApplicationSettings settings = ApplicationSettings.get(this);
                final ApplicationSettings.UserSettings userSettings = settings.getUserSettings();
                String path = data.getStringExtra(FolderPickerActivity.FOLDER_KEY);
                userSettings.setDownloadPath(path);
                downloadPath.setText(path);
                settings.update(this);
                init();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        if (serviceConnection != null && service != null) {
            unbindService(serviceConnection);
            service.removeHandler();
            service = null;
        }
        super.onDestroy();
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

    public static class ValueDialogFragment extends DialogFragment {

        public static final String TAG = "ValueDialogFragment";

        public static final String TITLE = "TITLE";
        public static final String VALUE = "VALUE";
        public static final String PARAMETER_NAME = "PARAMETER_NAME";

        private String title = "";
        private String value = "";
        private String parameterName = "";

        private EditText editText = null;

        public ValueDialogFragment() {
            super();
        }

        public static ValueDialogFragment createDialog(final String title, final String value, final String parameterName) {
            ValueDialogFragment valueDialogFragment = new ValueDialogFragment();
            Bundle bundle = new Bundle();
            bundle.putString(TITLE, title);
            bundle.putString(VALUE, value);
            bundle.putString(PARAMETER_NAME, parameterName);
            valueDialogFragment.setArguments(bundle);
            return valueDialogFragment;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(final Bundle savedInstanceState) {
            Bundle b = getArguments();
            if (savedInstanceState != null) {
                b = savedInstanceState;
            }

            title = b.getString(TITLE);
            value = b.getString(VALUE);
            parameterName = b.getString(PARAMETER_NAME);

            CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
            builder.setPositiveButton(getString(R.string.sv_ok), (arg0, arg1) -> {
                Context context = MangaApplication.getContext();
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                preferences.edit().putString(parameterName, editText.getText().toString()).apply();
                ApplicationSettings.get(context).invalidate(context);
                ProfileActivity activity = (ProfileActivity) getActivity();
                if (activity != null) {
                    activity.init();
                }
                dismiss();
            });
            builder.setNegativeButton(getString(R.string.sv_cancel), (dialogInterface, i) -> {
                dismiss();
            });
            LayoutInflater layoutInflater = getActivity().getLayoutInflater();
            View contentView = layoutInflater.inflate(R.layout.dialog_enter_string, null);
            editText = (EditText) contentView.findViewById(R.id.value);
            editText.setText(value);
            builder.setView(contentView);
            builder.setTitle(title);
            return builder.build();
        }

        @Override
        public void onSaveInstanceState(final Bundle outState) {
            outState.putString(TITLE, title);
            outState.putString(VALUE, editText.getText().toString());
            outState.putString(PARAMETER_NAME, parameterName);
            super.onSaveInstanceState(outState);
        }


    }

}
