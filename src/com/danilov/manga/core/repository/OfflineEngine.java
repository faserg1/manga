package com.danilov.manga.core.repository;

import com.danilov.manga.core.model.LocalManga;
import com.danilov.manga.core.model.Manga;
import com.danilov.manga.core.model.MangaChapter;
import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

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

    private Comparator<String> chapterNumericStringComparator = new Comparator<String>() {

        @Override
        public int compare(final String lhs, final String rhs) {
            String _lhs = lhs;
            String _rhs = rhs;
            int index = _lhs.indexOf('.');
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
        Collections.sort(urisList, chapterNumericStringComparator);
        List<MangaChapter> chapters = new ArrayList<MangaChapter>(urisList.size());
        for (int i = 0; i < urisList.size(); i++) {
            MangaChapter chapter = new MangaChapter("", i, urisList.get(i));
            chapters.add(chapter);
        }
        manga.setChapters(chapters);
        return true;
    }

    private Comparator<String> imageNumericStringComparator = new Comparator<String>() {

        @Override
        public int compare(final String lhs, final String rhs) {
            String _lhs = lhs;
            String _rhs = rhs;
            int index = _lhs.indexOf('.');
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
            index = _rhs.indexOf('.');
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

}
