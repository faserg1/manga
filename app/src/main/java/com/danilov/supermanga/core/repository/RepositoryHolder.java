package com.danilov.supermanga.core.repository;

import android.support.annotation.Nullable;

import com.danilov.supermanga.core.repository.special.JSCrud;
import com.danilov.supermanga.core.repository.special.JavaScriptRepository;
import com.danilov.supermanga.core.util.ServiceContainer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Semyon on 23.01.2016.
 */
public class RepositoryHolder {

    private List<RepositoryEngine.Repository> allRepos = new ArrayList<>();
    private List<RepositoryEngine.Repository> withoutOffline = new ArrayList<>();

    public void init() {
        withoutOffline.clear();
        allRepos.clear();
        RepositoryEngine.DefaultRepository[] values = RepositoryEngine.DefaultRepository.values();
        for (RepositoryEngine.DefaultRepository repository : values) {
            if (repository != RepositoryEngine.DefaultRepository.OFFLINE) {
                withoutOffline.add(repository);
            }
            allRepos.add(repository);
        }
        JSCrud jsCrud = ServiceContainer.getService(JSCrud.class);
        Collection<JavaScriptRepository> resultSet = jsCrud.select(jsCrud.getAllSelector());
        withoutOffline.addAll(resultSet);
        allRepos.addAll(resultSet);
    }

    @Nullable
    public RepositoryEngine.Repository valueOf(final String name) {
        RepositoryEngine.Repository result = null;
        for (RepositoryEngine.Repository repository : allRepos) {
            if (repository.toString().equals(name)) {
                result = repository;
            }
        }
        if (result == null) {
            int a = 0;
            a++;
        }
        return result;
    }

    public RepositoryEngine.Repository[] getRepositories() {
        return withoutOffline.toArray(new RepositoryEngine.Repository[withoutOffline.size()]);
    }

}
