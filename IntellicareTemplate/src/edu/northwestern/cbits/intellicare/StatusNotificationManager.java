package edu.northwestern.cbits.intellicare;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

public class StatusNotificationManager 
{
	private static final String CONDUCTOR_PACKAGE = "edu.northwestern.cbits.intellicare.conductor";

	public static final String LOG_MESSAGE = "conductor_log_message";
	public static final String PACKAGE = "package";
	public static final String TITLE = "title";
	public static final String MESSAGE = "message";
	public static final String URI = "uri";
	
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

	public void notifyBigText(int appId, int icon, String title, String message, PendingIntent intent, Uri uri)
	{
		this.notifyBigText(appId, icon, title, message, intent, uri, false);
	}

	@SuppressLint("NewApi")
	public void notifyBigText(int appId, int icon, String title, String message, PendingIntent intent, Uri uri, boolean persistent)
	{
		PackageManager packages = this._context.getPackageManager();
		
		try 
		{
			// Route to conductor if installed...
			
			packages.getPackageInfo(CONDUCTOR_PACKAGE, 0);
			
			Intent conductorIntent = new Intent(StatusNotificationManager.LOG_MESSAGE);
			conductorIntent.putExtra(StatusNotificationManager.PACKAGE, this._context.getApplicationContext().getPackageName());
			conductorIntent.putExtra(StatusNotificationManager.TITLE, title);
			conductorIntent.putExtra(StatusNotificationManager.MESSAGE, message);
			conductorIntent.putExtra(StatusNotificationManager.URI, uri.toString());
			
			this._context.startService(conductorIntent);
		} 
		catch (NameNotFoundException e) 
		{
			// Show native dialog if conductor is unavailable
			e.printStackTrace();
			
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
			
			note.flags = Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;
			
			if (persistent)
				note.flags = note.flags | Notification.FLAG_NO_CLEAR;
			else
				note.flags = note.flags | Notification.FLAG_AUTO_CANCEL;
			
			manager.notify(appId, note);
		}
	}
	
	public void notifyPersistentBigText(int appId, int icon, String title, String message, PendingIntent intent, Uri uri)
	{
		this.notifyBigText(appId, icon, title, message, intent, uri, true);
	}
	
	public void cancel(int noteId)
	{
		NotificationManager manager = (NotificationManager) this._context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		manager.cancel(noteId);
	}
}
