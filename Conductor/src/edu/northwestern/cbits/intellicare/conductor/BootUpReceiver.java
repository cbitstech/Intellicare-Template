package edu.northwestern.cbits.intellicare.conductor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class BootUpReceiver extends BroadcastReceiver
{
	public static final String BOOT_KEY = "system_last_boot";

    public void onReceive(Context context, Intent intent)
    {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    	
    	Editor e = prefs.edit();
    	e.putLong(BootUpReceiver.BOOT_KEY, System.currentTimeMillis());
    	e.commit();
    	
    	context.startService(new Intent(MessagesService.REFRESH_MESSAGES));
    }
}
