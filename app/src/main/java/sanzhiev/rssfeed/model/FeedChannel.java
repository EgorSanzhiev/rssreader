package sanzhiev.rssfeed.model;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.Getter;
import lombok.NonNull;

public final class FeedChannel implements Parcelable {
    @Getter
    @NonNull
    private final String title;

    @Getter
    @NonNull
    private final String description;

    @Getter
    @NonNull
    private final String link;

    public FeedChannel(final String title, final String description, final String link) {
        this.title = title;
        this.description = description;
        this.link = link;
    }

    private FeedChannel(final Parcel in) {
        title = in.readString();
        description = in.readString();
        link = in.readString();
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
    }

    public static final Creator<FeedChannel> CREATOR = new Creator<FeedChannel>() {
        @Override
        public FeedChannel createFromParcel(final Parcel in) {
            return new FeedChannel(in);
        }

        @Override
        public FeedChannel[] newArray(final int size) {
            return new FeedChannel[size];
        }
    };
}
