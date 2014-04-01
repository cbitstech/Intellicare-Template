package edu.northwestern.cbits.intellicare.thoughtchallenger;

import java.util.HashMap;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class CheckActivity extends ConsentedActivity 
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_check);
		
		this.getSupportActionBar().setTitle(R.string.title_check);
		this.getSupportActionBar().setSubtitle(R.string.subtitle_check);
	}
	
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_check, menu);

		return true;
	}
	
	protected void onResume()
	{
		super.onResume();
		
		HashMap<String, Object> payload = new HashMap<String, Object>();
		LogManager.getInstance(this).log("opened_check", payload);
	}
	
	protected void onPause()
	{
		super.onPause();

		HashMap<String, Object> payload = new HashMap<String, Object>();
		LogManager.getInstance(this).log("closed_check", payload);
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int itemId = item.getItemId();
		
		switch (itemId)
		{
			case R.id.action_next:
				Intent intent = new Intent(this, ChangeActivity.class);
				
				if (this.getIntent().hasExtra(ChangeActivity.THOUGHT_VALUE))
					intent.putExtra(ChangeActivity.THOUGHT_VALUE, this.getIntent().getStringExtra(ChangeActivity.THOUGHT_VALUE));
				
				this.startActivity(intent);

				break;
		}
		
		return true;
	}
}
