package edu.northwestern.cbits.intellicare.relax;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TimePicker;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class SettingsActivity extends PreferenceActivity 
{
	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		this.setTitle(R.string.title_settings);
		
		this.addPreferencesFromResource(R.layout.settings_activity);

        Preference version = this.findPreference("app_version");
        try
        {
            version.setTitle(this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName);
        }
        catch (PackageManager.NameNotFoundException e)
        {
            LogManager.getInstance(this).logException(e);
        }
	}
	
	protected void onResume()
	{
		super.onResume();
	}

	@SuppressWarnings("deprecation")
	public boolean onPreferenceTreeClick (PreferenceScreen screen, Preference preference)
	{
		String key = preference.getKey();
		
		if (key == null)
		{
			
		}
		else if (key.equals("copyright_statement"))
			ConsentedActivity.showCopyrightDialog(this);
		else if (key.equals("config_notification_time"))
		{
			final SettingsActivity me = this;

			final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(me);

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.title_schedule_reminder);
			
			LayoutInflater inflater = LayoutInflater.from(this);
			final View view = inflater.inflate(R.layout.view_schedule, null, false);

			TimePicker timePicker = (TimePicker) view.findViewById(R.id.time_picker);
			timePicker.setCurrentHour(prefs.getInt("config_notification_hour", 15));
			timePicker.setCurrentMinute(prefs.getInt("config_notification_minute", 0));

			CheckBox sunday = (CheckBox) view.findViewById(R.id.check_sun);
			sunday.setChecked(prefs.getBoolean("config_remind_sunday", false));
			
			CheckBox monday = (CheckBox) view.findViewById(R.id.check_mon);
			monday.setChecked(prefs.getBoolean("config_remind_monday", false));

			CheckBox tuesday = (CheckBox) view.findViewById(R.id.check_tue);
			tuesday.setChecked(prefs.getBoolean("config_remind_tuesday", false));

			CheckBox wednesday = (CheckBox) view.findViewById(R.id.check_wed);
			wednesday.setChecked(prefs.getBoolean("config_remind_wednesday", false));

			CheckBox thursday = (CheckBox) view.findViewById(R.id.check_thu);
			thursday.setChecked(prefs.getBoolean("config_remind_thursday", false));

			CheckBox friday = (CheckBox) view.findViewById(R.id.check_fri);
			friday.setChecked(prefs.getBoolean("config_remind_friday", false));

			CheckBox saturday = (CheckBox) view.findViewById(R.id.check_sat);
			saturday.setChecked(prefs.getBoolean("config_remind_saturday", false));
			
			builder.setView(view);
			
			builder.setPositiveButton(R.string.action_schedule, new OnClickListener()
			{
				public void onClick(DialogInterface arg0, int which) 
				{
					TimePicker timePicker = (TimePicker) view.findViewById(R.id.time_picker);
					
					CheckBox sunday = (CheckBox) view.findViewById(R.id.check_sun);
					CheckBox monday = (CheckBox) view.findViewById(R.id.check_mon);
					CheckBox tuesday = (CheckBox) view.findViewById(R.id.check_tue);
					CheckBox wednesday = (CheckBox) view.findViewById(R.id.check_wed);
					CheckBox thursday = (CheckBox) view.findViewById(R.id.check_thu);
					CheckBox friday = (CheckBox) view.findViewById(R.id.check_fri);
					CheckBox saturday = (CheckBox) view.findViewById(R.id.check_sat);
					
					int hour = timePicker.getCurrentHour();
					int minute = timePicker.getCurrentMinute();
					
					Editor e = prefs.edit();
					e.putBoolean("config_remind_sunday", sunday.isChecked());
					e.putBoolean("config_remind_monday", monday.isChecked());
					e.putBoolean("config_remind_tuesday", tuesday.isChecked());
					e.putBoolean("config_remind_wednesday", wednesday.isChecked());
					e.putBoolean("config_remind_thursday", thursday.isChecked());
					e.putBoolean("config_remind_friday", friday.isChecked());
					e.putBoolean("config_remind_saturday", saturday.isChecked());

					e.putInt("config_notification_hour", hour);
					e.putInt("config_notification_minute", minute);

					e.commit();
				}
			});

			builder.create().show();
			
		}

		return super.onPreferenceTreeClick(screen, preference);
	}
}
