package edu.northwestern.cbits.intellicare.mantra.activities;

import edu.northwestern.cbits.intellicare.mantra.MediaScannerService;
import edu.northwestern.cbits.intellicare.mantra.NotificationAlarm;
import edu.northwestern.cbits.intellicare.mantra.R;
import edu.northwestern.cbits.intellicare.mantra.R.layout;
import edu.northwestern.cbits.intellicare.mantra.R.menu;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.util.Log;
import android.view.Menu;

public class TransparentActivity extends Activity {

	protected static final String CN = "TransparentActivity";
	public static final String INTENT_IMAGES_FOUND = "imagesFound";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_transparent);
		
		if(getIntent().getExtras().getBoolean(INTENT_IMAGES_FOUND)) {
			displayImagesFoundDialog(this);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.transparent, menu);
		return true;
	}

	
	private void displayImagesFoundDialog(final Activity activity) {
		AlertDialog.Builder dlg1 = new AlertDialog.Builder(activity);
		dlg1.setTitle("Attach a photo to a mantra?");
		dlg1.setMessage("You've taken photos within the last " + NotificationAlarm.IMAGE_SCAN_RATE_MINUTES + " minutes. Would you like to attach one to a mantra?");
		dlg1.setPositiveButton("Yes", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.d(CN+".dialogOnNewPhotos", "yes path");
	    		Intent sua = new Intent(activity, SharedUrlActivity.class);
	    		sua.putExtra(MediaScannerService.INTENT_KEY_TO_RECEIVER_STRINGARRAY, true);
	    		activity.startActivity(sua);
			}
		});
		dlg1.setNegativeButton("No", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.d(CN+".dialogOnNewPhotos", "no path");
			}
		});
		dlg1.show();

	}
}
