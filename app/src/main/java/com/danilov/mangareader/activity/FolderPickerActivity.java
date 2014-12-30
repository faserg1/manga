package com.danilov.mangareader.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.danilov.mangareader.R;
import com.danilov.mangareader.core.adapter.BaseAdapter;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FolderPickerActivity extends BaseToolbarActivity implements AdapterView.OnItemClickListener {

    public static final String FOLDER_KEY = "FOLDER_KEY";

    private File curFolder;

    private GridView foldersView;

    private FolderAdapter adapter;

    private File baseFolder = new File("/mnt/");

    private File threeDotsFile = new File("...");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manga_folder_picker_activity);
        foldersView = findViewWithId(R.id.folders);
        String folder = getIntent().getStringExtra(FOLDER_KEY);
        if (folder != null && !folder.isEmpty()) {
            curFolder = new File(folder);
        } else {
            curFolder = baseFolder;
        }
        adapter = new FolderAdapter(getApplicationContext(), R.layout.folder_layout, getFiles(curFolder));
        foldersView.setOnItemClickListener(this);
        foldersView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_folder_picker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.ok) {
            Intent intent = new Intent();
            intent.putExtra(FOLDER_KEY, curFolder.getPath());
            setResult(RESULT_OK, intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(final AdapterView<?> adapterView, final View view, final int i, final long l) {
        List<File> files = adapter.getFileList();
        File file = files.get(i);
        if (file == threeDotsFile) { //no mistake, threeDots is the one and only!
            curFolder = curFolder.getParentFile();
        } else {
            curFolder = file;
        }
        files.clear();
        if (!curFolder.equals(baseFolder)) {
            files.add(threeDotsFile);
        }
        files.addAll(getFiles(curFolder));
        adapter.notifyDataSetChanged();
    }

    private List<File> getFiles(final String path) {
        return getFiles(new File(path));
    }

    private List<File> getFiles(final File file) {
        File[] filesArray = file.listFiles(new FileFilter() {
            @Override
            public boolean accept(final File file) {
                return file.isDirectory();
            }
        });
        List<File> files = new ArrayList<>(filesArray.length);
        for (File f : filesArray) {
            files.add(f);
        }
        return files;
    }

    public class FolderAdapter extends BaseAdapter<FolderPickerActivity.FolderAdapter.Holder, File> {

        private List<File> fileList = null;
        private int resource;

        @Override
        public int getCount() {
            if (fileList == null) {
                return 0;
            }
            return fileList.size();
        }

        public List<File> getFileList() {
            return fileList;
        }

        public FolderAdapter(final Context context, final int resource, final List<File> fileList) {
            super(context, resource);
            this.fileList = fileList;
            this.resource = resource;
        }

        @Override
        public void onBindViewHolder(final Holder holder, final int position) {
            File f = fileList.get(position);
            holder.title.setText(f.getName());
        }

        @Override
        public Holder onCreateViewHolder(final ViewGroup viewGroup, final int position) {
            View v = LayoutInflater.from(getApplicationContext()).inflate(resource, viewGroup, false);
            return new Holder(v);
        }

        public class Holder extends BaseAdapter.BaseHolder {

            public TextView title;

            protected Holder(final View view) {
                super(view);
                this.title = findViewById(R.id.folderName);
            }

        }

    }

}
