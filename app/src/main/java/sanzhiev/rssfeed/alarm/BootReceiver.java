package sanzhiev.rssfeed.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import sanzhiev.rssfeed.R;

public final class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String action = intent.getAction();
        final String actionBootCompleted = context.getString(R.string.action_boot_completed);

        if (action != null && action.equals(actionBootCompleted)) {
            new AlarmHelper(context).setUpdatingAlarm(context);
        }
    }
}
