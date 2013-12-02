package edu.northwestern.cbits.intellicare.dailyfeats;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class StartupActivity extends Activity 
{
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
		if (prefs.getBoolean(IntroActivity.INTRO_SHOWN, false))
			this.startActivity(new Intent(this, HomeActivity.class));
		else
			this.startActivity(new Intent(this, IntroActivity.class));
		
		ScheduleManager.getInstance(this);
    }

    protected void onResume() 
    {
        super.onResume();

        this.finish();
    }
}