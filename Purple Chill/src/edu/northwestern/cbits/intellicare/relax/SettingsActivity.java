package edu.northwestern.cbits.intellicare.relax;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity 
{
	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		this.setTitle(R.string.title_settings);
		
		this.addPreferencesFromResource(R.layout.settings_activity);
	}
	
	@SuppressWarnings("deprecation")
	protected void onResume()
	{
		super.onResume();
		
		Preference enabled = this.findPreference("config_enable_notifications");
		final Preference type = this.findPreference("config_notification_type");
		final Preference day = this.findPreference("config_notification_day");
		final Preference hour = this.findPreference("config_notification_hour");
		
		final SettingsActivity me = this;
		
		OnPreferenceChangeListener listener = new OnPreferenceChangeListener()
		{
			public boolean onPreferenceChange(Preference preference, Object value) 
			{
				Runnable r = new Runnable()
				{
					public void run() 
					{
						try 
						{
							Thread.sleep(250);
						} 
						catch (InterruptedException e) 
						{

						}
						
						me.runOnUiThread(new Runnable()
						{
							public void run() 
							{
								SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(me);

								if (prefs.getBoolean("config_enable_notifications", true) == false)
								{
									type.setEnabled(false);
									day.setEnabled(false);
									hour.setEnabled(false);
								}
								else
								{
									type.setEnabled(true);
									day.setEnabled(true);
									hour.setEnabled(true);

									String noteType = prefs.getString("config_notification_type", "random");
									
									if ("random".equals(noteType))
									{
										day.setEnabled(false);
										hour.setEnabled(false);
									}
									else if ("week".equals(noteType))
									{
										day.setEnabled(true);
										hour.setEnabled(true);
									}
									else if ("day".equals(noteType))
									{
										day.setEnabled(false);
										hour.setEnabled(true);
									}
								}
							}
						});
					}
				};
				
				Thread t = new Thread(r);
				t.start();

				return true;
			}
		};
		
		enabled.setOnPreferenceChangeListener(listener);
		type.setOnPreferenceChangeListener(listener);
		day.setOnPreferenceChangeListener(listener);
		hour.setOnPreferenceChangeListener(listener);
		
		listener.onPreferenceChange(null, null);
	}
}
