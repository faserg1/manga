package com.danilov.manga.test;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.danilov.manga.R;
import com.danilov.manga.core.repository.ReadmangaEngine;
import com.danilov.manga.core.repository.RepositoryEngine;

/**
 * Created by Semyon Danilov on 17.05.2014.
 */
public class QueryTestActivity extends Activity implements View.OnClickListener {

    private EditText query;

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
                repositoryEngine.queryRepository(q);
            }

        };
        t.start();
    }
}