package edu.northwestern.cbits.intellicare;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;

public class ConsentedActivity extends ActionBarActivity 
{
	protected void onResume()
	{
		super.onResume();
		
		if (ConsentActivity.isConsented() == false)
		{
			Intent consentActivity = new Intent(this, ConsentActivity.class);
			
			this.startActivityForResult(consentActivity, 999);
		}
		else if (RecruitmentActivity.showedRecruitment() == false)
		{
			Intent activity = new Intent(this, RecruitmentActivity.class);
			
			this.startActivity(activity);
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
