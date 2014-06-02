package edu.northwestern.cbits.intellicare;

import edu.northwestern.cbits.intellicare.logging.LogManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public abstract class ScheduleHelper extends BroadcastReceiver
{
	private static boolean _inited = false;
	
	@SuppressWarnings("unchecked")
	public void onReceive(Context context, Intent intent) 
	{
		context = context.getApplicationContext();
		
		if (ScheduleHelper._inited == false)
		{
			ScheduleHelper._inited = true;
			
			Class<ScheduleHelper> helperClass = (Class<ScheduleHelper>) this.getClass();

			try 
			{
				ScheduleHelper helper = helperClass.newInstance();
				
				IntentFilter filter = new IntentFilter(helper.action());
				
				context.registerReceiver(helper, filter);
			} 
			catch (InstantiationException e) 
			{
				LogManager.getInstance(context).logException(e);
			} 
			catch (IllegalAccessException e) 
			{
				LogManager.getInstance(context).logException(e);
			}
			
			ScheduleManager.getInstance(context, this.interval(), helperClass);
		}
		else
			this.runScheduledTask(context);
	}
	
	public abstract String action();

	@SuppressWarnings("rawtypes")
	public static void init(final Context context, final Class helperClass)
	{
		try 
		{
			ScheduleHelper helper = (ScheduleHelper) helperClass.newInstance();
			
			helper.onReceive(context, null);
		} 
		catch (InstantiationException e) 
		{
			LogManager.getInstance(context).logException(e);
		} 
		catch (IllegalAccessException e) 
		{
			LogManager.getInstance(context).logException(e);
		}
	}

	protected abstract long interval();

	public abstract void runScheduledTask(Context context);
}
