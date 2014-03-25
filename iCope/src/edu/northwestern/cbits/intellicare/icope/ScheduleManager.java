package edu.northwestern.cbits.intellicare.icope;

import java.util.Calendar;
import java.util.HashMap;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import edu.northwestern.cbits.intellicare.StatusNotificationManager;

public class ScheduleManager
{
	private static ScheduleManager _instance = null;

	private Context _context = null;
	
	private HashMap<String, Long> _lastFires = new HashMap<String, Long>();

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
		Calendar c = Calendar.getInstance();
		
		String where = CopeContentProvider.REMINDER_HOUR + " = ? AND " + 
					   CopeContentProvider.REMINDER_MINUTE + " = ?"; 
		
		String[] args = { "" + c.get(Calendar.HOUR_OF_DAY), "" + c.get(Calendar.MINUTE) }; 
		
		Cursor cursor = this._context.getContentResolver().query(CopeContentProvider.REMINDER_URI, null, where, args, null);
		
		String title = this._context.getString(R.string.title_card_reminder);
		
		long now = System.currentTimeMillis();

		while (cursor.moveToNext())
		{
			long reminderId = cursor.getLong(cursor.getColumnIndex(CopeContentProvider.ID));
			long cardId = cursor.getLong(cursor.getColumnIndex(CopeContentProvider.REMINDER_CARD_ID));
			
			boolean today = false;
			
			switch (c.get(Calendar.DAY_OF_WEEK))
			{
				case Calendar.SUNDAY:
					if (cursor.getInt(cursor.getColumnIndex(CopeContentProvider.REMINDER_SUNDAY)) != 0)
						today = true;
					
					break;
				case Calendar.MONDAY:
					if (cursor.getInt(cursor.getColumnIndex(CopeContentProvider.REMINDER_MONDAY)) != 0)
						today = true;
					
					break;
				case Calendar.TUESDAY:
					if (cursor.getInt(cursor.getColumnIndex(CopeContentProvider.REMINDER_TUESDAY)) != 0)
						today = true;
					
					break;
				case Calendar.WEDNESDAY:
					if (cursor.getInt(cursor.getColumnIndex(CopeContentProvider.REMINDER_WEDNESDAY)) != 0)
						today = true;
					
					break;
				case Calendar.THURSDAY:
					if (cursor.getInt(cursor.getColumnIndex(CopeContentProvider.REMINDER_THURSDAY)) != 0)
						today = true;
					
					break;
				case Calendar.FRIDAY:
					if (cursor.getInt(cursor.getColumnIndex(CopeContentProvider.REMINDER_FRIDAY)) != 0)
						today = true;
					
					break;
				case Calendar.SATURDAY:
					if (cursor.getInt(cursor.getColumnIndex(CopeContentProvider.REMINDER_SATURDAY)) != 0)
						today = true;
					
					break;
			}
			
			if (today)
			{
				String cardWhere = CopeContentProvider.ID + " = ?";
				String[] cardArgs = { "" + cardId };
			
				Cursor cardCursor = this._context.getContentResolver().query(CopeContentProvider.CARD_URI, null, cardWhere, cardArgs, null);
				
				if (cardCursor.moveToNext())
				{
					String message = cardCursor.getString(cardCursor.getColumnIndex(CopeContentProvider.CARD_REMINDER));
					
					Long lastShown = this._lastFires.get(message);
					
					if (lastShown == null)
						lastShown = Long.valueOf(0);
					
					if (now - lastShown > 60000)
					{
						Intent intent = new Intent(this._context, ViewCardActivity.class);
						intent.putExtra(ViewCardActivity.REMINDER_ID, reminderId);
						
						PendingIntent pi = PendingIntent.getActivity(this._context, 0, intent, 0);
						Uri u = Uri.withAppendedPath(ViewCardActivity.URI, "" + reminderId);
						
						StatusNotificationManager.getInstance(this._context).notifyBigText(97521, R.drawable.ic_notification, title, message, pi, u);
						
						this._lastFires.put(message, now);
					}
				}
				
				cardCursor.close();
			}
		}
		
		cursor.close();
	}
}
