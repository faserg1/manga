package com.danilov.supermanga.core.repository.special;

import android.support.annotation.NonNull;
import android.util.Log;

import com.danilov.supermanga.core.database.Database;
import com.danilov.supermanga.core.database.DatabaseHelper;
import com.danilov.supermanga.core.database.DatabaseOptions;
import com.danilov.supermanga.core.database.crud.Crud;
import com.danilov.supermanga.core.database.crud.MetaModel;
import com.danilov.supermanga.core.database.crud.SimpleCrud;

import java.util.HashMap;

/**
 * Created by Semyon on 04.02.2016.
 */
public class JSCrud extends SimpleCrud<JavaScriptRepository> {


    @Override
    public String getTableName() {
        return "jsrepos";
    }

    @Override
    public String getDbName() {
        return "jsrepos.db";
    }

    @Override
    public int getDAOVersion() {
        return 1;
    }

    @Override
    public MetaModel getMetaModel() {
        return metaModel;
    }

    @Override
    public DatabaseHelper.DatabaseUpgradeHandler getUpdateHandler() {
        return new UpgradeHandler();
    }

    public MetaModel metaModel = new MetaModel() {

        private HashMap<String, Column> map = new HashMap<>();

        {
            map.put(ID_NAME, new Column(MetaModel.TYPE_LONG, ID_NAME, false));
            map.put(JavaScriptRepository.FILE_PATH, new Column(MetaModel.TYPE_STRING, JavaScriptRepository.FILE_PATH, false));
            map.put(JavaScriptRepository.REPO_NAME, new Column(MetaModel.TYPE_STRING, JavaScriptRepository.REPO_NAME, false));
        }

        @NonNull
        @Override
        public HashMap<String, Column> getColumns() {
            return map;
        }
    };

    @Override
    public JavaScriptRepository create() {
        return new JavaScriptRepository();
    }

    private class UpgradeHandler implements DatabaseHelper.DatabaseUpgradeHandler {

        @Override
        public void onUpgrade(final Database database, final int currentVersion) {
            DatabaseOptions.Builder builder = new DatabaseOptions.Builder();
            builder.setName(getTableName());
            builder.addColumn(ID_NAME, DatabaseOptions.Type.INT, true, true);
            builder.addColumn(JavaScriptRepository.FILE_PATH, DatabaseOptions.Type.TEXT, false, false);
            builder.addColumn(JavaScriptRepository.REPO_NAME, DatabaseOptions.Type.TEXT, false, false);
            DatabaseOptions options = builder.build();
            String sqlStatement = options.toSQLStatement();
            try {
                database.execSQL("drop table if exists " + getTableName() + ";");
                database.execSQL(sqlStatement);
            } catch (Exception e) {
                Log.e(TAG, "UpgradeHandler onUpgrade failed: " + e.getMessage());
            }
        }

    }

    public Selector getByNameSelector(final String name) {
        return new Selector() {
            @Override
            public String formatQuery() {
                return "SELECT * from " + getTableName() + " WHERE " + JavaScriptRepository.REPO_NAME + " = '" + name + "';";
            }
        };
    }

    public Selector getAllSelector() {
        return new Selector() {
            @Override
            public String formatQuery() {
                return "SELECT * from " + getTableName() + ";";
            }
        };
    }

}
