package com.danilov.manga;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import com.android.httpimage.BitmapMemoryCache;
import com.android.httpimage.FileSystemPersistence;
import com.android.httpimage.HttpImageManager;
import com.danilov.manga.activity.MangaInfoActivity;
import com.danilov.manga.core.application.ApplicationSettings;
import com.danilov.manga.core.cache.CacheDirectoryManagerImpl;
import com.danilov.manga.core.http.ExtendedHttpClient;
import com.danilov.manga.core.http.HttpBitmapReader;
import com.danilov.manga.core.http.HttpBytesReader;
import com.danilov.manga.core.http.HttpStreamReader;
import com.danilov.manga.core.util.ServiceContainer;
import com.danilov.manga.test.QueryTestActivity;
import com.danilov.manga.test.TouchImageViewActivityTest;

import java.io.File;
import java.util.List;

public class MyActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ListView list = (ListView) findViewById(R.id.list);
        mydir = getBaseContext().getDir("mydir", Context.MODE_PRIVATE);

        fsp = new FileSystemPersistence(new CacheDirectoryManagerImpl(mydir, new ApplicationSettings(), "com.danilov.manga"));
        httpStreamReader = new HttpStreamReader(new ExtendedHttpClient(), getResources());
        httpBytesReader = new HttpBytesReader(httpStreamReader, getResources());
        httpBitmapReader = new HttpBitmapReader(httpBytesReader);
        httpImageManager = new HttpImageManager(new BitmapMemoryCache(), fsp, getResources(), httpBitmapReader);
        ServiceContainer.addService(httpBytesReader);
        ServiceContainer.addService(httpImageManager);
        String[] array = {"Episodes: 308",
                "Episodes: 308",
                "Episodes: 308",
                "Episodes: 308",
                "Episodes: 308",
                "Episodes: 308",
                "Episodes: 308",
                "Episodes: 308",
                "Episodes: 308", "Episodes: 308"};
        ArrayAdapter<String> adapter = new TestAdapter(this, R.layout.manga_list_item, R.id.manga_quantity,array);
        list.setAdapter(adapter);
    }

    public void firstTest(View view) {
        Intent intent = new Intent(this, TouchImageViewActivityTest.class);
        startActivity(intent);
    }

    public void secondTest(View view) {
        Intent intent = new Intent(this, QueryTestActivity.class);
        startActivity(intent);
    }

    public void thirdTest(View view) {
        Intent intent = new Intent(this, MangaInfoActivity.class);
        startActivity(intent);
    }

    private String sample_uri = "http://hc.readmanga.ru/auto/11/29/72/GTO_v01_020.png";
    private String sample_uri2 = "http://hc.readmanga.ru/auto/11/29/72/GTO_v01_021.png";

    File mydir = null; //Creating an internal dir;

    FileSystemPersistence fsp = null;
    HttpStreamReader httpStreamReader = null;
    HttpBytesReader httpBytesReader = null;
    HttpBitmapReader httpBitmapReader = null;

    private HttpImageManager httpImageManager = null;

    private class TestAdapter extends ArrayAdapter<String> {

        private List<Object> objects;

        public TestAdapter(final Context context, final int resource) {
            super(context, resource);
        }

        public TestAdapter(final Context context, final int resource, final int textViewResourceId) {
            super(context, resource, textViewResourceId);
        }

        public TestAdapter(final Context context, final int resource, final String[] objects) {
            super(context, resource, objects);
        }

        public TestAdapter(final Context context, final int resource, final int textViewResourceId, final String[] objects) {
            super(context, resource, textViewResourceId, objects);
        }

        public TestAdapter(final Context context, final int resource, final List<String> objects) {
            super(context, resource, objects);
        }

        @Override
        public int getCount() {
            return 10; //AHHHAHAHAHHAHA HAHAHHAHAHA
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            LayoutInflater inflater = MyActivity.this.getLayoutInflater();
            View v = convertView;
            if (v == null) {
                v = inflater.inflate(R.layout.manga_list_item, parent, false);
            }
            ImageView iv = (ImageView) v.findViewById(R.id.manga_cover);
            int sizeOfImage = getBaseContext().getResources().getDimensionPixelSize(R.dimen.manga_list_image_height);
            if(!TextUtils.isEmpty(sample_uri)){
                Bitmap bitmap = httpImageManager.loadImage(new HttpImageManager.LoadRequest(Uri.parse((position > 7) ? sample_uri2 : sample_uri), iv, sizeOfImage));
                if (bitmap != null) {
                    iv.setImageBitmap(bitmap);
                };
            }
            return v;
        }
    }

}
