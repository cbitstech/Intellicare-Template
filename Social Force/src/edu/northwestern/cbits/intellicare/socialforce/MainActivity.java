package edu.northwestern.cbits.intellicare.socialforce;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class MainActivity extends ConsentedActivity 
{
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        
        this.getSupportActionBar().setSubtitle("lIst NExT ConTACT EvEnT HeRE");
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        if (prefs.getBoolean(IntroActivity.INTRO_SHOWN, false) == false)
        {
	        Intent introIntent = new Intent(this, IntroActivity.class);
	        this.startActivity(introIntent);
        }
        else if (prefs.getBoolean(RatingActivity.CONTACTS_RATED, false) == false)
        {
	        Intent introIntent = new Intent(this, RatingActivity.class);
	        this.startActivity(introIntent);
        }

    }

    public boolean onCreateOptionsMenu(Menu menu) 
    {
        this.getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	int itemId = item.getItemId();
    	
		if (itemId == R.id.action_settings)
		{
//			Intent settingsIntent = new Intent(this, SettingsActivity.class);
//			this.startActivity(settingsIntent);
		}
		else if (item.getItemId() == R.id.action_feedback)
			this.sendFeedback(this.getString(R.string.app_name));
		else if (item.getItemId() == R.id.action_faq)
			this.showFaq(this.getString(R.string.app_name));

        return super.onOptionsItemSelected(item);
    }
}
