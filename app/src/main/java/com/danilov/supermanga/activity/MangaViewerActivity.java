package com.danilov.supermanga.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.danilov.supermanga.R;
import com.danilov.supermanga.core.application.ApplicationSettings;
import com.danilov.supermanga.core.database.DatabaseAccessException;
import com.danilov.supermanga.core.database.HistoryDAO;
import com.danilov.supermanga.core.database.MangaDAO;
import com.danilov.supermanga.core.dialog.RateDialog;
import com.danilov.supermanga.core.interfaces.MangaShowStrategy;
import com.danilov.supermanga.core.model.LocalManga;
import com.danilov.supermanga.core.model.Manga;
import com.danilov.supermanga.core.model.MangaChapter;
import com.danilov.supermanga.core.strategy.OfflineManga;
import com.danilov.supermanga.core.strategy.OnlineManga;
import com.danilov.supermanga.core.strategy.ShowMangaException;
import com.danilov.supermanga.core.strategy.StrategyDelegate;
import com.danilov.supermanga.core.strategy.StrategyHolder;
import com.danilov.supermanga.core.util.Constants;
import com.danilov.supermanga.core.util.ServiceContainer;
import com.danilov.supermanga.core.util.Utils;
import com.danilov.supermanga.core.view.MangaViewPager;
import com.danilov.supermanga.core.view.SlidingLayer;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Semyon Danilov on 06.08.2014.
 */
public class MangaViewerActivity extends BaseToolbarActivity implements StrategyDelegate.MangaShowListener, View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "MangaViewerActivity";

    private static final String PROGRESS_DIALOG_TAG = "MangaViewerActivityProgressDialog";

    private static final int CHAPTERS_READ_RATE_THRESHOLD = 4; //сколько глав до показа диалога "оцените"

    private static final String CURRENT_CHAPTER_KEY = "CCK";
    private static final String CURRENT_IMAGE_KEY = "CIK";
    private static final String CHAPTERS_KEY = "CK";
    private static final String URIS_KEY = "UK";
    private static final String CHAPTERS_READ = "CR";

    private MangaViewPager mangaViewPager;
    private TextView nextBtn;
    private TextView prevBtn;
    private TextView nextBtnBottom;
    private TextView prevBtnBottom;
    private Spinner pageSpinner;
    private Spinner chapterSpinner;
    private ProgressBar imageProgressBar;
    private CheckBox showButtonsCheckbox;
    private CheckBox rtlCheckbox;
    private Button nextChapter;
    private Button reInitButton;

    private View drawerRightOffsetTop;
    private View drawerRightOffsetBottom;
    private View bottomBar;

    private SlidingLayer slidingLayer;

    private StrategyDelegate strategy;
    private StrategyHolder strategyHolder;
    private Manga manga;
    private int fromChapter;
    private int fromPage;

    private int chaptersRead = 0;

    private ApplicationSettings settings;

    private View tutorials;

    private DialogFragment progressDialog = null;
    private RateDialog rateDialog = null;

    private boolean isFullscreen = false;
    private boolean isTutorialPassed = false;

    private boolean showOnline;

    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manga_viewer_activity);

        //advertisment
        adInit();

        this.mangaViewPager = findViewWithId(R.id.imageSwitcher);
        mangaViewPager.setFragmentManager(getSupportFragmentManager());
        this.nextBtn = findViewWithId(R.id.nextBtn);
        this.prevBtn = findViewWithId(R.id.prevBtn);
        this.nextBtnBottom = findViewWithId(R.id.nextBtnBottom);
        this.prevBtnBottom = findViewWithId(R.id.prevBtnBottom);
        this.pageSpinner = findViewWithId(R.id.imagePicker);
        this.chapterSpinner = findViewWithId(R.id.chapterPicker);
        this.imageProgressBar = findViewWithId(R.id.imageProgressBar);
        this.nextChapter = findViewWithId(R.id.next_chapter);
        this.slidingLayer = findViewWithId(R.id.selector);
        this.drawerRightOffsetBottom = findViewById(R.id.drawer_right_offset_bottom);
        this.drawerRightOffsetTop = findViewById(R.id.drawer_right_offset_top);
        this.tutorials = findViewById(R.id.tutorials);
        this.showButtonsCheckbox = findViewWithId(R.id.show_btns_checkbox);
        this.rtlCheckbox = findViewWithId(R.id.rtl_checkbox);
        this.bottomBar = findViewWithId(R.id.bottom_bar);
        this.reInitButton = findViewWithId(R.id.reinit);
        reInitButton.setOnClickListener(this);
        chapterSpinner.setOnItemSelectedListener(new ChapterSpinnerListener());
        pageSpinner.setOnItemSelectedListener(new ImageSpinnerListener());
        settings = ApplicationSettings.get(this);
        nextBtn.setOnClickListener(this);
        prevBtn.setOnClickListener(this);
        nextBtnBottom.setOnClickListener(this);
        prevBtnBottom.setOnClickListener(this);
        nextChapter.setOnClickListener(this);
        drawerRightOffsetTop.setOnTouchListener(new DisabledTouchEvent());
        drawerRightOffsetBottom.setOnTouchListener(new DisabledTouchEvent());
        toggleFullscreen(true);

        final ApplicationSettings.UserSettings userSettings = settings.getUserSettings();

        isTutorialPassed = userSettings.isTutorialViewerPassed();
        showTutorial(isTutorialPassed ? -1 : 1);
        this.showButtonsCheckbox.setChecked(userSettings.isAlwaysShowButtons());
        isRTL = settings.isRTLMode();
        this.rtlCheckbox.setChecked(isRTL);
        setReadingMode(isRTL);
        this.showButtonsCheckbox.setOnCheckedChangeListener(this);
        this.rtlCheckbox.setOnCheckedChangeListener(this);
        Intent intent = getIntent();
        if (savedInstanceState != null) {
            manga = savedInstanceState.getParcelable(Constants.MANGA_PARCEL_KEY);
            if (manga == null) {
                manga = intent.getParcelableExtra(Constants.MANGA_PARCEL_KEY);
            }
        } else {
            manga = intent.getParcelableExtra(Constants.MANGA_PARCEL_KEY);
        }
        TextView mangaTitleTextView = (TextView) findViewById(R.id.manga_title);
        mangaTitleTextView.setText(manga.getTitle());
        fromChapter = intent.getIntExtra(Constants.FROM_CHAPTER_KEY, -1);
        fromPage = intent.getIntExtra(Constants.FROM_PAGE_KEY, -1);
        showOnline = intent.getBooleanExtra(Constants.SHOW_ONLINE, true);

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (savedInstanceState != null) {
            strategyHolder = (StrategyHolder) fragmentManager.getFragment(savedInstanceState, StrategyHolder.NAME);
            progressDialog = (DialogFragment) fragmentManager.findFragmentByTag(PROGRESS_DIALOG_TAG);
            rateDialog = (RateDialog) fragmentManager.findFragmentByTag(RateDialog.TAG);
        }
        if (strategyHolder == null || strategyHolder.getStrategyDelegate() == null) {
            if (!showOnline) {
                strategy = new StrategyDelegate(mangaViewPager, new OfflineManga((LocalManga) manga), false);
            } else {
                prepareOnlineManga();
                strategy =  new StrategyDelegate(mangaViewPager, new OnlineManga(manga), true);
            }

            strategyHolder = StrategyHolder.newInstance(strategy);

            fragmentManager.beginTransaction().add(strategyHolder, StrategyHolder.NAME).commit();
        } else {
            strategy = strategyHolder.getStrategyDelegate();
        }

        init(savedInstanceState);
        closeKeyboard();
        toggleNextChapterButton(false);
        if (!userSettings.isAlwaysShowButtons()) {
            hideBtns(HIDE_TIME_OFFSET);
        }
    }

    private static final int HIDE_TIME_OFFSET = 2000;
    private static final int HIDE_TIME = 1000;

    private boolean isHidden = false;

    private void hideBtns(final int offset) {
        isHidden = true;
        Animation animation = new AlphaAnimation(1.f, 0.f);
        Animation animation1 = new AlphaAnimation(1.f, 0.f);
        Animation animation2 = new AlphaAnimation(1.f, 0.f);
        Animation animation3 = new AlphaAnimation(1.f, 0.f);
        animation.setDuration(HIDE_TIME);
        animation1.setDuration(HIDE_TIME);
        animation2.setDuration(HIDE_TIME);
        animation3.setDuration(HIDE_TIME);
        animation.setStartOffset(offset);
        animation1.setStartOffset(offset);
        animation2.setStartOffset(offset);
        animation3.setStartOffset(offset);
        animation.setFillAfter(true);
        animation1.setFillAfter(true);
        animation2.setFillAfter(true);
        animation3.setFillAfter(true);
        prevBtn.startAnimation(animation);
        prevBtnBottom.startAnimation(animation1);
        nextBtn.startAnimation(animation2);
        nextBtnBottom.startAnimation(animation3);
    }

    private void showBtns() {
        isHidden = false;
        Animation animation = new AlphaAnimation(0.f, 1.f);
        Animation animation1 = new AlphaAnimation(0.f, 1.f);
        Animation animation2 = new AlphaAnimation(0.f, 1.f);
        Animation animation3 = new AlphaAnimation(0.f, 1.f);
        animation.setDuration(HIDE_TIME);
        animation1.setDuration(HIDE_TIME);
        animation2.setDuration(HIDE_TIME);
        animation3.setDuration(HIDE_TIME);
        animation.setFillAfter(true);
        animation1.setFillAfter(true);
        animation2.setFillAfter(true);
        animation3.setFillAfter(true);
        prevBtn.startAnimation(animation);
        prevBtnBottom.startAnimation(animation1);
        nextBtn.startAnimation(animation2);
        nextBtnBottom.startAnimation(animation3);
    }

    private boolean isRTL = false;
    private void setReadingMode(final boolean isRTL) {
        this.isRTL = isRTL;

        final String btnForwardText = getString(R.string.btn_forward);
        final String btnBackText = getString(R.string.btn_back);

        this.nextBtnBottom.setText(isRTL ? btnBackText : btnForwardText);
        this.nextBtn.setText(isRTL ? btnBackText : btnForwardText);
        this.prevBtnBottom.setText(isRTL ? btnForwardText : btnBackText);
        this.prevBtn.setText(isRTL ? btnForwardText : btnBackText);
        if (isHidden) {
            hideBtns(0);
        }
        mangaViewPager.setRTL(isRTL);
    }

    private void prepareOnlineManga() {

        //TODO: if got id don't go to DAO
        MangaDAO mangaDAO = ServiceContainer.getService(MangaDAO.class);

        Manga _manga = null;
        try {
            _manga = mangaDAO.getByLinkAndRepository(manga.getUri(), manga.getRepository());
        } catch (DatabaseAccessException e) {
            e.printStackTrace();
            //TODO: show dialog "HISTORY WILL NOT BE SAVED" etc
        }
        if (_manga != null) {
            manga.setId(_manga.getId());
            manga.setFavorite(_manga.isFavorite());
        } else {
            try {
                long id = mangaDAO.addManga(manga);
                manga.setId((int) id);
            } catch (DatabaseAccessException e) {
                e.printStackTrace();
                //TODO: show dialog "HISTORY WILL NOT BE SAVED" etc
            }
        }

    }

    private void init(final Bundle savedState) {
        int currentChapterNumber = fromChapter;
        int currentImageNumber = fromPage;
        if (savedState != null) {
            currentChapterNumber = savedState.getInt(CURRENT_CHAPTER_KEY, -1);
            currentImageNumber = savedState.getInt(CURRENT_IMAGE_KEY, 0);
            chaptersRead = savedState.getInt(CHAPTERS_READ, 0);
            ArrayList<MangaChapter> chapters = savedState.getParcelableArrayList(CHAPTERS_KEY);
            if (chapters != null) {
                manga.setChapters(chapters);
            }
            ArrayList<String> uris = savedState.getStringArrayList(URIS_KEY);
            Log.d(TAG, "RESTORE CCN: " + currentChapterNumber + " CIN: " + currentImageNumber);
            if (strategy.restoreState(mangaViewPager)) {
                return;
            }
        }

        currentImageNumber = currentImageNumber == -1 ? 0 : currentImageNumber;
        if (!strategy.isStrategyInitialized()) {
            if (!strategy.isInitializationInProgress()) {
                showProgressDialog(getString(R.string.loading), getString(R.string.getting_manga_info));
                strategy.initStrategy(currentChapterNumber, currentImageNumber);
            }
        } else {
            strategy.showChapterAndImage(currentChapterNumber, currentImageNumber, false);
        }
    }

    private boolean saveTimerScheduled = false;

    @Override
    public void onShowImage(final int number) {
        if (!saveTimerScheduled) {
            timer.schedule(saveProgressTask, 1000, Constants.VIEWER_SAVE_PERIOD);
            saveTimerScheduled = true;
        }
        update(false);
    }

    @Override
    public void onPreviousPicture() {

    }

    @Override
    public void onShowChapter(final MangaShowStrategy.Result result, final String message) {
        switch (result) {
            case ERROR:
                onShowMessage(getString(R.string.error_while_loading_chapter));
                break;
            case LAST_DOWNLOADED:
                onShowMessage(getString(R.string.last_downloaded));
            case SUCCESS:
                break;
            case NOT_DOWNLOADED:
                onShowMessage(getString(R.string.not_downloaded));
                break;
            case NO_SUCH_CHAPTER:
                onShowMessage(getString(R.string.no_such_chapter));
                break;
            case ALREADY_FINAL_CHAPTER:
                onShowMessage(getString(R.string.already_last_chapter));
                break;
        }
        hideProgress();
        update(false);
    }

    @Override
    public void onNext(@Nullable final Integer chapterNum) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean rated = sharedPreferences.getBoolean(Constants.RATED, false);


        if (chapterNum != null) {
            if (chapterNum == -1) {
                if (!rated && rateDialog == null) {
                    rateDialog = new RateDialog();
                    rateDialog.show(getSupportFragmentManager(), RateDialog.TAG);
                }
            } else {
                chaptersRead++;
                if (!rated && chaptersRead >= CHAPTERS_READ_RATE_THRESHOLD && rateDialog == null) {
                    rateDialog = new RateDialog();
                    rateDialog.show(getSupportFragmentManager(), RateDialog.TAG);
                } else {
                    if (Constants.HAS_ADS) {
                        if (mInterstitialAd.isLoaded()) {
                            mInterstitialAd.show();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onInit(final MangaShowStrategy.Result result, final String message) {
        hideProgress();
        if (result == MangaShowStrategy.Result.SUCCESS) {
            showProgressDialog(getString(R.string.loading), getString(R.string.getting_chapter_info));
        } else if (result == MangaShowStrategy.Result.ERROR) {
            reInitButton.setVisibility(View.VISIBLE);
            onShowMessage(getString(R.string.error_while_init_manga) + ": " + message);
        }
        update(true);
    }

    private void onShowMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MangaViewerActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.prevBtn:
            case R.id.prevBtnBottom:
                if (isRTL) {
                    onNext();
                } else {
                    onPrevious();
                }
                break;
            case R.id.nextBtn:
            case R.id.nextBtnBottom:
                if (isRTL) {
                    onPrevious();
                } else {
                    onNext();
                }
                break;
            case R.id.next_chapter:
                if (!strategy.isStrategyInitialized()) {
                    return;
                }
                int curChapter = (int) chapterSpinner.getSelectedItem() - 1;
                showChapter(curChapter + 1, true);
                break;
            case R.id.reinit:
                if (!strategy.isStrategyInitialized()) {
                    if (!strategy.isInitializationInProgress()) {
                        showProgressDialog(getString(R.string.loading), getString(R.string.getting_manga_info));
                        strategy.reInit();
                    }
                }
                reInitButton.setVisibility(View.GONE);
                break;
        }
    }

    private class ChapterSpinnerListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(final AdapterView<?> adapterView, final View view, final int i, final long l) {
            if (adapterView.getTag() != null) {
                int tag = (int) adapterView.getTag();
                if (tag == i) {
                    return;
                }
            }
            int tmp = (int) chapterSpinner.getSelectedItem();
            Integer chapterNum = tmp - 1;
            if (chapterNum < 0) {
                chapterNum = 0;
            }
            showChapter(chapterNum, false);
        }

        @Override
        public void onNothingSelected(final AdapterView<?> adapterView) {

        }
    }

    private class ImageSpinnerListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(final AdapterView<?> adapterView, final View view, final int i, final long l) {
            if (adapterView.getTag() != null) {
                int tag = (int) adapterView.getTag();
                if (tag == i) {
                    return;
                }
            }
            int tmp = (int) pageSpinner.getSelectedItem();
            Integer imageNum = tmp - 1;
            if (imageNum < 0) {
                imageNum = 0;
            }
            strategy.showImage(imageNum);
        }

        @Override
        public void onNothingSelected(final AdapterView<?> adapterView) {

        }
    }

    private void showChapter(final int chapterNum, final boolean fromNext) {
        showProgressDialog(getString(R.string.loading), getString(R.string.getting_chapter_info));
        strategy.showChapterAndImage(chapterNum, 0, fromNext);
    }

    public void update(final boolean fromInit) {
        int currentChapter = strategy.getCurrentChapterNumber();
        int totalChapters = strategy.getTotalChaptersNumber();
        int currentImage = strategy.getCurrentImageNumber();
        int totalImages = strategy.getTotalImageNumber();


        if (currentChapter < totalChapters) {
            MangaControlSpinnerAdapter chapterAdapter = (MangaControlSpinnerAdapter) chapterSpinner.getAdapter();
            if (chapterAdapter == null) {
                chapterAdapter = new MangaControlSpinnerAdapter(0, totalChapters);
                chapterSpinner.setAdapter(chapterAdapter);
            } else {
                chapterAdapter.change(0, totalChapters);
            }
            chapterSpinner.setTag(currentChapter);
            chapterSpinner.setSelection(currentChapter, false);
        }


        if (currentImage < totalImages) {
            MangaControlSpinnerAdapter pageAdapter = (MangaControlSpinnerAdapter) pageSpinner.getAdapter();
            if (pageAdapter == null) {
                pageAdapter = new MangaControlSpinnerAdapter(0, totalImages);
                pageSpinner.setAdapter(pageAdapter);
            } else {
                pageAdapter.change(0, totalImages);
            }
            pageSpinner.setTag(currentImage);
            pageSpinner.setSelection(currentImage);
        }

        if (!fromInit) {
            toggleNextChapterButton(currentImage == totalImages - 1);
        }
    }

    private void toggleNextChapterButton(final boolean enable) {
        nextChapter.setVisibility(enable ? View.VISIBLE : View.GONE);
        bottomBar.setVisibility(enable ? View.INVISIBLE : View.VISIBLE);
    }

    private void showProgressDialog(final String title, final String message) {
        if (progressDialog == null) {
            progressDialog = Utils.easyDialogProgress(getSupportFragmentManager(), PROGRESS_DIALOG_TAG, title, message);
        } else {
            hideProgress();
            progressDialog = Utils.easyDialogProgress(getSupportFragmentManager(), PROGRESS_DIALOG_TAG, title, message);
        }
    }

    private void hideProgress() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    private class MangaControlSpinnerAdapter implements SpinnerAdapter {

        private int first;
        private int last;

        private DataSetObserver dataSetObserver;

        public MangaControlSpinnerAdapter(final int first, final int last) {
            this.first = first;
            this.last = last;
        }

        @Override
        public int getCount() {
            return last - first;
        }

        @Override
        public View getDropDownView(final int i, final View view, final ViewGroup viewGroup) {
            final Context context = getApplicationContext();
            TextView textView = null;
            if (view != null) {
                textView = (TextView) view;
            } else {
                textView = (TextView) View.inflate(context, R.layout.chapter_or_page_spinner_dropdown, null);
            }
            textView.setText("" + (i + 1 + first));
            return textView;
        }

        public void change(final int first, final int last) {
            if (this.first != first || this.last != last) {
                this.first = first;
                this.last = last;
                dataSetObserver.onChanged();
            }
        }

        @Override
        public void registerDataSetObserver(final DataSetObserver dataSetObserver) {
            this.dataSetObserver = dataSetObserver;
        }

        @Override
        public void unregisterDataSetObserver(final DataSetObserver dataSetObserver) {

        }

        @Override
        public Object getItem(final int i) {
            return i + 1 + first;
        }

        @Override
        public long getItemId(final int i) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getView(final int i, final View view, final ViewGroup viewGroup) {
            final Context context = getApplicationContext();
            TextView textView = null;
            ViewGroup cvg = null;
            if (view != null) {
                cvg = (ViewGroup) view;
            } else {
                cvg = (ViewGroup) View.inflate(context, R.layout.chapter_or_page_spinner_item_selected, null);
            }
            textView = (TextView) cvg.findViewById(android.R.id.text1);
            textView.setText("" + (i + 1 + first));
            return cvg;
        }

        @Override
        public int getItemViewType(final int i) {
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return first == -1;
        }
    }

    private void onPrevious() {
        try {
            strategy.previous();
        } catch (ShowMangaException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void onNext() {
        strategy.next();
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        int currentChapterNumber = strategy.getCurrentChapterNumber();
        int currentImageNumber = strategy.getCurrentImageNumber();
        Log.d(TAG, "CCN: " + currentChapterNumber + " CIN: " + currentImageNumber);
        outState.putInt(CURRENT_CHAPTER_KEY, strategy.getCurrentChapterNumber());
        outState.putInt(CURRENT_IMAGE_KEY, strategy.getCurrentImageNumber());
        outState.putInt(CHAPTERS_READ, chaptersRead);
        outState.putParcelable(Constants.MANGA_PARCEL_KEY, manga);

        ArrayList<MangaChapter> chapterList = Utils.listToArrayList(manga.getChapters());
        if (chapterList != null) {
            outState.putParcelableArrayList(CHAPTERS_KEY, chapterList);
        }
        ArrayList<String> uris = Utils.listToArrayList(strategy.getChapterUris());
        if (uris != null) {
            outState.putStringArrayList(URIS_KEY, uris);
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.putFragment(outState, StrategyHolder.NAME, strategyHolder);
        super.onSaveInstanceState(outState);
    }

    private boolean saved = false;

    @Override
    public void onBackPressed() {
        save();
        saved = true;
        finish();
    }

    @Override
    protected void onDestroy() {
        if (!saved) {
            save();
            saved = true;
        }

        timer.cancel();

//        strategy.destroy();
        super.onDestroy();
    }

    private void save() {
        int currentChapterNumber = strategy.getCurrentChapterNumber();
        int currentImageNumber = strategy.getCurrentImageNumber();

        HistoryDAO historyDAO = ServiceContainer.getService(HistoryDAO.class);
        try {
            historyDAO.updateHistory(manga, strategy.isOnline(), currentChapterNumber, currentImageNumber);
        } catch (DatabaseAccessException e) {
            Log.e(TAG, "Failed to update history: " + e.getMessage());
        }
    }

    // the part with MangaStrategyListener

    @Override
    public void onCheckedChanged(final CompoundButton compoundButton, final boolean isChecked) {
        switch (compoundButton.getId()) {
            case R.id.rtl_checkbox:
                settings.setRTLMode(isChecked);
                settings.update(this);
                setReadingMode(isChecked);
                break;
            case R.id.show_btns_checkbox:
                final ApplicationSettings.UserSettings userSettings = settings.getUserSettings();
                userSettings.setAlwaysShowButtons(isChecked);
                settings.update(getApplicationContext());
                if (isChecked) {
                    showBtns();
                } else {
                    hideBtns(0);
                }
                break;
        }
    }

    // MangaStrategyListener realization end

    private void closeKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(findViewById(android.R.id.content).getWindowToken(), 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.manga_viewer_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.full_screen:
                toggleFullscreen(true);
                return true;
        }
        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    private long startedReading;

    @Override
    protected void onPause() {
        long delta = System.currentTimeMillis() - startedReading;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        long timeRead = sharedPreferences.getLong(Constants.Settings.TIME_READ, 0);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putLong(Constants.Settings.TIME_READ, timeRead + delta).apply();

        strategy.onPause();
        if (!saved && strategy.isStrategyInitialized()) {
            save();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        startedReading = System.currentTimeMillis();
        super.onResume();
        strategy.onResume(this);
    }

    private void toggleFullscreen(final boolean fullscreen) {
        this.isFullscreen = fullscreen;
        boolean oldFullscreen = settings.isViewerFullscreen();
        if (oldFullscreen != fullscreen) {
            settings.setViewerFullscreen(fullscreen);
            settings.update(this);
        }
        if (fullscreen) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getSupportActionBar().hide();
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getSupportActionBar().show();
        }
    }

    private void showTutorial(final int number) {
        View tutorialView = null;
        switch (number) {
            case -1:
                tutorials.setVisibility(View.GONE);
                break;
            case 1:
                tutorials.setVisibility(View.VISIBLE);
                findViewById(R.id.tutorialView2).setVisibility(View.INVISIBLE);
                tutorialView = tutorials.findViewById(R.id.tutorialView);
                break;
            case 2:
                tutorials.setVisibility(View.VISIBLE);
                findViewById(R.id.tutorialView).setVisibility(View.INVISIBLE);
                tutorialView = tutorials.findViewById(R.id.tutorialView2);
                break;
        }
        if (tutorialView != null) {
            tutorialView.setVisibility(View.VISIBLE);
            Button closeTutorial = (Button) tutorialView.findViewById(R.id.close_tutorial);
            closeTutorial.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                   switch (number) {
                       case 1:
                           showTutorial(2);
                           break;
                       case 2:
                           slidingLayer.openLayer(true);
                           slidingLayer.postDelayed(new Runnable() {
                               @Override
                               public void run() {
                                   slidingLayer.closeLayer(true);
                               }
                           }, 1000);
                           Toast.makeText(MangaViewerActivity.this, getString(R.string.happy_reading), Toast.LENGTH_LONG).show();
                           showTutorial(-1);
                           final ApplicationSettings.UserSettings userSettings = settings.getUserSettings();
                           userSettings.setTutorialViewerPassed(true);
                           settings.update(getApplicationContext());
                           hideBtns(HIDE_TIME_OFFSET);
                           break;
                   }
                }
            });
        }
    }

    private class DisabledTouchEvent implements View.OnTouchListener {

        @Override
        public boolean onTouch(final View view, final MotionEvent motionEvent) {
            motionEvent.setEdgeFlags(4099);
            return false;
        }

    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(final int keyCode, final KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                onNext();
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                onPrevious();
                return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private Timer timer = new Timer();

    private TimerTask saveProgressTask = new TimerTask() {

        @Override
        public void run() {
            save();
        }

    };

    //ad routine

    private InterstitialAd mInterstitialAd;

    private void adInit() {
        if (!Constants.HAS_ADS) {
            return;
        }
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.banner_ad_unit_id));
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
            }
        });
        requestNewInterstitial();
    }

    private void requestNewInterstitial() {
        if (!Constants.HAS_ADS) {
            return;
        }
        AdRequest adRequest = new AdRequest.Builder()
//                .addTestDevice(Utils.getDeviceId(this))
                .build();
        mInterstitialAd.loadAd(adRequest);
    }


}