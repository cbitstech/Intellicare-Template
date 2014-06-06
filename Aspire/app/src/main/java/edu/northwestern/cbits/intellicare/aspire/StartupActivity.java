package edu.northwestern.cbits.intellicare.aspire;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class StartupActivity extends ConsentedActivity
{
    // public static final Uri URI = Uri.parse("intellicare://aspire/home");

	protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        
		if (prefs.getBoolean(IntroActivity.INTRO_SHOWN, false))
			this.startActivity(new Intent(this, MainActivity.class));
		else
			this.startActivity(new Intent(this, IntroActivity.class));

        Log.e("startup activity", IntroActivity.INTRO_SHOWN + " : intro" + prefs.getBoolean(IntroActivity.INTRO_SHOWN, false) + " :prefs");
		
		ScheduleManager.getInstance(this);
    }

    protected void onResume() 
    {
        super.onResume();

        this.finish();
    }
}