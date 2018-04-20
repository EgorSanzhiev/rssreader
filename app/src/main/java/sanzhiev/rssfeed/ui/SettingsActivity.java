package sanzhiev.rssfeed.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import lombok.NonNull;
import sanzhiev.rssfeed.R;
import sanzhiev.rssfeed.alarm.AlarmHelper;

public final class SettingsActivity extends PreferenceActivity {
    private SharedPreferences preferences;
    private final OnSharedPreferenceChangeListener onUpdateFrequencyChangeListener =
            new OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences,
                                                      final String key) {
                    final String updateFrequencyKey = getString
                            (R.string.settings_activity_update_frequency_pref_key);

                    if (key.equals(updateFrequencyKey)) {
                        new AlarmHelper(SettingsActivity.this)
                                .resetUpdatingAlarm(SettingsActivity.this);
                    }
                }
            };

    static void start(@NonNull final Context context) {
        final Intent startIntent = new Intent(context, SettingsActivity.class);

        context.startActivity(startIntent);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    protected void onResume() {
        super.onResume();

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        preferences.registerOnSharedPreferenceChangeListener(onUpdateFrequencyChangeListener);
    }

    @Override
    protected void onPause() {
        super.onPause();

        preferences.unregisterOnSharedPreferenceChangeListener(onUpdateFrequencyChangeListener);
    }
}
