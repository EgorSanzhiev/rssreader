package sanzhiev.rssfeed.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import lombok.NonNull;
import sanzhiev.rssfeed.R;
import sanzhiev.rssfeed.services.ChannelService;

public final class AlarmHelper {
    private static final String DEBUG_TAG = "AlarmHelper";
    private final AlarmManager alarmManager;
    private long updateFrequencyInMillis;

    public AlarmHelper(@NonNull final Context context) {
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            final SharedPreferences preferences = PreferenceManager
                    .getDefaultSharedPreferences(context);

            final String updateFrequencyPreferenceKey = context.getString
                    (R.string.settings_activity_update_frequency_pref_key);

            final String defaultUpdateFrequency = context.getString
                    (R.string.settings_activity_default_update_frequency);

            final String updateFrequencyPreference = preferences.getString
                    (updateFrequencyPreferenceKey, defaultUpdateFrequency);

            final int secondsInMinute = 60;
            final int millisInSecond = 1000;

            updateFrequencyInMillis = Long.parseLong(updateFrequencyPreference)
                    * secondsInMinute * millisInSecond;
        }
    }

    public void setUpdatingAlarm(@NonNull final Context context) {
        if (alarmManager != null) {
            PendingIntent pendingIntent = ChannelService.getPendingIntent
                    (context, PendingIntent.FLAG_NO_CREATE);

            if (pendingIntent == null && updateFrequencyInMillis != 0) {
                pendingIntent = ChannelService.getPendingIntent(context, 0);

                alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                                 SystemClock.elapsedRealtime() + 500,
                                                 updateFrequencyInMillis,
                                                 pendingIntent);
            }

            Log.d(DEBUG_TAG, String.valueOf(updateFrequencyInMillis));
        }
    }

    public void resetUpdatingAlarm(@NonNull final Context context) {
        if (alarmManager != null) {
            final PendingIntent pendingIntent = ChannelService.getPendingIntent(context, 0);

            if (updateFrequencyInMillis == 0) {
                alarmManager.cancel(pendingIntent);
            } else {
                alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                                 SystemClock.elapsedRealtime() + 500,
                                                 updateFrequencyInMillis,
                                                 pendingIntent);
            }

            Log.d(DEBUG_TAG, String.valueOf(updateFrequencyInMillis));
        }
    }
}
