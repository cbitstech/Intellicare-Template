package edu.northwestern.cbits.intellicare.moveme;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class MainActivity extends ConsentedActivity 
{
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        
        this.getSupportActionBar().setSubtitle(MoveProvider.fetchNextEventTitle(this));

        final MainActivity me = this;
        
        TextView dashboard = (TextView) this.findViewById(R.id.button_dashboard);
        dashboard.setOnClickListener(new OnClickListener()
        {
			public void onClick(View view) 
			{
				Intent intent = new Intent(me, DashboardActivity.class);
				me.startActivity(intent);
			}
        });

        TextView inspire = (TextView) this.findViewById(R.id.button_move_me);
        inspire.setOnClickListener(new OnClickListener()
        {
			public void onClick(View view) 
			{
				Intent intent = new Intent(me, MoveMeActivity.class);
				me.startActivity(intent);
			}
        });

        TextView boost = (TextView) this.findViewById(R.id.button_timer);
        boost.setOnClickListener(new OnClickListener()
        {
			public void onClick(View view) 
			{
				Intent intent = new Intent(me, TimerActivity.class);
				me.startActivity(intent);
			}
        });

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        if (prefs.getBoolean(IntroActivity.INTRO_SHOWN, false) == false)
        {
	        Intent introIntent = new Intent(this, IntroActivity.class);
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
			Intent settingsIntent = new Intent(this, SettingsActivity.class);
			this.startActivity(settingsIntent);
		}
		else if (item.getItemId() == R.id.action_feedback)
			this.sendFeedback(this.getString(R.string.app_name));
		else if (item.getItemId() == R.id.action_faq)
			this.showFaq(this.getString(R.string.app_name));
		else if (item.getItemId() == R.id.action_calendar)
		{
			Intent settingsIntent = new Intent(this, CalendarActivity.class);
			this.startActivity(settingsIntent);
		}

		return super.onOptionsItemSelected(item);
    }
}
