package edu.northwestern.cbits.intellicare.mantra.activities;

import java.util.HashMap;

import edu.northwestern.cbits.intellicare.mantra.Constants;
import edu.northwestern.cbits.intellicare.mantra.R;
import edu.northwestern.cbits.intellicare.mantra.R.string;
import edu.northwestern.cbits.intellicare.mantra.R.xml;

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
//import edu.northwestern.cbits.intellicare.ConsentedActivity;
//import edu.northwestern.cbits.intellicare.logging.LogManager;

public class SettingsActivity extends PreferenceActivity 
{
	private static final String CN = "SettingsActivity";

	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		this.setTitle(R.string.title_activity_settings);
		
		this.addPreferencesFromResource(R.xml.preferences);
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
		
		if (key == null)
		{
			
		}
		else if (key.equals("start_of_day_time") || key.equals("end_of_day_time"))
		{
			final String hourKey = key.equals("end_of_day_time") ? Constants.REMINDER_END_HOUR : Constants.REMINDER_START_HOUR;
			final String minuteKey = key.equals("end_of_day_time") ? Constants.REMINDER_END_MINUTE : Constants.REMINDER_START_MINUTE;
			
			final SettingsActivity me = this;
			
			final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			
			TimePickerDialog dialog = new TimePickerDialog(this, new OnTimeSetListener()
			{
				public void onTimeSet(TimePicker arg0, int hour, int minute) 
				{
			        Editor editor = prefs.edit();
			        
			        editor.putInt(hourKey, hour);
			        editor.putInt(minuteKey, minute);
			        editor.commit();
			        
//					HashMap<String, Object> payload = new HashMap<String, Object>();
//					payload.put("hour", hour);
//					payload.put("minute", minute);
//					payload.put("source", "settings");
//					
//					payload.put("full_mode", prefs.getBoolean("settings_full_mode", true));
//					LogManager.getInstance(me).log("set_reminder_time", payload);
				}
			}, 
			prefs.getInt(hourKey, 18), 
			prefs.getInt(minuteKey, 0), 
			DateFormat.is24HourFormat(this));
			
			dialog.show();

			return true;
		}
//		else if (key.equals("copyright_statement"))
//			ConsentedActivity.showCopyrightDialog(this);
		
		return super.onPreferenceTreeClick(screen, preference);
	}

}

