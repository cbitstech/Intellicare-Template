package edu.northwestern.cbits.intellicare.conductor;

import edu.northwestern.cbits.intellicare.StatusNotificationManager;
import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

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
			
			// TODO: Update Intellicare status icon...
		}
	}
}
