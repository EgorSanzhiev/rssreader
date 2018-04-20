package sanzhiev.rssfeed.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.NonNull;
import sanzhiev.rssfeed.database.RssFeedDbHelper;
import sanzhiev.rssfeed.model.FeedChannel;

import static sanzhiev.rssfeed.database.dao.FeedContract.FeedChannelContract;

@AllArgsConstructor
public class ChannelDao {
    private RssFeedDbHelper dbHelper;

    public List<FeedChannel> getAllChannels() {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        final ArrayList<FeedChannel> channels = new ArrayList<>();

        final String idSelection[] = {FeedChannelContract._ID};
        final String selectionClause = null;
        final String selectionArgs[] = null;
        final String groupBy = null;
        final String having = null;
        final String orderBy = null;

        @Cleanup
        final Cursor channelCursor = database.query(FeedChannelContract.TABLE_NAME,
                idSelection,
                selectionClause,
                selectionArgs,
                groupBy, having, orderBy);

        channelCursor.moveToFirst();

        while (!channelCursor.isAfterLast()) {
            final long id = channelCursor.getLong(0);

            channels.add(findChannelByIdOrNull(id));

            channelCursor.moveToNext();
        }

        return channels;
    }

    FeedChannel findChannelByIdOrNull(final long id) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        final String[] selection = {FeedChannelContract.TITLE,
                FeedChannelContract.DESCRIPTION,
                FeedChannelContract.LINK};
        final String selectionClause = FeedChannelContract._ID + "=?";
        final String[] selectionArgs = {String.valueOf(id)};
        final String groupBy = null;
        final String having = null;
        final String orderBy = null;

        @Cleanup
        final Cursor channelCursor = database.query(FeedChannelContract.TABLE_NAME,
                selection,
                selectionClause,
                selectionArgs,
                groupBy, having, orderBy);

        if (channelCursor.getCount() == 0) {
            return null;
        }

        channelCursor.moveToFirst();

        final int titleIndex = channelCursor.getColumnIndex(FeedChannelContract.TITLE);
        final int descriptionIndex = channelCursor.getColumnIndex(FeedChannelContract.DESCRIPTION);
        final int urlIndex = channelCursor.getColumnIndex(FeedChannelContract.LINK);

        final String title = channelCursor.getString(titleIndex);
        final String description = channelCursor.getString(descriptionIndex);
        final String url = channelCursor.getString(urlIndex);

        return new FeedChannel(title, description, url);
    }

    public long findChannelId(@NonNull final String channelUrl) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        final long id;

        final String idSelection[] = {FeedChannelContract._ID};
        final String selectionClause = FeedChannelContract.LINK + "=?";
        final String selectionArgs[] = {channelUrl};
        final String groupBy = null;
        final String having = null;
        final String orderBy = null;

        @Cleanup
        final Cursor cursor = database.query(FeedChannelContract.TABLE_NAME,
                idSelection,
                selectionClause,
                selectionArgs,
                groupBy, having, orderBy);

        if (cursor.getCount() == 0) {
            id = -1;
        } else {
            cursor.moveToFirst();
            id = cursor.getLong(cursor.getColumnIndex(FeedChannelContract._ID));
        }

        return id;
    }

    public void deleteChannel(final String channelUrl) {
        final SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.acquireReference();

        try {
            final long channelToDeleteId = findChannelId(channelUrl);

            final String deleteClause = FeedChannelContract._ID + "=?";
            final String[] deleteArgs = new String[]{String.valueOf(channelToDeleteId)};

            database.delete(FeedChannelContract.TABLE_NAME,
                    deleteClause,
                    deleteArgs);
        } finally {
            database.releaseReference();
        }
    }

    public void saveChannel(@NonNull FeedChannel channel) {
        final SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.acquireReference();

        try {
            final ContentValues channelValues = new ContentValues();

            channelValues.put(FeedChannelContract.TITLE, channel.getTitle());
            channelValues.put(FeedChannelContract.DESCRIPTION, channel.getDescription());
            channelValues.put(FeedChannelContract.LINK, channel.getLink());

            final long channelId = findChannelId(channel.getLink());

            if (channelId == -1) {
                database.insert(FeedChannelContract.TABLE_NAME, null, channelValues);
            }
        } finally {
            database.releaseReference();
        }
    }
}
