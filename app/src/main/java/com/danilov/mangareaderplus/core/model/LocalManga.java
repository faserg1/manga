package com.danilov.mangareaderplus.core.model;

import android.os.Parcel;
import com.danilov.mangareaderplus.core.repository.RepositoryEngine;

/**
 * Created by Semyon Danilov on 09.07.2014.
 */
public class LocalManga extends Manga {

    private int localId;

    private String localUri;

    public LocalManga(final String title, final String uri, final RepositoryEngine.Repository repository) {
        super(title, uri, repository);
    }

    public String getLocalUri() {
        return localUri;
    }

    public void setLocalUri(final String localUri) {
        this.localUri = localUri;
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
        this.localUri = parcel.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel parcel, final int i) {
        super.writeToParcel(parcel, i);
        parcel.writeInt(localId);
        parcel.writeString(localUri);
    }

    @Override
    public boolean isDownloaded() {
        return true;
    }

}