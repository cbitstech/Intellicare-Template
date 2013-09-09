package edu.northwestern.cbits.intellicare;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;

public class StatusNotificationManager 
{
	private static StatusNotificationManager _sharedInstance = null;
	
	private Context _context = null;
	
	public StatusNotificationManager(Context context) 
	{
		this._context = context;
	}

	public static StatusNotificationManager getInstance(Context context)
	{
		if (StatusNotificationManager._sharedInstance != null)
			return StatusNotificationManager._sharedInstance;
		
		if (context != null)
			StatusNotificationManager._sharedInstance = new StatusNotificationManager(context.getApplicationContext());
		
		return StatusNotificationManager._sharedInstance;
	}
	
	public void notifyBigText(int appId, int icon, String title, String message, PendingIntent intent)
	{
		Notification.Builder builder = new Notification.Builder(this._context);
		builder.setContentIntent(intent);
		builder.setContentTitle(title);
		builder.setContentText(message);
		builder.setSmallIcon(icon);
		builder.setStyle(new Notification.BigTextStyle().bigText(message));
		
		NotificationManager manager = (NotificationManager) this._context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		manager.notify(appId, builder.build());
	}
}
