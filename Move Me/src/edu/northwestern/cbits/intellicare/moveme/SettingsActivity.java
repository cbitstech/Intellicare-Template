package edu.northwestern.cbits.intellicare.moveme;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.TimePicker;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;
import edu.northwestern.cbits.intellicare.oauth.FitbitApi;
import edu.northwestern.cbits.intellicare.oauth.OAuthActivity;

public class SettingsActivity extends PreferenceActivity 
{
	private enum TimeOfDay
	{
		MORNING,
		EVENING,
		AFTERNOON,
	}
	
	public static final String GOOGLE_PLAY_PACKAGE = "com.google.android.music";
	public static final String SPOTIFY_PACKAGE = "com.spotify.mobile.android.ui";
	public static final String PANDORA_PACKAGE = "com.pandora.android";
	public static final String SIRIUSXM_PACKAGE = "com.sirius";
	public static final String AMAZON_MP3_PACKAGE = "com.amazon.mp3";

	public static final String SETTING_PLAYER = "settings_music_player";
	public static final String DEFAULT_PACKAGE = "default";
	
	public static final String SETTING_MORNING_ENABLED = "settings_enable_morning_note";
	public static final String SETTING_MORNING_TIME = "settings_time_morning_note";
	public static final String SETTING_AFTERNOON_ENABLED = "settings_enable_afternoon_note";
	public static final String SETTING_AFTERNOON_TIME = "settings_time_afternoon_note";
	public static final String SETTING_EVENING_ENABLED = "settings_enable_evening_note";
	public static final String SETTING_EVENING_TIME = "settings_time_evening_note";

	public static final boolean SETTING_MORNING_ENABLED_DEFAULT = true;
	public static final boolean SETTING_EVENING_ENABLED_DEFAULT = true;
	public static final boolean SETTING_AFTERNOON_ENABLED_DEFAULT = true;

	public static final String REMINDER_MORNING_SUNDAY = "settings_reminder_sunday_morning";
	public static final String REMINDER_MORNING_MONDAY = "settings_reminder_monday_morning";
	public static final String REMINDER_MORNING_TUESDAY = "settings_reminder_tuesday_morning";
	public static final String REMINDER_MORNING_WEDNESDAY = "settings_reminder_wednesday_morning";
	public static final String REMINDER_MORNING_THURSDAY = "settings_reminder_thursday_morning";
	public static final String REMINDER_MORNING_FRIDAY = "settings_reminder_friday_morning";
	public static final String REMINDER_MORNING_SATURDAY = "settings_reminder_saturday_morning";
	public static final String REMINDER_MORNING_HOUR = "settings_reminder_hour_morning";
	public static final String REMINDER_MORNING_MINUTE = "settings_reminder_minute_morning";
	public static final int SETTING_MORNING_HOUR_DEFAULT = 7;
	public static final int SETTING_MORNING_MINUTE_DEFAULT = 30;
	
	public static final String REMINDER_AFTERNOON_SUNDAY = "settings_reminder_sunnday_afternoon";
	public static final String REMINDER_AFTERNOON_MONDAY = "settings_reminder_monday_afternoon";
	public static final String REMINDER_AFTERNOON_TUESDAY = "settings_reminder_tuesday_afternoon";
	public static final String REMINDER_AFTERNOON_WEDNESDAY = "settings_reminder_wednesday_afternoon";
	public static final String REMINDER_AFTERNOON_THURSDAY = "settings_reminder_thursday_afternoon";
	public static final String REMINDER_AFTERNOON_FRIDAY = "settings_reminder_friday_afternoon";
	public static final String REMINDER_AFTERNOON_SATURDAY = "settings_reminder_saturday_afternoon";
	public static final String REMINDER_AFTERNOON_HOUR = "settings_reminder_hour_afternoon";
	public static final String REMINDER_AFTERNOON_MINUTE = "settings_reminder_minute_afternoon";
	public static final int SETTING_AFTERNOON_HOUR_DEFAULT = 13;
	public static final int SETTING_AFTERNOON_MINUTE_DEFAULT = 0;
	
	public static final String REMINDER_EVENING_SUNDAY = "settings_reminder_sunday_evening";
	public static final String REMINDER_EVENING_MONDAY = "settings_reminder_monday_evening";
	public static final String REMINDER_EVENING_TUESDAY = "settings_reminder_tuesday_evening";
	public static final String REMINDER_EVENING_WEDNESDAY = "settings_reminder_wednessday_evening";
	public static final String REMINDER_EVENING_THURSDAY = "settings_reminder_thursday_evening";
	public static final String REMINDER_EVENING_FRIDAY = "settings_reminder_friday_evening";
	public static final String REMINDER_EVENING_SATURDAY = "settings_reminder_saturday_evening";
	public static final String REMINDER_EVENING_HOUR = "settings_reminder_hour_evening";
	public static final String REMINDER_EVENING_MINUTE = "settings_reminder_minute_evening";
	public static final int SETTING_EVENING_HOUR_DEFAULT = 18;
	public static final int SETTING_EVENING_MINUTE_DEFAULT = 0;

	public static final String OAUTH_FITBIT_TOKEN = "oauth_fitbit_token";
	public static final String SETTING_FITBIT_LOGIN = "settings_fitbit_login";
	public static final String SETTING_FITBIT_ENABLED = "settings_fitbit_enabled";
	public static final String OAUTH_FITBIT_SECRET = "oauth_fitbit_secret";

	public static final boolean SETTING_FITBIT_ENABLED_DEFAULT = false;

	public static final boolean SUNDAY_ENABLED_DEFAULT = false;
	public static final boolean MONDAY_ENABLED_DEFAULT = true;
	public static final boolean TUESDAY_ENABLED_DEFAULT = true;
	public static final boolean WEDNESDAY_ENABLED_DEFAULT = true;
	public static final boolean THURSDAY_ENABLED_DEFAULT = true;
	public static final boolean FRIDAY_ENABLED_DEFAULT = true;
	public static final boolean SATURDAY_ENABLED_DEFAULT = false;
	
	public static final String SETTING_DAILY_GOAL = "settings_daily_goal";
	public static final int SETTING_DAILY_GOAL_DEFAULT = 30;
	
	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		this.setTitle(R.string.title_settings);
		
		this.addPreferencesFromResource(R.layout.activity_settings);
		
		ArrayList<String> players = new ArrayList<String>();
		ArrayList<String> packages = new ArrayList<String>();
		
		PackageManager packageManager = this.getPackageManager();
		
		try 
		{
			packageManager.getPackageInfo(GOOGLE_PLAY_PACKAGE, 0);
			
			players.add(this.getString(R.string.player_google_play));
			packages.add(GOOGLE_PLAY_PACKAGE);
		} 
		catch (NameNotFoundException e) 
		{
			
		}

		try 
		{
			packageManager.getPackageInfo(SPOTIFY_PACKAGE, 0);
			
			players.add(this.getString(R.string.player_spotify));
			packages.add(SPOTIFY_PACKAGE);
		} 
		catch (NameNotFoundException e) 
		{
			
		}

		try 
		{
			packageManager.getPackageInfo(PANDORA_PACKAGE, 0);
			
			players.add(this.getString(R.string.player_pandora));
			packages.add(PANDORA_PACKAGE);
		} 
		catch (NameNotFoundException e) 
		{
			
		}

		try 
		{
			packageManager.getPackageInfo(SIRIUSXM_PACKAGE, 0);
			
			players.add(this.getString(R.string.player_siriusxm));
			packages.add(SIRIUSXM_PACKAGE);
		} 
		catch (NameNotFoundException e) 
		{
			
		}

		try 
		{
			packageManager.getPackageInfo(AMAZON_MP3_PACKAGE, 0);
			
			players.add(this.getString(R.string.player_amazon_mp3));
			packages.add(PANDORA_PACKAGE);
		} 
		catch (NameNotFoundException e) 
		{
			
		}
		
		players.add(this.getString(R.string.player_default));
		packages.add(DEFAULT_PACKAGE);
		
		String[] playerArray = players.toArray(new String[0]);
		String[] packageArray = packages.toArray(new String[0]);
		
		ListPreference playerPref = (ListPreference) this.findPreference(SettingsActivity.SETTING_PLAYER);
		playerPref.setEntries(playerArray);
		playerPref.setEntryValues(packageArray);

		final SettingsActivity me = this;
		
		CheckBoxPreference morningEnabled = (CheckBoxPreference) this.findPreference(SettingsActivity.SETTING_MORNING_ENABLED);
		final Preference morningTime = this.findPreference(SettingsActivity.SETTING_MORNING_TIME);
		
		morningEnabled.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{
			public boolean onPreferenceChange(Preference arg0, Object value) 
			{
				if (value instanceof Boolean)
				{
					Boolean enabled = (Boolean) value;
					
					morningTime.setEnabled(enabled.booleanValue());
				}

				return true;
			}
		});
		
		morningTime.setOnPreferenceClickListener(new OnPreferenceClickListener()
		{
			public boolean onPreferenceClick(Preference arg0) 
			{
				me.fetchSchedule(TimeOfDay.MORNING, R.string.pref_morning_time);
				
				return true;
			}
		});
		
		CheckBoxPreference afternoonEnabled = (CheckBoxPreference) this.findPreference(SettingsActivity.SETTING_AFTERNOON_ENABLED);
		final Preference afternoonTime = this.findPreference(SettingsActivity.SETTING_AFTERNOON_TIME);

		afternoonEnabled.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{
			public boolean onPreferenceChange(Preference arg0, Object value) 
			{
				if (value instanceof Boolean)
				{
					Boolean enabled = (Boolean) value;
					
					afternoonTime.setEnabled(enabled.booleanValue());
				}

				return true;
			}
		});

		afternoonTime.setOnPreferenceClickListener(new OnPreferenceClickListener()
		{
			public boolean onPreferenceClick(Preference arg0) 
			{
				me.fetchSchedule(TimeOfDay.AFTERNOON, R.string.pref_morning_time);
				
				return true;
			}
		});

		CheckBoxPreference eveningEnabled = (CheckBoxPreference) this.findPreference(SettingsActivity.SETTING_EVENING_ENABLED);
		final Preference eveningTime = this.findPreference(SettingsActivity.SETTING_EVENING_TIME);

		eveningEnabled.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{
			public boolean onPreferenceChange(Preference arg0, Object value) 
			{
				if (value instanceof Boolean)
				{
					Boolean enabled = (Boolean) value;
					
					eveningTime.setEnabled(enabled.booleanValue());
				}

				return true;
			}
		});

		eveningTime.setOnPreferenceClickListener(new OnPreferenceClickListener()
		{
			public boolean onPreferenceClick(Preference arg0) 
			{
				me.fetchSchedule(TimeOfDay.EVENING, R.string.pref_morning_time);
				
				return true;
			}
		});

		this.updateEnabled();

		CheckBoxPreference fitbitEnabled = (CheckBoxPreference) this.findPreference(SettingsActivity.SETTING_FITBIT_ENABLED);
		final Preference fitbitLogin = this.findPreference(SettingsActivity.SETTING_FITBIT_LOGIN);

		fitbitEnabled.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{
			public boolean onPreferenceChange(Preference arg0, Object value) 
			{
				if (value instanceof Boolean)
				{
					Boolean enabled = (Boolean) value;
					
					fitbitLogin.setEnabled(enabled.booleanValue());
				}

				return true;
			}
		});

		fitbitLogin.setOnPreferenceClickListener(new OnPreferenceClickListener()
		{
			public boolean onPreferenceClick(Preference arg0) 
			{
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(me);

				if (prefs.contains(SettingsActivity.OAUTH_FITBIT_TOKEN))
				{
					Editor e = prefs.edit();
					e.remove(SettingsActivity.OAUTH_FITBIT_TOKEN);
					e.remove(SettingsActivity.OAUTH_FITBIT_SECRET);
					e.commit();
					
					me.updateEnabled();
				}
				else
					me.fetchFitbitAuth();
				
				return true;
			}
		});

		final Preference dailyGoal = this.findPreference(SettingsActivity.SETTING_DAILY_GOAL);
		
		dailyGoal.setOnPreferenceClickListener(new OnPreferenceClickListener()
		{
			public boolean onPreferenceClick(Preference arg0) 
			{
				me.setGoal();
				
				return true;
			}
		});

		this.updateEnabled();
		
		Preference version = this.findPreference("app_version");

		try  
		{
			version.setTitle(this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName);
		} 
		catch (NameNotFoundException e) 
		{
			LogManager.getInstance(this).logException(e);
		}
	}
	
	private void fetchFitbitAuth() 
	{
        Intent intent = new Intent(this, OAuthActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		intent.putExtra(OAuthActivity.CONSUMER_KEY, FitbitApi.CONSUMER_KEY);
		intent.putExtra(OAuthActivity.CONSUMER_SECRET, FitbitApi.CONSUMER_SECRET);
		intent.putExtra(OAuthActivity.REQUESTER, "fitbit");
		intent.putExtra(OAuthActivity.CALLBACK_URL, "http://tech.cbits.northwestern.edu/oauth/fitbit");
		
		this.startActivity(intent);
	}

	protected void fetchSchedule(TimeOfDay period, int titleResource) 
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(titleResource);

		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		LayoutInflater inflater = LayoutInflater.from(this);
		final View view = inflater.inflate(R.layout.view_week_time_picker, null, false);

		final CheckBox sunday = (CheckBox) view.findViewById(R.id.check_sun);
		final CheckBox monday = (CheckBox) view.findViewById(R.id.check_mon);
		final CheckBox tuesday = (CheckBox) view.findViewById(R.id.check_tue);
		final CheckBox wednesday = (CheckBox) view.findViewById(R.id.check_wed);
		final CheckBox thursday = (CheckBox) view.findViewById(R.id.check_thu);
		final CheckBox friday = (CheckBox) view.findViewById(R.id.check_fri);
		final CheckBox saturday = (CheckBox) view.findViewById(R.id.check_sat);

		final TimePicker timePicker = (TimePicker) view.findViewById(R.id.time_picker);

		builder.setView(view);
		
		final StringBuilder sundayKey = new StringBuilder(); 
		final StringBuilder mondayKey = new StringBuilder(); 
		final StringBuilder tuesdayKey = new StringBuilder(); 
		final StringBuilder wednesdayKey = new StringBuilder(); 
		final StringBuilder thursdayKey = new StringBuilder(); 
		final StringBuilder fridayKey = new StringBuilder(); 
		final StringBuilder saturdayKey = new StringBuilder(); 
		
		final StringBuilder hourKey = new StringBuilder(); 
		final StringBuilder minuteKey = new StringBuilder(); 
		
		if (period == TimeOfDay.MORNING)
		{
			sundayKey.append(SettingsActivity.REMINDER_MORNING_SUNDAY);
			mondayKey.append(SettingsActivity.REMINDER_MORNING_MONDAY);
			tuesdayKey.append(SettingsActivity.REMINDER_MORNING_TUESDAY);
			wednesdayKey.append(SettingsActivity.REMINDER_MORNING_WEDNESDAY);
			thursdayKey.append(SettingsActivity.REMINDER_MORNING_THURSDAY);
			fridayKey.append(SettingsActivity.REMINDER_MORNING_FRIDAY);
			saturdayKey.append(SettingsActivity.REMINDER_MORNING_SATURDAY);

			hourKey.append(SettingsActivity.REMINDER_MORNING_HOUR);
			minuteKey.append(SettingsActivity.REMINDER_MORNING_MINUTE);

			timePicker.setCurrentHour(prefs.getInt(hourKey.toString(), SettingsActivity.SETTING_MORNING_HOUR_DEFAULT));
			timePicker.setCurrentMinute(prefs.getInt(minuteKey.toString(), SettingsActivity.SETTING_MORNING_MINUTE_DEFAULT));
		}
		else if (period == TimeOfDay.AFTERNOON)
		{
			sundayKey.append(SettingsActivity.REMINDER_AFTERNOON_SUNDAY);
			mondayKey.append(SettingsActivity.REMINDER_AFTERNOON_MONDAY);
			tuesdayKey.append(SettingsActivity.REMINDER_AFTERNOON_TUESDAY);
			wednesdayKey.append(SettingsActivity.REMINDER_AFTERNOON_WEDNESDAY);
			thursdayKey.append(SettingsActivity.REMINDER_AFTERNOON_THURSDAY);
			fridayKey.append(SettingsActivity.REMINDER_AFTERNOON_FRIDAY);
			saturdayKey.append(SettingsActivity.REMINDER_AFTERNOON_SATURDAY);

			hourKey.append(SettingsActivity.REMINDER_AFTERNOON_HOUR);
			minuteKey.append(SettingsActivity.REMINDER_AFTERNOON_MINUTE);

			timePicker.setCurrentHour(prefs.getInt(hourKey.toString(), SettingsActivity.SETTING_AFTERNOON_HOUR_DEFAULT));
			timePicker.setCurrentMinute(prefs.getInt(minuteKey.toString(), SettingsActivity.SETTING_AFTERNOON_MINUTE_DEFAULT));
		}
		else
		{
			sundayKey.append(SettingsActivity.REMINDER_EVENING_SUNDAY);
			mondayKey.append(SettingsActivity.REMINDER_EVENING_MONDAY);
			tuesdayKey.append(SettingsActivity.REMINDER_EVENING_TUESDAY);
			wednesdayKey.append(SettingsActivity.REMINDER_EVENING_WEDNESDAY);
			thursdayKey.append(SettingsActivity.REMINDER_EVENING_THURSDAY);
			fridayKey.append(SettingsActivity.REMINDER_EVENING_FRIDAY);
			saturdayKey.append(SettingsActivity.REMINDER_EVENING_SATURDAY);

			hourKey.append(SettingsActivity.REMINDER_EVENING_HOUR);
			minuteKey.append(SettingsActivity.REMINDER_EVENING_MINUTE);

			timePicker.setCurrentHour(prefs.getInt(hourKey.toString(), SettingsActivity.SETTING_EVENING_HOUR_DEFAULT));
			timePicker.setCurrentMinute(prefs.getInt(minuteKey.toString(), SettingsActivity.SETTING_EVENING_MINUTE_DEFAULT));
		}

		sunday.setChecked(prefs.getBoolean(sundayKey.toString(), SettingsActivity.SUNDAY_ENABLED_DEFAULT));
		monday.setChecked(prefs.getBoolean(mondayKey.toString(), SettingsActivity.MONDAY_ENABLED_DEFAULT));
		tuesday.setChecked(prefs.getBoolean(tuesdayKey.toString(), SettingsActivity.TUESDAY_ENABLED_DEFAULT));
		wednesday.setChecked(prefs.getBoolean(wednesdayKey.toString(), SettingsActivity.WEDNESDAY_ENABLED_DEFAULT));
		thursday.setChecked(prefs.getBoolean(thursdayKey.toString(), SettingsActivity.THURSDAY_ENABLED_DEFAULT));
		friday.setChecked(prefs.getBoolean(fridayKey.toString(), SettingsActivity.FRIDAY_ENABLED_DEFAULT));
		saturday.setChecked(prefs.getBoolean(saturdayKey.toString(), SettingsActivity.SATURDAY_ENABLED_DEFAULT));

		builder.setPositiveButton(R.string.action_schedule_reminder, new OnClickListener()
		{
			public void onClick(DialogInterface arg0, int which) 
			{
				Editor e = prefs.edit();

				int hour = timePicker.getCurrentHour();
				int minute = timePicker.getCurrentMinute();

				e.putBoolean(sundayKey.toString(), sunday.isChecked());
				e.putBoolean(mondayKey.toString(), monday.isChecked());
				e.putBoolean(tuesdayKey.toString(), tuesday.isChecked());
				e.putBoolean(wednesdayKey.toString(), wednesday.isChecked());
				e.putBoolean(thursdayKey.toString(), thursday.isChecked());
				e.putBoolean(fridayKey.toString(), friday.isChecked());
				e.putBoolean(saturdayKey.toString(), saturday.isChecked());
				e.putInt(hourKey.toString(), hour);
				e.putInt(minuteKey.toString(), minute);

				e.commit();

				/*
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
				*/
			}
		});

		builder.setNegativeButton(R.string.action_not_now, new OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which) 
			{
//				HashMap<String, Object> payload = new HashMap<String, Object>();
//				LogManager.getInstance(me).log("canceled_scheduled_reminder", payload);
			}
		});

		builder.create().show();

	}
	
	private void setGoal()
	{
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		int goal = prefs.getInt(SettingsActivity.SETTING_DAILY_GOAL, SettingsActivity.SETTING_DAILY_GOAL_DEFAULT);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		builder.setTitle(R.string.prompt_daily_goal);
		
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	    View view = inflater.inflate(R.layout.view_daily_goal, null);

	    final SettingsActivity me = this;
	    
		final NumberPicker activeMinutes = (NumberPicker) view.findViewById(R.id.picker_goal_minutes); 
		
		final NumberPicker.Formatter formatter = new NumberPicker.Formatter()
		{
			public String format(int value) 
			{
				if (value == 1)
					return me.getString(R.string.format_picker_minute);

				return me.getString(R.string.format_picker_minutes, value);
			}
		};
		
		activeMinutes.setMinValue(0);
		activeMinutes.setMaxValue(120);
		activeMinutes.setFormatter(formatter);
		activeMinutes.setValue(goal);

		builder.setView(view);

		builder.setPositiveButton(R.string.action_close, new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface arg0, int arg1) 
			{
				Editor e = prefs.edit();
				e.putInt(SettingsActivity.SETTING_DAILY_GOAL, activeMinutes.getValue());
				e.commit();
			}
		});
		
		builder.create().show();
	}

	@SuppressWarnings("deprecation")
	private void updateEnabled() 
	{
		final Preference morningTime = this.findPreference(SettingsActivity.SETTING_MORNING_TIME);
		final Preference afternoonTime = this.findPreference(SettingsActivity.SETTING_AFTERNOON_TIME);
		final Preference eveningTime = this.findPreference(SettingsActivity.SETTING_EVENING_TIME);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		morningTime.setEnabled(prefs.getBoolean(SettingsActivity.SETTING_MORNING_ENABLED, SettingsActivity.SETTING_MORNING_ENABLED_DEFAULT));
		afternoonTime.setEnabled(prefs.getBoolean(SettingsActivity.SETTING_AFTERNOON_ENABLED, SettingsActivity.SETTING_AFTERNOON_ENABLED_DEFAULT));
		eveningTime.setEnabled(prefs.getBoolean(SettingsActivity.SETTING_EVENING_ENABLED, SettingsActivity.SETTING_EVENING_ENABLED_DEFAULT));

		Preference fitbit = this.findPreference(SettingsActivity.SETTING_FITBIT_LOGIN);

		if (prefs.contains(SettingsActivity.OAUTH_FITBIT_TOKEN))
			fitbit.setTitle(R.string.prefs_fitbit_logout);
		else
			fitbit.setTitle(R.string.prefs_fitbit_login);
		
		fitbit.setEnabled(prefs.getBoolean(SettingsActivity.SETTING_FITBIT_ENABLED, SettingsActivity.SETTING_FITBIT_ENABLED_DEFAULT));
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
		
		if (key == null)
		{
			
		}
		else if (key.equals("copyright_statement"))
			ConsentedActivity.showCopyrightDialog(this);

		return super.onPreferenceTreeClick(screen, preference);
	}
}
