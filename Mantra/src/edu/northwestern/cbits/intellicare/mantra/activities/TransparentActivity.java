//package edu.northwestern.cbits.intellicare.mantra.activities;
//
//import java.util.Date;
//
//import edu.northwestern.cbits.intellicare.mantra.MediaScannerService;
//import edu.northwestern.cbits.intellicare.mantra.NotificationAlarm;
//import edu.northwestern.cbits.intellicare.mantra.R;
//import edu.northwestern.cbits.intellicare.mantra.R.layout;
//import edu.northwestern.cbits.intellicare.mantra.R.menu;
//import android.os.Bundle;
//import android.preference.PreferenceManager;
//import android.app.Activity;
//import android.app.AlertDialog;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.content.DialogInterface.OnClickListener;
//import android.content.SharedPreferences.Editor;
//import android.util.Log;
//import android.view.Menu;
//
//public class TransparentActivity extends Activity {
//
//	protected static final String CN = "TransparentActivity";
//	public static final String INTENT_IMAGES_FOUND = "imagesFound";
//
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		
//		if(getIntent().getExtras().getBoolean(INTENT_IMAGES_FOUND)) {
////			displayImagesFoundDialog(this);
//			displayPhotosTakenDialog(this);
//		}
//	}
//
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.transparent, menu);
//		return true;
//	}
//
//	
//	private void displayPhotosTakenDialog(final Context context) {
//		 AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(context);
//		 dlgAlert.setTitle("Attach a photo to a mantra?");
//		 dlgAlert.setMessage("You've taken photos within the last " + NotificationAlarm.IMAGE_SCAN_RATE_MINUTES + " minutes. Would you like to attach one to a mantra?");
//		 dlgAlert.setPositiveButton("Yes",
//		    new DialogInterface.OnClickListener() {
//		        public void onClick(DialogInterface dialog, int which) {
//					Log.d(CN+".displayPhotosTakenDialog", "yes path");
//		    		Intent pa = new Intent(context, ProgressActivity.class);
//		    		pa.putExtra(MediaScannerService.INTENT_KEY_TO_RECEIVER_STRINGARRAY, true);
//		    		context.startActivity(pa);
//
//		    		clear12hFlag(context);
//		        }
//		    });
//		 dlgAlert.setNegativeButton("No", new OnClickListener() {
//			
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				clear12hFlag(context);
//			}
//		 });
//		 dlgAlert.create().show();
//	}
////	
////	
////	private void displayImagesFoundDialog(final Activity activity) {
////		AlertDialog.Builder dlg1 = new AlertDialog.Builder(activity);
////		dlg1.setTitle("Attach a photo to a mantra?");
////		dlg1.setMessage("You've taken photos within the last " + NotificationAlarm.IMAGE_SCAN_RATE_MINUTES + " minutes. Would you like to attach one to a mantra?");
////		dlg1.setPositiveButton("Yes", new OnClickListener() {
////			@Override
////			public void onClick(DialogInterface dialog, int which) {
////				Log.d(CN+".dialogOnNewPhotos", "yes path");
////	    		Intent pa = new Intent(activity, ProgressActivity.class);
////	    		pa.putExtra(MediaScannerService.INTENT_KEY_TO_RECEIVER_STRINGARRAY, true);
////	    		activity.startActivity(pa);
////			}
////		});
////		dlg1.setNegativeButton("No", new OnClickListener() {
////			@Override
////			public void onClick(DialogInterface dialog, int which) {
////				Log.d(CN+".dialogOnNewPhotos", "no path");
////			}
////		});
////		dlg1.show();
////	}
//	
//	
//	/**
//	 * @param context
//	 */
//	public void set12hFlag(final Context context) {
//		// set the 12h flag
//		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
//		Editor e = sharedPrefs.edit();
//		e.putString(NotificationAlarm._12H_RENOTIFICATION, (new Date()).toString());
//		e.apply();
//	}
//
//	/**
//	 * @param context
//	 */
//	public void clear12hFlag(final Context context) {
//		//clear the 12h flag
//		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
//		Editor e = sharedPrefs.edit();
//		e.putString(NotificationAlarm._12H_RENOTIFICATION, "");
//		e.apply();
//	}
//
//}
