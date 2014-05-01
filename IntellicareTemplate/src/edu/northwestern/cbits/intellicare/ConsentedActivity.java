package edu.northwestern.cbits.intellicare;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;
import edu.northwestern.cbits.ic_template.R;

public class ConsentedActivity extends ActionBarActivity 
{
	private static final String SHOWED_TESTING_MESSAGE = "showed_beta_testing_message";

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
		else if (MotivationActivity.showedQuestionnaire() == false)
		{
			Intent activity = new Intent(this, MotivationActivity.class);
			
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
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		if (prefs.getBoolean(ConsentedActivity.SHOWED_TESTING_MESSAGE, false) == false)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder = builder.setTitle(R.string.title_beta_testing);
			builder = builder.setMessage(R.string.message_beta_testing);
			builder = builder.setPositiveButton(R.string.button_continue, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which) 
				{

				}
			});
			
			builder.create().show();
			
			Editor e = prefs.edit();
			e.putBoolean(ConsentedActivity.SHOWED_TESTING_MESSAGE, true);
			e.commit();
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
	
	protected void sendFeedback(String appName)
	{
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("message/rfc822");
		intent.putExtra(Intent.EXTRA_EMAIL, new String[]{ this.getString(R.string.email_feedback_address) });
		intent.putExtra(Intent.EXTRA_SUBJECT, this.getString(R.string.email_feedback_subject, appName));
		intent.putExtra(Intent.EXTRA_TEXT   , this.getString(R.string.email_feedback_body, appName));

		try 
		{
		    startActivity(intent);
		} 
		catch (ActivityNotFoundException e) 
		{
		    Toast.makeText(this, R.string.email_toast_no_client, Toast.LENGTH_LONG).show();
		}
	}
	
	protected void showFaq(String appName)
	{
		Intent intent = new Intent(this, FaqActivity.class);
		intent.putExtra(FaqActivity.APP_NAME, appName);
		
		this.startActivity(intent);
	}

	public static void showCopyrightDialog(Activity activity)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder = builder.setTitle(R.string.copy_title);
		builder = builder.setMessage(R.string.copy_message);

		builder = builder.setPositiveButton(R.string.button_close, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which) 
			{

			}
		});
		
		builder.create().show();
	}
}
