package edu.northwestern.cbits.intellicare.messages;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScheduleHelper extends BroadcastReceiver 
{
	public void onReceive(Context context, Intent intent) 
	{
		ScheduleManager manager = ScheduleManager.getInstance(context);
		
		manager.updateSchedule();
	}
}
