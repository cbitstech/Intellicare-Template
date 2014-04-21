package edu.northwestern.cbits.intellicare.moveme;

import java.util.ArrayList;

import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class SettingsActivity extends PreferenceActivity 
{
	public static final String GOOGLE_PLAY_PACKAGE = "com.google.android.music";
	public static final String SPOTIFY_PACKAGE = "com.spotify.mobile.android.ui";
	public static final String PANDORA_PACKAGE = "com.pandora.android";
	public static final String SIRIUSXM_PACKAGE = "com.sirius";
	public static final String AMAZON_MP3_PACKAGE = "com.amazon.mp3";

	public static final String SETTING_PLAYER = "settings_music_player";
	public static final String DEFAULT_PACKAGE = "default";
	
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
