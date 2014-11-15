package com.danilov.manga.fragment;

import android.support.v4.app.Fragment;
import android.view.View;

/**
 * Created by Semyon on 15.11.2014.
 */
public class BaseFragment extends Fragment {

    protected View view;

    protected <T> T findViewById(final int id) {
        return (T) view.findViewById(id);
    }

}
