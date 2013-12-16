package edu.northwestern.cbits.intellicare.conductor;

import edu.northwestern.cbits.intellicare.StatusNotificationManager;
import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;

public class MessagesService extends IntentService 
{
	public MessagesService(String name) 
	{
		super(name);
	}

	public MessagesService() 
	{
		super("Intellicare Messaging Service");
	}

	protected void onHandleIntent(Intent intent) 
	{
		String action = intent.getAction();
		
		if (StatusNotificationManager.LOG_MESSAGE.equals(action))
		{
			ContentValues values = new ContentValues();
			values.put("package", intent.getStringExtra(StatusNotificationManager.PACKAGE));
			values.put("title", intent.getStringExtra(StatusNotificationManager.TITLE));
			values.put("message", intent.getStringExtra(StatusNotificationManager.MESSAGE));
			values.put("uri", intent.getStringExtra(StatusNotificationManager.URI));
			
			this.getContentResolver().insert(ConductorContentProvider.MESSAGES_URI, values);
			
			// TODO: Update Intellicare status icon...
		}
	}
}
