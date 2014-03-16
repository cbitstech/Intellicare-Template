package edu.northwestern.cbits.intellicare.icope;

import java.util.Date;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class ReminderActivity extends ConsentedActivity 
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
	
		this.setContentView(R.layout.activity_main);
	}
	
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_main, menu);
		
		java.text.DateFormat todayFormat = DateFormat.getMediumDateFormat(this);
		
		ActionBar actionBar = this.getSupportActionBar();
		actionBar.setTitle(todayFormat.format(new Date()));
		actionBar.setSubtitle(this.getString(R.string.subtitle_today_messages, 6));

		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int itemId = item.getItemId();
		
		switch (itemId)
		{
			default:
				break;
		}
		
		return true;
	}
}
