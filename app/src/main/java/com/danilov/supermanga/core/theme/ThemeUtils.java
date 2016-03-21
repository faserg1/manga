package com.danilov.supermanga.core.theme;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.AttrRes;
import android.util.TypedValue;

/**
 * Created by Semyon on 21.03.2016.
 */
public class ThemeUtils {

    public static int getReferencedResource(@AttrRes final int attr, final Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }

    public static Drawable getReferencedDrawable(@AttrRes final int attr, final Context context) {
        // Create an array of the attributes we want to resolve
        // using values from a theme
        int[] attrs = new int[]{attr};

        // Obtain the styled attributes. 'themedContext' is a context with a
        // theme, typically the current Activity (i.e. 'this')
        TypedArray ta = context.obtainStyledAttributes(attrs);

        // To get the value of the 'listItemBackground' attribute that was
        // set in the theme used in 'themedContext'. The parameter is the index
        // of the attribute in the 'attrs' array. The returned Drawable
        // is what you are after
        Drawable drawableFromTheme = ta.getDrawable(0);

        // Finally, free the resources used by TypedArray
        ta.recycle();
        return drawableFromTheme;
    }

}
