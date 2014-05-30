package edu.northwestern.cbits.intellicare.moveme;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.builder.ServiceBuilder;
import org.scribe.exceptions.OAuthException;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;

import edu.northwestern.cbits.intellicare.ScheduleHelper;
import edu.northwestern.cbits.intellicare.StatusNotificationManager;
import edu.northwestern.cbits.intellicare.logging.LogManager;
import edu.northwestern.cbits.intellicare.oauth.FitbitApi;

public class NotificationHelper extends ScheduleHelper
{
	private static final String LAST_NOTE = "last_note";
	
	private static final int FITBIT_UPDATE_INTERVAL = 1000 * 60 * 60;
	private static final String LAST_FITBIT_UPDATE = "last_fitbit_update";

	protected long interval() 
	{
		return 60000;
	}

	public String action() 
	{
		return "edu.northwestern.cbits.intellicare.moveme.ACTION_TIMED_JOB";
	}

	public void runScheduledTask(final Context context) 
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		long now = System.currentTimeMillis();
		
		long lastNotification = prefs.getLong(NotificationHelper.LAST_NOTE, 0);
		
		if (now - lastNotification < 60000)
			return;
		
		Calendar c = Calendar.getInstance();

		int messagesResource = -1;
		
		int timeOfDay = -1;
		
		if (prefs.getBoolean(SettingsActivity.SETTING_MORNING_ENABLED, SettingsActivity.SETTING_MORNING_ENABLED_DEFAULT))
		{
			boolean remindNow = false;

			switch (c.get(Calendar.DAY_OF_WEEK))
			{
				case Calendar.SUNDAY:
					remindNow = prefs.getBoolean(SettingsActivity.REMINDER_MORNING_SUNDAY, SettingsActivity.SUNDAY_ENABLED_DEFAULT);
					break;
				case Calendar.MONDAY:
					remindNow = prefs.getBoolean(SettingsActivity.REMINDER_MORNING_MONDAY, SettingsActivity.MONDAY_ENABLED_DEFAULT);
					break;
				case Calendar.TUESDAY:
					remindNow = prefs.getBoolean(SettingsActivity.REMINDER_MORNING_TUESDAY, SettingsActivity.TUESDAY_ENABLED_DEFAULT);
					break;
				case Calendar.WEDNESDAY:
					remindNow = prefs.getBoolean(SettingsActivity.REMINDER_MORNING_WEDNESDAY, SettingsActivity.WEDNESDAY_ENABLED_DEFAULT);
					break;
				case Calendar.THURSDAY:
					remindNow = prefs.getBoolean(SettingsActivity.REMINDER_MORNING_THURSDAY, SettingsActivity.THURSDAY_ENABLED_DEFAULT);
					break;
				case Calendar.FRIDAY:
					remindNow = prefs.getBoolean(SettingsActivity.REMINDER_MORNING_FRIDAY, SettingsActivity.FRIDAY_ENABLED_DEFAULT);
					break;
				case Calendar.SATURDAY:
					remindNow = prefs.getBoolean(SettingsActivity.REMINDER_MORNING_SATURDAY, SettingsActivity.SATURDAY_ENABLED_DEFAULT);
					break;
			}

			int reminderHour = prefs.getInt(SettingsActivity.REMINDER_MORNING_HOUR, SettingsActivity.SETTING_MORNING_HOUR_DEFAULT);
			int reminderMinute = prefs.getInt(SettingsActivity.REMINDER_MORNING_MINUTE, SettingsActivity.SETTING_MORNING_MINUTE_DEFAULT);

			if (remindNow && reminderHour == c.get(Calendar.HOUR_OF_DAY) && reminderMinute == c.get(Calendar.MINUTE))
			{
				messagesResource = R.array.array_morning_messages;
				
				timeOfDay = ReminderActivity.MORNING;
			}
		}

		if (prefs.getBoolean(SettingsActivity.SETTING_AFTERNOON_ENABLED, SettingsActivity.SETTING_AFTERNOON_ENABLED_DEFAULT))
		{
			boolean remindNow = false;

			switch (c.get(Calendar.DAY_OF_WEEK))
			{
				case Calendar.SUNDAY:
					remindNow = prefs.getBoolean(SettingsActivity.REMINDER_AFTERNOON_SUNDAY, SettingsActivity.SUNDAY_ENABLED_DEFAULT);
					break;
				case Calendar.MONDAY:
					remindNow = prefs.getBoolean(SettingsActivity.REMINDER_AFTERNOON_MONDAY, SettingsActivity.MONDAY_ENABLED_DEFAULT);
					break;
				case Calendar.TUESDAY:
					remindNow = prefs.getBoolean(SettingsActivity.REMINDER_AFTERNOON_TUESDAY, SettingsActivity.TUESDAY_ENABLED_DEFAULT);
					break;
				case Calendar.WEDNESDAY:
					remindNow = prefs.getBoolean(SettingsActivity.REMINDER_AFTERNOON_WEDNESDAY, SettingsActivity.WEDNESDAY_ENABLED_DEFAULT);
					break;
				case Calendar.THURSDAY:
					remindNow = prefs.getBoolean(SettingsActivity.REMINDER_AFTERNOON_THURSDAY, SettingsActivity.THURSDAY_ENABLED_DEFAULT);
					break;
				case Calendar.FRIDAY:
					remindNow = prefs.getBoolean(SettingsActivity.REMINDER_AFTERNOON_FRIDAY, SettingsActivity.FRIDAY_ENABLED_DEFAULT);
					break;
				case Calendar.SATURDAY:
					remindNow = prefs.getBoolean(SettingsActivity.REMINDER_AFTERNOON_SATURDAY, SettingsActivity.SATURDAY_ENABLED_DEFAULT);
					break;
			}

			int reminderHour = prefs.getInt(SettingsActivity.REMINDER_AFTERNOON_HOUR, SettingsActivity.SETTING_AFTERNOON_HOUR_DEFAULT);
			int reminderMinute = prefs.getInt(SettingsActivity.REMINDER_AFTERNOON_MINUTE, SettingsActivity.SETTING_AFTERNOON_MINUTE_DEFAULT);
			
			if (remindNow && reminderHour == c.get(Calendar.HOUR_OF_DAY) && reminderMinute == c.get(Calendar.MINUTE))
			{
				messagesResource = R.array.array_afternoon_messages;
				
				timeOfDay = ReminderActivity.AFTERNOON;
			}
		}

		if (prefs.getBoolean(SettingsActivity.SETTING_EVENING_ENABLED, SettingsActivity.SETTING_EVENING_ENABLED_DEFAULT))
		{
			boolean remindNow = false;

			switch (c.get(Calendar.DAY_OF_WEEK))
			{
				case Calendar.SUNDAY:
					remindNow = prefs.getBoolean(SettingsActivity.REMINDER_EVENING_SUNDAY, SettingsActivity.SUNDAY_ENABLED_DEFAULT);
					break;
				case Calendar.MONDAY:
					remindNow = prefs.getBoolean(SettingsActivity.REMINDER_EVENING_MONDAY, SettingsActivity.MONDAY_ENABLED_DEFAULT);
					break;
				case Calendar.TUESDAY:
					remindNow = prefs.getBoolean(SettingsActivity.REMINDER_EVENING_TUESDAY, SettingsActivity.TUESDAY_ENABLED_DEFAULT);
					break;
				case Calendar.WEDNESDAY:
					remindNow = prefs.getBoolean(SettingsActivity.REMINDER_EVENING_WEDNESDAY, SettingsActivity.WEDNESDAY_ENABLED_DEFAULT);
					break;
				case Calendar.THURSDAY:
					remindNow = prefs.getBoolean(SettingsActivity.REMINDER_EVENING_THURSDAY, SettingsActivity.THURSDAY_ENABLED_DEFAULT);
					break;
				case Calendar.FRIDAY:
					remindNow = prefs.getBoolean(SettingsActivity.REMINDER_EVENING_FRIDAY, SettingsActivity.FRIDAY_ENABLED_DEFAULT);
					break;
				case Calendar.SATURDAY:
					remindNow = prefs.getBoolean(SettingsActivity.REMINDER_EVENING_SATURDAY, SettingsActivity.SATURDAY_ENABLED_DEFAULT);
					break;
			}

			int reminderHour = prefs.getInt(SettingsActivity.REMINDER_EVENING_HOUR, SettingsActivity.SETTING_EVENING_HOUR_DEFAULT);
			int reminderMinute = prefs.getInt(SettingsActivity.REMINDER_EVENING_MINUTE, SettingsActivity.SETTING_EVENING_MINUTE_DEFAULT);
			
			if (remindNow && reminderHour == c.get(Calendar.HOUR_OF_DAY) && reminderMinute == c.get(Calendar.MINUTE))
			{
				messagesResource = R.array.array_evening_messages;
				
				timeOfDay = ReminderActivity.EVENING;
			}
		}
		
		if (messagesResource != -1)
		{
			now = System.currentTimeMillis();
			long day = (24 * 60 * 60 * 1000);
			
			long today = now / day;
			
			String[] messages = context.getResources().getStringArray(messagesResource);
			
			String message = messages[(int) (today % messages.length)];
			
			String title = context.getString(R.string.title_reminder_note);

			Intent intent = new Intent(context, MainActivity.class);
			PendingIntent pi = PendingIntent.getActivity(context, 0, intent, 0);

			StatusNotificationManager.getInstance(context).notifyBigText(97534, R.drawable.ic_notification, title, message, pi, MainActivity.URI);
			
			Editor e = prefs.edit();
			e.putLong(NotificationHelper.LAST_NOTE, now);
			e.commit();
			
			String where = MoveProvider.RECORDED + " > ?";
			String[] args = { "" + (now - (day * 2)) };
			
			Cursor cursor = context.getContentResolver().query(MoveProvider.EXERCISES_URI, null, where, args, null);
			
			if (cursor.getCount() == 0)
			{
				intent = new Intent(context, ReminderActivity.class);
				intent.putExtra(ReminderActivity.TIME_OF_DAY, timeOfDay);
				pi = PendingIntent.getActivity(context, 0, intent, 0);
				
				Uri u = ReminderActivity.MORNING_REMINDER_URI;
				title = context.getString(R.string.title_morning_reminder);
				
				if (timeOfDay == ReminderActivity.AFTERNOON)
				{
					u = ReminderActivity.AFTERNOON_REMINDER_URI;
					title = context.getString(R.string.title_afternoon_reminder);
				}
				else if (timeOfDay == ReminderActivity.EVENING)
				{
					u = ReminderActivity.EVENING_REMINDER_URI;
					title = context.getString(R.string.title_evening_reminder);
				}

				message = context.getString(R.string.message_exercise_reminder);
				
				StatusNotificationManager.getInstance(context).notifyBigText(97534, R.drawable.ic_notification, title, message, pi, u);
			}
			
			cursor.close();
		}
		
		this.updateFitbitGoals(context);
	}
	
	@SuppressLint("SimpleDateFormat")
	private void updateFitbitGoals(final Context context)
	{
		Runnable r = new Runnable()
		{
			public void run() 
			{
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
				
				long now = System.currentTimeMillis();
				
				if (now - prefs.getLong(NotificationHelper.LAST_FITBIT_UPDATE, 0) < NotificationHelper.FITBIT_UPDATE_INTERVAL)
					return;
				
				Editor editor = prefs.edit();
				editor.putLong(NotificationHelper.LAST_FITBIT_UPDATE, now);
				editor.commit();

				if (prefs.getBoolean(SettingsActivity.SETTING_FITBIT_ENABLED, SettingsActivity.SETTING_FITBIT_ENABLED_DEFAULT) && prefs.contains(SettingsActivity.OAUTH_FITBIT_TOKEN))
				{
		        	Calendar c = Calendar.getInstance();
		        	c.set(Calendar.HOUR_OF_DAY, 12);
		        	c.set(Calendar.MINUTE, 0);
		        	c.set(Calendar.SECOND, 0);
		        	c.set(Calendar.MILLISECOND, 0);
		        	
		        	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		        	
		        	for (int i = 0; i < 8; i++)
		        	{
			        	String dateString = sdf.format(c.getTime());
			        	
						String token = prefs.getString(SettingsActivity.OAUTH_FITBIT_TOKEN, "");
						String secret = prefs.getString(SettingsActivity.OAUTH_FITBIT_SECRET, "");
			
						Token accessToken = new Token(token, secret);
			        	
						final OAuthRequest request = new OAuthRequest(Verb.GET, "https://api.fitbit.com/1/user/-/activities/date/" + dateString + ".json");
			
			        	ServiceBuilder builder = new ServiceBuilder();
			        	builder = builder.provider(FitbitApi.class);
			        	builder = builder.apiKey(FitbitApi.CONSUMER_KEY);
			        	builder = builder.apiSecret(FitbitApi.CONSUMER_SECRET);
			
			        	final OAuthService service = builder.build();
			
						service.signRequest(accessToken, request);
			
						try
						{
							Response response = request.send();
			
							JSONObject body = new JSONObject(response.getBody());
							
							JSONObject summary = body.getJSONObject("summary");
			
							long logged = summary.getLong("veryActiveMinutes");
			
							JSONObject goals = body.getJSONObject("goals");
			
							long goal = goals.getLong("activeMinutes");
							
							long timestamp = c.getTimeInMillis();
							
							String where = MoveProvider.FITBIT_TIMESTAMP + " >= ? AND " + MoveProvider.FITBIT_TIMESTAMP + " <= ?";
							String[] args = { "" + (timestamp - (12 * 60 * 60 * 1000)), "" + (timestamp + (12 * 60 * 60 * 1000)) }; 
							
							context.getContentResolver().delete(MoveProvider.FITBIT_URI, where, args);
							
							ContentValues values = new ContentValues();
							values.put(MoveProvider.FITBIT_TIMESTAMP, timestamp);
							values.put(MoveProvider.FITBIT_LOGGED, logged);
							values.put(MoveProvider.FITBIT_GOAL, goal);
							
							context.getContentResolver().insert(MoveProvider.FITBIT_URI, values);
						} 
						catch (JSONException e) 
						{
			     			LogManager.getInstance(context).logException(e);
						}
						catch (OAuthException e)
						{
			     			LogManager.getInstance(context).logException(e);
						}
						catch (IllegalArgumentException e)
						{
			     			LogManager.getInstance(context).logException(e);
						}
						
						c.add(Calendar.DATE, -1);
		        	}			
				}
			}
		};
		
		Thread t = new Thread(r);
		t.start();
	}
}
