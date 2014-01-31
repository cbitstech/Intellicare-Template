package edu.northwestern.cbits.intellicare.slumbertime;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class AlarmService extends IntentService 
{
	public static final String START_TIMER = "edu.northwestern.cbits.intellicare.slumbertime.START_TIMER";
	public static final String TIMER_TICK = "edu.northwestern.cbits.intellicare.slumbertime.TIMER_TICK";
	public static final String START_ALARM = "edu.northwestern.cbits.intellicare.slumbertime.START_ALARM";
	public static final String STOP_ALARM = "edu.northwestern.cbits.intellicare.slumbertime.STOP_ALARM";

	private static final int ALARM_NOTE_ID = 82772387;
	
	private static Handler _handler = null;
	private static Runnable _tickRunnable = null;
	private static MediaPlayer _player = null;
	
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
		}
		else
		{
			Log.e("ST", "RECEIVED INTENT: " + action);
		}
	}
}
