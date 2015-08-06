package com.danilov.mangareaderplus.activity;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageSwitcher;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.danilov.mangareaderplus.R;
import com.danilov.mangareaderplus.core.application.ApplicationSettings;
import com.danilov.mangareaderplus.core.database.DatabaseAccessException;
import com.danilov.mangareaderplus.core.database.HistoryDAO;
import com.danilov.mangareaderplus.core.database.MangaDAO;
import com.danilov.mangareaderplus.core.interfaces.MangaShowObserver;
import com.danilov.mangareaderplus.core.interfaces.MangaShowStrategy;
import com.danilov.mangareaderplus.core.model.LocalManga;
import com.danilov.mangareaderplus.core.model.Manga;
import com.danilov.mangareaderplus.core.model.MangaChapter;
import com.danilov.mangareaderplus.core.strategy.OfflineManga;
import com.danilov.mangareaderplus.core.strategy.OnlineManga;
import com.danilov.mangareaderplus.core.strategy.ShowMangaException;
import com.danilov.mangareaderplus.core.util.Constants;
import com.danilov.mangareaderplus.core.util.Promise;
import com.danilov.mangareaderplus.core.util.ServiceContainer;
import com.danilov.mangareaderplus.core.util.Utils;
import com.danilov.mangareaderplus.core.view.InAndOutAnim;
import com.danilov.mangareaderplus.core.view.MangaViewPager;
import com.danilov.mangareaderplus.core.view.SubsamplingScaleImageView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.util.ArrayList;

/**
 * Created by Semyon Danilov on 06.08.2014.
 */
public class MangaViewerActivity extends BaseToolbarActivity implements ViewPager.OnPageChangeListener, MangaShowObserver, MangaShowStrategy.MangaStrategyListener, View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "MangaViewerActivity";

    private static final String CURRENT_CHAPTER_KEY = "CCK";
    private static final String CURRENT_IMAGE_KEY = "CIK";
    private static final String CHAPTERS_KEY = "CK";
    private static final String URIS_KEY = "UK";

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

    private View drawerRightOffsetTop;
    private View drawerRightOffsetBottom;
    private View bottomBar;

    private MangaShowStrategy currentStrategy;
    private Manga manga;
    private int fromChapter;
    private int fromPage;

    private ApplicationSettings settings;

    private View tutorials;

    private DialogFragment progressDialog = null;
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
        mangaViewPager.setFactory(new SubsamplingImageViewFactory());
        this.nextBtn = findViewWithId(R.id.nextBtn);
        this.prevBtn = findViewWithId(R.id.prevBtn);
        this.nextBtnBottom = findViewWithId(R.id.nextBtnBottom);
        this.prevBtnBottom = findViewWithId(R.id.prevBtnBottom);
        this.pageSpinner = findViewWithId(R.id.imagePicker);
        this.chapterSpinner = findViewWithId(R.id.chapterPicker);
        this.imageProgressBar = findViewWithId(R.id.imageProgressBar);
        this.nextChapter = findViewWithId(R.id.next_chapter);
        this.drawerRightOffsetBottom = findViewById(R.id.drawer_right_offset_bottom);
        this.drawerRightOffsetTop = findViewById(R.id.drawer_right_offset_top);
        this.tutorials = findViewById(R.id.tutorials);
        this.showButtonsCheckbox = findViewWithId(R.id.show_btns_checkbox);
        this.rtlCheckbox = findViewWithId(R.id.rtl_checkbox);
        this.bottomBar = findViewWithId(R.id.bottom_bar);
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
        isTutorialPassed = settings.isTutorialViewerPassed();
        showTutorial(isTutorialPassed ? -1 : 1);
        this.showButtonsCheckbox.setChecked(settings.isShowViewerButtonsAlways());
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

        //loading anims
        Animation nextInAnim = AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_in_right);
        Animation nextOutAnim = AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_out_left);
        Animation prevInAnim = AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_in_left);
        Animation prevOutAnim = AnimationUtils.loadAnimation(getBaseContext(), R.anim.slide_out_right);

        InAndOutAnim next = new InAndOutAnim(nextInAnim, nextOutAnim);
        next.setDuration(150);
        InAndOutAnim prev = new InAndOutAnim(prevInAnim, prevOutAnim);
        prev.setDuration(150);

        if (!showOnline) {
            currentStrategy = new OfflineManga((LocalManga) manga, mangaViewPager, next, prev);
        } else {
            prepareOnlineManga();
            currentStrategy = new OnlineManga(manga, mangaViewPager, next, prev);
        }
        currentStrategy.setOnStrategyListener(this);
        currentStrategy.setObserver(this);
        init(savedInstanceState);
        closeKeyboard();
        if (!settings.isShowViewerButtonsAlways()) {
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
        int _currentChapterNumber = fromChapter;
        int _currentImageNumber = fromPage;
        boolean _hasUriLoaded = false;
        if (savedState != null) {
            _currentChapterNumber = savedState.getInt(CURRENT_CHAPTER_KEY, -1);
            _currentImageNumber = savedState.getInt(CURRENT_IMAGE_KEY, 0);
            ArrayList<MangaChapter> chapters = savedState.getParcelableArrayList(CHAPTERS_KEY);
            if (chapters != null) {
                manga.setChapters(chapters);
            }
            ArrayList<String> uris = savedState.getStringArrayList(URIS_KEY);
            _hasUriLoaded = uris != null;
            Log.d(TAG, "RESTORE CCN: " + _currentChapterNumber + " CIN: " + _currentImageNumber);
            currentStrategy.restoreState(uris, _currentChapterNumber, _currentImageNumber);
        }
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        progressDialog = Utils.easyDialogProgress(getSupportFragmentManager(), "Loading", "Initializing chapters");

        final boolean hasUrisLoaded = _hasUriLoaded;
        final int currentChapterNumber = _currentChapterNumber;
        final int currentImageNumber = _currentImageNumber == -1 ? 0 : _currentImageNumber;

        currentStrategy.initStrategy().then(new Promise.Action<MangaShowStrategy.Result, Promise<MangaShowStrategy.Result>>() {

            @Override
            public Promise<MangaShowStrategy.Result> action(final MangaShowStrategy.Result data, final boolean success) {
                try {
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    if (hasUrisLoaded) {
                        currentStrategy.showImage(currentImageNumber);
                    } else {
                        return currentStrategy.showChapter(currentChapterNumber);
                    }
                } catch (ShowMangaException e) {
                    Log.e(TAG, "Failed to show chapter: " + e.getMessage(), e);
                }
                return null;
            }

        }).then(new Promise.Action<Promise<MangaShowStrategy.Result>, Object>() {

            @Override
            public Object action(final Promise<MangaShowStrategy.Result> promise, final boolean success) {
                if (promise != null) {
                    promise.then(new Promise.Action<MangaShowStrategy.Result, Object>() {

                        @Override
                        public Object action(final MangaShowStrategy.Result result, final boolean success) {
                            switch (result) {
                                case ERROR:
                                    break;
                                case LAST_DOWNLOADED:
                                    onShowMessage("Последняя из скачанных");
                                case SUCCESS:
                                    currentStrategy.showImage(currentImageNumber);
                                    break;
                                case NOT_DOWNLOADED:
                                    onShowMessage("Эта глава не загружена");
                                    break;
                                case NO_SUCH_CHAPTER:
                                    onShowMessage("Главы с таким номером нет");
                                    return null;
                                case ALREADY_FINAL_CHAPTER:
                                    onShowMessage("Это последняя глава");
                                    return null;
                            }
                            return null;
                        }

                    });
                }
                return null;
            }
        });
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
                if (mInterstitialAd.isLoaded()) {
//                    mInterstitialAd.show();
                }
                int curChapter = (int) chapterSpinner.getSelectedItem() - 1;
                showChapter(curChapter + 1);
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
            showChapter(chapterNum);
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
            currentStrategy.showImage(imageNum);
        }

        @Override
        public void onNothingSelected(final AdapterView<?> adapterView) {

        }
    }

    private void showChapter(final int chapterNum) {
        try {
            currentStrategy.showChapter(chapterNum).then(new Promise.Action<MangaShowStrategy.Result, Object>() {
                @Override
                public Object action(final MangaShowStrategy.Result data, final boolean success) {
                    switch (data) {
                        case ERROR:
                            break;
                        case LAST_DOWNLOADED:
                            Toast.makeText(MangaViewerActivity.this, "Последняя из скачанных", Toast.LENGTH_LONG).show();
                        case SUCCESS:
                            currentStrategy.showImage(0);
                            break;
                        case NOT_DOWNLOADED:
                            Toast.makeText(MangaViewerActivity.this, "Эта глава не загружена", Toast.LENGTH_LONG).show();
                            break;
                        case NO_SUCH_CHAPTER:
                            Toast.makeText(MangaViewerActivity.this, "Главы с таким номером нет", Toast.LENGTH_LONG).show();
                            return null;
                        case ALREADY_FINAL_CHAPTER:
                            Toast.makeText(MangaViewerActivity.this, "Это последняя глава", Toast.LENGTH_LONG).show();
                            return null;
                    }
                    return null;
                }
            });
        } catch (ShowMangaException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void onUpdate(final MangaShowStrategy strategy) {
        int currentChapter = strategy.getCurrentChapterNumber();
        int totalChapters = strategy.getTotalChaptersNumber();
        int currentImage = strategy.getCurrentImageNumber();
        int totalImages = strategy.getTotalImageNumber();

        MangaControlSpinnerAdapter chapterAdapter = (MangaControlSpinnerAdapter) chapterSpinner.getAdapter();
        if (chapterAdapter == null) {
            chapterAdapter = new MangaControlSpinnerAdapter(0, totalChapters);
            chapterSpinner.setAdapter(chapterAdapter);
        } else {
            chapterAdapter.change(0, totalChapters);
        }
        chapterSpinner.setTag(currentChapter);
        chapterSpinner.setSelection(currentChapter, false);


        MangaControlSpinnerAdapter pageAdapter = (MangaControlSpinnerAdapter) pageSpinner.getAdapter();
        if (pageAdapter == null) {
            pageAdapter = new MangaControlSpinnerAdapter(0, totalImages);
            pageSpinner.setAdapter(pageAdapter);
        } else {
            pageAdapter.change(0, totalImages);
        }
        pageSpinner.setTag(currentImage);
        pageSpinner.setSelection(currentImage);

        toggleNextChapterButton(currentImage == totalImages - 1);

    }

    private void toggleNextChapterButton(final boolean enable) {
        nextChapter.setVisibility(enable ? View.VISIBLE : View.GONE);
        bottomBar.setVisibility(enable ? View.INVISIBLE : View.VISIBLE);
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
            if (view != null) {
                textView = (TextView) view;
            } else {
                textView = (TextView) View.inflate(context, R.layout.chapter_or_page_spinner_item, null);
            }
            textView.setText("" + (i + 1 + first));
            return textView;
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
            currentStrategy.previous();
        } catch (ShowMangaException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void onNext() {
        try {
            Promise<MangaShowStrategy.Result> promise = currentStrategy.next();
            if (promise != null) {

                //это значит, что мы показываем следующую главу
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                }

                promise.then(new Promise.Action<MangaShowStrategy.Result, Object>() {
                    @Override
                    public Object action(final MangaShowStrategy.Result result, final boolean success) {
                        switch (result) {
                            case LAST_DOWNLOADED:
                                onShowMessage("Последняя из скачанных");
                                break;
                            case NOT_DOWNLOADED:
                                onShowMessage("Эта глава не загружена");
                                return null;
                            case NO_SUCH_CHAPTER:
                                onShowMessage("Главы с таким номером нет");
                                return null;
                            case ALREADY_FINAL_CHAPTER:
                                onShowMessage("Это последняя глава");
                                return null;
                        }
                        currentStrategy.showImage(0);
                        return null;
                    }
                });
            }
        } catch (ShowMangaException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        int currentChapterNumber = currentStrategy.getCurrentChapterNumber();
        int currentImageNumber = currentStrategy.getCurrentImageNumber();
        Log.d(TAG, "CCN: " + currentChapterNumber + " CIN: " + currentImageNumber);
        outState.putInt(CURRENT_CHAPTER_KEY, currentStrategy.getCurrentChapterNumber());
        outState.putInt(CURRENT_IMAGE_KEY, currentStrategy.getCurrentImageNumber());
        outState.putParcelable(Constants.MANGA_PARCEL_KEY, manga);

        ArrayList<MangaChapter> chapterList = Utils.listToArrayList(manga.getChapters());
        if (chapterList != null) {
            outState.putParcelableArrayList(CHAPTERS_KEY, chapterList);
        }
        ArrayList<String> uris = Utils.listToArrayList(currentStrategy.getChapterUris());
        if (uris != null) {
            outState.putStringArrayList(URIS_KEY, uris);
        }
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
        currentStrategy.destroy();
        super.onDestroy();
    }

    private void save() {
        int currentChapterNumber = currentStrategy.getCurrentChapterNumber();
        int currentImageNumber = currentStrategy.getCurrentImageNumber();

        HistoryDAO historyDAO = ServiceContainer.getService(HistoryDAO.class);
        try {
            historyDAO.updateHistory(manga, currentStrategy.isOnline(), currentChapterNumber, currentImageNumber);
        } catch (DatabaseAccessException e) {
            Log.e(TAG, "Failed to update history: " + e.getMessage());
        }
        saved = true;
    }

    // the part with MangaStrategyListener
    @Override
    public void onImageLoadStart(final MangaShowStrategy strategy) {
        imageProgressBar.setProgress(0);
        imageProgressBar.setMax(100);
        imageProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onImageLoadProgress(final MangaShowStrategy strategy, final int current, final int total) {
        imageProgressBar.setMax(total);
        imageProgressBar.setProgress(current);
    }

    @Override
    public void onImageLoadEnd(final MangaShowStrategy strategy, final boolean success, final String message) {
        imageProgressBar.setVisibility(View.GONE);
    }


    @Override
    public void onChapterInfoLoadStart(final MangaShowStrategy strategy) {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        progressDialog = Utils.easyDialogProgress(getSupportFragmentManager(), "Loading", "Loading chapter");
    }

    @Override
    public void onChapterInfoLoadEnd(final MangaShowStrategy strategy, final boolean success, final String message) {
        if (!success) {
            String errorMsg = Utils.errorMessage(this, message, R.string.p_failed_to_load_chapter_info);
            Utils.showToast(this, errorMsg);
        }
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onCheckedChanged(final CompoundButton compoundButton, final boolean isChecked) {
        switch (compoundButton.getId()) {
            case R.id.rtl_checkbox:
                settings.setRTLMode(isChecked);
                settings.update(this);
                setReadingMode(isChecked);
                break;
            case R.id.show_btns_checkbox:
                settings.setShowViewerButtonsAlways(isChecked);
                settings.update(getApplicationContext());
                if (isChecked) {
                    showBtns();
                } else {
                    hideBtns(0);
                }
                break;
        }
    }

    int pos = 0;

    @Override
    public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
        if (pos != position) {
            try {
                currentStrategy.next();
            } catch (ShowMangaException e) {
                e.printStackTrace();
            }
            pos = position;
        }
    }

    @Override
    public void onPageSelected(final int position) {

    }

    @Override
    public void onPageScrollStateChanged(final int state) {

    }

    // MangaStrategyListener realization end

    private class SubsamplingImageViewFactory implements ViewSwitcher.ViewFactory {

        @Override
        public View makeView() {
            SubsamplingScaleImageView touchImageView = new SubsamplingScaleImageView(MangaViewerActivity.this);

            touchImageView.setLayoutParams(new
                    ImageSwitcher.LayoutParams(
                    ImageSwitcher.LayoutParams.MATCH_PARENT, ImageSwitcher.LayoutParams.MATCH_PARENT));
            touchImageView.setVisibility(View.INVISIBLE);
            touchImageView.setMaxScale(4);
            //touchImageView.setDebug(true);
            return touchImageView;
        }

    }

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

    @Override
    protected void onPause() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
        super.onPause();
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
                           showTutorial(-1);
                           settings.setTutorialViewerPassed(true);
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



    //ad routine

    private InterstitialAd mInterstitialAd;

    private void adInit() {
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
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("EB1FD6B44B7963BCCA24AB79D46C7AD1")
                .build();
        mInterstitialAd.loadAd(adRequest);
    }

}