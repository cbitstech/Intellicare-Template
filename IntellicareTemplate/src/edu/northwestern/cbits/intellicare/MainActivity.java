package edu.northwestern.cbits.intellicare;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.CrashManagerListener;
import net.hockeyapp.android.UpdateManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import edu.northwestern.cbits.ic_template.R;

public class MainActivity extends ConsentedActivity 
{
	private static final String APP_ID = "429a32046c4a625e42bfa6aa00fa5f81";

	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_main);

		UpdateManager.register(this, APP_ID);
	}
	
	public void onResume()
	{
		super.onResume();
		
		CrashManager.register(this, APP_ID, new CrashManagerListener() 
		{
			public boolean shouldAutoUploadCrashes() 
			{
				    return true;
			}
		});
	}

	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.main, menu);
		
		menu.findItem(R.id.action_native).setVisible(false);

		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.action_native)
		{
			Intent nativeIntent = new Intent(this, MainActivity.class);
			this.startActivity(nativeIntent);
		}
		else if (item.getItemId() == R.id.action_drawables)
		{
			Intent drawablesIntent = new Intent(this, DrawablesActivity.class);
			this.startActivity(drawablesIntent);
		}
		else if (item.getItemId() == R.id.action_rating)
		{
			Intent ratingIntent = new Intent(this, RatingActivity.class);
			this.startActivity(ratingIntent);
		}
		
		return true;
	}
}
