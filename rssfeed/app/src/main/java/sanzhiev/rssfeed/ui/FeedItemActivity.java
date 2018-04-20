package sanzhiev.rssfeed.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import lombok.NonNull;
import sanzhiev.rssfeed.R;
import sanzhiev.rssfeed.model.FeedItem;

public final class FeedItemActivity extends BaseActivity {
    private static final String KEY_FEED_ITEM = "FEED_ITEM";

    @NonNull
    private FeedItem feedItem;

    public static void start(final Context context, final FeedItem feedItem) {
        final Intent startIntent = new Intent(context, FeedItemActivity.class)
                .putExtra(KEY_FEED_ITEM, feedItem);

        context.startActivity(startIntent);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final LayoutInflater inflater = getLayoutInflater();

        final FrameLayout contentLayout = getFrameLayout();

        final View contentView = inflater.inflate(R.layout.feed_item_activity, contentLayout, false);

        contentLayout.addView(contentView);

        setupItemViews();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);

        final MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.share_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        super.onOptionsItemSelected(item);

        final Intent shareIntent = new Intent(Intent.ACTION_SEND);

        shareIntent.putExtra(Intent.EXTRA_TEXT, feedItem.getLink());

        shareIntent.setType("text/plain");

        startActivity(shareIntent);

        return true;
    }

    private void setupItemViews() {
        final TextView titleView = (TextView) findViewById(R.id.titleView);
        final TextView descriptionView = (TextView) findViewById(R.id.descriptionView);
        final TextView linkView = (TextView) findViewById(R.id.linkView);
        final TextView channelNameView = (TextView) findViewById(R.id.channelView);

        final Intent intent = getIntent();

        feedItem = intent.getParcelableExtra(KEY_FEED_ITEM);

        titleView.setText(feedItem.getTitle());

        final String description = feedItem.getDescription();
        final Spanned spannedDescription = Html.fromHtml(description);
        descriptionView.setText(spannedDescription);

        linkView.setText(feedItem.getLink());
        channelNameView.setText(feedItem.getChannel().getTitle());

        final String datePattern = "dd.MM.yyyy HH:mm";

        final SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern, Locale.US);

        final Date pubDate = feedItem.getPubDate();

        if (pubDate != null) {
            final TextView pubDateView = (TextView) findViewById(R.id.pubDateView);
            pubDateView.setText(dateFormat.format(pubDate));
        }

    }
}
