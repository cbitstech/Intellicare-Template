package edu.northwestern.cbits.intellicare.mantra;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
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
		
		Bundle intentExtras = intent.getExtras();
		String[] fullFilePathsToScan = intentExtras.getStringArray(GetImagesTask.INTENT_KEY_FILE_PATHS_TO_SCAN);
		Log.d(CN+".onHandleIntent", "fullFilePathsToScan = " + fullFilePathsToScan);

		// begin all the image scans (will be async under-the-hood of MediaScannerConnectionClient)
		ArrayList<Boolean> allCompleted = new ArrayList<Boolean>();
		ArrayList<SingleMediaScanner> scanners = new ArrayList<SingleMediaScanner>();
		for(int i = 0; i < fullFilePathsToScan.length; i++) {
			String filePath = fullFilePathsToScan[i];
			Log.d(CN+".onHandleIntent", "scanning file (start): " + filePath);
			allCompleted.add(new Boolean(false));
			scanners.add(new SingleMediaScanner(this, new File(filePath), allCompleted.get(i)));
		}
		
		// loop to wait for all scanning to complete... 
		sleepLoop(100, fullFilePathsToScan, scanners);
		
		// * when stuff is is done *
		// send a broadcast intent to the MediaScannerServiceResponseReceiver 
		// and proceed from the fact that all images are now available for the user to select-from.
		Log.d(CN+".onHandleIntent", "sending broadcast: " + Constants.BROADCAST_ACTION);
		Intent localIntent = new Intent(Constants.BROADCAST_ACTION);
		localIntent.putExtra("message", "Scanned all " + fullFilePathsToScan.length + " files.");
		LocalBroadcastManager
			.getInstance(this)
			.sendBroadcast(localIntent);
		
		Log.d(CN+".onHandleIntent", "exiting...");
	}
	
	
	
	/**
	 * Implements a sleep-loop.
	 * TODO: Genericize (and librarify?) this.
	 * @param fullFilePathsToScan
	 * @param scanners
	 */
	private void sleepLoop(int sleepMillis, String[] fullFilePathsToScan, ArrayList<SingleMediaScanner> scanners) {
		// Justification for this method: without waiting for all onScanCompleted methods to complete, here is the top the stacktrace I got after the onStart exits:
		//
		//02-17 17:08:37.065: E/ActivityThread(13003): Service edu.northwestern.cbits.intellicare.mantra.MediaScannerService has leaked ServiceConnection android.media.MediaScannerConnection@423404a0 that was originally bound here
		//02-17 17:08:37.065: E/ActivityThread(13003): android.app.ServiceConnectionLeaked: Service edu.northwestern.cbits.intellicare.mantra.MediaScannerService has leaked ServiceConnection android.media.MediaScannerConnection@423404a0 that was originally bound here
		//02-17 17:08:37.065: E/ActivityThread(13003): 	at android.app.LoadedApk$ServiceDispatcher.<init>(LoadedApk.java:979)
		//02-17 17:08:37.065: E/ActivityThread(13003): 	at android.app.LoadedApk.getServiceDispatcher(LoadedApk.java:873)
		//02-17 17:08:37.065: E/ActivityThread(13003): 	at android.app.ContextImpl.bindServiceCommon(ContextImpl.java:1561)
		//02-17 17:08:37.065: E/ActivityThread(13003): 	at android.app.ContextImpl.bindService(ContextImpl.java:1544)
		//02-17 17:08:37.065: E/ActivityThread(13003): 	at android.content.ContextWrapper.bindService(ContextWrapper.java:517)
		//02-17 17:08:37.065: E/ActivityThread(13003): 	at android.media.MediaScannerConnection.connect(MediaScannerConnection.java:119)
		//02-17 17:08:37.065: E/ActivityThread(13003): 	at edu.northwestern.cbits.intellicare.mantra.SingleMediaScanner.<init>(MediaScannerService.java:109)
		//02-17 17:08:37.065: E/ActivityThread(13003): 	at edu.northwestern.cbits.intellicare.mantra.MediaScannerService.onStart(MediaScannerService.java:78)
		boolean allDone = false;
		
		while(!allDone) {
			// poll the completion state of each scanner... wish I had a language-level lambda expression here! (e.g.: http://msdn.microsoft.com/en-us/library/vstudio/bb910253(v=vs.90).aspx)
			int j = 0;
			for(int i = 0; i < fullFilePathsToScan.length; i++) {
				if(!(scanners.get(i)).isDone)
					continue;
				else
					j++;
			}
			
			// if all scanners completed, then we're done.
			if(j >= fullFilePathsToScan.length)
				allDone = true;
			
			// if not all file-scans complete, then wait 100ms before trying-again.
			if(!allDone) {
				try {
					Log.d(CN+".sleepLoop", "waiting " + sleepMillis + "ms before rechecking status...");
					Thread.sleep(sleepMillis);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}



/**
 * Forces a MediaScanner scan operation for an image.
 * Src: http://stackoverflow.com/questions/4646913/android-how-to-use-mediascannerconnection-scanfile
 * @author mohrlab
 *
 */
class SingleMediaScanner implements MediaScannerConnectionClient {
	public static final String CN = "SingleMediaScanner";
	
	private MediaScannerConnection mMs;
	private File mFile;
	public Boolean isDone = false;

	/**
	 * Ctor. isCompleted enables a wait-and-rejoin pattern in the caller, so galloping asynchronicity doesn't blow-up the caller on method-exit. 
	 * @param context
	 * @param f
	 * @param isCompleted
	 */
	public SingleMediaScanner(Context context, File f, Boolean isCompleted) {
		mFile = f;
		isDone = isCompleted;
		mMs = new MediaScannerConnection(context, this);
		Log.d(CN+".ctor", "connecting for file: " + f.getAbsolutePath());
		mMs.connect();
	}

	@Override
	public void onMediaScannerConnected() {
		mMs.scanFile(mFile.getAbsolutePath(), null);
	}

	@Override
	public void onScanCompleted(String path, Uri uri) {
		Log.d(CN+".onScanCompleted", "disconnecting for path: " + path + " and uri = " + uri);
		mMs.disconnect();
		isDone = true;
		Log.d(CN+".onScanCompleted", "scanning file (end): " + path + "; isDone = " + isDone);
	}
}