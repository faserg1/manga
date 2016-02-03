package com.danilov.supermanga.core.database.crud;

import android.database.Cursor;
import android.support.annotation.Nullable;

/**
 * Created by Semyon on 03.02.2016.
 */
public class ResultSet {

    private Cursor cursor;

    private MetaModel metaModel;

    public ResultSet(final Cursor cursor, final MetaModel metaModel) {
        this.cursor = cursor;
        this.metaModel = metaModel;
    }

    @Nullable
    public <T> T get(final String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        MetaModel.Column column = metaModel.getColumn(columnName);
        if (column != null) {
            int columnType = column.getColumnType();
            switch (columnType) {
                case MetaModel.TYPE_BLOB:
                    return (T) cursor.getBlob(columnIndex);
                case MetaModel.TYPE_DOUBLE:
                    return (T) ((Object) cursor.getDouble(columnIndex));
                case MetaModel.TYPE_FLOAT:
                    return (T) ((Object) cursor.getFloat(columnIndex));
                case MetaModel.TYPE_STRING:
                    return (T) ((Object) cursor.getString(columnIndex));
                case MetaModel.TYPE_LONG:
                    return (T) ((Object) cursor.getLong(columnIndex));
                case MetaModel.TYPE_INT:
                    return (T) ((Object) cursor.getInt(columnIndex));
            }
        }
        return null;
    }

    public boolean moveToFirst() {
        return cursor.moveToFirst();
    }

    public boolean moveToNext() {
        return cursor.moveToNext();
    }

}
