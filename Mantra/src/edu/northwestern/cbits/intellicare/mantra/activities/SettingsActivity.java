package edu.northwestern.cbits.intellicare.mantra.activities;

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
import android.util.Log;
import android.widget.TimePicker;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;
import edu.northwestern.cbits.intellicare.mantra.Constants;
import edu.northwestern.cbits.intellicare.mantra.R;

public class SettingsActivity extends PreferenceActivity 
{
	private static final String CN = "SettingsActivity";
	
	private static final String START_DAY_KEY = "start_of_day_time";
	private static final String END_DAY_KEY = "end_of_day_time";

	private static final int DEFAULT_START_HOUR = 9;
	private static final int DEFAULT_END_HOUR = 18;

	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		this.setTitle(R.string.title_activity_settings);
		this.addPreferencesFromResource(R.layout.activity_settings);
	}
	
	public void onResume()
	{
		super.onResume();
		
		// LogManager.getInstance(this).log("opened_settings", null);
		Log.d(CN+".onResume", "opened_settings");
	}
	
	public void onPause()
	{
		// LogManager.getInstance(this).log("closed_settings", null);
		Log.d(CN+".onResume", "closed_settings");
		
		super.onPause();
	}

	@SuppressWarnings("deprecation")
	public boolean onPreferenceTreeClick (PreferenceScreen screen, Preference preference)
	{
		String key = preference.getKey();
		
		final SettingsActivity me = this;
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		
		if (key == null)
		{
			
		}
		else if (SettingsActivity.START_DAY_KEY.equalsIgnoreCase(key))
		{
			TimePickerDialog dialog = new TimePickerDialog(this, new OnTimeSetListener()
			{
				public void onTimeSet(TimePicker arg0, int hour, int minute) 
				{
			        Editor editor = prefs.edit();
			        
			        editor.putInt(Constants.REMINDER_START_HOUR, hour);
			        editor.putInt(Constants.REMINDER_START_MINUTE, minute);
			        editor.commit();
			        
					HashMap<String, Object> payload = new HashMap<String, Object>();
					payload.put("hour", hour);
					payload.put("minute", minute);
					payload.put("source", "settings");
					
					LogManager.getInstance(me).log("set_reminder_time", payload);
				}
			}, prefs.getInt(Constants.REMINDER_START_HOUR, SettingsActivity.DEFAULT_START_HOUR), 
			   prefs.getInt(Constants.REMINDER_START_MINUTE, 0), DateFormat.is24HourFormat(this));
			
			dialog.show();

			return true;
		}
		else if (SettingsActivity.END_DAY_KEY.equalsIgnoreCase(key))
		{
			TimePickerDialog dialog = new TimePickerDialog(this, new OnTimeSetListener()
			{
				public void onTimeSet(TimePicker arg0, int hour, int minute) 
				{
			        Editor editor = prefs.edit();
			        
			        editor.putInt(Constants.REMINDER_END_HOUR, hour);
			        editor.putInt(Constants.REMINDER_END_MINUTE, minute);
			        editor.commit();
			        
					HashMap<String, Object> payload = new HashMap<String, Object>();
					payload.put("hour", hour);
					payload.put("minute", minute);
					payload.put("source", "settings");
					
					LogManager.getInstance(me).log("set_reminder_time", payload);
				}
			}, prefs.getInt(Constants.REMINDER_END_HOUR, SettingsActivity.DEFAULT_END_HOUR), 
			   prefs.getInt(Constants.REMINDER_END_MINUTE, 0), DateFormat.is24HourFormat(this));
			
			dialog.show();

			return true;
		}
		else if (key.equals("copyright_statement"))
			ConsentedActivity.showCopyrightDialog(this);
		
		return super.onPreferenceTreeClick(screen, preference);
	}
}