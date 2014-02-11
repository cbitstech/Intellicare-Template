package edu.northwestern.cbits.intellicare.dailyfeats;

import java.util.HashMap;

import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.format.DateFormat;
import android.widget.TimePicker;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class SettingsActivity extends PreferenceActivity 
{
	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		this.setTitle(R.string.title_settings);
		
		this.addPreferencesFromResource(R.layout.activity_settings);
	}
	
	public void onResume()
	{
		super.onResume();
		
		HashMap<String, Object> payload = new HashMap<String, Object>();
		LogManager.getInstance(this).log("opened_settings", payload);
	}
	
	public void onPause()
	{
		HashMap<String, Object> payload = new HashMap<String, Object>();
		LogManager.getInstance(this).log("closed_settings", payload);
		
		super.onPause();
	}

	@SuppressWarnings("deprecation")
	public boolean onPreferenceTreeClick (PreferenceScreen screen, Preference preference)
	{
		if (preference.getKey().equals("settings_reminder_time"))
		{
			final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			
			TimePickerDialog dialog = new TimePickerDialog(this, new OnTimeSetListener()
			{
				public void onTimeSet(TimePicker arg0, int hour, int minute) 
				{
			        Editor editor = prefs.edit();
			        
			        editor.putInt(ScheduleManager.REMINDER_HOUR, hour);
			        editor.putInt(ScheduleManager.REMINDER_MINUTE, minute);
			        editor.commit();
				}
			}, prefs.getInt(ScheduleManager.REMINDER_HOUR, 18), prefs.getInt(ScheduleManager.REMINDER_MINUTE, 0), DateFormat.is24HourFormat(this));
			
			dialog.show();

			return true;
		}
		
		return super.onPreferenceTreeClick(screen, preference);
	}
}
