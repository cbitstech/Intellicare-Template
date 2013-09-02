package edu.northwestern.cbits.ic_template;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends Activity 
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_main);
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
				
			case R.id.action_settings:
				Toast.makeText(this, "tOdO: sEtTiNgS aCtiViTy", Toast.LENGTH_LONG).show();
				
				break;
		}

		return true;
	}
}
