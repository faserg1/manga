package com.danilov.manga;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.danilov.manga.test.TouchImageViewActivityTest;

public class MyActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ListView list = (ListView) findViewById(R.id.list);
        String[] array = {"Episodes: 308", "Episodes: 308", "Episodes: 308", "Episodes: 308"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.manga_list_item, R.id.manga_quantity,array);
        list.setAdapter(adapter);
    }

    public void firstTest(View view) {
        Intent intent = new Intent(this, TouchImageViewActivityTest.class);
        startActivity(intent);
    }

}
