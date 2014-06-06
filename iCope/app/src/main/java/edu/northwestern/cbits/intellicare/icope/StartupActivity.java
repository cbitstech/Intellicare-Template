package edu.northwestern.cbits.intellicare.icope;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class StartupActivity extends ConsentedActivity
{
    // public static final Uri URI = Uri.parse("intellicare://iCope/home");

	protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        
		if (prefs.getBoolean(IntroActivity.INTRO_SHOWN, false))
			this.startActivity(new Intent(this, IntroActivity.class));
		else
			this.startActivity(new Intent(this, MainActivity.class));
		
		ScheduleManager.getInstance(this);
    }

    protected void onResume() 
    {
        super.onResume();

        this.finish();
    }
}