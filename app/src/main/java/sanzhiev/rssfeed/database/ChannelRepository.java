package sanzhiev.rssfeed.database;

import android.app.Application;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import lombok.NonNull;
import sanzhiev.rssfeed.RssFeedApplication;
import sanzhiev.rssfeed.database.dao.ChannelDao;
import sanzhiev.rssfeed.database.dao.FeedDao;
import sanzhiev.rssfeed.model.FeedChannel;

public class ChannelRepository {
    private ChannelDao channelDao;
    private FeedDao feedDao;

    public ChannelRepository(Application application) {
        RssFeedDbHelper dbHelper = ((RssFeedApplication) application).getDbHelper();
        channelDao = new ChannelDao(dbHelper);
        feedDao = new FeedDao(dbHelper);
    }

    public Flowable<FeedChannel> getAllChannels() {
        return Flowable.fromIterable(channelDao.getAllChannels());
    }

    public Completable deleteChannel(@NonNull FeedChannel channel) {
        return Completable.fromAction(() -> {
            long channelId = channelDao.findChannelId(channel.getLink());
            channelDao.deleteChannel(channel.getLink());
            feedDao.deleteItemsFromChannel(channelId);
        }).subscribeOn(Schedulers.io());
    }

    public Completable saveChannel(@NonNull FeedChannel channel) {
        return Completable.fromAction(() -> channelDao.saveChannel(channel)).subscribeOn(Schedulers.io());
    }
}
