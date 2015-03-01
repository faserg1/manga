package com.danilov.mangareader.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.danilov.mangareader.R;

/**
 * Created by Semyon on 01.03.2015.
 */
public class GenresFragment extends BaseFragment {


    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.handy, container, false);
        return view;
    }

}
