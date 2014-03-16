package edu.northwestern.cbits.intellicare.icope;

import java.util.Date;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class MainActivity extends ConsentedActivity 
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
			case R.id.action_add_message:
				Intent addIntent = new Intent(this, AddCardActivity.class);
				this.startActivity(addIntent);
				
				break;
			case R.id.action_view_messages:
				Intent libraryIntent = new Intent(this, LibraryActivity.class);
				this.startActivity(libraryIntent);
				
				break;
			default:
				break;
		}
		
		return true;
	}
}
