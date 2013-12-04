package edu.northwestern.cbits.intellicare.messages;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class DiagnosticActivity extends ConsentedActivity 
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_diagnostic);
		this.getSupportActionBar().setTitle(R.string.fire_dates_title);
	}
	
	@SuppressLint("UseSparseArrays")
	public void onResume()
	{
		super.onResume();
		
		final HashMap<Integer, Long> todaySchedule = new HashMap<Integer, Long>();
		final ArrayList<Integer> todayIds = new ArrayList<Integer>();
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		long startToday = System.currentTimeMillis(); // prefs.getLong("last_instruction_notification", 0);

		ScheduleManager schedule = ScheduleManager.getInstance(this);

		/*
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
*/		
		long end = startToday + (72 * 60 * 60 * 1000);
		
		int index = prefs.getInt(ScheduleManager.MESSAGE_INDEX, 0);
		
		while (startToday < end && index < 35)
		{
			long time = schedule.getNotificationTime(index % 5, startToday);
			
			if (time != -1 && startToday > time && Math.abs(startToday - time) < (30 * 60 * 1000))
			{
				todaySchedule.put(Integer.valueOf(index), Long.valueOf(time));
				todayIds.add(Integer.valueOf(index));
				
				index += 1;
			}

			startToday += (15 * 60 * 1000);
		}

		ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this, R.layout.row_schedule_time, todayIds)
		{
			public View getView(int position, View convertView, ViewGroup parent)
			{
				if (convertView == null)
				{
					LayoutInflater inflater = LayoutInflater.from(parent.getContext());
					convertView = inflater.inflate(R.layout.row_schedule_time, parent, false);
				}
				
				Integer index = todayIds.get(position);
				Long time = todaySchedule.get(index);
				
				TextView title = (TextView) convertView.findViewById(R.id.time_text);
				title.setText(index + ": " + (new Date(time.longValue())));

				return convertView;
			}
		};

		ListView list = (ListView) this.findViewById(R.id.list_view);
		list.setAdapter(adapter);
	}
}
