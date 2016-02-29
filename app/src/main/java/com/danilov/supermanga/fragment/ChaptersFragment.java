package com.danilov.supermanga.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.danilov.supermanga.R;
import com.danilov.supermanga.activity.MangaInfoActivity;
import com.danilov.supermanga.activity.SingleFragmentActivity;
import com.danilov.supermanga.core.dialog.CustomDialog;
import com.danilov.supermanga.core.dialog.CustomDialogFragment;
import com.danilov.supermanga.core.interfaces.RefreshableActivity;
import com.danilov.supermanga.core.model.Manga;
import com.danilov.supermanga.core.model.MangaChapter;
import com.danilov.supermanga.core.repository.RepositoryEngine;
import com.danilov.supermanga.core.repository.RepositoryException;
import com.danilov.supermanga.core.util.Constants;
import com.danilov.supermanga.core.util.Utils;

import java.util.ArrayList;
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
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                if (adapter != null) {
                    adapter.all(isChecked);
                }
            }
        });
        chaptersListView.setOnItemClickListener(this);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                activity.showInfoFragment(false);
            }
        });
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (adapter == null) {
                    return;
                }
                Intent intent = new Intent(activity, SingleFragmentActivity.class);
                intent.putExtra(Constants.FRAGMENTS_KEY, SingleFragmentActivity.DOWNLOAD_MANAGER_FRAGMENT);
                intent.putExtra(Constants.MANGA_PARCEL_KEY, manga);
                intent.putIntegerArrayListExtra(Constants.SELECTED_CHAPTERS_KEY, adapter.getSelectedChaptersList());
                startActivity(intent);
                activity.showInfoFragment(false);
            }
        });
        selectLast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
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
            }
        });
        selectRange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                showNumberPickerFragment();
            }
        });
        if (savedInstanceState == null) {
            Intent i = activity.getIntent();
            manga = i.getParcelableExtra(Constants.MANGA_PARCEL_KEY);
            if (manga != null) {
                loadChaptersInfo(manga);
            }
        } else {
            restoreInstanceState(savedInstanceState);
        }
        final int baseColor = getResources().getColor(R.color.color_primary);
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
        List<MangaChapter> chapters = manga.getChapters();
        adapter = new ChaptersAdapter(getActivity(), chapters);
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
        builder.setPositiveButton(R.string.sv_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
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
            }
        });
        builder.setNegativeButton(R.string.sv_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                dialogFragment.dismiss();
            }
        });
        builder.setView(contentView);
        builder.setTitle(R.string.sv_select_range_dialog);
        dialog = builder.build();
        dialogFragment.setDialog(dialog);
        dialogFragment.show(activity.getSupportFragmentManager(), "select-range");
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
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
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
                }

            });
        }

    }


    private class ChaptersAdapter extends ArrayAdapter<MangaChapter> {

        private List<MangaChapter> chapters = null;

        private boolean[] selectedChapters = null;

        @Override
        public int getCount() {
            return chapters.size();
        }

        public ChaptersAdapter(final Context context, final List<MangaChapter> objects) {
            super(context, 0, objects);
            this.chapters = objects;
            selectedChapters = new boolean[chapters.size()];
            for (int i = 0; i < chapters.size(); i++) {
                selectedChapters[i] = false;
            }
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            View view = convertView;
            Holder h = null;
            if (view == null) {
                view = activity.getLayoutInflater().inflate(R.layout.chapter_list_item, null);
                h = new Holder();
                TextView title = (TextView) view.findViewById(R.id.chapterTitle);
                CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
                h.checkBox = checkBox;
                h.title = title;
                view.setTag(h);
            } else {
                h = (Holder) view.getTag();
            }
            MangaChapter chapter = chapters.get(position);
            h.title.setText((chapter.getNumber() + 1) + ". " + chapter.getTitle());
            h.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                    select(position, isChecked);
                }
            });

            h.checkBox.setChecked(selectedChapters[position]);

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
            ArrayList<Integer> selection = new ArrayList<Integer>();
            for (int i = 0; i < selectedChapters.length; i++) {
                if (selectedChapters[i]) {
                    selection.add(i);
                }
            }
            return selection;
        }

        public void select(final int position) {
            selectedChapters[position] = !selectedChapters[position];
            notifyDataSetChanged();
        }

        public void select(final int position, final boolean isChecked) {
            selectedChapters[position] = isChecked;
        }

        public boolean isSelected(final int position) {
            return selectedChapters.length >= position && selectedChapters[position];
        }

        public void all(final boolean select) {
            for (int i = 0; i < selectedChapters.length; i++) {
                selectedChapters[i] = select;
            }
            notifyDataSetChanged();
        }

        private class Holder {

            public CheckBox checkBox;

            public TextView title;

        }

    }

}
