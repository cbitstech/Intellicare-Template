package edu.northwestern.cbits.intellicare.relax;

import android.app.IntentService;
import android.content.Intent;

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
}
