package sanzhiev.rssfeed.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import sanzhiev.rssfeed.database.FeedContract.FeedChannelContract;
import sanzhiev.rssfeed.database.FeedContract.FeedItemContract;
import sanzhiev.rssfeed.database.QueryBuilder.ColumnType;

public final class RssFeedDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 28;
    private static final String DATABASE_NAME = "RssFeed.db";

    public RssFeedDbHelper(final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        createTables(db);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        dropTables(db);
        createTables(db);
    }

    private void createTables(final SQLiteDatabase db) {
        final String createChannelsTableSql =
                new QueryBuilder(FeedChannelContract.TABLE_NAME)
                        .setQueryType(QueryBuilder.QueryType.CREATE)
                        .addColumn(FeedChannelContract._ID, ColumnType.ID)
                        .addColumn(FeedChannelContract.TITLE, ColumnType.TEXT)
                        .addColumn(FeedChannelContract.DESCRIPTION, ColumnType.TEXT)
                        .addColumn(FeedChannelContract.LINK, ColumnType.TEXT)
                        .getQuery();

        final String createItemsTableSql =
                new QueryBuilder(FeedItemContract.TABLE_NAME)
                        .setQueryType(QueryBuilder.QueryType.CREATE)
                        .addColumn(FeedItemContract._ID, ColumnType.ID)
                        .addColumn(FeedItemContract.TITLE, ColumnType.TEXT)
                        .addColumn(FeedItemContract.DESCRIPTION, ColumnType.TEXT)
                        .addColumn(FeedItemContract.LINK, ColumnType.TEXT)
                        .addColumn(FeedItemContract.PUB_DATE, ColumnType.INTEGER)
                        .addColumn(FeedItemContract.CHANNEL_ID, ColumnType.INTEGER)
                        .addColumn(FeedItemContract.IS_READ, ColumnType.INTEGER)
                        .setForeignKey(FeedItemContract.CHANNEL_ID,
                                       FeedChannelContract.TABLE_NAME,
                                       FeedChannelContract._ID)
                        .getQuery();

        db.execSQL(createChannelsTableSql);
        db.execSQL(createItemsTableSql);
    }

    private void dropTables(final SQLiteDatabase db) {
        final String dropChannelsTableSql =
                new QueryBuilder(FeedChannelContract.TABLE_NAME)
                        .setQueryType(QueryBuilder.QueryType.DROP)
                        .getQuery();

        final String dropItemsTableSql =
                new QueryBuilder(FeedItemContract.TABLE_NAME)
                        .setQueryType(QueryBuilder.QueryType.DROP)
                        .getQuery();

        db.execSQL(dropChannelsTableSql);
        db.execSQL(dropItemsTableSql);
    }
}
