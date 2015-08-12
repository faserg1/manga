package com.danilov.supermanga.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.danilov.supermanga.R;

/**
 * Created by Semyon on 06.12.2014.
 */
public class BaseToolbarActivity extends ActionBarActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setLogo(R.drawable.ic_launcher);
        setSupportActionBar(toolbar);
    }

    public <T> T findViewWithId(final int id) {
        return (T) super.findViewById(id);
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

    public Toolbar getToolbar() {
        return toolbar;
    }

    @Override
    public void setContentView(final View view, final ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        setupToolbar();
    }

}
