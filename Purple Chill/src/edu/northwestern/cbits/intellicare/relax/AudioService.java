package edu.northwestern.cbits.intellicare.relax;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AudioService extends IntentService 
{
	public static final String TOGGLE_PLAYBACK = "purple_chill_toggle_playback";
	
	public AudioService(String name) 
	{
		super(name);
	}

	public AudioService() 
	{
		super("Purple Chill Audio Playback Service");
	}

	protected void onHandleIntent(Intent intent) 
	{
		if (AudioService.TOGGLE_PLAYBACK.equals(intent.getAction()))
		{
			AudioFileManager.togglePlayback(this);
		}
	}

    public void onTaskRemoved (Intent rootIntent)
    {
        Log.e("PC", "TASK REMOVE INTENT: " + rootIntent);
        Log.e("PC", "TASK REMOVE INTENT EXTRAS: " + rootIntent.getExtras());

        NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll();

        Log.e("PC", "TASK REMOVE DONE");
    }
}
