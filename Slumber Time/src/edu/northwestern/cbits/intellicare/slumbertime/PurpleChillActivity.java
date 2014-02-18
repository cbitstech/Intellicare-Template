package edu.northwestern.cbits.intellicare.slumbertime;

import java.util.HashMap;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class PurpleChillActivity extends ConsentedActivity 
{
	private static final String CHILL_PACKAGE = "edu.northwestern.cbits.intellicare.relax";

	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		Intent intent = new Intent(Intent.ACTION_VIEW);

		try 
		{
			PackageManager packages = this.getPackageManager();
			
			packages.getPackageInfo(PurpleChillActivity.CHILL_PACKAGE, 0);

			// TODO: Link directly to sleep content...
			
			intent.setData(Uri.parse("intellicare://purple-chill/reminder"));

			HashMap<String, Object> payload = new HashMap<String, Object>();
			LogManager.getInstance(this).log("launched_purple_chill", payload);

		} 
		catch (NameNotFoundException e) 
		{
			HashMap<String, Object> payload = new HashMap<String, Object>();
			LogManager.getInstance(this).log("referred_to_play_store", payload);

			intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=edu.northwestern.cbits.intellicare.relax"));
		}

		this.startActivity(intent);

		this.finish();
	}
}
