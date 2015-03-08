package com.danilov.mangareader.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.danilov.mangareader.R;
import com.danilov.mangareader.core.application.ApplicationSettings;
import com.danilov.mangareader.core.database.DatabaseAccessException;
import com.danilov.mangareader.core.database.HistoryDAO;
import com.danilov.mangareader.core.database.MangaDAO;
import com.danilov.mangareader.core.interfaces.MangaShowObserver;
import com.danilov.mangareader.core.interfaces.MangaShowStrategy;
import com.danilov.mangareader.core.model.LocalManga;
import com.danilov.mangareader.core.model.Manga;
import com.danilov.mangareader.core.model.MangaChapter;
import com.danilov.mangareader.core.strategy.OfflineManga;
import com.danilov.mangareader.core.strategy.OnlineManga;
import com.danilov.mangareader.core.strategy.ShowMangaException;
import com.danilov.mangareader.core.util.Constants;
import com.danilov.mangareader.core.util.Promise;
import com.danilov.mangareader.core.util.ServiceContainer;
import com.danilov.mangareader.core.util.Utils;
import com.danilov.mangareader.core.view.InAndOutAnim;
import com.danilov.mangareader.core.view.MangaImageSwitcher;
import com.danilov.mangareader.core.view.SubsamplingScaleImageView;

import java.util.ArrayList;

/**
 * Created by Semyon Danilov on 06.08.2014.
 */
public class MangaViewerActivity extends BaseToolbarActivity implements MangaShowObserver, MangaShowStrategy.MangaStrategyListener, View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "MangaViewerActivity";

    private static final String CURRENT_CHAPTER_KEY = "CCK";
    private static final String CURRENT_IMAGE_KEY = "CIK";
    private static final String CHAPTERS_KEY = "CK";
    private static final String URIS_KEY = "UK";

    private MangaImageSwitcher imageSwitcher;
    private View nextBtn;
    private View prevBtn;
    private View nextBtnBottom;
    private View prevBtnBottom;
    private EditText currentImageEditText;
    private TextView totalImagesTextView;
    private EditText currentChapterEditText;
    private TextView totalChaptersTextView;
    private ProgressBar imageProgressBar;
    private CheckBox showButtonsCheckbox;

    private Button imageOk;
    private Button chapterOk;

    private View drawerRightOffsetTop;
    private View drawerRightOffsetBottom;

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
        this.imageSwitcher = (MangaImageSwitcher) findViewById(R.id.imageSwitcher);
        imageSwitcher.setFactory(new SubsamplingImageViewFactory());
        this.nextBtn = findViewById(R.id.nextBtn);
        this.prevBtn = findViewById(R.id.prevBtn);
        this.nextBtnBottom = findViewById(R.id.nextBtnBottom);
        this.prevBtnBottom = findViewById(R.id.prevBtnBottom);
        this.currentImageEditText = findViewWithId(R.id.imagePicker);
        this.totalImagesTextView = findViewWithId(R.id.imageQuantity);
        this.currentChapterEditText = findViewWithId(R.id.chapterPicker);
        this.totalChaptersTextView = findViewWithId(R.id.chapterQuantity);
        this.imageProgressBar = findViewWithId(R.id.imageProgressBar);
        this.imageOk = findViewWithId(R.id.imageOk);
        this.chapterOk = findViewWithId(R.id.chapterOk);
        this.drawerRightOffsetBottom = findViewById(R.id.drawer_right_offset_bottom);
        this.drawerRightOffsetTop = findViewById(R.id.drawer_right_offset_top);
        this.tutorials = findViewById(R.id.tutorials);
        this.showButtonsCheckbox = findViewWithId(R.id.show_btns_checkbox);
        settings = ApplicationSettings.get(this);
        nextBtn.setOnClickListener(this);
        prevBtn.setOnClickListener(this);
        nextBtnBottom.setOnClickListener(this);
        prevBtnBottom.setOnClickListener(this);
        imageOk.setOnClickListener(this);
        chapterOk.setOnClickListener(this);
        drawerRightOffsetTop.setOnTouchListener(new DisabledTouchEvent());
        drawerRightOffsetBottom.setOnTouchListener(new DisabledTouchEvent());
        toggleFullscreen(true);
        isTutorialPassed = settings.isTutorialViewerPassed();
        showTutorial(isTutorialPassed ? -1 : 1);
        this.showButtonsCheckbox.setChecked(settings.isShowViewerButtonsAlways());
        this.showButtonsCheckbox.setOnCheckedChangeListener(this);
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
            currentStrategy = new OfflineManga((LocalManga) manga, imageSwitcher, next, prev);
        } else {
            prepareOnlineManga();
            currentStrategy = new OnlineManga(manga, imageSwitcher, next, prev);
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

    private void hideBtns(final int offset) {
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
        progressDialog = Utils.easyDialogProgress(getSupportFragmentManager(), "Loading", "Initializing chapters");

        final boolean hasUrisLoaded = _hasUriLoaded;
        final int currentChapterNumber = _currentChapterNumber;
        final int currentImageNumber = _currentImageNumber == -1 ? 0 : _currentImageNumber;

        currentStrategy.initStrategy().then(new Promise.Action<MangaShowStrategy.Result, Promise<MangaShowStrategy.Result>>() {

            @Override
            public Promise<MangaShowStrategy.Result> action(final MangaShowStrategy.Result data, final boolean success) {
                try {
                    progressDialog.dismiss();
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
                                    Toast.makeText(MangaViewerActivity.this, "Последняя из скачанных", Toast.LENGTH_LONG).show();
                                case SUCCESS:
                                    currentStrategy.showImage(currentImageNumber);
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
                }
                return null;
            }
        });
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.prevBtn:
                onPrevious();
                break;
            case R.id.nextBtn:
                onNext();
                break;
            case R.id.prevBtnBottom:
                onPrevious();
                break;
            case R.id.nextBtnBottom:
                onNext();
                break;
            case R.id.imageOk:
                goToImageFromImagePicker();
                break;
            case R.id.chapterOk:
                goToChapterFromChapterPicker();
                break;
        }
    }

    private void goToChapterFromChapterPicker() {
        String chapterString = currentChapterEditText.getText().toString();
        Integer tmp;
        try {
            tmp = Integer.valueOf(chapterString);
        } catch (NumberFormatException e) {
            return;
        }
        Integer chapterNum = tmp - 1;
        if (chapterNum < 0) {
            chapterNum = 0;
        }
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
        closeKeyboard();
    }

    private void goToImageFromImagePicker() {
        String imageString = currentImageEditText.getText().toString();
        Integer tmp;
        try {
            tmp = Integer.valueOf(imageString);
        } catch (NumberFormatException e) {
            return;
        }
        Integer imageNum = tmp - 1;
        if (imageNum < 0) {
            imageNum = 0;
        }
        currentStrategy.showImage(imageNum);
        closeKeyboard();
    }

    @Override
    public void onUpdate(final MangaShowStrategy strategy) {
        int currentChapter = strategy.getCurrentChapterNumber();
        String totalChapters = strategy.getTotalChaptersNumber();
        int currentImage = strategy.getCurrentImageNumber();
        int totalImages = strategy.getTotalImageNumber();
        currentChapterEditText.setText(String.valueOf(currentChapter + 1));
        currentImageEditText.setText(String.valueOf(currentImage + 1));
        totalImagesTextView.setText(String.valueOf(totalImages));
        totalChaptersTextView.setText(totalChapters);
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
                promise.then(new Promise.Action<MangaShowStrategy.Result, Object>() {
                    @Override
                    public Object action(final MangaShowStrategy.Result result, final boolean success) {
                        switch (result) {
                            case LAST_DOWNLOADED:
                                Toast.makeText(MangaViewerActivity.this, "Последняя из скачанных", Toast.LENGTH_LONG).show();
                                break;
                            case NOT_DOWNLOADED:
                                Toast.makeText(MangaViewerActivity.this, "Эта глава не загружена", Toast.LENGTH_LONG).show();
                                return null;
                            case NO_SUCH_CHAPTER:
                                Toast.makeText(MangaViewerActivity.this, "Главы с таким номером нет", Toast.LENGTH_LONG).show();
                                return null;
                            case ALREADY_FINAL_CHAPTER:
                                Toast.makeText(MangaViewerActivity.this, "Это последняя глава", Toast.LENGTH_LONG).show();
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
        currentStrategy.destroy();
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
    public void onCheckedChanged(final CompoundButton compoundButton, final boolean b) {
        settings.setShowViewerButtonsAlways(b);
        settings.update(getApplicationContext());
        if (b) {
            showBtns();
        } else {
            hideBtns(0);
        }
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

}