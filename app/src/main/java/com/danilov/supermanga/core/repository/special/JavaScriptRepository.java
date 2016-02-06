package com.danilov.supermanga.core.repository.special;

import com.danilov.supermanga.R;
import com.danilov.supermanga.core.application.MangaApplication;
import com.danilov.supermanga.core.database.crud.Model;
import com.danilov.supermanga.core.database.crud.ResultSet;
import com.danilov.supermanga.core.model.Manga;
import com.danilov.supermanga.core.repository.RepositoryEngine;
import com.danilov.supermanga.core.repository.special.test.JSTestEngine;

/**
 * Created by Semyon on 23.01.2016.
 */
public class JavaScriptRepository extends Model implements RepositoryEngine.Repository {

    public static final String FILE_PATH = "filePath";
    public static final String REPO_NAME = "repoName";

    private String filePath;
    private String repoName;
    private JavaScriptEngine javaScriptEngine;

    public JavaScriptRepository(final String filePath, final String repoName) {
        this.filePath = filePath;
        this.repoName = repoName;
        init();
    }

    public JavaScriptRepository() {

    }

    private void init() {
        javaScriptEngine = new JavaScriptEngine(repoName, filePath) {
            @Override
            public String getLanguage() {
                return "";
            }

            @Override
            public boolean requiresAuth() {
                return false;
            }
        };
        javaScriptEngine.setRepository(this);
    }

    @Override
    public int getCountryIconId() {
        return R.drawable.ic_js;
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

    public String getRepoName() {
        return repoName;
    }

    public String getFilePath() {
        return filePath;
    }

    @Override
    public void load(final ResultSet resultSet) {
        filePath = resultSet.get(FILE_PATH);
        repoName = resultSet.get(REPO_NAME);
        init();
    }

}