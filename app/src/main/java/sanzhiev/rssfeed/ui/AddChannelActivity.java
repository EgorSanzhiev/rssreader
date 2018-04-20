package sanzhiev.rssfeed.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import sanzhiev.rssfeed.R;
import sanzhiev.rssfeed.networking.ChannelUpdater;

public final class AddChannelActivity extends BaseActivity {
    private ChannelUpdater channelUpdater;
    public static void start(final Context context) {
        final Intent startIntent = new Intent(context, AddChannelActivity.class);

        context.startActivity(startIntent);
    }

    private final static String PREFERENCE_USER_INPUT_KEY =
            "sanzhiev.rssfeed.ui.AddChannelActivity.PREFERENCE_USER_INPUT_KEY";
    private final static String PREFERENCE_CURSOR_POSITION_KEY =
            "sanzhiev.rssfeed.ui.AddChannelActivity.PREFERENCE_CURSOR_POSITION_KEY";
    private EditText urlField;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        channelUpdater = new ChannelUpdater(getApplication());

        initializeViews();
    }

    @Override
    protected void onResume() {
        super.onResume();

        final Intent startIntent = getIntent();

        final String action = startIntent.getAction();

        if (action != null && action.equals(Intent.ACTION_VIEW)) {
            receiveChannelUrlFromBrowser(startIntent);
        } else {
            restoreUserInput();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        saveUserInput();
    }

    private void initializeViews() {
        final LayoutInflater inflater = getLayoutInflater();

        final FrameLayout contentLayout = getFrameLayout();

        final View contentView = inflater.inflate(R.layout.add_channel_activity, contentLayout, false);

        contentLayout.addView(contentView);

        urlField = (EditText) findViewById(R.id.urlField);

        final Button subscribeButton = (Button) findViewById(R.id.subscribeButton);

        subscribeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final String url = urlField.getText().toString();

                channelUpdater.saveChannel(url).subscribeOn(Schedulers.io()).subscribe();

                finish();
            }
        });

        setTitle(getString(R.string.add_channel_activity_title));
    }

    private void receiveChannelUrlFromBrowser(final Intent intent) {
        final Uri uri = intent.getData();

        if (uri != null) {
            final String channelUrl = uri.toString();

            urlField.setText(channelUrl);
        }
    }

    private void saveUserInput() {
        final SharedPreferences preferences = getPreferences(MODE_PRIVATE);

        final SharedPreferences.Editor preferencesEditor = preferences.edit();

        final String userInput = urlField.getText().toString();

        final int cursorPosition = urlField.getSelectionEnd();

        preferencesEditor.putString(PREFERENCE_USER_INPUT_KEY, userInput);

        preferencesEditor.putInt(PREFERENCE_CURSOR_POSITION_KEY, cursorPosition);

        preferencesEditor.apply();
    }

    private void restoreUserInput() {
        final SharedPreferences preferences = getPreferences(MODE_PRIVATE);

        final String defaultText = "";

        final String userInput = preferences.getString(PREFERENCE_USER_INPUT_KEY, defaultText);

        final int defaultCursorPosition = 0;

        final int cursorPosition = preferences.getInt(PREFERENCE_CURSOR_POSITION_KEY,
                                                      defaultCursorPosition);

        urlField.setText(userInput);
        urlField.setSelection(cursorPosition);
    }
}
