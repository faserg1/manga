package com.danilov.supermanga.core.view.helper;

import android.annotation.SuppressLint;
import android.os.Build;
import android.widget.GridView;

import java.lang.reflect.Field;

/**
 * Created by Semyon on 25.03.2016.
 */
public class GridViewHelper {

    @SuppressLint("NewApi")
    public static int getColumnWidth(final GridView gridView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            return gridView.getColumnWidth();
        else {
            try {
                Field field = GridView.class.getDeclaredField("mColumnWidth");
                field.setAccessible(true);
                Integer value = (Integer) field.get(gridView);
                field.setAccessible(false);
                return value;
            } catch (NoSuchFieldException e) {

            } catch (IllegalAccessException e) {

            }
        }
        return 200;
    }

    private GridViewHelper() {

    }

}
