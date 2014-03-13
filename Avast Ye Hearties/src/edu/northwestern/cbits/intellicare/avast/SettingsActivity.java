package edu.northwestern.cbits.intellicare.avast;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class SettingsActivity extends PreferenceActivity 
{
	private static final String FOURSQUARE_PACKAGE = "com.joelapenna.foursquared";

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		this.setTitle(R.string.title_settings);
		
		this.addPreferencesFromResource(R.layout.activity_settings);
		
		PackageManager packages = this.getPackageManager();
		
		try 
		{
			packages.getPackageInfo(FOURSQUARE_PACKAGE, 0);
			
			PreferenceScreen screen = this.getPreferenceScreen();
			screen.removePreference(this.findPreference("settings_install_foursquare"));
		} 
		catch (NameNotFoundException e) 
		{
			
		}
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

	public boolean onPreferenceTreeClick (PreferenceScreen screen, Preference preference)
	{
		String key = preference.getKey();
		
		if (key == null)
		{
			
		}
		else if (key.equals("settings_install_foursquare"))
		{
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("market://details?id=" + FOURSQUARE_PACKAGE));
			
			this.startActivity(intent);

			return true;
		}
		else if (key.equals("copyright_statement"))
			ConsentedActivity.showCopyrightDialog(this);

		return super.onPreferenceTreeClick(screen, preference);
	}
}

