package sanzhiev.rssfeed.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;

public class IntentFilterActivity extends Activity {
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent callingIntent = getIntent();

        final Intent channelsActivityIntent = new Intent(this, ChannelsActivity.class);

        final String action = callingIntent.getAction();
        final Uri data = callingIntent.getData();

        final Intent addChannelActivityIntent = new Intent(this, AddChannelActivity.class);

        addChannelActivityIntent.setAction(action)
                                .setData(data);

        TaskStackBuilder.create(this)
                        .addNextIntent(channelsActivityIntent)
                        .addNextIntent(addChannelActivityIntent)
                        .startActivities();

        finish();
    }
}
