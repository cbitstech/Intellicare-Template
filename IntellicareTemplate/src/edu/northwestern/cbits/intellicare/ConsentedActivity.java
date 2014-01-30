package edu.northwestern.cbits.intellicare;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import edu.northwestern.cbits.ic_template.R;

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
		else if (DemographicActivity.showedQuestionnaire() == false)
		{
			Intent activity = new Intent(this, DemographicActivity.class);
			
			this.startActivity(activity);
		}
		else if (this.showedConductor() == false)
		{
			try 
			{
				PackageManager packages = this.getPackageManager();
				
				packages.getPackageInfo(StatusNotificationManager.CONDUCTOR_PACKAGE, 0);
			} 
			catch (NameNotFoundException e) 
			{
				final ConsentedActivity me = this;
				
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder = builder.setTitle(R.string.install_intellicare_title);
				builder = builder.setMessage(R.string.install_intellicare_message);

				builder = builder.setNegativeButton(R.string.install_intellicare_no, new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{

					}
				});
				
				builder = builder.setPositiveButton(R.string.install_intellicare_yes, new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						Intent launch = new Intent(Intent.ACTION_VIEW);
						launch.setData(Uri.parse(me.getString(R.string.install_intellicare_link)));
						
						me.startActivity(launch);
					}
				});
				
				builder.create().show();
			}
		}
	}
	
	private boolean showedConductor() 
	{
		if (StatusNotificationManager.CONDUCTOR_PACKAGE.equals(this.getPackageName()))
			return true;
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		boolean showed = prefs.getBoolean("intellicare_showed_conductor_invite", false);
		
		if (showed == false)
		{
			Editor e = prefs.edit();
			e.putBoolean("intellicare_showed_conductor_invite", true);
			e.commit();
		}
		
		return showed;
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
