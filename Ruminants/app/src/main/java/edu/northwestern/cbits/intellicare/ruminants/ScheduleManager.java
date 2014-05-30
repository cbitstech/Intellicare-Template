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
    public static final String REMINDER_HOUR = "hour";
    public static final String REMINDER_MINUTE = "minute";

    public static final int DEFAULT_HOUR = 9;
    public static final int DEFAULT_MINUTE = 00;

    private static final int HELPER_NOTIFICATION_ID = 1234567;

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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this._context);

        int settingsHour = prefs.getInt(ScheduleManager.REMINDER_HOUR, ScheduleManager.DEFAULT_HOUR);
        int settingsMinute = prefs.getInt(ScheduleManager.REMINDER_MINUTE, ScheduleManager.DEFAULT_MINUTE);

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(now);

        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        boolean timeToFire = false;

        if (hour == settingsHour && minute == settingsMinute) {
            timeToFire = true;
        }
        else  {
            timeToFire = false;
        };

        // Helper notification...
        if (timeToFire)
        {
            Intent intent = new Intent(this._context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this._context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

            String title = this._context.getString(R.string.help_note_title);
            String message = this._context.getString(R.string.help_note_message);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this._context);
            builder.setContentIntent(pendingIntent);
            builder.setAutoCancel(true);
            builder.setContentTitle(title);
            builder.setContentText(message);
            builder.setTicker(this._context.getString(R.string.help_note_message));
            builder.setSmallIcon(R.drawable.notification);

            Notification note = builder.build();

            NotificationManager noteManager = (NotificationManager) this._context.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
            noteManager.notify(ScheduleManager.HELPER_NOTIFICATION_ID, note);

            Uri u = Uri.parse("intellicare://ruminants/main");

            StatusNotificationManager.getInstance(this._context).notifyBigText(ScheduleManager.HELPER_NOTIFICATION_ID, R.drawable.ic_action_process_start, title, message, pendingIntent, u);
        }

    }
}
