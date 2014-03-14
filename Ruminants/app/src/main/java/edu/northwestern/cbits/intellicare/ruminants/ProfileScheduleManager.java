package edu.northwestern.cbits.intellicare.ruminants;

/**
 * Created by Gwen on 3/14/14.
 */
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
//import edu.northwestern.cbits.intellicare.StatusNotificationManager;
//import edu.northwestern.cbits.intellicare.logging.LogManager;

public class ProfileScheduleManager
{
    public static final String REMINDER_HOUR = "preferred_hour";
    public static final String REMINDER_MINUTE = "preferred_minutes";
    public static final int DEFAULT_HOUR = 18;
    public static final int DEFAULT_MINUTE = 0;
    private static final String LAST_NOTIFICATION = "last_notification";

    private static ProfileScheduleManager _instance = null;

    private Context _context = null;

    public ProfileScheduleManager(Context context)
    {
        this._context  = context.getApplicationContext();

        AlarmManager alarm = (AlarmManager) this._context.getSystemService(Context.ALARM_SERVICE);

        Intent broadcast = new Intent(this._context, ProfileScheduleHelper.class);
        PendingIntent pi = PendingIntent.getBroadcast(this._context, 0, broadcast, PendingIntent.FLAG_UPDATE_CURRENT);

        alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 0, 60000, pi);
    }

    public static ProfileScheduleManager getInstance(Context context)
    {
        if (ProfileScheduleManager._instance == null)
        {
            ProfileScheduleManager._instance = new ProfileScheduleManager(context.getApplicationContext());
            ProfileScheduleManager._instance.updateSchedule();
        }

        return ProfileScheduleManager._instance;
    }

    public void updateSchedule()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this._context);

        long lastNotification = prefs.getLong(ProfileScheduleManager.LAST_NOTIFICATION, 0);
        long now = System.currentTimeMillis();

        int hour = prefs.getInt(ProfileScheduleManager.REMINDER_HOUR, ProfileScheduleManager.DEFAULT_HOUR);
        int minutes = prefs.getInt(ProfileScheduleManager.REMINDER_MINUTE, ProfileScheduleManager.DEFAULT_MINUTE);

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(now);
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minutes);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        long scheduled = c.getTimeInMillis();

        if (lastNotification < scheduled && now > scheduled && lastNotification < now - 604800000)
        {
            String title = this._context.getString(R.string.profile_note_title);
            String message = this._context.getString(R.string.profile_note_message);

            Intent intent = new Intent(this._context, ProfileActivity.class);

            PendingIntent pi = PendingIntent.getActivity(this._context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

            Editor e = prefs.edit();
            e.putLong(ProfileScheduleManager.LAST_NOTIFICATION, now);
            e.commit();
        }
    }
}