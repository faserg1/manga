package com.danilov.supermanga.core.repository.realweb;

import com.danilov.supermanga.BuildConfig;
import com.danilov.supermanga.core.model.Manga;
import com.danilov.supermanga.core.repository.HentaichanEngine;
import com.danilov.supermanga.core.repository.RepositoryEngine;
import com.danilov.supermanga.core.repository.RepositoryException;
import com.danilov.supermanga.core.repository.RepositoryHolder;
import com.danilov.supermanga.core.util.ServiceContainer;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.Collections;
import java.util.List;

/**
 * Created by Semyon on 19.03.2016.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class HentaichanTest {

    private  HentaichanEngine engine;

    @Before
    public void setUp() {
        RepositoryHolder service = ServiceContainer.getService(RepositoryHolder.class);
        RepositoryEngine.Repository repository = service.valueOf(RepositoryEngine.DefaultRepository.HENTAICHAN.toString());
        engine = (HentaichanEngine) repository.getEngine();

        engine.setLogin("husername");
        engine.setPassword("hpassword");
        Assert.assertTrue(engine.login());
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testQueryRepository() throws Exception {
        final List<Manga> kuroiwa = engine.queryRepository("Souguu! Amazoness Oyako", null);
        Assert.assertFalse(kuroiwa.isEmpty());
    }

    @Test
    public void testQueryForMangaDescription() throws Exception {
        List<Manga> searchResults = null;
        try {
            searchResults = engine.queryRepository("Souguu! Amazoness Oyako", Collections.emptyList());
        } catch (RepositoryException e) {
            Assert.fail("Should not fail: " + e.getMessage());
        }
        Assert.assertNotNull(searchResults);
        Assert.assertTrue(!searchResults.isEmpty());
        Manga manga = searchResults.get(0);
        try {
            boolean success = engine.queryForMangaDescription(manga);
            Assert.assertTrue(success);
        } catch (RepositoryException e) {
            Assert.fail("Should not fail: " + e.getMessage());
        }
    }

}
