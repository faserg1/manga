package com.github.amlcurran.showcaseview.targets;

import android.app.Activity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewParent;

import com.danilov.supermanga.activity.BaseToolbarActivity;

/**
 * Created by Semyon on 01.03.2015.
 */
public class ToolbarReflector implements Reflector {

    private BaseToolbarActivity mActivity;

    public ToolbarReflector(Activity activity) {
        mActivity = (BaseToolbarActivity) activity;
    }

    @Override
    public ViewParent getActionBarView() {
        return getHomeButton().getParent().getParent();
    }

    @Override
    public View getHomeButton() {
        Toolbar toolbar = mActivity.getToolbar();
        View homeButton = toolbar.findViewById(android.R.id.home);
        if (homeButton == null) {
            throw new RuntimeException(
                    "insertShowcaseViewWithType cannot be used when the theme " +
                            "has no ActionBar");
        }
        return homeButton;
    }

    @Override
    public void showcaseActionItem(int itemId) {

    }
}