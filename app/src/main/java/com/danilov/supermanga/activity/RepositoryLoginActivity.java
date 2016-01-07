package com.danilov.supermanga.activity;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

import com.danilov.supermanga.R;
import com.danilov.supermanga.core.repository.RepositoryEngine;
import com.danilov.supermanga.core.repository.special.AuthorizableEngine;
import com.danilov.supermanga.core.util.Constants;

public class RepositoryLoginActivity extends BaseToolbarActivity {


    private RepositoryEngine.Repository repository;
    private RepositoryEngine engine = null;

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repository_login);
        if (savedInstanceState == null) {
            String repositoryString = getIntent().getStringExtra(Constants.REPOSITORY_KEY);
            repository = RepositoryEngine.Repository.valueOf(repositoryString);
            engine = repository.getEngine();
        } else {
            //why I do not use getString with default value? Because it's API 12 :(
            String repositoryString = savedInstanceState.getString(Constants.REPOSITORY_KEY);
            if (repositoryString == null) {
                repository = RepositoryEngine.Repository.READMANGA;
            } else {
                repository = RepositoryEngine.Repository.valueOf(repositoryString);
            }
            engine = repository.getEngine();
        }
        webView = findViewWithId(R.id.webView);
        if (engine.requiresAuth()) {
            AuthorizableEngine authorizableEngine = (AuthorizableEngine) engine;
            webView.loadUrl(authorizableEngine.authPageUrl());
        }
    }

}
