package edu.northwestern.cbits.intellicare.aspire;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class EditActivity extends ConsentedActivity 
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_edit);
		
		this.getSupportActionBar().setTitle("tOdO: PaTH eDItOr");
	}
	
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_edit, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item)
	{
		int itemId = item.getItemId();
		
		switch (itemId)
		{
			case R.id.action_add_path:
				Toast.makeText(this, "tOdO: ADD PaTh", Toast.LENGTH_LONG).show();
				break;
			case R.id.action_close:
				this.finish();

				break;
		}
		
		return true;
	}
}
