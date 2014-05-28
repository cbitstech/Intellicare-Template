package edu.northwestern.cbits.intellicare.icope;

import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

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
