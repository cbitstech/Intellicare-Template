package edu.northwestern.cbits.intellicare.icope;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class LibraryActivity extends ConsentedActivity 
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
	
		this.setContentView(R.layout.activity_main);

		ActionBar actionBar = this.getSupportActionBar();
		actionBar.setTitle("lIbraRY aCTiViTY");
	}
	
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_library, menu);

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
			case R.id.action_view_card:
				Intent cardIntent = new Intent(this, CardActivity.class);
				this.startActivity(cardIntent);
				
				break;
			default:
				break;
		}
		
		return true;
	}
}
