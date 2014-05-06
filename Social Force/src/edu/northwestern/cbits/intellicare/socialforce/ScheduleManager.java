package edu.northwestern.cbits.intellicare.socialforce;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
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
		
		alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 0, 60000, pi);
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
		long now = System.currentTimeMillis() - (15 * 60000);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this._context);
		
		String selection = CalendarContract.Events.DESCRIPTION + " LIKE ? AND " + CalendarContract.Events.DTEND + " < ?";
		String[] args = { "%" + this._context.getString(R.string.suffix_via_social_force) + "%", "" + now };

		Cursor c = this._context.getContentResolver().query(CalendarContract.Events.CONTENT_URI, null, selection, args, null);
		
		while (c.moveToNext())
		{
			String title = this._context.getString(R.string.app_name);
			
			String eventTitle = c.getString(c.getColumnIndex(CalendarContract.Events.TITLE));
			
			long id = c.getLong(c.getColumnIndex(CalendarContract.Events._ID));
			
			String key = "notified_event_id_" + id;
			
			if (prefs.getBoolean(key, false) == false)
			{
				String attSelection = CalendarContract.Attendees.EVENT_ID + " = ?";
				String[] attArgs = { "" + id };
				
				Cursor attendees = this._context.getContentResolver().query(CalendarContract.Attendees.CONTENT_URI, null, attSelection, attArgs, null);
				
				StringBuffer sb = new StringBuffer();
				
				while (attendees.moveToNext())
				{
					if (sb.length() > 0)
						sb.append(", ");
					
					sb.append(attendees.getString(attendees.getColumnIndex(CalendarContract.Attendees.ATTENDEE_NAME)));
				}
				
				attendees.close();
				
				Uri eventUri = EventReviewActivity.uriForEvent(id);
				
				Intent intent = new Intent(this._context, EventReviewActivity.class);
				intent.putExtra(EventReviewActivity.EVENT_ID, id);
				
				PendingIntent pi = PendingIntent.getActivity(this._context, 0, intent, 0);
				
				String message = this._context.getString(R.string.note_message_event, eventTitle);
				
				StatusNotificationManager.getInstance(this._context).notifyBigText(97521, R.drawable.ic_notification, title, message, pi, eventUri);

				Editor e = prefs.edit();
				e.putBoolean(key, true);
				e.commit();
			}
		}
		
		c.close();
	}
}
