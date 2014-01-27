package edu.northwestern.cbits.intellicare.slumbertime;

import android.content.Intent;
import android.content.res.Configuration;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class PortraitActivity extends ConsentedActivity
{
	protected void onResume() 
	{
		super.onResume();
		
		if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
		{
			this.finish();
			
			Intent clockIntent = new Intent(this, ClockActivity.class);
			this.startActivity(clockIntent);
		}
		
		this.getSupportActionBar().setIcon(R.drawable.ic_launcher_plain);
	}
}
