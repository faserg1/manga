package com.danilov.supermanga.core.database.crud;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;

/**
 * Created by Semyon on 03.02.2016.
 */
public abstract class MetaModel {

    public static final int TYPE_INT = 0;

    public static final int TYPE_LONG = 1;

    public static final int TYPE_STRING = 2;

    public static final int TYPE_FLOAT = 3;

    public static final int TYPE_DOUBLE = 4;

    public static final int TYPE_BLOB = 5;


    public class Column {

        private int columnType;

        private String columnName;

        private boolean isNullable;

        public Column(final int columnType, final String columnName, final boolean isNullable) {
            this.columnType = columnType;
            this.columnName = columnName;
            this.isNullable = isNullable;
        }

        public int getColumnType() {
            return columnType;
        }

        public void setColumnType(final int columnType) {
            this.columnType = columnType;
        }

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(final String columnName) {
            this.columnName = columnName;
        }

        public boolean isNullable() {
            return isNullable;
        }

        public void setIsNullable(final boolean isNullable) {
            this.isNullable = isNullable;
        }

    }

    @NonNull
    public abstract HashMap<String, Column> getColumns();

    @Nullable
    public Column getColumn(final String columnName) {
        return getColumns().get(columnName);
    }

}
