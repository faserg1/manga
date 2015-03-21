package com.danilov.mangareaderplus.core.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Semyon Danilov on 05.07.2014.
 */
public class DatabaseOptions {

    private final List<Column> columns;
    private final Map<String, List<Column>> constraints;
    private final String tableName;

    public enum Type {

        TEXT("text"),
        INT("integer"),
        REAL("real"),
        BLOB("blob");

        public final String sql;

        Type(final String sql) {
            this.sql = sql;
        }


    }

    private DatabaseOptions(final String tableName, final List<Column> columns, final Map<String, List<Column>> constraints) {
        this.tableName = tableName;
        this.columns = columns;
        this.constraints = constraints;
    }

    public Map<String, List<Column>> getConstraints() {
        return constraints;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public String toSQLStatement() {
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement.append("create table ").append(tableName).append(" (");
        for (int i = 0; i < columns.size(); i++) {
            Column column = columns.get(i);
            sqlStatement.append(column.name).append(" ");
            sqlStatement.append(column.type.sql);
            if (column.isPrimaryKey) {
                sqlStatement.append(" primary key");
            }
            if (column.isAutoincrement) {
                sqlStatement.append(" autoincrement");
            }
            if (i != columns.size() - 1) {
                sqlStatement.append(", ");
            }
        }
        if (constraints != null && !constraints.isEmpty()) {
            sqlStatement.append(", ");
            Set<Map.Entry<String, List<Column>>> entries = constraints.entrySet();
            int size = entries.size();
            int i = 0;
            for (Map.Entry<String, List<Column>> entry : entries) {
                sqlStatement.append(" unique(");
                List<Column> columnsUnique = entry.getValue();
                for (int j = 0; j < columnsUnique.size(); j++) {
                    sqlStatement.append(columnsUnique.get(j).name);
                    if (j != columnsUnique.size() - 1) {
                        sqlStatement.append(", ");
                    }
                }
                sqlStatement.append(")");
                if (i != size - 1) {
                    sqlStatement.append(",");
                }
                i++;
            }
        }
        sqlStatement.append(");");
        return sqlStatement.toString();
    }

    public static class Column {

        public String name;
        public Type type;
        public boolean isPrimaryKey;
        public boolean isAutoincrement;

        public Column(final String name, final Type type) {
            this.type = type;
            this.name = name;
        }

        public void setAutoincrement(final boolean isAutoincrement) {
            if (type != Type.INT && isAutoincrement) {
                throw new IllegalStateException("Autoincrement can be applied only for numeric columns");
            }
            this.isAutoincrement = isAutoincrement;
        }

        public void setIsPrimaryKey(final boolean isPrimaryKey) {
            this.isPrimaryKey = isPrimaryKey;
        }

    }

    public static class Builder {

        private boolean hasPrimaryKey;

        private List<Column> columns = new ArrayList<Column>();

        private Map<String, List<Column>> constraints = new HashMap<String, List<Column>>();

        private String tableName;

        public void addColumn(final String name, final Type type, final boolean isPrimaryKey, final boolean isAutoIncrement) {
            addColumn(name, type, isPrimaryKey, isAutoIncrement, null);
        }

        public void addColumn(final String name, final Type type, final boolean isPrimaryKey, final boolean isAutoIncrement, final String constraintName) {
            if (isPrimaryKey && hasPrimaryKey) {
                throw new IllegalStateException("Already has PrimaryKey");
            }
            Column column = new Column(name, type);
            column.setAutoincrement(isAutoIncrement);
            if (isPrimaryKey) {
                this.hasPrimaryKey = true;
            }
            column.setIsPrimaryKey(isPrimaryKey);
            columns.add(column);
            if (constraintName != null) {
                List<Column> constraint = constraints.get(constraintName);
                if (constraint == null) {
                    constraint = new ArrayList<Column>();
                    constraints.put(constraintName, constraint);
                }
                constraint.add(column);
            }
        }

        public void setName(final String tableName) {
            this.tableName = tableName;
        }

        public DatabaseOptions build() {
            return new DatabaseOptions(tableName, columns, constraints);
        }

    }


}
