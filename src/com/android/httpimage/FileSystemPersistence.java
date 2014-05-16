package com.android.httpimage;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import com.danilov.manga.core.cache.CacheDirectoryManager;
import com.danilov.manga.core.util.IoUtils;

import java.io.*;

/**
 * File system implementation of persistent storage for downloaded images.
 *
 * @author zonghai@gmail.com
 */
public class FileSystemPersistence implements BitmapCache {

    private static final String TAG = "ThumbnailFSPersistent";

    private final CacheDirectoryManager mCacheManager;
    private final File mBaseDir;

    public FileSystemPersistence(CacheDirectoryManager cacheManager) {
        this.mCacheManager = cacheManager;

        this.mBaseDir = cacheManager.getThumbnailsCacheDirectory();
        if (!this.mBaseDir.exists()) {
            this.mBaseDir.mkdirs();
        }
    }

    @Override
    public void clear() {
        IoUtils.deleteDirectory(this.mBaseDir);
    }

    @Override
    public boolean exists(String key) {
        File file = new File(this.mBaseDir, key);
        return file.exists();
    }

    @Override
    public Bitmap loadData(String key) {
        if (!this.exists(key)) {
            return null;
        }

        File file = new File(this.mBaseDir, key);
        Bitmap bitmap = null;
        try {
            bitmap = this.readBitmapFromFile(file);
        } catch (FileNotFoundException e) {
            // ignore
        }

        return bitmap;
    }

    @Override
    public void storeData(String key, Bitmap data) {
        try {
            File file = new File(this.mBaseDir, key);
            this.writeBitmapToFile(data, file);
        } catch (FileNotFoundException e) {
            // No space left
            this.mCacheManager.trimCacheIfNeeded();
            Log.e(TAG, e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private boolean writeBitmapToFile(Bitmap bitmap, File file) throws IOException, FileNotFoundException {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            return bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } finally {
            IoUtils.closeStream(out);
        }
    }

    private Bitmap readBitmapFromFile(File file) throws FileNotFoundException {
        FileInputStream fis = null;
        Bitmap bitmap = null;
        try {
            fis = new FileInputStream(file);
            bitmap = BitmapFactory.decodeStream(fis);
            if (bitmap == null) {
                file.delete();
            }

            return bitmap;
        } finally {
            IoUtils.closeStream(fis);
        }
    }
}