package edu.northwestern.cbits.intellicare.relax;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import edu.northwestern.cbits.intellicare.SequentialPageActivity;

public class IntroActivity extends SequentialPageActivity 
{
	public static String SEQUENCE_TITLES = "sequence_titles";
	public static String SEQUENCE_URLS = "sequence_urls";
	public static String SEQUENCE_KEY = "sequence_key";
	
	public int pagesSequence() 
	{
		return this.getIntent().getIntExtra(IntroActivity.SEQUENCE_URLS, 0);
	}

	public int titlesSequence() 
	{
		return this.getIntent().getIntExtra(IntroActivity.SEQUENCE_TITLES, 0);
	}
	
	public void onSequenceComplete()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		Editor e = prefs.edit();
		
		e.putBoolean(this.getIntent().getStringExtra(IntroActivity.SEQUENCE_KEY), true);
		
		e.commit();
	}
}
