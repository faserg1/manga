package com.danilov.supermanga.activity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.danilov.supermanga.R;
import com.danilov.supermanga.core.util.Utils;

/**
 * Created by Semyon on 06.12.2014.
 */
public class BaseToolbarActivity extends AppCompatActivity {

    protected Toolbar toolbar;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setLogo(R.drawable.ic_launcher);
        setSupportActionBar(toolbar);

        TextView toolbarTitleTextView = getToolbarTitleView(this, toolbar);
        if (toolbarTitleTextView != null) {
            Typeface typeface = Utils.getTypeface("fonts/rmedium.ttf");
            if (typeface != null) {
                toolbarTitleTextView.setTypeface(typeface);
            }
        }
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

    public static TextView getToolbarTitleView(AppCompatActivity activity, Toolbar toolbar){
        ActionBar actionBar = activity.getSupportActionBar();
        CharSequence actionbarTitle = null;
        if(actionBar != null)
            actionbarTitle = actionBar.getTitle();
        actionbarTitle = TextUtils.isEmpty(actionbarTitle) ? toolbar.getTitle() : actionbarTitle;
        if(TextUtils.isEmpty(actionbarTitle)) return null;
        // can't find if title not set
        for(int i= 0; i < toolbar.getChildCount(); i++){
            View v = toolbar.getChildAt(i);
            if(v != null && v instanceof TextView){
                TextView t = (TextView) v;
                CharSequence title = t.getText();
                if(!TextUtils.isEmpty(title) && actionbarTitle.equals(title) && t.getId() == View.NO_ID){
                    //Toolbar does not assign id to views with layout params SYSTEM, hence getId() == View.NO_ID
                    //in same manner subtitle TextView can be obtained.
                    return t;
                }
            }
        }
        return null;
    }

}
