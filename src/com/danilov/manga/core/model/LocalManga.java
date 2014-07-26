package com.danilov.manga.core.model;

import android.os.Parcel;
import com.danilov.manga.core.repository.RepositoryEngine;

/**
 * Created by Semyon Danilov on 09.07.2014.
 */
public class LocalManga extends Manga {

    private int localId;

    public LocalManga(final String title, final String uri, final RepositoryEngine.Repository repository) {
        super(title, uri, repository);
    }

    public int getLocalId() {
        return localId;
    }

    public void setLocalId(final int localId) {
        this.localId = localId;
    }

    public static final Creator<LocalManga> CREATOR = new Creator<LocalManga>() {

        @Override
        public LocalManga createFromParcel(final Parcel parcel) {
            return new LocalManga(parcel);
        }

        @Override
        public LocalManga[] newArray(final int size) {
            return new LocalManga[size];
        }

    };

    protected LocalManga(final Parcel parcel) {
        super(parcel);
        this.localId = parcel.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel parcel, final int i) {
        super.writeToParcel(parcel, i);
        parcel.writeInt(localId);
    }

}