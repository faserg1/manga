package com.danilov.mangareader.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import com.danilov.mangareader.R;
import com.danilov.mangareader.activity.MangaInfoActivity;
import com.danilov.mangareader.core.adapter.MangaListAdapter;
import com.danilov.mangareader.core.model.Manga;
import com.danilov.mangareader.core.repository.ReadmangaEngine;
import com.danilov.mangareader.core.repository.RepositoryEngine;
import com.danilov.mangareader.core.util.Constants;

import java.util.List;

/**
 * Created by Semyon Danilov on 17.05.2014.
 */
public class QueryTestActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private EditText query;
    private MangaListAdapter adapter = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_query_activity);
        query = (EditText) findViewById(R.id.query);
        Button btn = (Button) findViewById(R.id.start_query);
        btn.setOnClickListener(this);
    }

    @Override
    //poka tak
    public void onClick(final View v) {
        Thread t = new Thread() {

            @Override
            public void run() {
                RepositoryEngine repositoryEngine = new ReadmangaEngine();
                String q = query.getText().toString();
                final List<Manga> mangaList = repositoryEngine.queryRepository(q);
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        ListView listView = (ListView) QueryTestActivity.this.findViewById(R.id.manga_list);
                        adapter = new MangaListAdapter(QueryTestActivity.this, R.layout.manga_list_item, mangaList);
                        listView.setAdapter(adapter);
                        listView.setOnItemClickListener(QueryTestActivity.this);
                    }

                });
            }

        };
        t.start();
    }


    @Override
    public void onItemClick(final AdapterView<?> adapterView, final View view, final int i, final long l) {
        Manga manga = adapter.getItem(i);
        Intent intent = new Intent(this, MangaInfoActivity.class);
        intent.putExtra(Constants.MANGA_PARCEL_KEY, manga);
        startActivity(intent);
    }

}