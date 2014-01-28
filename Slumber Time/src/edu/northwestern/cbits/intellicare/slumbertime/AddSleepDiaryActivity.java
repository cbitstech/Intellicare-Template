package edu.northwestern.cbits.intellicare.slumbertime;

import android.os.Bundle;
import android.view.Menu;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class AddSleepDiaryActivity extends ConsentedActivity
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_add_diary);
		
		this.getSupportActionBar().setTitle(R.string.tool_sleep_diary);
		this.getSupportActionBar().setIcon(R.drawable.ic_launcher_plain);
	}

	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_add_sleep_diary, menu);

		return true;
	}
}
