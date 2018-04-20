package sanzhiev.rssfeed.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Cleanup;
import lombok.NonNull;
import sanzhiev.rssfeed.database.RssFeedDbHelper;
import sanzhiev.rssfeed.model.FeedChannel;
import sanzhiev.rssfeed.model.FeedItem;

public class FeedDao {
    private RssFeedDbHelper dbHelper;
    private ChannelDao channelDao;

    public FeedDao(RssFeedDbHelper dbHelper) {
        this.dbHelper = dbHelper;
        channelDao = new ChannelDao(dbHelper);
    }

    public List<FeedItem> fetchFeed() {
        final SQLiteDatabase database = dbHelper.getReadableDatabase();
        database.acquireReference();

        try {
            final String[] selectionColumns = null;
            final String selectionClause = null;
            final String[] selectionArgs = null;
            final String groupBy = null;
            final String having = null;
            final String orderBy = FeedContract.FeedItemContract.PUB_DATE + " DESC";

            @Cleanup
            final Cursor cursor = database.query(FeedContract.FeedItemContract.TABLE_NAME,
                    selectionColumns,
                    selectionClause,
                    selectionArgs,
                    groupBy,
                    having,
                    orderBy);
            final ArrayList<FeedItem> items = new ArrayList<>(cursor.getCount());

            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                items.add(getItem(cursor));
                cursor.moveToNext();
            }

            return items;
        } finally {
            database.releaseReference();
        }
    }

    public ArrayList<FeedItem> fetchFeed(@NonNull final String channelUrl) {
        final SQLiteDatabase database = dbHelper.getReadableDatabase();
        database.acquireReference();

        try {
            final long channelId = channelDao.findChannelId(channelUrl);

            final String[] selectionColumns = null;
            final String selectionClause = FeedContract.FeedItemContract.CHANNEL_ID + "=?";
            final String[] selectionArgs = new String[]{String.valueOf(channelId)};
            final String groupBy = null;
            final String having = null;
            final String orderBy = FeedContract.FeedItemContract.PUB_DATE + " DESC";

            @Cleanup
            final Cursor cursor = database.query(FeedContract.FeedItemContract.TABLE_NAME,
                    selectionColumns,
                    selectionClause,
                    selectionArgs,
                    groupBy,
                    having,
                    orderBy);

            final ArrayList<FeedItem> items = new ArrayList<>(cursor.getCount());

            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                items.add(getItem(cursor));
                cursor.moveToNext();
            }

            return items;
        } finally {
            database.releaseReference();
        }
    }

    public void saveItem(@NonNull final FeedItem item) {
        final SQLiteDatabase database = dbHelper.getWritableDatabase();
        final long channelId = channelDao.findChannelId(item.getChannel().getLink());

        final ContentValues itemValues = new ContentValues();

        itemValues.put(FeedContract.FeedItemContract.TITLE, item.getTitle());
        itemValues.put(FeedContract.FeedItemContract.DESCRIPTION, item.getDescription());
        itemValues.put(FeedContract.FeedItemContract.LINK, item.getLink());
        itemValues.put(FeedContract.FeedItemContract.CHANNEL_ID, channelId);
        itemValues.put(FeedContract.FeedItemContract.IS_READ, item.isRead() ? 1 : 0);

        final Date pubDate = item.getPubDate();

        if (pubDate != null) {
            itemValues.put(FeedContract.FeedItemContract.PUB_DATE, pubDate.getTime());
        }

        if (findItemId(item) == -1) {
            database.insert(FeedContract.FeedItemContract.TABLE_NAME, null, itemValues);
        }
    }

    public long findItemId(@NonNull final FeedItem item) {
        long itemId = -1;

        SQLiteDatabase database = dbHelper.getReadableDatabase();
        final long channelId = channelDao.findChannelId(item.getChannel().getLink());

        final String[] selection = new String[]{FeedContract.FeedItemContract._ID};
        final String selectionClause = FeedContract.FeedItemContract.TITLE + "=? AND "
                + FeedContract.FeedItemContract.CHANNEL_ID + "=?";
        final String[] selectionArgs = new String[]{item.getTitle(), String.valueOf(channelId)};
        final String groupBy = null;
        final String having = null;
        final String orderBy = null;

        @Cleanup
        final Cursor cursor = database.query(FeedContract.FeedItemContract.TABLE_NAME,
                selection,
                selectionClause,
                selectionArgs,
                groupBy, having, orderBy);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            itemId = cursor.getLong(cursor.getColumnIndex(FeedContract.FeedItemContract._ID));
        }

        return itemId;
    }

    private FeedItem getItem(@NonNull final Cursor cursor) {
        final SQLiteDatabase database = dbHelper.getReadableDatabase();
        database.acquireReference();

        try {
            final String title = cursor.getString
                    (cursor.getColumnIndex(FeedContract.FeedItemContract.TITLE));
            final String description = cursor.getString
                    (cursor.getColumnIndex(FeedContract.FeedItemContract.DESCRIPTION));
            final String url = cursor.getString
                    (cursor.getColumnIndex(FeedContract.FeedItemContract.LINK));
            final long channelId = cursor.getLong
                    (cursor.getColumnIndex(FeedContract.FeedItemContract.CHANNEL_ID));
            final FeedChannel channel = channelDao.findChannelByIdOrNull(channelId);

            Date pubDate = null;
            final int pubDateIndex = cursor.getColumnIndex(FeedContract.FeedItemContract.PUB_DATE);

            if (!cursor.isNull(pubDateIndex)) {
                final long pubDateInMilliseconds = cursor.getLong(pubDateIndex);
                pubDate = new Date(pubDateInMilliseconds);
            }

            final int isReadRepresentation = cursor.getInt
                    (cursor.getColumnIndex(FeedContract.FeedItemContract.IS_READ));

            final boolean isRead = (isReadRepresentation > 0);

            return new FeedItem(title, description, url, pubDate, channel, isRead);
        } finally {
            database.releaseReference();
        }
    }

    public void deleteItemsFromChannel(final long channelId) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.acquireReference();
        try {
            final String deleteClause = FeedContract.FeedItemContract.CHANNEL_ID + "=?";
            final String[] deleteArgs = new String[]{String.valueOf(channelId)};

            database.delete(FeedContract.FeedItemContract.TABLE_NAME,
                    deleteClause,
                    deleteArgs);
        } finally {
            database.releaseReference();
        }
    }

}
