package edu.northwestern.cbits.intellicare.ruminants;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;


import java.util.Calendar;

import edu.northwestern.cbits.intellicare.StatusNotificationManager;

/**
 * Created by Gwen on 3/14/14.
 */
public class ScheduleManager {
//    public static final String REMINDER_HOUR = "preferred_hour";
//    public static final String REMINDER_MINUTE = "preferred_minutes";

    public static final int DEFAULT_PROFILE_HOUR = 16;
    public static final int DEFAULT_PROFILE_MINUTE = 35;

//    private static final String LAST_PROFILE_NOTIFICATION = "last_notification";
//    private static final String LAST_PROFILE_NOTIFICATION = "last_notification";

    private static final int HELPER_NOTIFICATION_ID = 1234567;
    private static final int PROFILE_NOTIFICATION_ID = 1234568;

    private static ScheduleManager _instance = null;

    private Context _context = null;

    public ScheduleManager(Context context)
    {
        this._context  = context.getApplicationContext();

        AlarmManager alarm = (AlarmManager) this._context.getSystemService(Context.ALARM_SERVICE);

        Intent broadcast = new Intent(this._context, ScheduleHelper.class);
        PendingIntent pi = PendingIntent.getBroadcast(this._context, 0, broadcast, PendingIntent.FLAG_UPDATE_CURRENT);

        alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 0, 15000, pi);
    }

    public static ScheduleManager getInstance(Context context)
    {
        if (ScheduleManager._instance == null)
        {
            ScheduleManager._instance = new ScheduleManager(context.getApplicationContext());
            ScheduleManager._instance.updateSchedule();
        }

        return ScheduleManager._instance;
    }

    public void updateSchedule()
    {
        long now = System.currentTimeMillis();

//        int hour = prefs.getInt(ScheduleManager.REMINDER_HOUR, ScheduleManager.DEFAULT_HOUR);
//        int minutes = prefs.getInt(ScheduleManager.REMINDER_MINUTE, ScheduleManager.DEFAULT_MINUTE);

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(now);
        // c.set(Calendar.HOUR_OF_DAY, hour);
        // c.set(Calendar.MINUTE, minutes);
        // c.set(Calendar.SECOND, 0);
        // c.set(Calendar.MILLISECOND, 0);

        long scheduled = c.getTimeInMillis();

        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int day = c.get(Calendar.DAY_OF_WEEK);

        // Profile notification...
        if (hour == ScheduleManager.DEFAULT_PROFILE_HOUR && minute == ScheduleManager.DEFAULT_PROFILE_MINUTE &&
                Calendar.MONDAY == day)
        {
            Intent intent = new Intent(this._context, ProfileActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this._context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this._context);
            builder.setContentIntent(pendingIntent);
            builder.setAutoCancel(true);
            builder.setContentTitle(this._context.getString(R.string.profile_note_title));
            builder.setContentText(this._context.getString(R.string.profile_note_message));
            builder.setTicker(this._context.getString(R.string.profile_note_message));
            builder.setSmallIcon(R.drawable.ic_action_chat);

            Notification note = builder.build();

            NotificationManager noteManager = (NotificationManager) this._context.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
            noteManager.notify(ScheduleManager.PROFILE_NOTIFICATION_ID, note);
       }

        int helpFrequency = 0;

        String[] columns = { "help_frequency"};

        Cursor k = this._context.getContentResolver().query(RuminantsContentProvider.PROFILE_URI, columns, null, null, "_id DESC");

        if (k.moveToNext())
            helpFrequency = k.getInt(k.getColumnIndex("help_frequency"));

        k.close();

        boolean timeToFire = false;

        if ((helpFrequency == 1) && (hour == 9 && minute == 0)) {
            timeToFire = true;
        }
        else if ((helpFrequency == 2) && ((hour == 9 && minute == 0|| hour == 13 && minute == 0))) {
            timeToFire = true;
        }
        else if  ((helpFrequency == 3) && ((hour == 9 && minute == 0 || hour == 13 && minute == 0 || hour == 18 && minute == 0))) {
            timeToFire = true;
        }
        /*Testing below
        else if ((hour == 14 && minute == 52)) {
            timeToFire = true;
        } */

        // Helper notification...
        if (timeToFire)
        {
            Intent intent = new Intent(this._context, ToolChooserActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this._context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

            String title = this._context.getString(R.string.help_note_title);
            String message = this._context.getString(R.string.help_note_message);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this._context);
            builder.setContentIntent(pendingIntent);
            builder.setAutoCancel(true);
            builder.setContentTitle(title);
            builder.setContentText(message);
            builder.setTicker(this._context.getString(R.string.help_note_message));
            builder.setSmallIcon(R.drawable.ic_action_star);

            Notification note = builder.build();

            NotificationManager noteManager = (NotificationManager) this._context.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
            noteManager.notify(ScheduleManager.HELPER_NOTIFICATION_ID, note);

            Uri u = Uri.parse("intellicare://ruminants/tool-chooser");

            StatusNotificationManager.getInstance(this._context).notifyBigText(ScheduleManager.HELPER_NOTIFICATION_ID, R.drawable.ic_action_star, title, message, pendingIntent, u);
        }
    }
}
