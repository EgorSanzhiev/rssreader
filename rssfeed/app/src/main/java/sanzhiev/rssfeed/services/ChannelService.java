package sanzhiev.rssfeed.services;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import io.reactivex.schedulers.Schedulers;
import lombok.NonNull;
import sanzhiev.rssfeed.RssFeedApplication;
import sanzhiev.rssfeed.database.FeedRepository;
import sanzhiev.rssfeed.networking.ChannelUpdater;
import sanzhiev.rssfeed.ui.Toaster;

// один из вариантов обработки ошибок подключения - записать себе, что не получилось подключиться, а когда юзер открывает
// основную активность, показать ему диалог "пока тебя не было, не получилось скачать"
// и две кнопки "попробовать снова" и "не надо"
public final class ChannelService extends IntentService {
    public final static String FEED_UPDATED = "sanzhiev.rssfeed.services.FEED_UPDATED";
    private final static String ACTION_SUBSCRIBE = "sanzhiev.rssfeed.services.SUBSCRIBE";
    private final static String ACTION_UPDATE = "sanzhiev.rssfeed.services.UPDATE";
    private final static String URL_KEY = "channelURL";
    private final static String LOG_TAG = "ChannelService";

    private ChannelUpdater channelUpdater;

    public ChannelService() {
        super(LOG_TAG);
    }

    public static PendingIntent getPendingIntent(@NonNull final Context context,
                                                 final int flag) {
        final Intent startIntent = new Intent(context, ChannelService.class)
                .setAction(ACTION_UPDATE);

        return PendingIntent.getService(context, 0, startIntent, flag);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        channelUpdater = new ChannelUpdater(getApplication());
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        try {
            final String action = intent.getAction();

            if (action == null) {
                return;
            }

            if (action.equals(ACTION_SUBSCRIBE)) {
                final String channelUrl = intent.getStringExtra(URL_KEY);

                channelUpdater.saveChannel(channelUrl)
                        .subscribeOn(Schedulers.io())
                        .subscribe();
            }

            if (action.equals(ACTION_UPDATE)) {
                channelUpdater.updateFeed().subscribe(
                        item -> {},
                        e -> Toaster.makeShortToast(this, e.getMessage()),
                        () -> sendBroadcast(FEED_UPDATED)
                );
            }
        } catch (final Throwable throwable) {
            Log.d(LOG_TAG, throwable.toString());
        }
    }

    private void sendBroadcast(final String action) {
        final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        final Intent broadcastIntent = new Intent(action);
        broadcastManager.sendBroadcast(broadcastIntent);
    }
}
