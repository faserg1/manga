package com.danilov.supermanga.core.repository.special;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.annimon.stream.Stream;
import com.danilov.supermanga.core.application.MangaApplication;
import com.danilov.supermanga.core.http.cookie.PersistentCookieStore;
import com.danilov.supermanga.core.repository.RepositoryEngine;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Semyon on 07.01.2016.
 */
public abstract class AuthorizableEngine implements RepositoryEngine {

    @NonNull
    protected OkHttpClient httpClient;

    private static final int CONNECT_TIMEOUT_MILLIS = 30 * 1000;
    private static final int READ_TIMEOUT_MILLIS = 30 * 1000;
    private static final int WRITE_TIMEOUT_MILLIS = 30 * 1000;

    public AuthorizableEngine() {
        httpClient = new OkHttpClient();
        httpClient.setConnectTimeout(CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        httpClient.setReadTimeout(READ_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        httpClient.setWriteTimeout(WRITE_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        httpClient.setFollowSslRedirects(true);
        httpClient.setFollowRedirects(true);

        httpClient.setCookieHandler(new CookieManager(
                new PersistentCookieStore(MangaApplication.get()),
                CookiePolicy.ACCEPT_ALL));
    }

    public abstract void setPassword(@NonNull String password);

    public abstract void setLogin(@NonNull String login);

    /**
     * Try to authorize
     * @return authorization result
     */
    public boolean login() {
        final String authPageUrl = authPageUrl();

        final RequestBody requestBody =
                Stream.of(loginParams().entrySet())
                .collect(FormEncodingBuilder::new,
                        (builder, entry) -> builder.add(entry.getKey(), entry.getValue()))
                .build();

        final Call call = httpClient.newCall(new Request.Builder()
                .url(authPageUrl).post(requestBody).build());
        final Response response;
        try {
            response = call.execute();
        } catch (IOException e) {
            return false;
        }

        final String authSuccessUrl = authSuccessUrl();
//        final String authSuccessMethod = authSuccessMethod();
        if (authSuccessUrl != null && response.isRedirect()) {
            if (response.request().urlString().equals(authSuccessUrl)) {
                return true;
            }
        }

        try {
            return checkAuthResponse(response.body().string());
        } catch (IOException e) {
            return false;
        }
    }

    public abstract void logout();

    @Override
    public boolean requiresAuth() {
        return true;
    }

    public abstract String getLogin();

    public abstract boolean isAuthorized();

    public abstract String authPageUrl();

    @Nullable
    public abstract String authSuccessUrl();

    /**
     * Check authorization result by method response,
     * true by default, because usually we can guess by redirect URL
     * @return authorization result
     */
    public boolean checkAuthResponse(final String response) {
        return true;
    }

    public abstract String authSuccessMethod();

    @NonNull
    public abstract Map<String, String> loginParams();

}