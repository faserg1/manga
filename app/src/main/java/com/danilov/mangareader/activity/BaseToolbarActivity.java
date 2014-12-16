package com.danilov.mangareader.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.danilov.mangareader.R;

/**
 * Created by Semyon on 06.12.2014.
 */
public class BaseToolbarActivity extends ActionBarActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setLogo(R.drawable.ic_launcher);
        setSupportActionBar(toolbar);
    }

    @Override
    public void setContentView(final int layoutResID) {
        super.setContentView(layoutResID);
        setupToolbar();
    }

    @Override
    public void setContentView(final View view) {
        super.setContentView(view);
        setupToolbar();
    }

    @Override
    public void setContentView(final View view, final ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        setupToolbar();
    }

}
