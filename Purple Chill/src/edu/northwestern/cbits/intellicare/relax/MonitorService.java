package edu.northwestern.cbits.intellicare.relax;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class MonitorService extends Service 
{
	public void onCreate() 
	{
		super.onCreate();
	}

	public int onStartCommand(Intent intent, int flags, int startId) 
	{
		return START_STICKY;
	}

	public IBinder onBind(Intent intent) 
	{
		return null;
	}

	@SuppressLint("NewApi")
	public void onTaskRemoved(Intent rootIntent) 
	{
		NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.cancel(AudioFileManager.NOTIFICATION_ID);
		
		this.stopSelf();
	
	    super.onTaskRemoved(rootIntent);
	}
}
