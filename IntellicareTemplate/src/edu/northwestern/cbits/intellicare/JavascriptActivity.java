package edu.northwestern.cbits.intellicare;

import org.apache.cordova.CordovaWebView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import edu.northwestern.cbits.ic_template.R;

public class JavascriptActivity extends ConsentedActivity// implements CordovaInterface
{
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_javascript);
		
		CordovaWebView cordova = (CordovaWebView) this.findViewById(R.id.cordova_view);

		cordova.loadUrl("file:///android_asset/www/index.html", 100); // show splash screen 3 sec before loading app
	}

	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.main, menu);
		
		menu.findItem(R.id.action_cordova).setVisible(false);

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

	/*
	@Override
	public void startActivityForResult(CordovaPlugin command, Intent intent, int requestCode) 
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void setActivityResultCallback(CordovaPlugin plugin) 
	{
		// TODO Auto-generated method stub
	}

	@Override
	public Activity getActivity() 
	{
		return this;
	}

	@Override
	public Object onMessage(String id, Object data) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ExecutorService getThreadPool() 
	{
		// TODO Auto-generated method stub
		return this._threadPool;
	}
	*/
}
