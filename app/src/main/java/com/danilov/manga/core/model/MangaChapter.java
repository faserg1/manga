package com.danilov.manga.core.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Semyon Danilov on 21.05.2014.
 */
public class MangaChapter implements Parcelable {

    private String title;

    private String uri;

    private int number;

    public MangaChapter(final String title, final int number, final String uri) {
        this.title = title;
        this.uri = uri;
        this.number = number;
    }

    public String getTitle() {
        return title;
    }

    public String getUri() {
        return uri;
    }

    public int getNumber() {
        return number;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MangaChapter> CREATOR = new Creator<MangaChapter>() {

        @Override
        public MangaChapter createFromParcel(final Parcel source) {
            return new MangaChapter(source);
        }

        @Override
        public MangaChapter[] newArray(final int size) {
            return new MangaChapter[size];
        }

    };

    private MangaChapter(final Parcel parcel) {
        title = parcel.readString();
        uri = parcel.readString();
        number = parcel.readInt();
    }

    @Override
    public void writeToParcel(final Parcel parcel, final int flags) {
        parcel.writeString(title);
        parcel.writeString(uri);
        parcel.writeInt(number);
    }

}
