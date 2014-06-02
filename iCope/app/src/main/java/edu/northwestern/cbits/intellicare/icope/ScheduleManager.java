package edu.northwestern.cbits.intellicare.icope;

import java.util.Calendar;
import java.util.HashMap;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;

import edu.northwestern.cbits.intellicare.StatusNotificationManager;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class ScheduleManager
{
	private static ScheduleManager _instance = null;

	private Context _context = null;

    private static final int TRAY_NOTIFICATION_ID = 1234567;

    private long _lastNote = 0;

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
        long now = System.currentTimeMillis();

        if (now - this._lastNote < 60000)
            return;

        this._lastNote = now;

		Calendar c = Calendar.getInstance();

        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
		
		String where = CopeContentProvider.REMINDER_HOUR + " = ? AND " + 
					   CopeContentProvider.REMINDER_MINUTE + " = ?"; 
		
		String[] args = { "" + c.get(Calendar.HOUR_OF_DAY), "" + c.get(Calendar.MINUTE) }; 
		
		Cursor cursor = this._context.getContentResolver().query(CopeContentProvider.REMINDER_URI, null, where, args, null);
		
		String title = this._context.getString(R.string.title_card_reminder);

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

            boolean timeToFire = false;

            int reminderHour = (cursor.getInt(cursor.getColumnIndex(CopeContentProvider.REMINDER_HOUR)));
            int reminderMinute = (cursor.getInt(cursor.getColumnIndex(CopeContentProvider.REMINDER_MINUTE)));

            if (hour == reminderHour && minute == reminderMinute) {
                timeToFire = true;
            }
            else  {
                timeToFire = false;
            };

            if (timeToFire) {
                Intent intent = new Intent(this._context, MainActivity.class);
                intent.putExtra(ViewCardActivity.REMINDER_ID, reminderId);
                PendingIntent pendingIntent = PendingIntent.getActivity(this._context, 0, intent, 0);

                String noteWhere = CopeContentProvider.ID + "= ?";
                String[] noteArgs = {"" + cardId};

                Cursor noteCursor = _context.getContentResolver().query(CopeContentProvider.CARD_URI, null, noteWhere, noteArgs, null);

                if (noteCursor.moveToNext()) {

                    String event = (noteCursor.getString(noteCursor.getColumnIndex(CopeContentProvider.CARD_EVENT)));
                    String reminder = (noteCursor.getString(noteCursor.getColumnIndex(CopeContentProvider.CARD_REMINDER)));

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this._context);
                    builder =  builder.setStyle(new NotificationCompat.BigTextStyle()
                               .bigText(reminder));
                    builder = builder.setContentIntent(pendingIntent);
                    builder = builder.setContentTitle(event);
                    builder = builder.setContentText(reminder);
                    builder = builder.setTicker(reminder);
                    builder =  builder.setSmallIcon(R.drawable.ic_cope_cloud);

                    Notification note = builder.build();
                    note.flags = note.flags | Notification.FLAG_AUTO_CANCEL;

                    // note.flags = Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.PRIORITY_HIGH;

                    NotificationManager noteManager = (NotificationManager) this._context.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
                    noteManager.notify(ScheduleManager.TRAY_NOTIFICATION_ID, note);

                    //StatusNotificationManager.getInstance(this._context).notifyBigText(ScheduleManager.TRAY_NOTIFICATION_ID, R.drawable.ic_action_process_start, event, reminder, pendingIntent, null);
                }
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
						
						//StatusNotificationManager.getInstance(this._context).notifyBigText(97521, R.drawable.ic_notification, title, message, pi, u);
						
						this._lastFires.put(message, now);
					}
					
					HashMap<String, Object> payload = new HashMap<String, Object>();
					payload.put("message", message);
					LogManager.getInstance(this._context).log("fired_reminder", payload);
				}
				
				cardCursor.close();
			}
		}
		
		cursor.close();
	}
}
