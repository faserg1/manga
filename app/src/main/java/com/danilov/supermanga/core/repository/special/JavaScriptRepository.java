package com.danilov.supermanga.core.repository.special;

import com.danilov.supermanga.R;
import com.danilov.supermanga.core.application.MangaApplication;
import com.danilov.supermanga.core.model.Manga;
import com.danilov.supermanga.core.repository.RepositoryEngine;
import com.danilov.supermanga.core.repository.special.test.JSTestEngine;

/**
 * Created by Semyon on 23.01.2016.
 */
public class JavaScriptRepository implements RepositoryEngine.Repository {

    private String filePath;
    private String repoName;
    private JavaScriptEngine javaScriptEngine;

    public JavaScriptRepository(final String filePath, final String repoName) {
        this.filePath = filePath;
        this.repoName = repoName;
        javaScriptEngine = new JSTestEngine(MangaApplication.getContext(), repoName, filePath);
        javaScriptEngine.setRepository(this);
    }

    @Override
    public int getCountryIconId() {
        return R.drawable.ic_russia;
    }

    @Override
    public String getName() {
        return repoName;
    }

    @Override
    public RepositoryEngine getEngine() {
        return javaScriptEngine;
    }

    @Override
    public String toString() {
        return repoName;
    }
}