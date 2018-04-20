package sanzhiev.rssfeed.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import lombok.NonNull;
import sanzhiev.rssfeed.R;
import sanzhiev.rssfeed.RssFeedApplication;
import sanzhiev.rssfeed.database.FeedRepository;
import sanzhiev.rssfeed.model.FeedChannel;
import sanzhiev.rssfeed.model.FeedItem;
import sanzhiev.rssfeed.networking.ChannelUpdater;

public final class FeedActivity extends BaseActivity {
    private Flowable<FeedItem> feedItemFlowable;
    private Disposable feedItemSubscription;
    private ChannelUpdater channelUpdater;

    private final class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {
        final class FeedViewHolder extends RecyclerView.ViewHolder {
            private final TextView titleView;

            private final TextView descriptionView;

            private final TextView channelView;

            FeedViewHolder(final View itemView) {
                super(itemView);

                titleView = (TextView) itemView.findViewById(R.id.titleView);
                descriptionView = (TextView) itemView.findViewById(R.id.descriptionView);
                channelView = (TextView) itemView.findViewById(R.id.channelView);
            }

            void bindItem(final FeedItem item) {
                final Resources resources = getResources();
                final int textColor;
                if (item.isRead()) {
                    textColor = resources.getColor(R.color.feed_activity_item_read_color);
                } else {
                    textColor = resources.getColor(R.color.feed_activity_item_not_read_color);
                }

                titleView.setTextColor(textColor);
                descriptionView.setTextColor(textColor);
                channelView.setTextColor(textColor);

                titleView.setText(item.getTitle());

                final String description = item.getDescription();
                final Spanned formattedDescription = Html.fromHtml(description);
                descriptionView.setText(formattedDescription);

                channelView.setText(item.getChannel().getTitle());
            }
        }

        private List<FeedItem> feed = new ArrayList<>();

        void addFeedItem(FeedItem item) {
            feed.add(item);
            notifyDataSetChanged();
        }

        @Override
        public FeedViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
            final View itemView = LayoutInflater.from(FeedActivity.this)
                    .inflate(R.layout.feed_item, parent, false);
            return new FeedViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final FeedViewHolder holder, final int position) {
            final FeedItem item = feed.get(position);

            holder.bindItem(item);

            holder.itemView.setOnClickListener(v -> {
                if (!item.isRead()) {
                    item.setRead();
                    notifyItemChanged(holder.getAdapterPosition());
                }

                FeedItemActivity.start(FeedActivity.this, item);
            });
        }

        @Override
        public int getItemCount() {
            return feed == null ? 0 : feed.size();
        }
    }

    private final static String CHANNEL_EXTRA = "channelUrl";

    private FeedAdapter feedViewAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    private String channelUrl = null;

    public static void start(@NonNull final Context context) {
        final Intent startIntent = new Intent(context, FeedActivity.class);

        context.startActivity(startIntent);
    }

    public static void start(final Context context, @NonNull final FeedChannel channel) {
        final Intent startIntent = new Intent(context, FeedActivity.class);

        startIntent.putExtra(CHANNEL_EXTRA, channel);

        context.startActivity(startIntent);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeViews();

        initializeChannel();

        FeedRepository feedRepository = new FeedRepository((RssFeedApplication) getApplication());

        if (channelUrl == null) {
            feedItemFlowable = feedRepository.getAllItems();
        } else {
            feedItemFlowable = feedRepository.getItemsFromChannel(channelUrl);
        }

        channelUpdater = new ChannelUpdater(getApplication());
    }

    @Override
    protected void onResume() {
        super.onResume();

        feedItemSubscription = feedItemFlowable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        item -> feedViewAdapter.addFeedItem(item),
                        t -> {Toaster.makeShortToast(this, t.getMessage());
                            Log.e("FeedActivity", "onResume: " + t.getMessage());},
                        () -> swipeRefreshLayout.setRefreshing(false)
                );
    }

    @Override
    protected void onPause() {
        super.onPause();

        feedItemSubscription.dispose();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);

        final MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.update_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        super.onOptionsItemSelected(item);

        final int itemId = item.getItemId();

        if (itemId == R.id.updateAction) {
            updateFeed();
            swipeRefreshLayout.setRefreshing(true);
        }

        return true;
    }

    private void updateFeed() {
        channelUpdater.updateFeed()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        feedItem -> feedViewAdapter.addFeedItem(feedItem),
                        e -> Toaster.makeShortToast(this, e.getMessage()),
                        () -> swipeRefreshLayout.setRefreshing(false)
                );
    }

    private void initializeViews() {
        final LayoutInflater inflater = getLayoutInflater();

        final FrameLayout contentLayout = getFrameLayout();

        final View contentView = inflater.inflate(R.layout.feed_activity, contentLayout, false);

        contentLayout.addView(contentView);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_feed);

        swipeRefreshLayout.setOnRefreshListener(this::updateFeed);

        final RecyclerView feedView = (RecyclerView) findViewById(R.id.feedView);

        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

        feedView.setLayoutManager(layoutManager);

        feedViewAdapter = new FeedAdapter();

        feedView.setAdapter(feedViewAdapter);

        feedView.addItemDecoration(new FeedItemDecoration(this));

        setTitle(getString(R.string.feed_activity_title));
    }

    private void initializeChannel() {
        final Intent startIntent = getIntent();

        if (startIntent != null) {
            final FeedChannel channel = startIntent.getParcelableExtra(CHANNEL_EXTRA);

            if (channel != null) {
                channelUrl = channel.getLink();
                setTitle(channel.getTitle());
            }
        }
    }
}
