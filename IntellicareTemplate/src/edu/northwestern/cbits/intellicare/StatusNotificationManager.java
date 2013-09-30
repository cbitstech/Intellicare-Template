package edu.northwestern.cbits.intellicare;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

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
	
	@SuppressLint("NewApi")
	public void notifyBigText(int appId, int icon, String title, String message, PendingIntent intent)
	{
		Notification note = null;
				
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
		{
			Notification.Builder builder = new Notification.Builder(this._context);
			
			builder.setContentIntent(intent);
			builder.setContentTitle(title);
			builder.setContentText(message);
			builder.setSmallIcon(icon);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
				builder.setStyle(new Notification.BigTextStyle().bigText(message));

			builder.setTicker(message);

			note = builder.build();

		}
		else
		{
			NotificationCompat.Builder builder = new NotificationCompat.Builder(this._context);

			builder.setContentIntent(intent);
			builder.setContentTitle(title);
			builder.setContentText(message);
			builder.setSmallIcon(icon);

			note = builder.build();
		}
		
		NotificationManager manager = (NotificationManager) this._context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		note.flags = Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.FLAG_AUTO_CANCEL;
		
		manager.notify(appId, note);
	}
}
