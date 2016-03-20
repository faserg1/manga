package com.danilov.supermanga.core.theme;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.util.TypedValue;

/**
 * Created by Semyon on 21.03.2016.
 */
public class ThemeUtils {

    public static final int getColor(@AttrRes final int attr, final Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }

}
