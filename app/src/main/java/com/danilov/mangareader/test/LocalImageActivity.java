package com.danilov.mangareader.test;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import com.danilov.mangareader.R;
import com.danilov.mangareader.core.service.LocalImageManager;
import com.danilov.mangareader.core.util.ServiceContainer;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Semyon Danilov on 07.07.2014.
 */
public class LocalImageActivity extends Activity {

    private ListView listView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_local_image_activity);
        listView = (ListView) findViewById(R.id.list);
        List<String> uris = getAllImagesInFolder("/storage/sdcard0/manga/download/fairytail/0");
        listView.setAdapter(new TestAdapter(this, R.layout.test_localimage_item, uris));
    }

    private List<String> getAllImagesInFolder(final String folder) {
        File file = new File(folder);
        String[] uris = file.list(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String filename) {
                return true;
            }
        });
        for (int i = 0; i < uris.length; i++) {
            String uri = uris[i];
            uris[i] = folder + "/" + uri;
        }
        return Arrays.asList(uris);
    }

    private static class TestAdapter extends ArrayAdapter<String> {

        private List<String> objects;

        private int resourceId;

        LocalImageManager localImageManager = ServiceContainer.getService(LocalImageManager.class);

        int sizeOfImage = 0;

        @Override
        public int getCount() {
            return objects.size();
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(resourceId, parent, false); //attaching to parent == (false), because it attaching later by android
            }

            String uri = getItem(position);
            Object tag = view.getTag();
            ViewBag viewBag;

            if (tag != null && tag instanceof ViewBag) {
                viewBag = (ViewBag) tag;
            } else {
                viewBag = new ViewBag();
                viewBag.imageView = (ImageView) view.findViewById(R.id.image);
            }
            if (viewBag.imageView != null) {
                Drawable drawable = viewBag.imageView.getDrawable();
                if (drawable instanceof BitmapDrawable) {
                    BitmapDrawable bd = (BitmapDrawable) drawable;
                    Bitmap bitmap = bd.getBitmap();
                    if (bitmap != null) {
                        if (!localImageManager.hasImageInCache(bitmap)) {
                            bitmap.recycle();
                        }
                    }
                }
            }
            view.setTag(viewBag);
            Bitmap bitmap = localImageManager.loadBitmap(viewBag.imageView, uri, sizeOfImage);
            if (bitmap != null) {
                viewBag.imageView.setImageBitmap(bitmap);
            }
            return view;
        }

        public TestAdapter(final Context context, final int resourceId, final List<String> objects) {
            super(context, resourceId, objects);
            this.objects = objects;
            this.resourceId = resourceId;
            sizeOfImage = context.getResources().getDimensionPixelSize(R.dimen.manga_info_height);
        }

        private static class ViewBag {
            protected ImageView imageView;
        }

    }

}