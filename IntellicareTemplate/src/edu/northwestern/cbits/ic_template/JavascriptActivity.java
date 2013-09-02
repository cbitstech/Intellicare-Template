package edu.northwestern.cbits.ic_template;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class JavascriptActivity extends Activity implements CordovaInterface
{
	private final ExecutorService _threadPool = Executors.newCachedThreadPool();
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_javascript);
		
		CordovaWebView cordova = (CordovaWebView) this.findViewById(R.id.cordova_view);

//		cordova.setStringProperty("loadingDialog", "Title,Message"); // show loading dialog
//		cordova.setStringProperty("errorUrl", "file:///android_asset/www/error.html"); // if error loading file in super.loadUrl().

		// super.appView.clearCache(true);

		// super.setIntegerProperty("splashscreen", R.drawable.splash); // load splash.jpg image from the resource drawable directory

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
		switch (item.getItemId())
		{
			case R.id.action_native:
				Intent nativeIntent = new Intent(this, MainActivity.class);
				this.startActivity(nativeIntent);
				
				break;
				
			case R.id.action_settings:
				Toast.makeText(this, "tOdO: sEtTiNgS aCtiViTy", Toast.LENGTH_LONG).show();
				
				break;
		}

		return true;
	}

	public void startActivityForResult(CordovaPlugin command, Intent intent, int requestCode) 
	{
		// TODO Auto-generated method stub
	}

	public void setActivityResultCallback(CordovaPlugin plugin) 
	{
		// TODO Auto-generated method stub
	}

	public Activity getActivity() 
	{
		return this;
	}

	public Object onMessage(String id, Object data) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	public ExecutorService getThreadPool() 
	{
		return this._threadPool;
	}
}
