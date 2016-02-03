package com.danilov.supermanga.core.database.crud;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import com.danilov.supermanga.core.application.ApplicationSettings;
import com.danilov.supermanga.core.database.Database;
import com.danilov.supermanga.core.database.DatabaseAccessException;
import com.danilov.supermanga.core.database.DatabaseHelper;
import com.danilov.supermanga.core.util.Logger;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Semyon on 03.02.2016.
 */
public abstract class SimpleCrud<T extends Model> implements Crud<T>, ModelFactory<T> {

    private static final Logger LOGGER = new Logger(SimpleCrud.class);

    public static final String ID_NAME = "_id";

    public final static String TAG = "SimpleCrud";
    private static final String packageName = ApplicationSettings.PACKAGE_NAME;

    private DatabaseHelper databaseHelper;

    public SimpleCrud() {
        File externalStorageDir = Environment.getExternalStorageDirectory();
        //{SD_PATH}/Android/data/com.danilov.manga/db/
        File dbPathFile = new File(externalStorageDir, "Android" + File.separator + "data" + File.separator + packageName + File.separator + "db");
        if (!dbPathFile.exists()) {
            boolean created = dbPathFile.mkdirs();
            if (!created) {
                Log.e(TAG, "Can't create file");
            }
        }
        String dbPath = dbPathFile + "/" + getDbName();
        databaseHelper = new DatabaseHelper(dbPath, getDAOVersion(), getUpdateHandler());
    }

    public abstract String getTableName();
    public abstract String getDbName();
    public abstract int getDAOVersion();
    public abstract MetaModel getMetaModel();
    public abstract DatabaseHelper.DatabaseUpgradeHandler getUpdateHandler();

    @Override
    public T create(final T t) {
        Database database = null;
        try {
            database = databaseHelper.openWritable();
        } catch (DatabaseAccessException e) {
            //FIXME: THROW EXCEPTION
            return null;
        }
        HashMap<String, MetaModel.Column> columns = getMetaModel().getColumns();
        ContentValues cv = objToCV(t, false);
        long id = database.insertOrThrow(getTableName(), null, cv);
        if (id == -1) {
            //FIXME: THROW EXCEPTION
            return null;
        }
        t.setId(id);
        return t;
    }

    @Override
    public void delete(final T t) {
        Database database = null;
        try {
            database = databaseHelper.openWritable();
        } catch (DatabaseAccessException e) {
            //FIXME: THROW EXCEPTION
            return;
        }
        String selection = ID_NAME + " = ?";
        String[] selectionArgs = new String[] {"" + t.getId()};
        database.delete(getTableName(), selection, selectionArgs);
    }

    @Override
    public T update(final T t) {
        Database database = null;
        try {
            database = databaseHelper.openWritable();
        } catch (DatabaseAccessException e) {
            //FIXME: THROW EXCEPTION
            return null;
        }
        HashMap<String, MetaModel.Column> columns = getMetaModel().getColumns();
        ContentValues cv = objToCV(t, true);
        String selection = ID_NAME + " = ?";
        String[] selectionArgs = new String[] {"" + t.getId()};
        long rowsAffected = database.update(getTableName(), cv, selection, selectionArgs);
        if (rowsAffected == 0) {
            //FIXME: THROW EXCEPTION
            return null;
        }
        return t;
    }

    @NonNull
    @Override
    public Collection<T> select(final Selector selector) {
        Database database = null;
        try {
            database = databaseHelper.openWritable();
        } catch (DatabaseAccessException e) {
            //FIXME: THROW EXCEPTION
            return Collections.emptyList();
        }
        Cursor cursor = database.rawQuery(selector.formatQuery(), null);
        ResultSet resultSet = new ResultSet(cursor, getMetaModel());
        if (!resultSet.moveToFirst()) {
            return Collections.emptyList();
        }
        List<T> res = new ArrayList<>();
        do {
            T value = create();
            value.load(resultSet);
            res.add(value);
        } while (resultSet.moveToNext());
        return res;
    }

    public ContentValues objToCV(final T t, final boolean withId) {
        HashMap<String, MetaModel.Column> columns = getMetaModel().getColumns();
        ContentValues cv = new ContentValues();
        for (MetaModel.Column column : columns.values()) {
            int columnType = column.getColumnType();
            String columnName = column.getColumnName();
            if (columnName.equals(ID_NAME) && !withId) {
                continue;
            }
            switch (columnType) {
                case MetaModel.TYPE_BLOB:
                    cv.put(columnName, (byte[]) getValue(t, columnName));
                    break;
                case MetaModel.TYPE_DOUBLE:
                    cv.put(columnName, (double) getValue(t, columnName));
                    break;
                case MetaModel.TYPE_FLOAT:
                    cv.put(columnName, (float) getValue(t, columnName));
                    break;
                case MetaModel.TYPE_STRING:
                    cv.put(columnName, (String) getValue(t, columnName));
                    break;
                case MetaModel.TYPE_LONG:
                    cv.put(columnName, (long) getValue(t, columnName));
                    break;
                case MetaModel.TYPE_INT:
                    cv.put(columnName, (int) getValue(t, columnName));
                    break;
            }
        }
        return cv;
    }

    public static Object getValue(final Object object, final String fieldName) {
        Class<?> aClass = object.getClass();
        String getterName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        try {
            Method declaredMethod = aClass.getDeclaredMethod("get" + getterName, null);
            return declaredMethod.invoke(object);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

}
