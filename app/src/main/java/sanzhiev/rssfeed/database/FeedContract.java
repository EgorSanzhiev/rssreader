package sanzhiev.rssfeed.database;

import android.provider.BaseColumns;

final class FeedContract {
    private FeedContract() {
        throw new UnsupportedOperationException();
    }

    static final class FeedChannelContract implements BaseColumns {
        static final String TABLE_NAME = "channels";
        static final String TITLE = "title";
        static final String DESCRIPTION = "description";
        static final String LINK = "link";
    }

    static final class FeedItemContract implements BaseColumns {
        static final String TABLE_NAME = "items";
        static final String TITLE = "title";
        static final String DESCRIPTION = "description";
        static final String LINK = "link";
        static final String PUB_DATE = "pub_date";
        static final String IS_READ = "is_read";
        static final String CHANNEL_ID = "channel_id";
    }
}
