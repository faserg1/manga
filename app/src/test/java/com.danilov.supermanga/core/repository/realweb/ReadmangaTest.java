package com.danilov.supermanga.core.repository.realweb;

import com.danilov.supermanga.BuildConfig;
import com.danilov.supermanga.core.model.MangaSuggestion;
import com.danilov.supermanga.core.repository.RepositoryEngine;
import com.danilov.supermanga.core.repository.RepositoryException;
import com.danilov.supermanga.core.repository.RepositoryHolder;
import com.danilov.supermanga.core.util.ServiceContainer;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

/**
 * Created by Semyon on 19.03.2016.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class ReadmangaTest {

    @Test
    public void testSuggestions() {
        RepositoryHolder service = ServiceContainer.getService(RepositoryHolder.class);
        RepositoryEngine.Repository repository = service.valueOf(RepositoryEngine.DefaultRepository.READMANGA.toString());
        RepositoryEngine engine = repository.getEngine();
        List<MangaSuggestion> suggestions = null;
        try {
            suggestions = engine.getSuggestions("Naru");
        } catch (RepositoryException e) {
            Assert.fail("Should not fail: " + e.getMessage());
        }

        Assert.assertNotNull(suggestions);

        Assert.assertTrue(!suggestions.isEmpty());
    }

}
