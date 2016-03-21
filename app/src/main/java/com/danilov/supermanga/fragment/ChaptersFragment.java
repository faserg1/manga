package com.danilov.supermanga.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.danilov.supermanga.R;
import com.danilov.supermanga.activity.MangaInfoActivity;
import com.danilov.supermanga.activity.SingleFragmentActivity;
import com.danilov.supermanga.core.database.DatabaseAccessException;
import com.danilov.supermanga.core.database.UpdatesDAO;
import com.danilov.supermanga.core.dialog.CustomDialog;
import com.danilov.supermanga.core.dialog.CustomDialogFragment;
import com.danilov.supermanga.core.interfaces.RefreshableActivity;
import com.danilov.supermanga.core.model.Manga;
import com.danilov.supermanga.core.model.MangaChapter;
import com.danilov.supermanga.core.model.UpdatesElement;
import com.danilov.supermanga.core.repository.RepositoryEngine;
import com.danilov.supermanga.core.repository.RepositoryException;
import com.danilov.supermanga.core.theme.ThemeUtils;
import com.danilov.supermanga.core.util.Constants;
import com.danilov.supermanga.core.util.ServiceContainer;
import com.danilov.supermanga.core.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Semyon on 09.11.2014.
 */
public class ChaptersFragment extends BaseFragmentNative implements AdapterView.OnItemClickListener {

    private final String TAG = "ChaptersFragment";
    private static final String CHAPTERS_KEY = "CK";
    private static final String SELECTED_CHAPTERS = "SC";

    private MangaInfoActivity activity;
    private RefreshableActivity refreshable;

    private ListView chaptersListView;

    private Button backButton;
    private Button download;
    private Button selectRange;
    private CheckBox checkBox;
    private View selectLast;

    private Manga manga;

    private ChaptersAdapter adapter = null;

    private boolean isLoading = false;

    private boolean[] selection = null;

    public static ChaptersFragment newInstance(final Manga manga) {
        ChaptersFragment chaptersFragment = new ChaptersFragment();
        chaptersFragment.manga = manga;
        return chaptersFragment;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.manga_chapters_fragment, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = (MangaInfoActivity) getActivity();
        refreshable = (RefreshableActivity) getActivity();
        chaptersListView = (ListView) view.findViewById(R.id.chaptersListView);
        backButton = (Button) view.findViewById(R.id.back);
        download = (Button) view.findViewById(R.id.download);
        selectRange = (Button) view.findViewById(R.id.number_select);
        checkBox = (CheckBox) view.findViewById(R.id.select_all);
        selectLast = view.findViewById(R.id.select_last);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (adapter != null) {
                adapter.all(isChecked);
            }
        });
        chaptersListView.setOnItemClickListener(this);
        backButton.setOnClickListener(v -> activity.showInfoFragment(false));
        download.setOnClickListener(v -> {
            if (adapter == null) {
                return;
            }
            Intent intent = new Intent(activity, SingleFragmentActivity.class);
            intent.putExtra(Constants.FRAGMENTS_KEY, SingleFragmentActivity.DOWNLOAD_MANAGER_FRAGMENT);
            intent.putExtra(Constants.MANGA_PARCEL_KEY, manga);
            intent.putIntegerArrayListExtra(Constants.SELECTED_CHAPTERS_KEY, adapter.getSelectedChaptersList());
            startActivity(intent);
            activity.showInfoFragment(false);
        });
        selectLast.setOnClickListener(view1 -> {
            int size = chaptersListView.getCount();
            if (size == 0) {
                return;
            }
            int pos = size - 1;
            if (adapter != null) {
                if (!adapter.isSelected(pos)) {
                    adapter.select(pos);
                }
                chaptersListView.setSelection(pos);
            }
        });
        selectRange.setOnClickListener(v -> showNumberPickerFragment());
        if (savedInstanceState == null) {
            Intent i = activity.getIntent();
            manga = i.getParcelableExtra(Constants.MANGA_PARCEL_KEY);
            if (manga != null) {
                loadChaptersInfo(manga);
            }
        } else {
            restoreInstanceState(savedInstanceState);
        }
        final int baseColor = ThemeUtils.getReferencedResource(R.attr.color_primary, getActivity());
        activity.getToolbar().setBackgroundColor(Utils.getColorWithAlpha(1.0f, baseColor));
    }

    @Override
    public boolean onBackPressed() {
        activity.showInfoFragment(false);
        return true;
    }

    private void loadChaptersInfo(final Manga manga) {
        List<MangaChapter> chapters = manga.getChapters();
        if (chapters != null) {
            showChapters();
        } else {
            isLoading = true;
            refreshable.startRefresh();
            MangaChaptersQueryThread thread = new MangaChaptersQueryThread(manga);
            thread.start();
        }
    }

    private void showChapters() {
        UpdatesDAO updatesDAO = ServiceContainer.getService(UpdatesDAO.class);

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

        List<MangaChapter> chapters = manga.getChapters();
        adapter = new ChaptersAdapter(getActivity(), chapters, newChapters);
        if (selection != null) {
            adapter.setSelectedChapters(selection);
        }
        chaptersListView.setAdapter(adapter);
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        if (manga != null) {
            outState.putParcelable(Constants.MANGA_PARCEL_KEY, manga);
            ArrayList<MangaChapter> chapterList = Utils.listToArrayList(manga.getChapters());
            if (chapterList != null) {
                outState.putParcelableArrayList(CHAPTERS_KEY, chapterList);
            }
            if (adapter != null) {
                outState.putBooleanArray(SELECTED_CHAPTERS, adapter.getSelectedChapters());
            }
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        if (dialogFragment != null) {
            dialogFragment.dismiss();
        }
        super.onPause();
    }

    private boolean isDetached = false;

    @Override
    public void onDetach() {
        isDetached = true;
        super.onDetach();
    }

    public void restoreInstanceState(final Bundle savedInstanceState) {
        manga = savedInstanceState.getParcelable(Constants.MANGA_PARCEL_KEY);
        ArrayList<MangaChapter> chapters = savedInstanceState.getParcelableArrayList(CHAPTERS_KEY);
        if (chapters != null) {
            manga.setChapters(chapters);
            manga.setChaptersQuantity(chapters.size());
            selection = savedInstanceState.getBooleanArray(SELECTED_CHAPTERS);
        }
        if (manga != null) {
            loadChaptersInfo(manga);
        }
        if (isLoading) {
            refreshable.startRefresh();
        } else {
            refreshable.stopRefresh();
        }
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        if (adapter != null) {
            adapter.select(position);
        }
    }

    private CustomDialogFragment dialogFragment = null;

    private void showNumberPickerFragment() {
        dialogFragment = new CustomDialogFragment();
        CustomDialog dialog = null;

        View contentView = getActivity().getLayoutInflater().inflate(R.layout.dialog_select_range, null);
        final EditText from = (EditText) contentView.findViewById(R.id.from);
        final EditText to = (EditText) contentView.findViewById(R.id.to);
        TextView max = (TextView) contentView.findViewById(R.id.max);

        if (manga.getChapters() != null) {
            String all = manga.getChapters().size() + " " + Utils.stringResource(activity, R.string.sv_all);
            max.setText(all);
        }


        CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
        builder.setPositiveButton(R.string.sv_ok, (dialog1, which) -> {
            if (adapter != null) {
                Integer _from = Utils.stringToInt(from.getText().toString());
                Integer _to = Utils.stringToInt(to.getText().toString());
                if (_from == null || _to == null) {
                    return;
                }
                _from--;
                _to--;
                adapter.selectFromAndTo(_from, _to);
            }
            dialogFragment.dismiss();
        });
        builder.setNegativeButton(R.string.sv_cancel, (dialog1, which) -> {
            dialogFragment.dismiss();
        });
        builder.setView(contentView);
        builder.setTitle(R.string.sv_select_range_dialog);
        dialog = builder.build();
        dialogFragment.setDialog(dialog);
        dialogFragment.show(activity.getFragmentManager(), "select-range");
    }

    private class MangaChaptersQueryThread extends Thread {

        private boolean loaded = false;
        private Manga manga;
        private String error = null;

        public MangaChaptersQueryThread(final Manga manga) {
            this.manga = manga;
        }

        @Override
        public void run() {
            RepositoryEngine repositoryEngine = manga.getRepository().getEngine();
            try {
                loaded = repositoryEngine.queryForChapters(manga);
            } catch (RepositoryException e) {
                error = e.getMessage();
                Log.d(TAG, e.getMessage());
            }

            activity.runOnUiThread(() -> {
                if (isDetached) {
                    return;
                }
                if (loaded) {
                    showChapters();
                } else {
                    Context context = getActivity();
                    String message = Utils.errorMessage(context, error, R.string.p_internet_error);
                    Utils.showToast(context, message);
                }
                isLoading = false;
                refreshable.stopRefresh();
            });
        }

    }


    private class ChaptersAdapter extends ArrayAdapter<MangaChapter> {

        private List<MangaChapter> chapters = null;

        private boolean[] selectedChapters = null;

        private int newChapters = 0;

        private boolean reversed = false;

        public void reverse() {
            all(false);
            Collections.reverse(chapters);
            reversed = !reversed;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return chapters.size();
        }

        public ChaptersAdapter(final Context context, final List<MangaChapter> objects, final int newChapters) {
            super(context, 0, objects);
            this.chapters = objects;
            selectedChapters = new boolean[chapters.size()];
            for (int i = 0; i < chapters.size(); i++) {
                selectedChapters[i] = false;
            }
            this.newChapters = newChapters;
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            View view = convertView;
            Holder h = null;
            if (view == null) {
                view = activity.getLayoutInflater().inflate(R.layout.chapter_list_item, null);
                h = new Holder();
                TextView title = (TextView) view.findViewById(R.id.chapterTitle);
                TextView isNew = (TextView) view.findViewById(R.id.is_new);
                h.checkBox = (CheckBox) view.findViewById(R.id.checkbox);
                h.title = title;
                h.isNew = isNew;
                view.setTag(h);
            } else {
                h = (Holder) view.getTag();
            }
            MangaChapter chapter = chapters.get(position);
            h.title.setText((chapter.getNumber() + 1) + ". " + chapter.getTitle());
            h.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> select(position, isChecked));

            h.checkBox.setChecked(selectedChapters[reversed ? (getCount() - 1 - position) : position]);

            boolean isNew = (reversed ? getCount() - 1 - position : position) >= getCount() - newChapters;
            h.isNew.setVisibility(isNew ? View.VISIBLE : View.GONE);

            return view;
        }

        public boolean[] getSelectedChapters() {
            return selectedChapters;
        }

        public void setSelectedChapters(final boolean[] selectedChapters) {
            this.selectedChapters = selectedChapters;
        }

        public void selectFromAndTo(final int from, final int to) {
            for (int i = from; i <= to; i++) {
                if (i < 0 || i > selectedChapters.length - 1) {
                    continue;
                }
                selectedChapters[i] = true;
            }
            notifyDataSetChanged();
        }

        public ArrayList<Integer> getSelectedChaptersList() {
            ArrayList<Integer> selection = new ArrayList<>();
            for (int i = 0; i < selectedChapters.length; i++) {
                if (selectedChapters[i]) {
                    selection.add(i);
                }
            }
            return selection;
        }

        public void select(final int position) {
            int pos = position;
            if (reversed) {
                pos = getCount() - 1 - position;
            }
            selectedChapters[pos] = !selectedChapters[pos];
            notifyDataSetChanged();
        }

        public void select(final int position, final boolean isChecked) {
            int pos = position;
            if (reversed) {
                pos = getCount() - 1 - position;
            }
            selectedChapters[pos] = isChecked;
        }

        public boolean isSelected(final int position) {
            int pos = position;
            if (reversed) {
                pos = getCount() - 1 - position;
            }
            return selectedChapters.length >= pos && selectedChapters[pos];
        }

        public void all(final boolean select) {
            for (int i = 0; i < selectedChapters.length; i++) {
                selectedChapters[i] = select;
            }
            notifyDataSetChanged();
        }

        private class Holder {

            CheckBox checkBox;

            TextView title;

            TextView isNew;

        }

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
                adapter.reverse();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
