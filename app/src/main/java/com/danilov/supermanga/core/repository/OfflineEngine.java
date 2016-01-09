package com.danilov.supermanga.core.repository;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.danilov.supermanga.core.application.MangaApplication;
import com.danilov.supermanga.core.database.DatabaseAccessException;
import com.danilov.supermanga.core.database.MangaDAO;
import com.danilov.supermanga.core.http.RequestPreprocessor;
import com.danilov.supermanga.core.model.LocalManga;
import com.danilov.supermanga.core.model.Manga;
import com.danilov.supermanga.core.model.MangaChapter;
import com.danilov.supermanga.core.model.MangaSuggestion;
import com.danilov.supermanga.core.util.IoUtils;
import com.danilov.supermanga.core.util.ServiceContainer;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Semyon Danilov on 21.06.2014.
 */
public class OfflineEngine implements RepositoryEngine {

    private static final String TAG = "OfflineEngine";

    @Override
    public String getLanguage() {
        return null;
    }

    @Override
    public boolean requiresAuth() {
        return false;
    }

    @Override
    public List<MangaSuggestion> getSuggestions(final String query) {
        return null;
    }

    @Override
    public List<Manga> queryRepository(final String query, final List<Filter.FilterValue> filterValues) throws RepositoryException {
        return null;
    }

    @Override
    public List<Manga> queryRepository(final Genre genre) throws RepositoryException {
        return null;
    }

    @Override
    public boolean queryForMangaDescription(final Manga manga) throws RepositoryException {
        return false;
    }

    private Comparator<String> chapterNumericStringComparator = new Comparator<String>() {

        @Override
        public int compare(final String lhs, final String rhs) {
            String _lhs = lhs;
            String _rhs = rhs;
            int index = _lhs.lastIndexOf('/');
            if (index == -1) {
                index = _lhs.lastIndexOf('\\');
            }
            if (index != -1) {
                _lhs = _lhs.substring(index + 1);
            }

            index = _rhs.lastIndexOf('/');
            if (index == -1) {
                index = _rhs.lastIndexOf('\\');
            }
            if (index != -1) {
                _rhs = _rhs.substring(index + 1);
            }
            try {
                Integer left = Integer.valueOf(_lhs);
                Integer right = Integer.valueOf(_rhs);
                return left.compareTo(right);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        }

    };

    @Override
    public boolean queryForChapters(final Manga manga) throws RepositoryException {
        LocalManga localManga = (LocalManga) manga;

        String[] dirs = getMangaChaptersUris(localManga);
        String mangaUri = localManga.getLocalUri();

        for (int i = 0; i < dirs.length; i++) {
            String uri = dirs[i];
            dirs[i] = mangaUri + "/" + uri;
        }
        List<String> urisList = Arrays.asList(dirs);
        Collections.sort(urisList, chapterNumericStringComparator);
        List<MangaChapter> chapters = new ArrayList<MangaChapter>(urisList.size());
        for (int i = 0; i < urisList.size(); i++) {
            String uri = urisList.get(i);
            int index = uri.lastIndexOf('/');
            if (index == -1) {
                index = uri.lastIndexOf('\\');
            }
            if (index != -1) {
                uri = uri.substring(index + 1);
            }
            Integer number = 0;
            try {
                number = Integer.valueOf(uri);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            MangaChapter chapter = new MangaChapter("", number, urisList.get(i));
            chapters.add(chapter);
        }
        manga.setChapters(chapters);
        return true;
    }

    private String[] getMangaChaptersUris(final LocalManga localManga) {
        FilenameFilter filenameFilter = new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String filename) {
                File maybeDir = new File(dir.getPath() + "/" + filename);
                return maybeDir.isDirectory();
            }
        };
        String mangaUri = localManga.getLocalUri();
        String[] chapters = null;
        File folder = new File(mangaUri);
        boolean exists = folder.exists();
        chapters = folder.list(filenameFilter);
        if (!exists || chapters.length == 0) {
            MangaDAO mangaDAO = ServiceContainer.getService(MangaDAO.class);
            Context context = MangaApplication.getContext();
            mangaUri = IoUtils.createPathForManga(localManga, context);
            localManga.setLocalUri(mangaUri);
            try {
                mangaDAO.update(localManga);
            } catch (DatabaseAccessException e) {
                Log.e(TAG, "Too bad, path was not updated");
                e.printStackTrace();
                //failed to update path to local manga,
                //but it certainly will try to update next time
            }
            folder = new File(mangaUri);
            if (folder.exists()) {
                return folder.list(filenameFilter);
            }
        } else {
            return chapters;
        }
        return new String[0];
    }

    private Comparator<String> imageNumericStringComparator = new Comparator<String>() {

        @Override
        public int compare(final String lhs, final String rhs) {
            String _lhs = lhs;
            String _rhs = rhs;
            int index = _lhs.lastIndexOf('.');
            if (index != -1) {
                _lhs = _lhs.substring(0, index);
                index = _lhs.lastIndexOf('/');
                if (index == -1) {
                    index = _lhs.lastIndexOf('\\');
                }
                if (index != -1) {
                    _lhs = _lhs.substring(index + 1);
                }
            }
            index = _rhs.lastIndexOf('.');
            if (index != -1) {
                _rhs = _rhs.substring(0, index);
                index = _rhs.lastIndexOf('/');
                if (index == -1) {
                    index = _rhs.lastIndexOf('\\');
                }
                if (index != -1) {
                    _rhs = _rhs.substring(index + 1);
                }
            }
            try {
                Integer left = Integer.valueOf(_lhs);
                Integer right = Integer.valueOf(_rhs);
                return left.compareTo(right);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        }

    };

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
        Collections.sort(urisList, imageNumericStringComparator);
        return urisList;
    }

    @Override
    public String getBaseSearchUri() {
        return null;
    }

    @Override
    public String getBaseUri() {
        return null;
    }

    @NonNull
    @Override
    public List<FilterGroup> getFilters() {
        return null;
    }

    @NonNull
    @Override
    public List<Genre> getGenres() {
        return null;
    }

    @Nullable
    @Override
    public RequestPreprocessor getRequestPreprocessor() {
        return null;
    }

}