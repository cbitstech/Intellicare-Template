package edu.northwestern.cbits.intellicare.slumbertime;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class AlarmService extends IntentService 
{
	public static final String START_TIMER = "edu.northwestern.cbits.intellicare.slumbertime.START_TIMER";
	public static final String TIMER_TICK = "edu.northwestern.cbits.intellicare.slumbertime.TIMER_TICK";
	public static final String START_ALARM = "edu.northwestern.cbits.intellicare.slumbertime.START_ALARM";
	public static final String STOP_ALARM = "edu.northwestern.cbits.intellicare.slumbertime.STOP_ALARM";
	
	private static Handler _handler = null;
	private static Runnable _tickRunnable = null;
	
	private static String _lastDateCheck = null;

	public AlarmService()
	{
		super("Slumber Time Alarm Service");
	}

	public AlarmService(String name) 
	{
		super(name);
	}

	protected void onHandleIntent(Intent intent) 
	{
		String action = intent.getAction();
		
		if (AlarmService.START_TIMER.equals(action))
		{
			if (AlarmService._handler == null)
			{
				AlarmService._handler = new Handler(Looper.getMainLooper());
				
				Intent tickIntent = new Intent(AlarmService.TIMER_TICK, null, this.getApplicationContext(), AlarmService.class);
				this.startService(tickIntent);
			}
		}
		else if (AlarmService.TIMER_TICK.equals(action))
		{
			DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(this);
			String thisTime = timeFormat.format(new Date());
			
			if (thisTime.equals(AlarmService._lastDateCheck) == false)
			{
				AlarmService._lastDateCheck = thisTime;

				Calendar c = Calendar.getInstance();
				
				int day = c.get(Calendar.DAY_OF_WEEK);
				
				String where = SlumberContentProvider.ALARM_HOUR + " = ?";
				where += " AND " + SlumberContentProvider.ALARM_MINUTE + " = ?";

				switch (day)
				{
					case Calendar.SUNDAY:
						where += " AND " + SlumberContentProvider.ALARM_SUNDAY + " = 1";
						break;
					case Calendar.MONDAY:
						where += " AND " + SlumberContentProvider.ALARM_MONDAY + " = 1";
						break;
					case Calendar.TUESDAY:
						where += " AND " + SlumberContentProvider.ALARM_TUESDAY + " = 1";
						break;
					case Calendar.WEDNESDAY:
						where += " AND " + SlumberContentProvider.ALARM_WEDNESDAY + " = 1";
						break;
					case Calendar.THURSDAY:
						where += " AND " + SlumberContentProvider.ALARM_THURSDAY + " = 1";
						break;
					case Calendar.FRIDAY:
						where += " AND " + SlumberContentProvider.ALARM_FRIDAY + " = 1";
						break;
					case Calendar.SATURDAY:
						where += " AND " + SlumberContentProvider.ALARM_SATURDAY + " = 1";
						break;
				}

				where += " AND " + SlumberContentProvider.ALARM_ENABLED + " = 1";
				
				String[] args = { "" + c.get(Calendar.HOUR_OF_DAY), "" + c.get(Calendar.MINUTE) }; 
				
				Cursor cursor = this.getContentResolver().query(SlumberContentProvider.ALARMS_URI, null, where, args, null);
				
				if (cursor.moveToNext())
				{
					Intent startIntent = new Intent(AlarmService.START_ALARM, null, this.getApplicationContext(), AlarmService.class);
					startIntent.setData(Uri.parse(cursor.getString(cursor.getColumnIndex(SlumberContentProvider.ALARM_CONTENT_URI))));
					startIntent.putExtra(SlumberContentProvider.ALARM_NAME, cursor.getString(cursor.getColumnIndex(SlumberContentProvider.ALARM_NAME)));

					this.startService(startIntent);
				}
				
				cursor.close();
			}

			if (AlarmService._tickRunnable == null)
			{
				final Context context = this.getApplicationContext();
		
				AlarmService._tickRunnable = new Runnable()
				{
					public void run() 
					{
						Intent tickIntent = new Intent(AlarmService.TIMER_TICK, null, context, AlarmService.class);
						context.startService(tickIntent);
					}
				};
			}
			
 			AlarmService._handler.removeCallbacks(AlarmService._tickRunnable);
			AlarmService._handler.postDelayed(AlarmService._tickRunnable, 1000);
		}
		else if (AlarmService.START_ALARM.equals(action))
		{
			String name = intent.getStringExtra(SlumberContentProvider.ALARM_NAME);
			
			Log.e("ST", "STARTING ALARM: " + name + " -- " + intent.getData());
		}
		else
		{
			Log.e("ST", "RECEIVED INTENT: " + action);
		}
	}
}
