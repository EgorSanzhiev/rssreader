package sanzhiev.rssfeed.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

import lombok.Getter;
import lombok.NonNull;

public final class FeedItem implements Parcelable {
    @Getter
    private final String title;

    @Getter
    private final String description;

    @Getter
    private final String link;

    @Getter
    private final Date pubDate;

    @Getter
    @NonNull
    private final FeedChannel channel;

    @Getter
    private boolean isRead;

    public FeedItem(final String title, final String description, final String link,
                    final Date pubDate, final FeedChannel channel, final boolean isRead) {
        this.title = title;
        this.description = description;
        this.link = link;
        this.pubDate = pubDate;
        this.channel = channel;
        this.isRead = isRead;
    }

    public void setRead() {
        isRead = true;
    }

    private FeedItem(final Parcel in) {
        title = in.readString();
        description = in.readString();
        link = in.readString();
        pubDate = (Date) in.readSerializable();
        channel = in.readParcelable(getClass().getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@android.support.annotation.NonNull final Parcel dest,
                              final int flags) {
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(link);
        dest.writeSerializable(pubDate);
        dest.writeParcelable(channel, 0);
    }

    public static final Creator<FeedItem> CREATOR = new Creator<FeedItem>() {
        @Override
        public FeedItem createFromParcel(final Parcel in) {
            return new FeedItem(in);
        }

        @Override
        public FeedItem[] newArray(final int size) {
            return new FeedItem[size];
        }
    };
}
