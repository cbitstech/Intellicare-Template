package edu.northwestern.cbits.intellicare.store;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		DataService ds = new DataService();
		String productsJsonText = ds.getJsonFromFile(this, "products.json");
		String profileJsonText = ds.getJsonFromFile(this, "playerProfile.example.json");
		
		int userId = ds.getJSONvalue("userId", profileJsonText);
		
		Log.d("MainActivity.onCreate", "userId = " + userId);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
