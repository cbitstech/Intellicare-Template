package edu.northwestern.cbits.intellicare.conductor;

import java.util.HashMap;

import edu.northwestern.cbits.intellicare.logging.LogManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity 
{
	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		this.setTitle(R.string.title_settings);
		
		this.addPreferencesFromResource(R.layout.activity_settings);
		
		Preference sleepDiaryPref = this.findPreference("sleep_diary_url");
		
		if (this.packageInstalled("edu.northwestern.cbits.intellicare.slumbertime") == false)
		{
			sleepDiaryPref.setEnabled(false);
			sleepDiaryPref.setSummary(R.string.summary_install_slumber_time);
		}
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
	
	private boolean packageInstalled(String packageName)
	{
        PackageManager pm = this.getPackageManager();        

        for (ApplicationInfo packageInfo : pm.getInstalledApplications(0)) 
        {
        	if(packageInfo.packageName.equals(packageName)) 
        		return true;
        }       
        
        return false;
    }

}
