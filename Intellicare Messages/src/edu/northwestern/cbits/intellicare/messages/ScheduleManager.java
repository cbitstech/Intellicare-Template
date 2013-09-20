package edu.northwestern.cbits.intellicare.messages;

import java.util.Calendar;
import java.util.Date;

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
import edu.northwestern.cbits.intellicare.StatusNotificationManager;

public class ScheduleManager
{
	private static final String MESSAGE_INDEX = "message_index";
	private static final String FIRST_RUN = "first_run";
	public static final String INSTRUCTION_COMPLETED = "instruction_completed";
	public static final String IS_INSTRUCTION = "is_instruction";
	public static final String MESSAGE_TITLE = "message_title";
	public static final String MESSAGE_MESSAGE = "message_message";
	
	private static ScheduleManager _instance = null;

	private Context _context = null;

	public ScheduleManager(Context context) 
	{
		this._context  = context;
		
		AlarmManager alarm = (AlarmManager) this._context.getSystemService(Context.ALARM_SERVICE);
		
		Intent broadcast = new Intent(this._context, ScheduleHelper.class);
		
		PendingIntent pi = PendingIntent.getBroadcast(this._context, 0, broadcast, PendingIntent.FLAG_UPDATE_CURRENT);
		
//		alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 0, AlarmManager.INTERVAL_FIFTEEN_MINUTES, pi);
		alarm.setRepeating(AlarmManager.ELAPSED_REALTIME, 0, 60000, pi);
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
		
		if (prefs.contains(ScheduleManager.FIRST_RUN) == false)
		{
			Editor e = prefs.edit();
			e.putLong(ScheduleManager.FIRST_RUN, System.currentTimeMillis());
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

		Log.e("IM", "CURRENT_LESSON: " + currentLesson);
		
		boolean lessonComplete = prefs.getBoolean(LessonsActivity.LESSON_READ_PREFIX + currentLesson, false);
		
		if (lessonComplete)
		{
			int index = prefs.getInt(ScheduleManager.MESSAGE_INDEX, 0);
			
			long notificationTime = this.getNotificationTime(index % 5);
			
			Log.e("D2D", "NEXT NOTE TIME: " + (new Date(notificationTime)).toString());
			
			if (notificationTime > 0)
			{
				if (index > 0 && index % 5 == 0 && prefs.contains(ScheduleManager.INSTRUCTION_COMPLETED) == false)
					index -= 5;
				
				Message msg = this.getMessage(currentLesson, index);
				
				Log.e("D2D", "MSG " + index + " => " + msg);
				
				if (msg != null)
				{
					if (System.currentTimeMillis() > notificationTime)
					{
						Log.e("D2D", "NOTIFYING " + msg.message);
						
						Intent intent = new Intent(this._context, MessageRatingActivity.class);
						
						int id = 0;

						Editor e = prefs.edit();

						if (index % 5 == 0)
						{
							intent.putExtra(ScheduleManager.IS_INSTRUCTION, true);

							id = 1;
							msg.title = this._context.getString(R.string.note_instruction);
							
							e.remove(ScheduleManager.INSTRUCTION_COMPLETED);

							// Set colored icon
						}
						
						intent.putExtra(ScheduleManager.MESSAGE_MESSAGE, msg.message);
						intent.putExtra(ScheduleManager.MESSAGE_TITLE, msg.title);

						StatusNotificationManager.getInstance(this._context).notifyBigText(id, R.drawable.ic_notification, msg.title, msg.message, PendingIntent.getActivity(this._context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
						
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
			
			PendingIntent pi = PendingIntent.getActivity(this._context, 0, lessonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			
			Log.e("D2D", "NOTE : " + title + " (" + message + ")");
			
			ContentValues values = new ContentValues();
			values.put("complete", 1);

			String where = "id = ?";
			String[] whereArgs = { "" + currentLesson };
			
			int updated = this._context.getContentResolver().update(ContentProvider.LESSONS_URI, values, where, whereArgs);
			
			Log.e("D2D", "UPDATED: " + updated + " FOR " + currentLesson);

			StatusNotificationManager.getInstance(this._context).notifyBigText(0, R.drawable.ic_notification, title, message, pi);
		}
	}
	
	private Message getMessage(int lessonId, int index) 
	{
		if (index >= 5) // 35)
			return null;
		
		Log.e("D2D", "MSG INDEX: " + index + " / " + lessonId);
		
		String selection = "lesson_id = ?";
		String[] msgArgs = { "" + lessonId };

		Cursor cursor = this._context.getContentResolver().query(ContentProvider.MESSAGE_GROUPS_URI, null, selection, msgArgs, "group_order");
		
		Message message = null;
		
		int day = index / 5;
		int offset = index % 5;
		
		Log.e("D2D", "COUNT: " + cursor.getCount() + " vs. " + day + " . " + offset);
		
		if (cursor.getCount() > day)
		{
			cursor.moveToPosition(day);
			
			message = new Message();
			message.title = this._context.getString(R.string.note_title);
			
			switch(offset)
			{
				case 0:
					message.message = cursor.getString(cursor.getColumnIndex("prompt"));
					break;
				case 1:
					message.message = cursor.getString(cursor.getColumnIndex("first"));
					break;
				case 2:
					message.message = cursor.getString(cursor.getColumnIndex("second"));
					break;
				case 3:
					message.message = cursor.getString(cursor.getColumnIndex("third"));
					break;
				case 4:
					message.message = cursor.getString(cursor.getColumnIndex("fourth"));
					break;
			}
		}
		
		cursor.close();

		return message;
	}

	private long getNotificationTime(int index) 
	{
		if (1 < 2 * 3)
			return System.currentTimeMillis() - 5000;
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this._context);
		
		long firstRun = prefs.getLong(ScheduleManager.FIRST_RUN, 0);
		
		int startHour = Integer.parseInt(prefs.getString("config_day_start", "09"));
		int endHour = Integer.parseInt(prefs.getString("config_day_end", "21"));
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		
		calendar.set(Calendar.MINUTE, 0);

		calendar.set(Calendar.HOUR_OF_DAY, startHour);
		long start = calendar.getTimeInMillis();
		
		Log.e("D2D", "START: " + (new Date(start)).toString());

		calendar.set(Calendar.HOUR_OF_DAY, endHour);
		long end = calendar.getTimeInMillis();

		Log.e("D2D", "END: " + (new Date(end)).toString());

		if (end < firstRun)
			return -1; // Running after 9pm...

		if (start < firstRun)
			start = firstRun;

		if (start > end)
			end += (24 * 60 * 60 * 1000);
		
		Log.e("D2D", "USING INDEX " + index + "...");
		
		long delta = (end - start) / 5;

		return start + (index * delta);
	}

	private class Message
	{
		public String title = null;
		public String message = null;
	}
}
