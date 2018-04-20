package sanzhiev.rssfeed.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import sanzhiev.rssfeed.R;
import sanzhiev.rssfeed.database.ChannelRepository;
import sanzhiev.rssfeed.model.FeedChannel;

abstract class BaseActivity extends AppCompatActivity {
    private ChannelRepository channelRepository;
    private final class DrawerChannelsAdapter extends ArrayAdapter<FeedChannel> {
        private ArrayList<FeedChannel> channels;

        DrawerChannelsAdapter(final Context context, final ArrayList<FeedChannel> objects) {
            super(context, R.layout.channel_layout, objects);

            channels = objects;
        }

        void addChannel(FeedChannel channel) {
            channels.add(channel);
            notifyDataSetChanged();
        }

        @android.support.annotation.NonNull
        @Override
        public View getView(final int position, final View convertView,
                            @android.support.annotation.NonNull final ViewGroup parent) {
            View view = convertView;

            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.channel_layout, parent, false);
            }

            final FeedChannel channel = channels.get(position);

            if (channel != null) {
                final TextView channelTitle = (TextView) view.findViewById(R.id.channelName);

                channelTitle.setText(channel.getTitle());
            }

            return view;
        }
    }

    private final class OnChannelClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(final AdapterView<?> parent, final View view,
                                final int position, final long id) {
            final FeedChannel channel = drawerChannelsAdapter.getItem(position);

            FeedActivity.start(BaseActivity.this, channel);

            drawerLayout.closeDrawer(leftDrawer);
        }
    }

    private final class OnOptionClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(final AdapterView<?> parent, final View view, final int position,
                                final long id) {
            switch (position) {
                case 0:
                    ChannelsActivity.start(BaseActivity.this);
                    break;
                case 1:
                    FeedActivity.start(BaseActivity.this);
                    break;
                default:
                    break;
            }

            drawerLayout.closeDrawer(leftDrawer);
        }
    }

    private DrawerLayout drawerLayout;

    private FrameLayout frameLayout;
    private DrawerChannelsAdapter drawerChannelsAdapter;
    private LinearLayout leftDrawer;

    private String[] options;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_drawer_layout);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);

        frameLayout = (FrameLayout) findViewById(R.id.contentFrame);

        leftDrawer = (LinearLayout) findViewById(R.id.leftDrawer);

        final ListView drawerList = (ListView) leftDrawer.findViewById(R.id.optionsList);

        initializeOptions();

        drawerList.setAdapter(new ArrayAdapter<>(this, R.layout.drawer_list_item, options));

        drawerList.setOnItemClickListener(new OnOptionClickListener());

        channelRepository = new ChannelRepository(getApplication());
    }

    @Override
    protected void onResume() {
        super.onResume();

        final ListView channelsList = (ListView) leftDrawer.findViewById(R.id.channelsOptions);

        drawerChannelsAdapter = new DrawerChannelsAdapter(this, new ArrayList<>());

        channelsList.setAdapter(drawerChannelsAdapter);

        channelsList.setOnItemClickListener(new OnChannelClickListener());

        channelRepository.getAllChannels()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    feedChannel -> drawerChannelsAdapter.addChannel(feedChannel),
                    e -> Toaster.makeShortToast(this, e.getMessage())
                );

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);
        final MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.options_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        super.onOptionsItemSelected(item);
        final int itemId = item.getItemId();

        if (itemId == R.id.settingsAction) {
            SettingsActivity.start(this);
        }

        return true;
    }

    protected FrameLayout getFrameLayout() {
        return frameLayout;
    }

    private void initializeOptions() {
        options = getResources().getStringArray(R.array.navigation_drawer_options);
    }
}
