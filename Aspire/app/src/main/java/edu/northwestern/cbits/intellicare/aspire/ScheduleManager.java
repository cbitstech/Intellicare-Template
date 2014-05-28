package edu.northwestern.cbits.intellicare.aspire;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.preference.PreferenceManager;
import edu.northwestern.cbits.intellicare.StatusNotificationManager;

public class ScheduleManager
{
	private static ScheduleManager _instance = null;

	private Context _context = null;

	public ScheduleManager(Context context) 
	{
		this._context  = context.getApplicationContext();
		
		AlarmManager alarm = (AlarmManager) this._context.getSystemService(Context.ALARM_SERVICE);
		
		Intent broadcast = new Intent(this._context, ScheduleHelper.class);
		PendingIntent pi = PendingIntent.getBroadcast(this._context, 0, broadcast, PendingIntent.FLAG_UPDATE_CURRENT);
		
		alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 0, 30000, pi);
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
		
		Calendar c = Calendar.getInstance();

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this._context);
		
		int reminderHour = prefs.getInt(SettingsActivity.REMINDER_HOUR, 9);
		int reminderMinute = prefs.getInt(SettingsActivity.REMINDER_MINUTE, 0);
		long lastReminder = prefs.getLong(SettingsActivity.LAST_REMINDER, 0);
		
		if (now - lastReminder > 60000 && c.get(Calendar.HOUR_OF_DAY) == reminderHour && c.get(Calendar.MINUTE) == reminderMinute)
		{
			boolean remindNow = false;
			
			switch (c.get(Calendar.DAY_OF_WEEK))
			{
				case Calendar.SUNDAY:
					remindNow = prefs.getBoolean(SettingsActivity.REMINDER_SUNDAY, false);
					break;
				case Calendar.MONDAY:
					remindNow = prefs.getBoolean(SettingsActivity.REMINDER_MONDAY, true);
					break;
				case Calendar.TUESDAY:
					remindNow = prefs.getBoolean(SettingsActivity.REMINDER_TUESDAY, true);
					break;
				case Calendar.WEDNESDAY:
					remindNow = prefs.getBoolean(SettingsActivity.REMINDER_WEDNESDAY, true);
					break;
				case Calendar.THURSDAY:
					remindNow = prefs.getBoolean(SettingsActivity.REMINDER_THURSDAY, true);
					break;
				case Calendar.FRIDAY:
					remindNow = prefs.getBoolean(SettingsActivity.REMINDER_FRIDAY, true);
					break;
				case Calendar.SATURDAY:
					remindNow = prefs.getBoolean(SettingsActivity.REMINDER_SATURDAY, false);
					break;
			}
			
			if (remindNow)
			{
				String title = this._context.getString(R.string.title_reminder_note);
				String message = this._context.getString(R.string.message_reminder_note);
						
				Intent intent = new Intent(this._context, MainActivity.class);
				PendingIntent pi = PendingIntent.getActivity(this._context, 0, intent, 0);
				
				StatusNotificationManager.getInstance(this._context).notifyBigText(97534, R.drawable.ic_aspire_note, title, message, pi, MainActivity.URI);
				
				Editor e = prefs.edit();
				
				e.putLong(SettingsActivity.LAST_REMINDER, now);
				e.commit();
			}
		}

		int graphHour = prefs.getInt(SettingsActivity.GRAPH_HOUR, 9);
		int graphMinute = prefs.getInt(SettingsActivity.GRAPH_MINUTE, 0);
		long lastGraph = prefs.getLong(SettingsActivity.LAST_GRAPH, 0);
		
		if (now - lastGraph > 60000 && c.get(Calendar.HOUR_OF_DAY) == graphHour && c.get(Calendar.MINUTE) == graphMinute)
		{
			boolean remindNow = false;
			
			switch (c.get(Calendar.DAY_OF_WEEK))
			{
				case Calendar.SUNDAY:
					remindNow = prefs.getBoolean(SettingsActivity.GRAPH_SUNDAY, false);
					break;
				case Calendar.MONDAY:
					remindNow = prefs.getBoolean(SettingsActivity.GRAPH_MONDAY, true);
					break;
				case Calendar.TUESDAY:
					remindNow = prefs.getBoolean(SettingsActivity.GRAPH_TUESDAY, true);
					break;
				case Calendar.WEDNESDAY:
					remindNow = prefs.getBoolean(SettingsActivity.GRAPH_WEDNESDAY, true);
					break;
				case Calendar.THURSDAY:
					remindNow = prefs.getBoolean(SettingsActivity.GRAPH_THURSDAY, true);
					break;
				case Calendar.FRIDAY:
					remindNow = prefs.getBoolean(SettingsActivity.GRAPH_FRIDAY, true);
					break;
				case Calendar.SATURDAY:
					remindNow = prefs.getBoolean(SettingsActivity.GRAPH_SATURDAY, false);
					break;
			}
			
			if (remindNow)
			{
				String title = this._context.getString(R.string.title_graph_note);
				
				String selection = AspireContentProvider.TASK_DAY + " = ? AND " + AspireContentProvider.TASK_MONTH + " = ? AND " +
								   AspireContentProvider.TASK_YEAR + " = ?";
				
				String[] args = { "" + c.get(Calendar.DAY_OF_MONTH), "" +  c.get(Calendar.MONTH), "" +  c.get(Calendar.YEAR) };

				Cursor cursor = this._context.getContentResolver().query(AspireContentProvider.ASPIRE_TASK_URI, null, selection, args, null);
				
				int count = cursor.getCount();
				
				cursor.close();
				
				String message = this._context.getString(R.string.message_graph_note_single);
				
				if (count != 1)
					message = this._context.getString(R.string.message_graph_note, count);
					

				Intent intent = new Intent(this._context, GraphActivity.class);
				PendingIntent pi = PendingIntent.getActivity(this._context, 0, intent, 0);
				
				StatusNotificationManager.getInstance(this._context).notifyBigText(97534, R.drawable.ic_aspire_note, title, message, pi, GraphActivity.URI);
				
				Editor e = prefs.edit();
				
				e.putLong(SettingsActivity.LAST_GRAPH, now);
				e.commit();
			}
		}
	}
}
