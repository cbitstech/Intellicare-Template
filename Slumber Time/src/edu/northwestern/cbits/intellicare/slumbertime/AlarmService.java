package edu.northwestern.cbits.intellicare.slumbertime;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import edu.northwestern.cbits.intellicare.StatusNotificationManager;
import edu.northwestern.cbits.intellicare.logging.LogManager;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class AlarmService extends IntentService 
{
	public static final String START_TIMER = "edu.northwestern.cbits.intellicare.slumbertime.START_TIMER";
	public static final String TIMER_TICK = "edu.northwestern.cbits.intellicare.slumbertime.TIMER_TICK";
	public static final String START_ALARM = "edu.northwestern.cbits.intellicare.slumbertime.START_ALARM";
	public static final String STOP_ALARM = "edu.northwestern.cbits.intellicare.slumbertime.STOP_ALARM";
	public static final String BROADCAST_TRACK_INFO = "edu.northwestern.cbits.intellicare.slumbertime.BROADCAST_TRACK_INFO";

	private static final int ALARM_NOTE_ID = 82772387;
	protected static final String REMINDER_HOUR = "reminder_hour";
	protected static final String REMINDER_MINUTE = "reminder_minute";
	
	private static Handler _handler = null;
	private static Runnable _tickRunnable = null;
	private static MediaPlayer _player = null;
	
	private static String _lastDateCheck = null;
	private static String _currentTrackName = null;
	private static Uri _currentUri = null;
	
	private static long _lastReminder = 0;

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
					String name = cursor.getString(cursor.getColumnIndex(SlumberContentProvider.ALARM_NAME));
					
					try
					{
						Uri uri = Uri.parse(cursor.getString(cursor.getColumnIndex(SlumberContentProvider.ALARM_CONTENT_URI)));
	
						Intent startIntent = new Intent(AlarmService.START_ALARM, null, this.getApplicationContext(), AlarmService.class);
						startIntent.setData(uri);
						startIntent.putExtra(SlumberContentProvider.ALARM_NAME, name);
						
						AlarmService._currentTrackName = name;
						AlarmService._currentUri = uri;

						this.startService(startIntent);
					}
					catch (NullPointerException e)
					{
						LogManager.getInstance(this).logException(e);
					}
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
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			
			int remindHour = prefs.getInt(AlarmService.REMINDER_HOUR, 9);
			int remindMinute = prefs.getInt(AlarmService.REMINDER_MINUTE, 0);
			
			Calendar calendar = Calendar.getInstance();
			long now = System.currentTimeMillis();
			
			if (now - AlarmService._lastReminder > 60000 && calendar.get(Calendar.HOUR_OF_DAY) == remindHour && calendar.get(Calendar.MINUTE) == remindMinute)
			{
				AlarmService._lastReminder = now;
				
				Log.e("ST", "DIARY REMINDER");

				String title = this.getString(R.string.note_title);
				String message = this.getString(R.string.note_message);
				
				Intent launchIntent = new Intent(this, AddSleepDiaryActivity.class);
				PendingIntent pi = PendingIntent.getActivity(this, 0, launchIntent, PendingIntent.FLAG_ONE_SHOT);

				StatusNotificationManager.getInstance(this).notifyBigText(97531, R.drawable.ic_note, title, message, pi, AddSleepDiaryActivity.URI);
			}
			
 			AlarmService._handler.removeCallbacks(AlarmService._tickRunnable);
			AlarmService._handler.postDelayed(AlarmService._tickRunnable, 1000);
		}
		else if (AlarmService.START_ALARM.equals(action))
		{
			String name = intent.getStringExtra(SlumberContentProvider.ALARM_NAME);

			LocalBroadcastManager broadcasts = LocalBroadcastManager.getInstance(this.getApplicationContext());

			if (AlarmService._player != null)
			{
				if (AlarmService._player.isPlaying())
					AlarmService._player.stop();

				AlarmService._player.release();
				
				AlarmService._player = null;
			}
			
			try 
			{
				if (AlarmService._player == null)
					AlarmService._player = new MediaPlayer();
				
				AlarmService._player.setLooping(true);
				AlarmService._player.setDataSource(this.getApplicationContext(), intent.getData());
				
				AlarmService._player.prepare();
				
				AlarmService._player.setVolume(1.0f, 1.0f);
				
				AlarmService._player.start();
			}
			catch (IllegalArgumentException e) 
			{
				e.printStackTrace();
			} 
			catch (SecurityException e) 
			{
				e.printStackTrace();
			} 
			catch (IllegalStateException e) 
			{
				e.printStackTrace();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}

			broadcasts.sendBroadcast(intent);

			DateFormat format = android.text.format.DateFormat.getTimeFormat(this);

			NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
			builder = builder.setAutoCancel(true);
			builder = builder.setLights(0x0000ff00, 500, 500);
			builder = builder.setTicker(name);
			builder = builder.setContentTitle(name);
			builder = builder.setContentText(this.getString(R.string.note_slumber_time, format.format(new Date())));
			builder = builder.setSmallIcon(R.drawable.ic_note);
			
			Intent stopIntent = new Intent(AlarmService.STOP_ALARM, null, this.getApplicationContext(), AlarmService.class);

			builder = builder.setContentIntent(PendingIntent.getService(this, 0, stopIntent, 0));
			
			long[] pattern = { 0, 500, 500 };
			builder.setVibrate(pattern);
			
			NotificationManager notes = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
			notes.notify(AlarmService.ALARM_NOTE_ID, builder.build());
			
			HashMap<String, Object> payload = new HashMap<String, Object>();
			LogManager.getInstance(this).log("fired_alarm", payload);
		}
		else if (AlarmService.STOP_ALARM.equals(action))
		{
			LocalBroadcastManager broadcasts = LocalBroadcastManager.getInstance(this.getApplicationContext());

			if (AlarmService._player != null)
			{
				if (AlarmService._player.isPlaying())
					AlarmService._player.stop();

				AlarmService._player.release();
				
				AlarmService._player = null;
			}

			broadcasts.sendBroadcast(intent);
			
			NotificationManager notes = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
			notes.cancel(AlarmService.ALARM_NOTE_ID);
			
			AlarmService._currentTrackName = null;
			AlarmService._currentUri = null;
			
			HashMap<String, Object> payload = new HashMap<String, Object>();
			LogManager.getInstance(this).log("cancelled_alarm", payload);
		}
		else if (AlarmService.BROADCAST_TRACK_INFO.equals(action))
		{
			if (AlarmService._currentUri != null && AlarmService._currentTrackName != null)
			{
				LocalBroadcastManager broadcasts = LocalBroadcastManager.getInstance(this.getApplicationContext());

				Intent startIntent = new Intent(AlarmService.BROADCAST_TRACK_INFO, null, this.getApplicationContext(), AlarmService.class);
				startIntent.setData(AlarmService._currentUri);
				startIntent.putExtra(SlumberContentProvider.ALARM_NAME, AlarmService._currentTrackName);
				
				broadcasts.sendBroadcast(startIntent);
			}
		}
		else
		{
			Log.e("ST", "RECEIVED INTENT: " + action);
		}
	}
	
	public static Date nextAlarm(Context context)
	{
		ArrayList<Date> dates = new ArrayList<Date>();
				
		Cursor cursor = context.getContentResolver().query(SlumberContentProvider.ALARMS_URI, null, null, null, null);
		
		if (cursor.moveToNext())
		{
			Calendar alarmCalendar = Calendar.getInstance();
			
			alarmCalendar.set(Calendar.HOUR_OF_DAY, cursor.getInt(cursor.getColumnIndex(SlumberContentProvider.ALARM_HOUR)));
			alarmCalendar.set(Calendar.MINUTE, cursor.getInt(cursor.getColumnIndex(SlumberContentProvider.ALARM_MINUTE)));
			
			for (int i = 0; i < 7; i++)
			{
				if (cursor.getInt(cursor.getColumnIndex(SlumberContentProvider.ALARM_SUNDAY)) == 1 && alarmCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
				{
					dates.add(alarmCalendar.getTime());
				}
				else if (cursor.getInt(cursor.getColumnIndex(SlumberContentProvider.ALARM_MONDAY)) == 1 && alarmCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY)
				{
					dates.add(alarmCalendar.getTime());
				}
				else if (cursor.getInt(cursor.getColumnIndex(SlumberContentProvider.ALARM_TUESDAY)) == 1 && alarmCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY)
				{
					dates.add(alarmCalendar.getTime());
				}
				else if (cursor.getInt(cursor.getColumnIndex(SlumberContentProvider.ALARM_WEDNESDAY)) == 1 && alarmCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY)
				{
					dates.add(alarmCalendar.getTime());
				}
				else if (cursor.getInt(cursor.getColumnIndex(SlumberContentProvider.ALARM_THURSDAY)) == 1 && alarmCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY)
				{
					dates.add(alarmCalendar.getTime());
				}
				else if (cursor.getInt(cursor.getColumnIndex(SlumberContentProvider.ALARM_FRIDAY)) == 1 && alarmCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY)
				{
					dates.add(alarmCalendar.getTime());
				}
				else if (cursor.getInt(cursor.getColumnIndex(SlumberContentProvider.ALARM_SATURDAY)) == 1 && alarmCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
				{
					dates.add(alarmCalendar.getTime());
				}

				alarmCalendar.add(Calendar.DATE, 1);
			}
		}
		
		cursor.close();

		Date now = new Date();

		Collections.sort(dates);
		
		for (Date d : dates)
		{
			if (d.after(now))
				return d;
		}
		
		return null;
	}
}
