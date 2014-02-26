package edu.northwestern.cbits.intellicare.relax;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

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
									day.setEnabled(false);
									hour.setEnabled(false);
								}
								else
								{
									day.setEnabled(true);
									hour.setEnabled(true);
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
		
		listener.onPreferenceChange(null, null);
	}

	@SuppressWarnings("deprecation")
	public boolean onPreferenceTreeClick (PreferenceScreen screen, Preference preference)
	{
		String key = preference.getKey();
		
		if (key.equals("copyright_statement"))
			ConsentedActivity.showCopyrightDialog(this);
		
		return super.onPreferenceTreeClick(screen, preference);
	}
}
