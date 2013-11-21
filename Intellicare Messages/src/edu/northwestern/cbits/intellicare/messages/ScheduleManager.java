package edu.northwestern.cbits.intellicare.messages;

import java.util.Calendar;
import java.util.HashMap;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.util.Log;
import edu.northwestern.cbits.intellicare.PhqFourActivity;
import edu.northwestern.cbits.intellicare.StatusNotificationManager;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class ScheduleManager
{
	public static final String MESSAGE_INDEX = "message_index";
	private static final String FIRST_RUN = "first_run";
	public static final String INSTRUCTION_COMPLETED = "instruction_completed";
	public static final String IS_INSTRUCTION = "is_instruction";
	public static final String MESSAGE_TITLE = "message_title";
	public static final String MESSAGE_MESSAGE = "message_message";
	private static final String SHOW_NOTIFICATION = "show_notification";
	private static final String MESSAGE_IMAGE = "message_image";
	
	private static ScheduleManager _instance = null;

	private Context _context = null;

	public ScheduleManager(Context context) 
	{
		this._context  = context;
		
		AlarmManager alarm = (AlarmManager) this._context.getSystemService(Context.ALARM_SERVICE);
		
		Intent broadcast = new Intent(this._context, ScheduleHelper.class);
		PendingIntent pi = PendingIntent.getBroadcast(this._context, 0, broadcast, PendingIntent.FLAG_UPDATE_CURRENT);
		
		alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 0, AlarmManager.INTERVAL_FIFTEEN_MINUTES, pi);
///		alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 0, 15000, pi);
	}

	public static ScheduleManager getInstance(Context context)
	{
		if (ScheduleManager._instance == null)
		{
			ScheduleManager._instance = new ScheduleManager(context.getApplicationContext());
			ScheduleManager._instance.updateSchedule();
		}
		
		return ScheduleManager._instance;
	}
	
	public void updateSchedule()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this._context);
		
		if (prefs.contains(HelpActivity.HELP_COMPLETED) == false)
			return;
		
		boolean showNotification = prefs.getBoolean(ScheduleManager.SHOW_NOTIFICATION, false);

		long now = System.currentTimeMillis();
		
		if (prefs.contains(ScheduleManager.FIRST_RUN) == false)
		{
			Editor e = prefs.edit();
			e.putLong(ScheduleManager.FIRST_RUN, now);
			e.commit();
		}
		
		int currentLesson = prefs.getInt(LessonsActivity.LESSON_LEVEL, 0);
		
		if (currentLesson == 0)
		{
			Cursor lessonCursor = this._context.getContentResolver().query(ContentProvider.LESSONS_URI, null, null, null, "lesson_order");
			
			if (lessonCursor.moveToNext())
			{
				currentLesson = lessonCursor.getInt(lessonCursor.getColumnIndex("id"));
				
				ContentValues values = new ContentValues();
				values.put("complete", 1);

				String where = "id = ?";
				String[] whereArgs = { "" + currentLesson };
				
				this._context.getContentResolver().update(ContentProvider.LESSONS_URI, values, where, whereArgs);

				Editor e = prefs.edit();
				e.putInt(LessonsActivity.LESSON_LEVEL, currentLesson);
				e.remove(ScheduleManager.MESSAGE_INDEX);
				e.commit();
			}
			
			lessonCursor.close();
		}

		boolean lessonComplete = prefs.getBoolean(LessonsActivity.LESSON_READ_PREFIX + currentLesson, false);
		
		if (lessonComplete)
		{
			int index = prefs.getInt(ScheduleManager.MESSAGE_INDEX, 0);
			
			long notificationTime = this.getNotificationTime(index % 5, now);
			
			if (notificationTime > 0)
			{
				boolean completed = prefs.getBoolean(ScheduleManager.INSTRUCTION_COMPLETED, false);
				
				if (index > 0 && index % 5 == 0 && completed == false)
					index -= 5;
				
				Message msg = this.getMessage(currentLesson, index);
				
				if (msg != null)
				{
					if (now >= notificationTime)
					{
						int id = 0;

						Editor e = prefs.edit();
						
						int icon = R.drawable.ic_notification;

						if (index % 5 == 0)
						{
							id = 1;
							msg.title = this._context.getString(R.string.note_instruction);
							e.remove(ScheduleManager.INSTRUCTION_COMPLETED);
							icon = R.drawable.ic_notification_color;
						}

						String descIndex = currentLesson + "." + (index % 35);

						boolean isInstruction = ((index % 5) == 0);
						
						if (isInstruction)
						{
							Intent intent = new Intent(this._context, TaskActivity.class);
							intent.setAction("ACTION_" + now);
							intent.putExtra(TaskActivity.MESSAGE, msg.message);
							intent.putExtra(TaskActivity.IMAGE, msg.image);

							intent.putExtra(ScheduleManager.MESSAGE_MESSAGE, msg.message);
							intent.putExtra(ScheduleManager.MESSAGE_IMAGE, msg.image);
							intent.putExtra(ScheduleManager.MESSAGE_INDEX, descIndex);
							intent.putExtra(ScheduleManager.IS_INSTRUCTION, true);

							intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

							if (showNotification)
							{
								HashMap<String, Object> payload = new HashMap<String, Object>();
								payload.put("message_index", descIndex);
								LogManager.getInstance(this._context).log("notification_shown", payload);
		
								StatusNotificationManager.getInstance(this._context).notifyBigText(id, icon, msg.title, msg.message, PendingIntent.getActivity(this._context, 0, intent, PendingIntent.FLAG_ONE_SHOT));
							}
							else
								this._context.startActivity(intent);
							
							e.putLong("last_instruction_notification", System.currentTimeMillis());
							
						}
						else
						{
							Message prompt = this.getMessage(currentLesson, index - (index % 5));

							Intent intent = new Intent(this._context, TipActivity.class);
							intent.setAction("ACTION_" + now);
							intent.putExtra(TipActivity.MESSAGE, msg.message);
							intent.putExtra(TipActivity.TASK, prompt.message);
							intent.putExtra(TipActivity.IMAGE, msg.image);
							
							intent.putExtra(ScheduleManager.MESSAGE_MESSAGE, msg.message);
							intent.putExtra(ScheduleManager.MESSAGE_IMAGE, msg.image);
							intent.putExtra(ScheduleManager.MESSAGE_INDEX, descIndex);
							intent.putExtra(ScheduleManager.IS_INSTRUCTION, isInstruction);
							
							intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

							if (showNotification)
							{
								HashMap<String, Object> payload = new HashMap<String, Object>();
								payload.put("message_index", descIndex);
								LogManager.getInstance(this._context).log("notification_shown", payload);
		
								StatusNotificationManager.getInstance(this._context).notifyBigText(id, icon, msg.title, msg.message, PendingIntent.getActivity(this._context, 0, intent, PendingIntent.FLAG_ONE_SHOT));
							}
							else
								this._context.startActivity(intent);
						}
						
						index += 1;
	
						e.putInt(ScheduleManager.MESSAGE_INDEX, index);
						e.commit();
					}
				}
				else
				{
					Cursor c = this._context.getContentResolver().query(ContentProvider.LESSONS_URI, null, null, null, "lesson_order");
					
					while (c.moveToNext())
					{
						int thisId = c.getInt(c.getColumnIndex("id"));
						
						if (thisId == currentLesson)
							currentLesson = 0;
						else if (currentLesson == 0)
							currentLesson = thisId;
					}
							
					Editor e = prefs.edit();
					e.putInt(LessonsActivity.LESSON_LEVEL, currentLesson);
					e.remove(ScheduleManager.MESSAGE_INDEX);
					e.commit();

					PhqFourActivity.administer(this._context, false);
				}
			}
			else
			{
				// Skip - too late!
			}
		}
		else
		{
			String title = null;
			String message = null;
			
			Intent lessonIntent = new Intent(this._context, LessonActivity.class);
			lessonIntent.setAction("ACTION_" + now);

			lessonIntent.putExtra(LessonsActivity.LESSON_LEVEL, currentLesson);

			switch (currentLesson)
			{
				case 1:
					title = this._context.getString(R.string.title_lesson_one);
					message = this._context.getString(R.string.desc_lesson_one);
					lessonIntent.putExtra(LessonActivity.TITLE_LIST, R.array.one_titles);
					lessonIntent.putExtra(LessonActivity.URL_LIST, R.array.one_urls);
					break;
				case 2:
					title = this._context.getString(R.string.title_lesson_two);
					message = this._context.getString(R.string.desc_lesson_two);
					lessonIntent.putExtra(LessonActivity.TITLE_LIST, R.array.two_titles);
					lessonIntent.putExtra(LessonActivity.URL_LIST, R.array.two_urls);
					break;
				case 3:
					title = this._context.getString(R.string.title_lesson_three);
					message = this._context.getString(R.string.desc_lesson_three);
					lessonIntent.putExtra(LessonActivity.TITLE_LIST, R.array.three_titles);
					lessonIntent.putExtra(LessonActivity.URL_LIST, R.array.three_urls);
					break;
				case 4:
					title = this._context.getString(R.string.title_lesson_four);
					message = this._context.getString(R.string.desc_lesson_four);
					lessonIntent.putExtra(LessonActivity.TITLE_LIST, R.array.four_titles);
					lessonIntent.putExtra(LessonActivity.URL_LIST, R.array.four_urls);
					break;
				case 5:
					title = this._context.getString(R.string.title_lesson_five);
					message = this._context.getString(R.string.desc_lesson_five);
					lessonIntent.putExtra(LessonActivity.TITLE_LIST, R.array.five_titles);
					lessonIntent.putExtra(LessonActivity.URL_LIST, R.array.five_urls);
					break;
			}

			String descIndex = currentLesson + "";

			ContentValues values = new ContentValues();
			values.put("complete", 1);

			String where = "id = ?";
			String[] whereArgs = { "" + currentLesson };
			
			this._context.getContentResolver().update(ContentProvider.LESSONS_URI, values, where, whereArgs);

			lessonIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			if (showNotification)
			{
				PendingIntent pi = PendingIntent.getActivity(this._context, 1, lessonIntent, PendingIntent.FLAG_ONE_SHOT);
				
				HashMap<String, Object> payload = new HashMap<String, Object>();
				payload.put("message_index", descIndex);
				LogManager.getInstance(this._context).log("lesson_notification_shown", payload);

				StatusNotificationManager.getInstance(this._context).notifyBigText(0, R.drawable.ic_notification_color, title, message, pi);
			}
			else
				this._context.startActivity(lessonIntent);
		}
	}
	
	private Message getMessage(int lessonId, int index) 
	{
		if (index >= 35)
			return null;
		
		String selection = "lesson_id = ?";
		String[] msgArgs = { "" + lessonId };

		Cursor cursor = this._context.getContentResolver().query(ContentProvider.MESSAGE_GROUPS_URI, null, selection, msgArgs, "group_order");
		
		Message message = null;
		
		int day = index / 5;
		int offset = index % 5;

		if (cursor.getCount() > day)
		{
			cursor.moveToPosition(day);
			
			message = new Message();
			message.title = this._context.getString(R.string.note_title);
			
			switch(offset)
			{
				case 0:
					message.message = cursor.getString(cursor.getColumnIndex("prompt"));
					message.image = cursor.getString(cursor.getColumnIndex("prompt_img"));
					break;
				case 1:
					message.message = cursor.getString(cursor.getColumnIndex("first"));
					message.image = cursor.getString(cursor.getColumnIndex("first_img"));
					break;
				case 2:
					message.message = cursor.getString(cursor.getColumnIndex("second"));
					message.image = cursor.getString(cursor.getColumnIndex("second_img"));
					break;
				case 3:
					message.message = cursor.getString(cursor.getColumnIndex("third"));
					message.image = cursor.getString(cursor.getColumnIndex("third_img"));
					break;
				case 4:
					message.image = cursor.getString(cursor.getColumnIndex("fourth_img"));
					message.message = cursor.getString(cursor.getColumnIndex("fourth"));
					break;
			}
		}
		
		cursor.close();

		return message;
	}

	private long getNotificationTime(int index, long now) 
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this._context);

		long lastInst = prefs.getLong("last_instruction_notification", 0);
		
		Log.e("D2D", "OFF: " + this.getOffTime());
		
		if (index % 5 == 0 && now - lastInst < this.getOffTime())
			return -1;
		
		long firstRun = prefs.getLong(ScheduleManager.FIRST_RUN, 0);
		
		int startHour = Integer.parseInt(prefs.getString("config_day_start", "09"));
		int endHour = Integer.parseInt(prefs.getString("config_day_end", "21"));
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(now);
		
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);

		calendar.set(Calendar.HOUR_OF_DAY, startHour);
		long start = calendar.getTimeInMillis();

		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);

		calendar.set(Calendar.HOUR_OF_DAY, endHour);
		long end = calendar.getTimeInMillis();

		if (end < firstRun)
			return -1; // Running after 9pm...

		if (start < firstRun)
			start = firstRun;

		if (start > end)
			end += (24 * 60 * 60 * 1000);
		
		if (now > start && now < end)
		{
			long delta = (end - start) / 5;
	
			return start + (index * delta);
		}
		
		return -1;
	}

	private long getOffTime() 
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this._context);
		
		int startHour = Integer.parseInt(prefs.getString("config_day_start", "09"));
		int endHour = Integer.parseInt(prefs.getString("config_day_end", "21"));
		
		long now = System.currentTimeMillis();
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(now);
		
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);

		calendar.set(Calendar.HOUR_OF_DAY, startHour);
		long start = calendar.getTimeInMillis();

		calendar.set(Calendar.HOUR_OF_DAY, endHour);
		long end = calendar.getTimeInMillis();

		if (start > end)
			end += (24 * 60 * 60 * 1000);
		
		long onTime = end - start;
		
		return (24 * 60 * 60 * 1000) - onTime;
	}

	private class Message
	{
		public String image = null;
		public String title = null;
		public String message = null;
		
		public String toString()
		{
			return this.message;
		}
	}
}
