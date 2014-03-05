package edu.northwestern.cbits.intellicare.mantra;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * 
 */

/**
 * @author mohrlab
 *
 */
public class AutoStart extends BroadcastReceiver {

	private static final String CN = "AutoStart";
	NotificationAlarm alarm = new NotificationAlarm();
	private boolean isAlreadyCalled = false;
	
	
	/* Starts Mantra notification processing on Android boot-up.
	 * Src: http://stackoverflow.com/questions/4459058/alarm-manager-example
	 * 
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context arg0, Intent arg1) {
		Log.d(CN+".onReceive", "entered; arg0 = " + arg0.toString() + "; arg1 = " + arg1.toString());
		
		if(arg1.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
			if(!isAlreadyCalled ) {
				Log.d(CN+".onReceive", "BOOT COMPLETED; STARTING Mantra NotificationService...");
				alarm.SetAlarm(arg0);
			}
		}
	}

}
