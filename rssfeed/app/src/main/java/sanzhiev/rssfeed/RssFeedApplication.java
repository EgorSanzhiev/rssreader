package sanzhiev.rssfeed;

import android.app.Application;

import sanzhiev.rssfeed.alarm.AlarmHelper;
import sanzhiev.rssfeed.database.RssFeedDbHelper;

public class RssFeedApplication extends Application {
    private RssFeedDbHelper dbHelper;

    @Override
    public void onCreate() {
        super.onCreate();

        dbHelper = new RssFeedDbHelper(this);

        final AlarmHelper alarmHelper = new AlarmHelper(this);

        alarmHelper.setUpdatingAlarm(this);
    }

    public RssFeedDbHelper getDbHelper() {
        return dbHelper;
    }
}
