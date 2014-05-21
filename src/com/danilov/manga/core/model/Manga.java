package com.danilov.manga.core.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by Semyon Danilov on 17.05.2014.
 */
public class Manga implements Parcelable {

    private String uri;

    private String title;

    private String author = ""; //optional desu ka?

    private String coverUri;

    public Manga(final String title, final String uri) {
        this.title = title;
        this.uri = uri;
    }

    //<!--lazy load -->
    private String description;

    private List<MangaChapter> chapters;
    //<--lazy load --!>

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
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

    private Manga(final Parcel parcel) {
        uri = parcel.readString();
        title = parcel.readString();
        author = parcel.readString();
        coverUri = parcel.readString();
        description = parcel.readString();
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
    }

}
