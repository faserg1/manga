package com.danilov.supermanga.core.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.danilov.supermanga.core.repository.RepositoryEngine;
import com.danilov.supermanga.core.repository.RepositoryHolder;
import com.danilov.supermanga.core.util.Pair;
import com.danilov.supermanga.core.util.ServiceContainer;

import java.util.List;

/**
 * Created by Semyon Danilov on 17.05.2014.
 */
public class Manga implements Parcelable {

    private int id;

    private String uri;

    private String title;

    private String author = ""; //optional

    private String coverUri;

    private boolean isFavorite;

    private String genres;

    //TODO: add type (MANGA OR MANHWA)

    private RepositoryEngine.Repository repository;

    public Manga(final String title, final String uri, final RepositoryEngine.Repository repository) {
        this.title = title;
        this.uri = uri;
        this.repository = repository;
    }

    //<!--lazy load -->
    private String description;

    private int chaptersQuantity = 0;

    private List<MangaChapter> chapters;
    //<--lazy load --!>

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public int getChaptersQuantity() {
        return chaptersQuantity;
    }

    public void setChaptersQuantity(final int chaptersQuantity) {
        this.chaptersQuantity = chaptersQuantity;
    }

    public List<MangaChapter> getChapters() {
        return chapters;
    }

    public void setChapters(final List<MangaChapter> chapters) {
        this.chapters = chapters;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(final String uri) {
        this.uri = uri;
    }

    public String getCoverUri() {
        return coverUri;
    }

    public void setCoverUri(final String coverUri) {
        this.coverUri = coverUri;
    }

    public String getGenres() {
        return genres;
    }

    public void setGenres(final String genres) {
        this.genres = genres;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(final String author) {
        this.author = author;
    }

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(final boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    /**
     * Getting chapter by its number (e.g. chapter #1 is getChapterByNumber(0)
     * @param number
     * @return
     */
    public MangaChapter getChapterByNumber(final int number) {
        if (chapters == null) {
            return null;
        }
        for (MangaChapter chapter : chapters) {
            if (chapter.getNumber() == number) {
                return chapter;
            }
        }
        return null;
    }

    /**
     * Getting chapter (in Pair.first) by its number (e.g. chapter #1 is getChapterByNumber(0)
     * also returns boolean isLast in Pair.second
     * @param number
     * @return
     */
    public Pair getChapterAndIsLastByNumber(final int number) {
        if (chapters == null) {
            return null;
        }
        int size = chapters.size();
        for (int i = 0; i < size; i++) {
            MangaChapter chapter = chapters.get(i);
            if (chapter.getNumber() == number) {
                Pair pair = Pair.obtain();
                pair.first = chapter;
                pair.second = i == (size - 1);
                return pair;
            }
        }
        return null;
    }


    /**
     * Getting first chapter (in Pair.first) in list
     * also returns boolean isLast in Pair.second
     * @return
     */
    public Pair getFirstExistingChapterAndIsLast() {
        if (chapters == null) {
            return null;
        }
        int size = chapters.size();
        if (size == 0) {
            return null;
        }
        MangaChapter chapter = chapters.get(0);
        Pair pair = Pair.obtain();
        pair.first = chapter;
        pair.second = 0 == (size - 1);
        return pair;
    }

    /**
     * Getting chapter by it's position in list
     * @param pos
     * @return
     */
    public MangaChapter getChapterByListPos(final int pos) {
        if (chapters == null) {
            return null;
        }
        if (pos >= chapters.size() || pos < 0) {
            return null;
        }
        return chapters.get(pos);
    }

    public RepositoryEngine.Repository getRepository() {
        return repository;
    }

    //    parcelable implementation

    public static final Creator<Manga> CREATOR = new Creator<Manga>() {

        @Override
        public Manga createFromParcel(final Parcel parcel) {
            return new Manga(parcel);
        }

        @Override
        public Manga[] newArray(final int size) {
            return new Manga[size];
        }

    };

    protected Manga(final Parcel parcel) {
        uri = parcel.readString();
        title = parcel.readString();
        author = parcel.readString();
        coverUri = parcel.readString();
        description = parcel.readString();
        chaptersQuantity = parcel.readInt();

        RepositoryHolder repositoryHolder = ServiceContainer.getService(RepositoryHolder.class);
        repository = repositoryHolder.valueOf(parcel.readString());

        id = parcel.readInt();
        isFavorite = parcel.readInt() == 1;
        genres = parcel.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel parcel, final int i) {
        parcel.writeString(uri);
        parcel.writeString(title);
        parcel.writeString(author);
        parcel.writeString(coverUri);
        parcel.writeString(description);
        parcel.writeInt(chaptersQuantity);
        if (repository == null) {
            int a = 0;
            a++;
        }
        parcel.writeString(repository.toString());
        parcel.writeInt(id);
        parcel.writeInt(isFavorite ? 1 : 0);
        parcel.writeString(genres);
    }

    public boolean isDownloaded() {
        return false;
    }

}
