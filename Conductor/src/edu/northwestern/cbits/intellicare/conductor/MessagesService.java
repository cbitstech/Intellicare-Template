package edu.northwestern.cbits.intellicare.conductor;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import edu.northwestern.cbits.intellicare.StatusNotificationManager;

public class MessagesService extends IntentService 
{
	public static final String REFRESH_MESSAGES = "conductor_refresh_messages";
	private static final int APP_ID = 63847582;

	private static PendingIntent timedIntent = null;
	private static long lastRefresh = 0;
	
	public MessagesService(String name) 
	{
		super(name);
	}

	public MessagesService() 
	{
		super("Intellicare Messaging Service");
	}

	@SuppressLint("NewApi")
	protected void onHandleIntent(Intent intent) 
	{
		if (MessagesService.timedIntent == null)
		{
			AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
			
			Intent broadcast = new Intent(MessagesService.REFRESH_MESSAGES);
			MessagesService.timedIntent = PendingIntent.getService(this, 0, broadcast, PendingIntent.FLAG_UPDATE_CURRENT);
			
			long now = System.currentTimeMillis();

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
				alarm.setExact(AlarmManager.RTC_WAKEUP, now + 60000, MessagesService.timedIntent);
			else
				alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 0, 60000, MessagesService.timedIntent);
		}
		
		String action = intent.getAction();
		
		if (StatusNotificationManager.LOG_MESSAGE.equals(action))
		{
			String packageName = intent.getStringExtra(StatusNotificationManager.PACKAGE);

			ContentValues values = new ContentValues();
			values.put("package", packageName);

			String selection = "package = ?";
			String[] args = { packageName };
			
			Cursor c = this.getContentResolver().query(ConductorContentProvider.APPS_URI, null, selection, args, null);
			
			while (c.moveToNext())
				values.put("name", c.getString(c.getColumnIndex("name")));
			
			c.close();
			
			values.put("message", intent.getStringExtra(StatusNotificationManager.MESSAGE));
			values.put("uri", intent.getStringExtra(StatusNotificationManager.URI));
			values.put("date", System.currentTimeMillis());
			
			this.getContentResolver().insert(ConductorContentProvider.MESSAGES_URI, values);
			
			Intent refreshIntent = new Intent(MessagesService.REFRESH_MESSAGES);
			this.startService(refreshIntent);
		}
		else if (MessagesService.REFRESH_MESSAGES.equals(action))
		{
			long now = System.currentTimeMillis();
			
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && now - MessagesService.lastRefresh > 45000)
			{
				AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
				
				alarm.setExact(AlarmManager.RTC_WAKEUP, now + 60000, MessagesService.timedIntent);
			}
			
			NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

	        String selection = "responded = 0";
	        String sortOrder = "date DESC";

	        Cursor c = this.getContentResolver().query(ConductorContentProvider.MESSAGES_URI, null, selection, null, sortOrder);
			
			if (c.getCount() > 0)
			{
				Notification note = null;
				PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
			
		        int title = R.string.note_new_messages_title;
			
		        String message = this.getString(R.string.note_new_messages_desc, c.getCount());
		        
				if (c.getCount() == 1)
				{
			        title = R.string.note_new_message_title;
			        message = this.getString(R.string.note_new_message_desc);
				}

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
				{
					Notification.Builder builder = new Notification.Builder(this);
					
					builder.setContentIntent(pi);
					builder.setContentTitle(this.getString(title));
					builder.setContentText(message);
					builder.setSmallIcon(R.drawable.ic_stat_messages);
	
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
					{
						Notification.InboxStyle style = new Notification.InboxStyle();
						
						style = style.setBigContentTitle(this.getString(title));
						style = style.setSummaryText(message);
						
						int count = 0;
						
						while (c.moveToNext() && count < 5)
						{
			                String line = c.getString(c.getColumnIndex("message"));
			                String appName = c.getString(c.getColumnIndex("name"));
						
							style = style.addLine(appName + ": " + line);
							
							count += 1;
						}
						
						builder.setStyle(style);
					}
	
					builder.setTicker(message);
	
					note = builder.build();
				}
				else
				{
					NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
	
					builder.setContentIntent(pi);
					builder.setContentTitle(this.getString(title));
					builder.setContentText(message);
					builder.setSmallIcon(R.drawable.ic_stat_messages);
	
					note = builder.build();
				}
	
				note.flags = Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.FLAG_NO_CLEAR;
	
				manager.notify(MessagesService.APP_ID, note);
			}
			else
				manager.cancel(MessagesService.APP_ID);
			
			c.close();
			
			MessagesService.lastRefresh = now;
		}
	}
}
