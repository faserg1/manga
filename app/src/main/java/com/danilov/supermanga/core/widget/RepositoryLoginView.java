package com.danilov.supermanga.core.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.danilov.supermanga.R;
import com.danilov.supermanga.activity.MangaQueryActivity;

/**
 * Created by Semyon on 07.01.2016.
 */
public class RepositoryLoginView extends LinearLayout {

    private TextView loginView;
    private Button loginButton;

    private boolean isAuthorized = false;

    private boolean needed = false;

    public RepositoryLoginView(final Context context) {
        super(context);
    }

    public RepositoryLoginView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public RepositoryLoginView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setNeeded(final boolean needed) {
        this.needed = needed;
        onUpdate();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        loginView = (TextView) findViewById(R.id.login);
        loginButton = (Button) findViewById(R.id.login_button);

        onUpdate();
    }

    public void setAuthorized(final boolean authorized) {
        this.isAuthorized = authorized;
        onUpdate();
    }

    public void setLogin(final String login) {
        this.loginView.setText(login);
        onUpdate();
    }

    private void onUpdate() {
        setVisibility(needed ? View.VISIBLE : View.GONE);
    }

    public void setOnLoginButtonClickListener(final OnClickListener onClickListener) {
        this.loginButton.setOnClickListener(onClickListener);
    }

}