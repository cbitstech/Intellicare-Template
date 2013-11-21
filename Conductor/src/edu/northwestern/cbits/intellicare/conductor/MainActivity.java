package edu.northwestern.cbits.intellicare.conductor;

import android.os.Bundle;
import android.view.Menu;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class MainActivity extends ConsentedActivity
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
	
		this.setContentView(R.layout.activity_main);

//		UpdateManager.register(this, APP_ID);
	}

	public void onResume()
	{
		super.onResume();
		
/*		CrashManager.register(this, APP_ID, new CrashManagerListener() 
		{
			public boolean shouldAutoUploadCrashes() 
			{
				    return true;
			}
		});
*/
	}
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.main, menu);

		return true;
	}
}
