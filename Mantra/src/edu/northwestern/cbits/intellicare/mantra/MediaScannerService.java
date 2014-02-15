package edu.northwestern.cbits.intellicare.mantra;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Scans a set of media files in the background and broadcasts an intent when all files are completed.
 * Src: https://developer.android.com/training/run-background-service/create-service.html 
 * @author mohrlab
 *
 */
public class MediaScannerService extends IntentService {
	public static final String CN = "MediaScannerService";
	
	public MediaScannerService() { super(null); }
	
	public MediaScannerService(String name) {
		super(name);
		Log.d(CN+".ctor", "entered; name = " + name);
	}


	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(CN+".onHandleIntent", "entered; intent = " + intent);
		
		String dataString = intent.getDataString();
		
		// TODO do stuff
		
		// * when stuff is done *
		Intent localIntent = new Intent(Constants.BROADCAST_ACTION);
		LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
	}

}