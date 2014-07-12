package com.danilov.manga.core.repository;

import com.danilov.manga.core.model.LocalManga;
import com.danilov.manga.core.model.Manga;
import com.danilov.manga.core.model.MangaChapter;
import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Semyon Danilov on 21.06.2014.
 */
public class OfflineEngine implements RepositoryEngine {

    @Override
    public String getLanguage() {
        return null;
    }

    @Override
    public JSONObject getSuggestions(final String query) {
        return null;
    }

    @Override
    public List<Manga> queryRepository(final String query) {
        return null;
    }

    @Override
    public boolean queryForMangaDescription(final Manga manga) throws RepositoryException {
        return false;
    }

    @Override
    public boolean queryForChapters(final Manga manga) throws RepositoryException {
        LocalManga localManga = (LocalManga) manga;
        String mangaUri = localManga.getLocalUri();
        File folder = new File(mangaUri);
        String[] dirs = folder.list(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String filename) {
                File maybeDir = new File(dir.getPath() + "/" + filename);
                return maybeDir.isDirectory();
            }
        });
        for (int i = 0; i < dirs.length; i++) {
            String uri = dirs[i];
            dirs[i] = mangaUri + "/" + uri;
        }
        List<String> urisList = Arrays.asList(dirs);
        Collections.sort(urisList);
        List<MangaChapter> chapters = new ArrayList<MangaChapter>(urisList.size());
        for (int i = 0; i < urisList.size(); i++) {
            MangaChapter chapter = new MangaChapter("", i, urisList.get(i));
            chapters.add(chapter);
        }
        manga.setChapters(chapters);
        return true;
    }

    @Override
    public List<String> getChapterImages(final MangaChapter chapter) throws RepositoryException {
        String chapterUri = chapter.getUri();
        File folder = new File(chapterUri);
        String[] uris = folder.list(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String filename) {
                return true;
            }
        });
        for (int i = 0; i < uris.length; i++) {
            String uri = uris[i];
            uris[i] = chapterUri + "/" + uri;
        }
        List<String> urisList = Arrays.asList(uris);
        Collections.sort(urisList);
        return Arrays.asList(uris);
    }

    @Override
    public String getBaseSearchUri() {
        return null;
    }

    @Override
    public String getBaseUri() {
        return null;
    }

}
