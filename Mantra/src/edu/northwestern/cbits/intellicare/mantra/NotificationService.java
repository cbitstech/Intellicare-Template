package edu.northwestern.cbits.intellicare.mantra;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * Handles notifications for Mantra.
 * @author mohrlab
 *
 */
public class NotificationService extends IntentService {

	private String CN = "NotificationService";

	NotificationAlarm alarm = new NotificationAlarm();

	public NotificationService(String name) {
		super(name);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(CN+".onHandleIntent", "entered; intent = " + intent.toString());
		
		alarm.SetAlarm(this);
	}
	
}
