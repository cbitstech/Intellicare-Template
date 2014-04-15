package edu.northwestern.cbits.intellicare.messages;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.CrashManagerListener;
import net.hockeyapp.android.UpdateManager;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import edu.northwestern.cbits.intellicare.ConsentActivity;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.messages.ScheduleManager.Message;

public class LaunchActivity extends ConsentedActivity 
{
	private static final String APP_ID = "455953ac6a6eb7c89be9af9848731279";

	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_launch);
		this.getSupportActionBar().setTitle(R.string.title_launch);

		UpdateManager.register(this, APP_ID);
	}
	
	@SuppressLint("SimpleDateFormat")
	public void onResume()
	{
		super.onResume();
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		long startToday = prefs.getLong("last_instruction_notification", 0);

		int currentLesson = prefs.getInt(LessonsActivity.LESSON_LEVEL, 0);
		
		if (currentLesson == 0)
			this.getSupportActionBar().setSubtitle(R.string.subtitle_launch_waiting);
		else
		{
			ScheduleManager schedule = ScheduleManager.getInstance(this);
	
			long now = System.currentTimeMillis();
			
			if (startToday == 0)
			{
				int startHour = Integer.parseInt(prefs.getString("config_day_start", "09"));
				
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(now);
				
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.SECOND, 0);
		
				calendar.set(Calendar.HOUR_OF_DAY, startHour);
				
				startToday = calendar.getTimeInMillis();
			}
			
			long end = startToday + (72 * 60 * 60 * 1000);
			
			int index = prefs.getInt(ScheduleManager.MESSAGE_INDEX, 0);
	
			Log.e("D2D", "INDEX: " + index);
	
			Date scheduled = null;
			
			Date nextScheduled = null;
			
			while (startToday < end && index < 35)
			{
				long time = schedule.getNotificationTime(index % 5, startToday);
				
				if (time != -1 && startToday > time && scheduled == null)
				{
					scheduled = new Date(time);
					
					index += 1;
				}
				
				if (time > now && nextScheduled == null)
					nextScheduled = new Date(time);
	
				startToday += (15 * 60 * 1000);
			}
			
			String next = this.getString(R.string.launch_date_none);
			
			if (scheduled != null && nextScheduled != null)
			{
				if (scheduled.getTime() < now)
					scheduled = new Date(scheduled.getTime() + (1000 * 60 * 60 * 24));
	
				SimpleDateFormat sdf = new SimpleDateFormat(this.getString(R.string.launch_date_format));
				
				next = this.getString(R.string.subtitle_launch, sdf.format(scheduled));
			}
			
			this.getSupportActionBar().setSubtitle(next);
		}
		
		if (ConsentActivity.isConsented() == true && prefs.getBoolean(HelpActivity.HELP_COMPLETED, false) == false)
			this.startActivity(new Intent(this, HelpActivity.class));
		
		CrashManager.register(this, APP_ID, new CrashManagerListener() 
		{
			public boolean shouldAutoUploadCrashes() 
			{
			    return true;
			}
		});
		
		this.refreshList();
	}
	
	private ArrayList<Message> fetchSeenMessages()
	{
		ArrayList<Message> messages = new ArrayList<Message>();
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		int index = prefs.getInt(ScheduleManager.MESSAGE_INDEX, 0);
		int currentLesson = prefs.getInt(LessonsActivity.LESSON_LEVEL, 0);
		
		Log.e("D2D", "INDEX " + index + " -- " + currentLesson);
		
		ArrayList<Integer> lessons = new ArrayList<Integer>();
		
		String where = "complete = ?";
		String[] args = { "1" };
		
		Cursor lessonCursor = this.getContentResolver().query(ContentProvider.LESSONS_URI, null, where, args, "lesson_order");
		
		while (lessonCursor.moveToNext())
		{
			lessons.add(lessonCursor.getInt(lessonCursor.getColumnIndex("id")));
		}
		
		lessonCursor.close();
		
		for (Integer lessonId : lessons)
		{
			if (lessonId != currentLesson)
			{
				for (int i = 0; i < 35; i++)
				{
					Message message = ScheduleManager.getInstance(this).getMessage(lessonId, i);

	    			String key = "response_" + lessonId + "_" + i;
	    			
	    			if (prefs.contains(key))
	    				messages.add(message);
				}
			}
		}
		
		if (currentLesson != 0)
		{
			for (int i = 0; i < index; i++)
			{
				Message message = ScheduleManager.getInstance(this).getMessage(currentLesson, i);

    			String key = "response_" + currentLesson + "_" + i;
    			
    			if (prefs.contains(key))
    				messages.add(message);
			}
		}
		
		Collections.reverse(messages);

		return messages;
	}
	
	private void refreshList() 
	{
		final LaunchActivity me = this;
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		final DateFormat dateFormat = android.text.format.DateFormat.getLongDateFormat(this);
		final DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(this);

		ListView listView = (ListView) this.findViewById(R.id.list_view);

		final ArrayList<Message> messages = this.fetchSeenMessages();
		
		ArrayAdapter<Message> adapter = new ArrayAdapter<Message>(this, R.layout.row_message, messages)
		{
			public View getView (int position, View convertView, ViewGroup parent)
			{
    			Context context = parent.getContext();

    			if (convertView == null)
    			{
    				LayoutInflater inflater = LayoutInflater.from(context);
    				convertView = inflater.inflate(R.layout.row_message, parent, false);
    			}
    			
    			TextView message = (TextView) convertView.findViewById(R.id.message_text);
    			
    			ImageView image = (ImageView) convertView.findViewById(R.id.icon);
    			
    			Message item = this.getItem(position);

    			if (item.index % 5 == 0)
    			{
    				image.setImageResource(R.drawable.ic_action_tick);
    				image.setVisibility(View.VISIBLE);
    			}
    			else
    				image.setVisibility(View.INVISIBLE);
    			
    			
    			message.setText(item.message);

    			TextView date = (TextView) convertView.findViewById(R.id.message_date);

    			String key = "response_" + item.lessonId + "_" + item.index;
    			
    			if (prefs.contains(key))
    			{
    				long timestamp = prefs.getLong(key, 0);
    				
    				Date response = new Date(timestamp);
    				
        			date.setText(timeFormat.format(response) + ", " + dateFormat.format(response));
    			}
    			else
        			date.setText(R.string.unknown_response_date);

    			return convertView;
			}
		};
		
		listView.setAdapter(adapter);
		
		listView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id)
			{
				Message message = messages.get(position);
				
				Uri uri = TipActivity.uriForTip(message);
				
				if (message.index % 5 == 0)
					uri = TaskActivity.uriForTask(message);
				
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(uri);
				
				me.startActivity(intent);
			}
		});
		
		listView.setEmptyView(this.findViewById(R.id.empty_list));
	}

	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_start, menu);

		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.action_settings)
		{
			Intent settingsIntent = new Intent(this, SettingsActivity.class);
			this.startActivity(settingsIntent);
		}
		else if (item.getItemId() == R.id.action_help)
		{
			Intent helpIntent = new Intent(this, HelpActivity.class);
			this.startActivity(helpIntent);
		}
		else if (item.getItemId() == R.id.action_feedback)
		{
			this.sendFeedback(this.getString(R.string.app_name));
		}
		
		return true;
	}
}
