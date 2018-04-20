package sanzhiev.rssfeed.database;

import android.util.Log;

import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.schedulers.Schedulers;
import lombok.NonNull;
import sanzhiev.rssfeed.RssFeedApplication;
import sanzhiev.rssfeed.database.dao.FeedDao;
import sanzhiev.rssfeed.model.FeedItem;

public class FeedRepository {
    private FeedDao feedDao;

    public FeedRepository(RssFeedApplication application) {
        feedDao = new FeedDao(application.getDbHelper());
    }

    public Flowable<FeedItem> getAllItems() {
        Flowable<FeedItem> itemFlowable = Flowable.create(emitter -> {
            try {
                List<FeedItem> feedItems = feedDao.fetchFeed();
                feedItems.forEach(emitter::onNext);
                emitter.onComplete();
            } catch (Exception e) {
                emitter.onError(e);
            }
        }, BackpressureStrategy.BUFFER);

        return itemFlowable.subscribeOn(Schedulers.io());
    }

    public Flowable<FeedItem> getItemsFromChannel(@NonNull final String channelUrl) {
        Log.d("FeedRepo", "getItemsFromChannel: privet");
        Flowable<FeedItem> itemFlowable = Flowable.create(emitter -> {
            try {
                List<FeedItem> feedItems = feedDao.fetchFeed(channelUrl);
                Log.d("FeedRepo", "getItemsFromChannel: " + feedItems.size());
                feedItems.forEach(emitter::onNext);
                emitter.onComplete();
            } catch (Exception e) {
                emitter.onError(e);
            }
        }, BackpressureStrategy.BUFFER);

        return itemFlowable.subscribeOn(Schedulers.io());
    }

    public Flowable<FeedItem> saveAll(List<FeedItem> feedItemList) {
        Flowable<FeedItem> itemFlowable = Flowable.create(
                emitter -> {
                    try {
                        feedItemList.stream()
                                .filter(feedItem -> feedDao.findItemId(feedItem) == -1)
                                .forEach(feedItem -> saveItem(emitter, feedItem));
                        emitter.onComplete();
                    } catch (Exception e) {
                        emitter.onError(e);
                    }
                },
                BackpressureStrategy.BUFFER);

        return itemFlowable.subscribeOn(Schedulers.io());
    }

    private void saveItem(FlowableEmitter<FeedItem> emitter, FeedItem item) {
        try {
            feedDao.saveItem(item);
            emitter.onNext(item);
        } catch (Exception e) {
            emitter.onError(e);
        }
    }
}
