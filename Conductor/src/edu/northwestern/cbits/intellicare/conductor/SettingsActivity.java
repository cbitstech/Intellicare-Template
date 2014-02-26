package edu.northwestern.cbits.intellicare.conductor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.widget.Toast;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;

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

	@SuppressWarnings("deprecation")
	public boolean onPreferenceTreeClick (PreferenceScreen screen, Preference preference)
	{
		String key = preference.getKey();
		
		if (key == null)
		{
			
		}
		else if (key.equals("clear_cookies"))
		{
			File root = Environment.getExternalStorageDirectory();
			File intellicare = new File(root, "Intellicare Shared");

			if (intellicare.exists() == true)
			{
				try 
				{
					FileUtils.deleteDirectory(intellicare);
					
					Toast.makeText(this, R.string.toast_cookies_cleared, Toast.LENGTH_LONG).show();
				} 
				catch (IOException e) 
				{
					LogManager.getInstance(this).logException(e);
				}
			}

			return true;
		}
		else if (key.equals("copyright_statement"))
			ConsentedActivity.showCopyrightDialog(this);
		
		return super.onPreferenceTreeClick(screen, preference);
	}
}
