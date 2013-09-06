package edu.northwestern.cbits.intellicare;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;

public class ConsentedActivity extends ActionBarActivity 
{
	public void onResume()
	{
		super.onResume();
		
		if (ConsentActivity.isConsented() == false)
		{
			Intent consentActivity = new Intent(this, ConsentActivity.class);
			
			this.startActivityForResult(consentActivity, 999);
		}
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == 999) 
		{
		     if (resultCode == RESULT_CANCELED) 
		    	 this.finish();
		}
	}

}
