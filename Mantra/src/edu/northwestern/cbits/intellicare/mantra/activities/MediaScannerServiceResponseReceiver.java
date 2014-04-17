package edu.northwestern.cbits.intellicare.mantra.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import edu.northwestern.cbits.intellicare.mantra.GetImagesTask;
import edu.northwestern.cbits.intellicare.mantra.MediaScannerService;

/**
 * Handles a response from the MediaScannerService.
 * Src: https://developer.android.com/training/run-background-service/report-status.html
 * @author mohrlab
 *
 */
public class MediaScannerServiceResponseReceiver extends BroadcastReceiver {
	public static final String CN = "MediaScannerServiceResponseReceiver";
	
	private Activity activity = null; 
	
	public MediaScannerServiceResponseReceiver() throws Exception { throw new Exception("Wrong constructor; use MediaScannerServiceResponseReceiver(Activity a)."); }
	
	public MediaScannerServiceResponseReceiver(Activity a) {
		activity = a;
	}
	
	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		Log.d(CN+".onReceive", "arg1 = " + arg1);
		
		Bundle extras = arg1.getExtras();
		if(extras != null) {
			Log.d(CN+".onReceive", "intent not null");
			Log.d(CN+".onReceive", "message = " + arg1.getStringExtra("message"));
			Log.d(CN+".onReceive", "extras.containsKey(MediaScannerService.INTENT_KEY_TO_RECEIVER_STRINGARRAY) = " + extras.containsKey(MediaScannerService.INTENT_KEY_TO_RECEIVER_STRINGARRAY));
			
//			if(extras.containsKey(MediaScannerService.INTENT_KEY_TO_RECEIVER_STRINGARRAY)) {
				// display the Android image gallery for the folder
				// ATTEMPT 1; displays Gallery or Photos, but only allows picking 1 image
				Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
				Log.d(CN+".onReceive", "Launching image picker");
				activity.startActivityForResult(intent, GetImagesTask.RESULT_LOAD_IMAGE);
//			}
		}
		else {
			Log.d(CN+".onReceive", "intent is null");
		}
	}
}
