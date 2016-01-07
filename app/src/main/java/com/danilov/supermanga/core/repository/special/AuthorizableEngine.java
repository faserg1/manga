package com.danilov.supermanga.core.repository.special;

import com.danilov.supermanga.core.repository.RepositoryEngine;

/**
 * Created by Semyon on 07.01.2016.
 */
public abstract class AuthorizableEngine implements RepositoryEngine {

    @Override
    public boolean requiresAuth() {
        return true;
    }

    public abstract String getLogin();

    public abstract boolean isAuthorized();

    public abstract String authPageUrl();

    public abstract String authSuccessUrl();

    public abstract String authSuccessMethod();

}