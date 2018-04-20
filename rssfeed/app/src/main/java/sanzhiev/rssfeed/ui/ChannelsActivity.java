package sanzhiev.rssfeed.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import sanzhiev.rssfeed.R;
import sanzhiev.rssfeed.database.ChannelRepository;
import sanzhiev.rssfeed.model.FeedChannel;

public final class ChannelsActivity extends BaseActivity {
    private final class ChannelsAdapter extends RecyclerView.Adapter<ChannelsAdapter.ChannelViewHolder> {
        final class ChannelViewHolder extends RecyclerView.ViewHolder {
            private final TextView channelNameView;

            ChannelViewHolder(final View itemView) {
                super(itemView);

                channelNameView = (TextView) itemView.findViewById(R.id.channelName);
            }

            void bindChannel(final FeedChannel channel) {
                channelNameView.setText(channel.getTitle());
            }
        }

        private ArrayList<FeedChannel> channels = new ArrayList<>();


        @Override
        public ChannelViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
            final View channelView = LayoutInflater.from(ChannelsActivity.this)
                                                   .inflate(R.layout.channel_layout, parent, false);

            return new ChannelViewHolder(channelView);
        }

        @Override
        public void onBindViewHolder(final ChannelViewHolder holder, final int position) {
            final FeedChannel channel = channels.get(position);

            holder.bindChannel(channel);
        }

        @Override
        public int getItemCount() {
            return channels == null ? 0 : channels.size();
        }

        void addChannel(FeedChannel channel) {
            channels.add(channel);
            notifyDataSetChanged();
        }

        void delete(final int position) {
            final FeedChannel removedChannel = channels.get(position);
            channelRepository.deleteChannel(removedChannel)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                    () -> {channels.remove(position); notifyDataSetChanged();},
                    (e) -> Toaster.makeShortToast(ChannelsActivity.this, "Error deleteing channel: " + e.getMessage())
            );
        }
    }

    private ChannelsAdapter channelsAdapter;
    private RecyclerView channelsView;
    private ChannelRepository channelRepository;

    static void start(final Context context) {
        final Intent startIntent = new Intent(context, ChannelsActivity.class);
        context.startActivity(startIntent);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeViews();

        setupItemTouchHelper();

        channelRepository = new ChannelRepository(getApplication());
    }

    @Override
    protected void onResume() {
        super.onResume();

        channelRepository.getAllChannels()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        channelsAdapter::addChannel,
                        e -> Toaster.makeShortToast(this, e.getMessage())
                );
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void initializeViews() {
        final LayoutInflater inflater = getLayoutInflater();

        final FrameLayout contentLayout = getFrameLayout();

        final View contentView = inflater.inflate(R.layout.channels_list_activity, contentLayout, false);

        contentLayout.addView(contentView);

        channelsView = (RecyclerView) findViewById(R.id.channelsList);

        final RecyclerView.LayoutManager channelsLayoutManager = new LinearLayoutManager(this);

        channelsView.setLayoutManager(channelsLayoutManager);

        final FloatingActionButton button = (FloatingActionButton) findViewById(R.id.addChannelButton);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                AddChannelActivity.start(ChannelsActivity.this);
            }
        });

        channelsAdapter = new ChannelsAdapter();

        channelsView.setAdapter(channelsAdapter);

        channelsView.addItemDecoration(new FeedItemDecoration(this));

        setTitle(getString(R.string.channels_activity_title));
    }

    private void setupItemTouchHelper() {
        final ItemTouchHelper.SimpleCallback itemTouchCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                    @Override
                    public boolean onMove(final RecyclerView recyclerView,
                                          final RecyclerView.ViewHolder viewHolder,
                                          final RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(final RecyclerView.ViewHolder viewHolder, final int direction) {
                        final int position = viewHolder.getAdapterPosition();
                        channelsAdapter.delete(position);
                    }
                };

        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchCallback);
        itemTouchHelper.attachToRecyclerView(channelsView);
    }
}
