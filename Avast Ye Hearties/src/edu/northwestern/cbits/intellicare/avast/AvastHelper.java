package edu.northwestern.cbits.intellicare.avast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AvastHelper extends BroadcastReceiver 
{
	public void onReceive(Context context, Intent intent) 
	{
		AvastVenuesManager manager = AvastVenuesManager.getInstance(context);
		
		manager.setup();
	}
}
