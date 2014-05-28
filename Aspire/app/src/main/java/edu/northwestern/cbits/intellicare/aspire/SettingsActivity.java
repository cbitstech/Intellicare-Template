package edu.northwestern.cbits.intellicare.aspire;

import java.util.HashMap;

import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TimePicker;

public class SettingsActivity extends PreferenceActivity 
{
	protected static final String REMINDER_SUNDAY = "reminder_sunday";
	protected static final String REMINDER_MONDAY = "reminder_monday";
	protected static final String REMINDER_TUESDAY = "reminder_tuesday";
	protected static final String REMINDER_WEDNESDAY = "reminder_wednesday";
	protected static final String REMINDER_THURSDAY = "reminder_thursday";
	protected static final String REMINDER_FRIDAY = "reminder_friday";
	protected static final String REMINDER_SATURDAY = "reminder_saturday";
	protected static final String REMINDER_HOUR = "reminder_hour";
	protected static final String REMINDER_MINUTE = "reminder_minute";

	protected static final String GRAPH_SUNDAY = "graph_sunday";
	protected static final String GRAPH_MONDAY = "graph_monday";
	protected static final String GRAPH_TUESDAY = "graph_tuesday";
	protected static final String GRAPH_WEDNESDAY = "graph_wednesday";
	protected static final String GRAPH_THURSDAY = "graph_thursday";
	protected static final String GRAPH_FRIDAY = "graph_friday";
	protected static final String GRAPH_SATURDAY = "graph_saturday";
	protected static final String GRAPH_HOUR = "graph_hour";
	protected static final String GRAPH_MINUTE = "graph_minute";

	protected static final String LAST_REMINDER = "last_reminder";
	protected static final String LAST_GRAPH = "last_graph";

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
		
		LogManager.getInstance(this).log("opened_settings", null);
	}
	
	public void onPause()
	{
		LogManager.getInstance(this).log("closed_settings", null);
		
		super.onPause();
	}

	@SuppressWarnings("deprecation")
	public boolean onPreferenceTreeClick (PreferenceScreen screen, Preference preference)
	{
		String key = preference.getKey();
		
		final SettingsActivity me = this;
		
		if (key == null)
		{
			
		}
		else if (key.equals("copyright_statement"))
			ConsentedActivity.showCopyrightDialog(this);
		else if (key.equals("reminder_schedule"))
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.reminder_schedule);
			
			final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(me);

			LayoutInflater inflater = LayoutInflater.from(this);
			final View view = inflater.inflate(R.layout.view_schedule, null, false);
			
			final CheckBox sunday = (CheckBox) view.findViewById(R.id.check_sun);
			final CheckBox monday = (CheckBox) view.findViewById(R.id.check_mon);
			final CheckBox tuesday = (CheckBox) view.findViewById(R.id.check_tue);
			final CheckBox wednesday = (CheckBox) view.findViewById(R.id.check_wed);
			final CheckBox thursday = (CheckBox) view.findViewById(R.id.check_thu);
			final CheckBox friday = (CheckBox) view.findViewById(R.id.check_fri);
			final CheckBox saturday = (CheckBox) view.findViewById(R.id.check_sat);

			final TimePicker timePicker = (TimePicker) view.findViewById(R.id.time_picker);

			builder.setView(view);
			
			sunday.setChecked(prefs.getBoolean(SettingsActivity.REMINDER_SUNDAY, false));
			monday.setChecked(prefs.getBoolean(SettingsActivity.REMINDER_MONDAY, true));
			tuesday.setChecked(prefs.getBoolean(SettingsActivity.REMINDER_TUESDAY, true));
			wednesday.setChecked(prefs.getBoolean(SettingsActivity.REMINDER_WEDNESDAY, true));
			thursday.setChecked(prefs.getBoolean(SettingsActivity.REMINDER_THURSDAY, true));
			friday.setChecked(prefs.getBoolean(SettingsActivity.REMINDER_FRIDAY, true));
			saturday.setChecked(prefs.getBoolean(SettingsActivity.REMINDER_SATURDAY, false));
			
			timePicker.setCurrentHour(prefs.getInt(SettingsActivity.REMINDER_HOUR, 9));
			timePicker.setCurrentMinute(prefs.getInt(SettingsActivity.REMINDER_MINUTE, 0));
			
			builder.setPositiveButton(R.string.action_schedule, new OnClickListener()
			{
				public void onClick(DialogInterface arg0, int which) 
				{
					Editor e = prefs.edit();
					
					int hour = timePicker.getCurrentHour();
					int minute = timePicker.getCurrentMinute();

					e.putBoolean(SettingsActivity.REMINDER_SUNDAY, sunday.isChecked());
					e.putBoolean(SettingsActivity.REMINDER_MONDAY, monday.isChecked());
					e.putBoolean(SettingsActivity.REMINDER_TUESDAY, tuesday.isChecked());
					e.putBoolean(SettingsActivity.REMINDER_WEDNESDAY, wednesday.isChecked());
					e.putBoolean(SettingsActivity.REMINDER_THURSDAY, thursday.isChecked());
					e.putBoolean(SettingsActivity.REMINDER_FRIDAY, friday.isChecked());
					e.putBoolean(SettingsActivity.REMINDER_SATURDAY, saturday.isChecked());
					e.putInt(SettingsActivity.REMINDER_HOUR, hour);
					e.putInt(SettingsActivity.REMINDER_MINUTE, minute);

					e.commit();
					
					HashMap<String, Object> payload = new HashMap<String, Object>();
					payload.put("hour", hour);
					payload.put("minute", minute);
					payload.put("sunday", sunday.isChecked());
					payload.put("monday", monday.isChecked());
					payload.put("tuesday", tuesday.isChecked());
					payload.put("wednesday", wednesday.isChecked());
					payload.put("thursday", thursday.isChecked());
					payload.put("friday", friday.isChecked());
					payload.put("saturday", saturday.isChecked());
					
					LogManager.getInstance(me).log("scheduled_reminder", payload);
				}
			});

			builder.setNegativeButton(R.string.action_not_now, new OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which) 
				{
					HashMap<String, Object> payload = new HashMap<String, Object>();
					LogManager.getInstance(me).log("canceled_scheduled_reminder", payload);
				}
			});

			builder.create().show();
		}
		else if (key.equals("graph_schedule"))
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.graph_schedule);
			
			final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(me);

			LayoutInflater inflater = LayoutInflater.from(this);
			final View view = inflater.inflate(R.layout.view_schedule, null, false);
			
			final CheckBox sunday = (CheckBox) view.findViewById(R.id.check_sun);
			final CheckBox monday = (CheckBox) view.findViewById(R.id.check_mon);
			final CheckBox tuesday = (CheckBox) view.findViewById(R.id.check_tue);
			final CheckBox wednesday = (CheckBox) view.findViewById(R.id.check_wed);
			final CheckBox thursday = (CheckBox) view.findViewById(R.id.check_thu);
			final CheckBox friday = (CheckBox) view.findViewById(R.id.check_fri);
			final CheckBox saturday = (CheckBox) view.findViewById(R.id.check_sat);

			final TimePicker timePicker = (TimePicker) view.findViewById(R.id.time_picker);

			builder.setView(view);
			
			sunday.setChecked(prefs.getBoolean(SettingsActivity.GRAPH_SUNDAY, false));
			monday.setChecked(prefs.getBoolean(SettingsActivity.GRAPH_MONDAY, true));
			tuesday.setChecked(prefs.getBoolean(SettingsActivity.GRAPH_TUESDAY, true));
			wednesday.setChecked(prefs.getBoolean(SettingsActivity.GRAPH_WEDNESDAY, true));
			thursday.setChecked(prefs.getBoolean(SettingsActivity.GRAPH_THURSDAY, true));
			friday.setChecked(prefs.getBoolean(SettingsActivity.GRAPH_FRIDAY, true));
			saturday.setChecked(prefs.getBoolean(SettingsActivity.GRAPH_SATURDAY, false));
			
			timePicker.setCurrentHour(prefs.getInt(SettingsActivity.GRAPH_HOUR, 19));
			timePicker.setCurrentMinute(prefs.getInt(SettingsActivity.GRAPH_MINUTE, 0));
			
			builder.setPositiveButton(R.string.action_schedule, new OnClickListener()
			{
				public void onClick(DialogInterface arg0, int which) 
				{
					Editor e = prefs.edit();
					
					int hour = timePicker.getCurrentHour();
					int minute = timePicker.getCurrentMinute();

					e.putBoolean(SettingsActivity.GRAPH_SUNDAY, sunday.isChecked());
					e.putBoolean(SettingsActivity.GRAPH_MONDAY, monday.isChecked());
					e.putBoolean(SettingsActivity.GRAPH_TUESDAY, tuesday.isChecked());
					e.putBoolean(SettingsActivity.GRAPH_WEDNESDAY, wednesday.isChecked());
					e.putBoolean(SettingsActivity.GRAPH_THURSDAY, thursday.isChecked());
					e.putBoolean(SettingsActivity.GRAPH_FRIDAY, friday.isChecked());
					e.putBoolean(SettingsActivity.GRAPH_SATURDAY, saturday.isChecked());
					e.putInt(SettingsActivity.GRAPH_HOUR, hour);
					e.putInt(SettingsActivity.GRAPH_MINUTE, minute);

					e.commit();
					
					HashMap<String, Object> payload = new HashMap<String, Object>();
					payload.put("hour", hour);
					payload.put("minute", minute);
					payload.put("sunday", sunday.isChecked());
					payload.put("monday", monday.isChecked());
					payload.put("tuesday", tuesday.isChecked());
					payload.put("wednesday", wednesday.isChecked());
					payload.put("thursday", thursday.isChecked());
					payload.put("friday", friday.isChecked());
					payload.put("saturday", saturday.isChecked());
					
					LogManager.getInstance(me).log("scheduled_graph", payload);
				}
			});

			builder.setNegativeButton(R.string.action_not_now, new OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which) 
				{
					HashMap<String, Object> payload = new HashMap<String, Object>();
					LogManager.getInstance(me).log("canceled_scheduled_graph", payload);
				}
			});

			builder.create().show();
		}

		return super.onPreferenceTreeClick(screen, preference);
	}
}
