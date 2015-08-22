package com.danilov.supermanga.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.danilov.supermanga.R;
import com.danilov.supermanga.activity.MangaInfoActivity;
import com.danilov.supermanga.core.adapter.MangaListAdapter;
import com.danilov.supermanga.core.model.Manga;
import com.danilov.supermanga.core.repository.ReadmangaEngine;
import com.danilov.supermanga.core.repository.RepositoryEngine;
import com.danilov.supermanga.core.repository.RepositoryException;
import com.danilov.supermanga.core.util.Constants;

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

    }


    @Override
    public void onItemClick(final AdapterView<?> adapterView, final View view, final int i, final long l) {
        Manga manga = adapter.getItem(i);
        Intent intent = new Intent(this, MangaInfoActivity.class);
        intent.putExtra(Constants.MANGA_PARCEL_KEY, manga);
        startActivity(intent);
    }

}