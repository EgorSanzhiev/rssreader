package sanzhiev.rssfeed.networking;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import sanzhiev.rssfeed.RssFeedApplication;
import sanzhiev.rssfeed.database.ChannelRepository;
import sanzhiev.rssfeed.database.FeedRepository;
import sanzhiev.rssfeed.model.FeedChannel;
import sanzhiev.rssfeed.model.FeedItem;
import sanzhiev.rssfeed.services.parser.Parser;

public class ChannelUpdater {
    private final static int READ_TIMEOUT_IN_MILLISECONDS = 10000;
    private final static int CONNECT_TIMEOUT_IN_MILLISECONDS = 15000;
    private final static String LOG_TAG = "ChannelService";

    private RssFeedApplication application;
    private ChannelRepository channelRepository;
    private FeedRepository feedRepository;

    public ChannelUpdater(Application application) {
        this.application = (RssFeedApplication) application;
        channelRepository = new ChannelRepository(application);
        feedRepository = new FeedRepository(this.application);
    }

    public Flowable<FeedItem> updateFeed() {
        return channelRepository.getAllChannels()
                .map(this::getItemsFromChannel)
                .flatMap(feedRepository::saveAll)
                .subscribeOn(Schedulers.io());
    }

    public Completable saveChannel(String url) {
        return Completable.fromAction(() -> {
            FeedChannel feedChannel = subscribeToChannel(url);
            channelRepository.saveChannel(feedChannel).subscribeOn(Schedulers.io()).subscribe(
                    () -> feedRepository.saveAll(getItemsFromChannel(feedChannel)).subscribeOn(Schedulers.io()).subscribe()
            );
        });
    }

    private List<FeedItem> getItemsFromChannel(FeedChannel channel) throws Exception {
        InputStream feedStream = downloadFileOrNull(channel.getLink());
        try {
            final Parser parser = Parser.createParser(feedStream, channel.getLink());
            return parser.parseFeed(channel);
        } finally {
            close(feedStream);
        }
    }

    private FeedChannel subscribeToChannel(final String channelUrl) throws Exception {
        InputStream feedStream = downloadFileOrNull(channelUrl);
        try {
            if (feedStream == null) {
                return null;
            }

            final Parser parser = Parser.createParser(feedStream, channelUrl);

            return parser.parseChannel();
        } finally {
            close(feedStream);
        }
    }

    private InputStream downloadFileOrNull(final String url) throws IOException {

        final ConnectivityManager connectivityManager = (ConnectivityManager)
                application.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            return null;
        }

        final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            Log.d(LOG_TAG, url);

            final URL urlToConnect = new URL(url);

            final HttpURLConnection connection = (HttpURLConnection) urlToConnect.openConnection();

            final String requestMethod = "GET";

            connection.setReadTimeout(READ_TIMEOUT_IN_MILLISECONDS);
            connection.setConnectTimeout(CONNECT_TIMEOUT_IN_MILLISECONDS);
            connection.setRequestMethod(requestMethod);

            connection.connect();

            final int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                return connection.getInputStream();
            } else {
                throw new IOException();
            }
        } else {
            throw new ConnectException();
        }
    }

    private void close(final Closeable stream) {
        if (stream == null) {
            return;
        }

        try {
            stream.close();
        } catch (final IOException e) {
            Log.d(LOG_TAG, e.toString());
        }
    }
}
