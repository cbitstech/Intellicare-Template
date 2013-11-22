package com.example.cowsareruminants;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class StartupActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_startup);
		
		// get an outer reference to this activity
		final Activity startupActivity = this;
		Toast.makeText(startupActivity, "StartupActivity loaded...", Toast.LENGTH_SHORT).show();
		
		// on Next button click, go to next activity.
		Button nextButton = (Button) this.findViewById(R.id.nextButton);
		nextButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				TextView tv = (TextView) startupActivity.findViewById(R.id.message);
				Toast.makeText(startupActivity, "should nav to next activity...", Toast.LENGTH_SHORT).show();
				tv.setText("should leave this world now...");
				
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.startup, menu);
		return true;
	}

}
