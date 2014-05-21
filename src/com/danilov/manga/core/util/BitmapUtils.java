package com.danilov.manga.core.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import com.danilov.manga.R;

/**
 * Created by Semyon Danilov on 16.05.2014.
 */
public class BitmapUtils {

    public static Bitmap reduceBitmapSize(final Resources resources, final Bitmap bitmap, final int newSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int oldSize = Math.max(width, height);

        float scale = newSize / (float) oldSize;

        if (scale >= 1.0) {
            return bitmap;
        }

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, (int) (width * scale), (int) (height * scale), true);
        bitmap.recycle();
        return resizedBitmap;
    }
}
