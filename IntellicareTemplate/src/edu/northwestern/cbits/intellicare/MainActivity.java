package edu.northwestern.cbits.intellicare;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
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
		
		CrashManager.register(this, APP_ID);
	}

	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.main, menu);
		
		menu.findItem(R.id.action_native).setVisible(false);

		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.action_cordova:
				Intent cordovaIntent = new Intent(this, JavascriptActivity.class);
				this.startActivity(cordovaIntent);
				
				break;
			case R.id.action_drawables:
				Intent drawablesIntent = new Intent(this, DrawablesActivity.class);
				this.startActivity(drawablesIntent);
				
				break;
			case R.id.action_rating:
				Intent ratingIntent = new Intent(this, RatingActivity.class);
				this.startActivity(ratingIntent);
				
				break;
			case R.id.action_settings:
				Toast.makeText(this, "tOdO: sEtTiNgS aCtiViTy", Toast.LENGTH_LONG).show();
				
				break;
		}

		return true;
	}
}
