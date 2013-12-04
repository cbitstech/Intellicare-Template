package edu.northwestern.cbits.intellicare.dailyfeats;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

public class StartupActivity extends Activity 
{
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        
        Log.e("DF", "SHOW INTRO: " +  prefs.contains(IntroActivity.INTRO_SHOWN) + " -- " + prefs.getBoolean(IntroActivity.INTRO_SHOWN, false));
        
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