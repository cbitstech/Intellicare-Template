package edu.northwestern.cbits.intellicare.mantra;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import edu.northwestern.cbits.intellicare.mantra.activities.SharedUrlActivity;


class GetImagesTaskBackgroundRet {
	String saveFolderPath;
	SharedUrlActivity activity;
	public GetImagesTaskBackgroundRet(String p, SharedUrlActivity a) { saveFolderPath = p; activity = a; }
}


/**
 * Gets images from a set of remote URLs.
 * Inspirational src: http://developer.android.com/reference/android/os/AsyncTask.html
 * @author mohrlab
 *
 */
public class GetImagesTask extends AsyncTask<GetImagesTaskParams, Void, Object> {
	public static final String CN = "GetImagesTask";
	
	public static final int RESULT_LOAD_IMAGE = 1;
	public static final String INTENT_KEY_FILE_PATHS_TO_SCAN = "fullFilePathsToScan";
	public static final String INTENT_KEY_ORIGINATING_ACTIVITY = "originatingActivity";

	
	@Override
	protected Object doInBackground(GetImagesTaskParams... arg0) {
		final SharedUrlActivity activity = arg0[0].activity;
		Map<String, Integer> imagesToDownload = arg0[0].imagesToDownload;
		int count = imagesToDownload.keySet().size();
		int totalSize = 0;

		// create the temp folder if it doesn't exist
		// http://developer.android.com/reference/android/os/Environment.html#getExternalStoragePublicDirectory(java.lang.String)
		// and: http://stackoverflow.com/questions/7592800/android-how-to-use-and-create-temporary-folder
		final File mantraTempFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/MantraTmp");
		Log.d(CN+".GetImagesTask.doInBackground", "folder must exist: " + mantraTempFolder);
		if(!mantraTempFolder.exists()) {
			Log.d(CN+".GetImagesTask.doInBackground", "folder doesn't exist; creating: " + mantraTempFolder);
			mantraTempFolder.mkdirs();
		}
		
		// fetch and save each image, updating progress and scanning the image (for gallery-viewability) along the way.
		long startTime = System.currentTimeMillis();
		int i = 0, imageCount = imagesToDownload.keySet().size();

		String[] fullFilePathsToScan = new String[imageCount];
		Thread thread = null;
		for(String url : imagesToDownload.keySet()) {
			Log.d(CN+".GetImagesTask.doInBackground", "saving image to (" + mantraTempFolder + ") from: " + url);
	        
			int imageSize = imagesToDownload.get(url);
			totalSize += imageSize;
	        publishProgress((int) ((imageSize / (float) count) * 100));
	        // Escape early if cancel() is called
	        if (isCancelled()) break;
			
	        long startTimeForImage = System.currentTimeMillis();
			String fileName = ImageExtractor.downloadAndSaveImage(mantraTempFolder.getAbsolutePath() + "/", url);
	        fullFilePathsToScan[i] = mantraTempFolder.getAbsolutePath() + "/" + fileName;
			long endTimeForImage = System.currentTimeMillis();

			Log.d(CN+".GetImagesTask.doInBackground", "elapsed time to fetch image (" + url + ") (ms) = " + ((double)endTimeForImage - startTimeForImage));
			i++;
		}
		
		// ATTEMPT 3: run the image-scanning in a service called via an intent.
		Intent mediaScannerIntent = new Intent(activity, MediaScannerService.class);
		mediaScannerIntent.putExtra(INTENT_KEY_FILE_PATHS_TO_SCAN, fullFilePathsToScan);
//		mediaScannerIntent.putExtra(INTENT_KEY_ORIGINATING_ACTIVITY, activity);
		activity.startService(mediaScannerIntent);
		
		long endTime = System.currentTimeMillis();
		Log.d(CN+".GetImagesTask.doInBackground", "exiting; ELAPSED TIME (ms) = " + ((double)endTime - startTime));

		return null;
	}
	

	private void publishProgress(int i) {
		Log.d(CN+".publishProgress", "progress: i = " + i);
	}
//
}