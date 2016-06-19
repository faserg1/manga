package com.danilov.supermanga.core.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.danilov.supermanga.R;
import com.danilov.supermanga.core.repository.special.AuthorizableEngine;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Semyon on 07.01.2016.
 */
public class RepositoryLoginView extends LinearLayout {

    @Bind(R.id.login)
    TextView loginView;

    @Bind(R.id.login_button)
    Button loginButton;

    @Bind(R.id.login_details)
    ViewGroup loginDetails;

    @Bind(R.id.auth_form)
    ViewGroup authForm;

    @Bind(R.id.auth_login)
    EditText loginEditText;

    @Bind(R.id.auth_password)
    EditText passwordEditText;

    @Bind(R.id.auth_button)
    Button authSubmit;

    private AuthorizableEngine engine;

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

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    private void init() {
        ButterKnife.bind(this);
        this.loginButton.setOnClickListener(v -> {
            showAuthForm();
        });
        this.authSubmit.setOnClickListener(v -> {
            login();
        });
    }

    private void login() {
        String login = loginEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        new Thread() {
            @Override
            public void run() {
                engine.setLogin(login);
                engine.setPassword(password);
                final boolean loginResult = engine.login();
                post(() -> {
                    if (loginResult) {
                        setAuthorized(true);
                        setLogin(login);
                        showDetailsForm();
                    }
                });
            }
        }.start();
    }

    private void showAuthForm() {
        authForm.setVisibility(View.VISIBLE);
        loginDetails.setVisibility(View.GONE);
    }

    private void showDetailsForm() {
        authForm.setVisibility(View.GONE);
        loginDetails.setVisibility(View.VISIBLE);
    }

    public void setNeeded(final AuthorizableEngine engine) {
        setVisibility(View.VISIBLE);
        this.engine = engine;

        if (engine.isAuthorized()) {
            setLogin(engine.getLogin());
            showDetailsForm();
        }
    }

    public void setAuthorized(final boolean authorized) {
        this.isAuthorized = authorized;
    }

    public void setLogin(final String login) {
        this.loginView.setText(login);
        onUpdate();
    }

    private void onUpdate() {
    }

}