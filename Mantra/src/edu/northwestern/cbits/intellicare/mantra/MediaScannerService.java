package edu.northwestern.cbits.intellicare.mantra;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import android.app.IntentService;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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
	
	public static final String INTENT_KEY_FILE_PATHS_TO_SCAN = "fullFilePathsToScan";
	public static final String INTENT_KEY_TO_RECEIVER_STRINGARRAY = "toReceiverStrings";

	public MediaScannerService() { super(null); }
	
	public MediaScannerService(String name) {
		super(name);
		Log.d(CN+".ctor", "entered; name = " + name);
	}


	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(CN+".onHandleIntent", "entered; intent = " + intent);
		
		Bundle intentExtras = intent.getExtras();
		String[] fullFilePathsToScan = intentExtras.getStringArray(INTENT_KEY_FILE_PATHS_TO_SCAN);
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
		if(intentExtras.containsKey(INTENT_KEY_TO_RECEIVER_STRINGARRAY)) { localIntent.putStringArrayListExtra(INTENT_KEY_TO_RECEIVER_STRINGARRAY, intentExtras.getStringArrayList(INTENT_KEY_TO_RECEIVER_STRINGARRAY)); }
		LocalBroadcastManager
			.getInstance(this)
			.sendBroadcast(localIntent);
		
		Log.d(CN+".onHandleIntent", "exiting...");
	}
	
	
	/**
	 * Removes a set of paths from the media database.
	 * Src: http://stackoverflow.com/questions/8379690/androids-media-scanner-how-do-i-remove-files
	 * @param paths
	 * @param context
	 */
	public static void removeAllForPaths(String[] paths, Context context)
	{
		StringBuilder sb = new StringBuilder(); for(int i = 0; i < paths.length; i++) { sb.append(paths[i]); }
		Log.d(CN+".removeAllForPaths", "entered; paths = " + sb.toString());
	    int whereDoesThisMethodFail = 0;
		final String[] FIELDS = { MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.TITLE };
	    if(paths == null || paths.length == 0) return;
	    String select = "";
	    for(String path : paths)
	    {
	        if(!select.equals("")) select += " OR ";
	        select += MediaStore.MediaColumns.DATA + "=?";
	    }

	    Uri uri;
	    Cursor ca;

	    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
	    ca = context.getContentResolver().query(uri, FIELDS, select, paths, null);
	    for(ca.moveToFirst(); !ca.isAfterLast(); ca.moveToNext()){
	        int id = ca.getInt(ca.getColumnIndex(MediaStore.MediaColumns._ID));
	        uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
	        Log.d(CN+".removeAllForPaths:for2", "deleting URI = " + uri);
	        int deletedRowsCount = context.getContentResolver().delete(uri, null, null);
	    }
	    Log.d(CN+".removeAllForPaths", "exiting");
	    ca.close();

	    // More of the same just setting the URI to Video and Images
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