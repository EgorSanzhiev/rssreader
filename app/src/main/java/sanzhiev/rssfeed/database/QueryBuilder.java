package sanzhiev.rssfeed.database;

import java.util.ArrayList;

import lombok.Getter;

class QueryBuilder {
    enum QueryType {
        CREATE("CREATE TABLE"),
        DROP("DROP TABLE IF EXISTS");

        private final String stringValue;

        QueryType(final String stringValue) {
            this.stringValue = stringValue;
        }

        @Override
        public String toString() {
            return stringValue;
        }
    }

    enum ColumnType {
        INTEGER("INTEGER"),
        TEXT("TEXT"),
        ID("INTEGER PRIMARY KEY");

        private final String stringValue;

        ColumnType(final String stringValue) {
            this.stringValue = stringValue;
        }

        @Override
        public String toString() {
            return stringValue;
        }
    }

    private final class Column {
        @Getter
        private final String name;

        @Getter
        private final ColumnType type;

        Column(final String name, final ColumnType type) {
            this.name = name;
            this.type = type;
        }
    }

    private final class ForeignKey {
        @Getter
        private final String keyColumn;

        @Getter
        private final String referencedTable;

        @Getter
        private final String referencedColumn;

        ForeignKey(final String key, final String referencedTable, final String referencedColumn) {
            this.keyColumn = key;
            this.referencedTable = referencedTable;
            this.referencedColumn = referencedColumn;
        }
    }

    private final String tableName;
    private final ArrayList<Column> columns;
    private QueryType queryType;
    private ForeignKey foreignKey = null;

    QueryBuilder(final String tableName) {
        columns = new ArrayList<>();
        this.tableName = tableName;
    }

    QueryBuilder setQueryType(final QueryType type) {
        queryType = type;

        return this;
    }

    QueryBuilder addColumn(final String name, final ColumnType type) {
        columns.add(new Column(name, type));

        return this;
    }

    QueryBuilder setForeignKey(final String foreignKeyColumn,
                               final String referencedTable,
                               final String referencedColumn) {
        foreignKey = new ForeignKey(foreignKeyColumn, referencedTable, referencedColumn);

        return this;
    }

    String getQuery() {
        validate();

        final StringBuilder stringBuilder = new StringBuilder()
                .append(queryType)
                .append(" ")
                .append(tableName);

        if (queryType == QueryType.CREATE) {
            stringBuilder.append(" (");

            for (final Column column : columns) {
                stringBuilder
                        .append(column.getName())
                        .append(" ")
                        .append(column.getType())
                        .append(", ");
            }

            stringBuilder.delete(stringBuilder.lastIndexOf(", "), stringBuilder.length());

            if (foreignKey != null) {
                stringBuilder
                        .append(", FOREIGN KEY (")
                        .append(foreignKey.getKeyColumn())
                        .append(") REFERENCES ")
                        .append(foreignKey.getReferencedTable())
                        .append("(")
                        .append(foreignKey.getReferencedColumn())
                        .append(")");
            }

            stringBuilder.append(")");
        }

        return stringBuilder.toString();
    }

    private void validate() {
        //TODO: валидация
    }
}
