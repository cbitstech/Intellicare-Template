package edu.northwestern.cbits.intellicare;

import edu.northwestern.cbits.intellicare.logging.LogManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class ScheduleManager 
{
	private static ScheduleManager _instance = null;

	private Context _context = null;

	public ScheduleManager(Context context, long interval, Class<ScheduleHelper> helperClass) 
	{
		this._context  = context.getApplicationContext();

		try 
		{
			ScheduleHelper helper = helperClass.newInstance();

			AlarmManager alarm = (AlarmManager) this._context.getSystemService(Context.ALARM_SERVICE);

			Intent broadcast = new Intent(helper.action());
			PendingIntent pi = PendingIntent.getBroadcast(this._context, 0, broadcast, PendingIntent.FLAG_UPDATE_CURRENT);

			alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 0, interval, pi);
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

	public static ScheduleManager getInstance(Context context, long interval, Class<ScheduleHelper> helperClass)
	{
		if (ScheduleManager._instance == null)
		{
			ScheduleManager._instance = new ScheduleManager(context.getApplicationContext(), interval, helperClass);
			
			try 
			{
				ScheduleHelper helper = helperClass.newInstance();
				
				helper.runScheduledTask(context);
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

		return ScheduleManager._instance;
	}
}
