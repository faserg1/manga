package com.danilov.supermanga.core.repository;

import android.support.annotation.Nullable;

import com.danilov.supermanga.core.repository.special.JavaScriptRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Semyon on 23.01.2016.
 */
public class RepositoryHolder {

    private List<RepositoryEngine.Repository> repositoryList = new ArrayList<>();

    public void init() {

        RepositoryEngine.DefaultRepository[] values = RepositoryEngine.DefaultRepository.getWithoutOffline();
        for (RepositoryEngine.DefaultRepository repository : values) {
            repositoryList.add(repository);
        }
        repositoryList.add(new JavaScriptRepository("", "MangaFox"));


    }

    @Nullable
    public RepositoryEngine.Repository valueOf(final String name) {
        for (RepositoryEngine.Repository repository : repositoryList) {
            if (repository.toString().equals(name)) {
                return repository;
            }
        }
        return null;
    }

    public RepositoryEngine.Repository[] getRepositories() {
        return repositoryList.toArray(new RepositoryEngine.Repository[repositoryList.size()]);
    }


}
