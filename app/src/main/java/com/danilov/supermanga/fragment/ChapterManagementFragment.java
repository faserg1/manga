package com.danilov.supermanga.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.danilov.supermanga.R;
import com.danilov.supermanga.activity.BaseToolbarActivity;
import com.danilov.supermanga.activity.MainActivity;
import com.danilov.supermanga.core.database.DatabaseAccessException;
import com.danilov.supermanga.core.database.HistoryDAO;
import com.danilov.supermanga.core.database.MangaDAO;
import com.danilov.supermanga.core.database.UpdatesDAO;
import com.danilov.supermanga.core.decor.DividerItemDecoration;
import com.danilov.supermanga.core.model.HistoryElement;
import com.danilov.supermanga.core.model.Manga;
import com.danilov.supermanga.core.model.MangaChapter;
import com.danilov.supermanga.core.model.UpdatesElement;
import com.danilov.supermanga.core.repository.OfflineEngine;
import com.danilov.supermanga.core.repository.RepositoryEngine;
import com.danilov.supermanga.core.repository.RepositoryException;
import com.danilov.supermanga.core.util.Constants;
import com.danilov.supermanga.core.util.IoUtils;
import com.danilov.supermanga.core.util.ServiceContainer;
import com.danilov.supermanga.core.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Semyon on 06.03.2016.
 */
public class ChapterManagementFragment extends BaseFragmentNative {

    private static final String TOOLBAR_OFFSET_KEY = "TOOLBAR_OFFSET";

    private RecyclerView chaptersView;

    private Manga manga;

    private boolean withToolbarOffset;

    public static ChapterManagementFragment newInstance(final Manga manga, final boolean withToolbarOffset) {
        ChapterManagementFragment fragment = new ChapterManagementFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.MANGA_PARCEL_KEY, manga);
        bundle.putBoolean(TOOLBAR_OFFSET_KEY, withToolbarOffset);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.chapter_management_fragment, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        chaptersView = findViewById(R.id.chapters);
        chaptersView.setLayoutManager(new LinearLayoutManager(getContext()));
        Bundle bundle = getArguments();
        manga = bundle.getParcelable(Constants.MANGA_PARCEL_KEY);

        withToolbarOffset = bundle.getBoolean(TOOLBAR_OFFSET_KEY);
        findViewById(R.id.fake_toolbar).setVisibility(withToolbarOffset ? View.VISIBLE : View.GONE);

        BaseToolbarActivity infoActivity = (BaseToolbarActivity) getActivity();
        final int baseColor = getResources().getColor(R.color.color_primary);
        infoActivity.getToolbar().setBackgroundColor(Utils.getColorWithAlpha(1.0f, baseColor));

        setupRecycler();
        initChapters();
    }

    private void setupRecycler() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder, final RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                ChapterAdapter adapter = (ChapterAdapter) chaptersView.getAdapter();
                removeChapter(adapter.chapterAt(viewHolder.getAdapterPosition()));
                adapter.deleteMangaChapterAt(viewHolder.getAdapterPosition());
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                ChapterAdapter adapter = (ChapterAdapter) chaptersView.getAdapter();
                ChapterItem chapterItem = adapter.infoAt(viewHolder.getAdapterPosition());
                if (!chapterItem.saved) {
                    return 0;
                }
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(chaptersView);
        chaptersView.addItemDecoration(new DividerItemDecoration(getActivity(), null));
    }

    private void initChapters() {
        ChapterAdapter adapter = new ChapterAdapter(getChaptersInfo());
        chaptersView.setAdapter(adapter);
    }

    private class ChapterAdapter extends RecyclerView.Adapter<ChapterVH> {

        @NonNull
        private List<ChapterItem> chapterItemList;

        public ChapterAdapter(@NonNull final List<ChapterItem> chapterItemList) {
            this.chapterItemList = chapterItemList;
        }

        @Override
        public ChapterVH onCreateViewHolder(final ViewGroup parent, final int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chapter_item, parent, false);
            return new ChapterVH(v);
        }

        @Override
        public void onBindViewHolder(final ChapterVH holder, final int position) {
            ChapterItem item = chapterItemList.get(position);
            MangaChapter chapter = item.chapter;
            holder.chapterTitle.setText(getString(R.string.cap_chapter) + " " + (chapter.getNumber() + 1) + ". " + chapter.getTitle());
            holder.isSaved.setVisibility(item.saved ? View.VISIBLE : View.GONE);
            holder.isRed.setVisibility(item.isRed ? View.VISIBLE : View.GONE);
            holder.isNew.setVisibility(item.isNew ? View.VISIBLE : View.GONE);
        }

        @Override
        public int getItemCount() {
            return chapterItemList.size();
        }

        public MangaChapter chapterAt(final int position) {
            return chapterItemList.get(position).chapter;
        }

        public ChapterItem infoAt(final int position) {
            return chapterItemList.get(position);
        }

        public void deleteMangaChapterAt(final int position) {
            ChapterItem chapterItem = chapterItemList.remove(position);
            chapterItem.saved = false;
            RecyclerView.Adapter adapter = chaptersView.getAdapter();
            adapter.notifyItemRemoved(position);
            chapterItemList.add(position, chapterItem);
            adapter.notifyItemInserted(position);
        }

        private void reverse() {
            Collections.reverse(this.chapterItemList);
            notifyDataSetChanged();
        }

    }

    private class ChapterVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView chapterTitle;

        TextView isNew;

        ImageView isSaved;

        ImageView isRed;

        public ChapterVH(final View itemView) {
            super(itemView);
            chapterTitle = (TextView) itemView.findViewById(R.id.chapter_title);
            isSaved = (ImageView) itemView.findViewById(R.id.is_saved);
            isRed = (ImageView) itemView.findViewById(R.id.is_red);
            isNew = (TextView) itemView.findViewById(R.id.is_new);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(final View v) {
            ChapterAdapter adapter = (ChapterAdapter) chaptersView.getAdapter();
            ChapterItem chapterItem = adapter.infoAt(getAdapterPosition());
            Callback activity = (Callback) getActivity();
            activity.onChapterSelected(manga, chapterItem.chapter, !chapterItem.saved);
        }

    }

    public interface Callback {

        void onChapterSelected(final Manga manga, final MangaChapter chapter, final boolean isOnline);

    }

    private class ChapterItem {

        MangaChapter chapter;

        boolean saved;

        boolean selected;

        boolean isRed;

        boolean isNew;

        public ChapterItem(final MangaChapter chapter, final boolean saved, final boolean selected, final boolean isRed, final boolean isNew) {
            this.chapter = chapter;
            this.saved = saved;
            this.selected = selected;
            this.isRed = isRed;
            this.isNew = isNew;
        }

    }

    //FIXME: printing stacktrace --> error handling
    private List<ChapterItem> getChaptersInfo() {

        MangaDAO mangaDAO = ServiceContainer.getService(MangaDAO.class);
        UpdatesDAO updatesDAO = ServiceContainer.getService(UpdatesDAO.class);
        try {
            Manga _manga = mangaDAO.getByLinkAndRepository(manga.getUri(), manga.getRepository());
            manga = _manga != null ? _manga : manga;
        } catch (DatabaseAccessException e) {
            e.printStackTrace();
        }


        HistoryDAO historyDAO = ServiceContainer.getService(HistoryDAO.class);
        int maxChapter = -1;

        try {
            HistoryElement onlineHistory = historyDAO.getHistoryByManga(manga, true);
            HistoryElement offlineHistory = historyDAO.getHistoryByManga(manga, false);
            if (onlineHistory != null) {
                maxChapter = onlineHistory.getChapter();
            }
            if (offlineHistory != null) {
                if (offlineHistory.getChapter() > maxChapter) {
                    maxChapter = offlineHistory.getChapter();
                }
            }
        } catch (DatabaseAccessException e) {
            e.printStackTrace();
        }

        int newChapters = 0;

        try {
            //апдейты, которые пользователь ещё не удалил
            UpdatesElement updatesByManga = updatesDAO.getUpdatesByManga(manga);
            if (updatesByManga != null) {
                newChapters = updatesByManga.getDifference();
            }
        } catch (DatabaseAccessException e) {
            e.printStackTrace();
        }

        if (newChapters < 0) {
            newChapters = 0;
        }


        List<ChapterItem> info = new ArrayList<>();
        int chaptersQuantity = manga.getChaptersQuantity();
        for (int i = 0; i < chaptersQuantity; i++) {
            MangaChapter mangaChapter = new MangaChapter("", i, null);
            //добавим галочку новым главам
            boolean isNew = i >= chaptersQuantity - newChapters;
            ChapterItem item = new ChapterItem(mangaChapter, false, false, maxChapter > i, isNew);
            info.add(item);
        }
        if (!manga.isDownloaded()) {
            return info;
        }

        OfflineEngine engine = (OfflineEngine) RepositoryEngine.DefaultRepository.OFFLINE.getEngine();
        try {
            boolean success = engine.queryForChapters(manga);
            if (!success) {
                return info;
            }
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        List<MangaChapter> downloadedChapters = manga.getChapters();
        for (MangaChapter mangaChapter : downloadedChapters) {
            int number = mangaChapter.getNumber();
            if (number > info.size() - 1 || number < 0) {
                continue;
            }
            ChapterItem chapterItem = info.get(number);
            chapterItem.saved = true;
            chapterItem.chapter = mangaChapter;
        }
        return info;
    }

    private HandlerThread removeMangaChapterThread = null;

    private Handler removeMangaHandler = null;

    private void removeChapter(final MangaChapter mangaChapter) {
        if (removeMangaChapterThread == null || !removeMangaChapterThread.isAlive()) {
            initRemoval();
        }
        removeMangaHandler.post(new Runnable() {
            @Override
            public void run() {
                String uri = mangaChapter.getUri();
                IoUtils.deleteDirectory(new File(uri));
            }
        });
    }

    private void initRemoval() {
        removeMangaChapterThread = new HandlerThread("remove-manga-thread");
        removeMangaChapterThread.start();
        removeMangaHandler = new Handler(removeMangaChapterThread.getLooper());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.chapter_management_fragment_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.inverse:
                ChapterAdapter adapter = (ChapterAdapter) chaptersView.getAdapter();
                adapter.reverse();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}