package edu.northwestern.cbits.intellicare.messages;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class LaunchActivity extends ConsentedActivity 
{
	private static final String APP_ID = "455953ac6a6eb7c89be9af9848731279";

	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_launch);
		this.getSupportActionBar().setTitle(R.string.app_name);
	}
	
	@SuppressLint("SimpleDateFormat")
	public void onResume()
	{
		super.onResume();
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		long startToday = prefs.getLong("last_instruction_notification", 0);

		ScheduleManager schedule = ScheduleManager.getInstance(this);

		if (startToday == 0)
		{
			int startHour = Integer.parseInt(prefs.getString("config_day_start", "09"));
			
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(System.currentTimeMillis());
			
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
	
			calendar.set(Calendar.HOUR_OF_DAY, startHour);
			
			startToday = calendar.getTimeInMillis();
		}
		
		long end = startToday + (72 * 60 * 60 * 1000);
		
		int index = prefs.getInt(ScheduleManager.MESSAGE_INDEX, 0);
		
		Date scheduled = null;
		
		while (startToday < end && index < 35)
		{
			long time = schedule.getNotificationTime(index % 5, startToday);
			
			if (time != -1 && startToday > time && scheduled == null)
			{
				scheduled = new Date(time);
				
				index += 1;
			}

			startToday += (15 * 60 * 1000);
		}

		TextView scheduledText = (TextView) this.findViewById(R.id.next_schedule_time);

		if (scheduled != null)
		{
			SimpleDateFormat sdf = new SimpleDateFormat(this.getString(R.string.launch_date_format));
			scheduledText.setText(sdf.format(scheduled));
		}
		else
			scheduledText.setText(R.string.launch_date_none);
		
		UpdateManager.register(this, APP_ID);
		CrashManager.register(this, APP_ID);
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
		
		return true;
	}
}
