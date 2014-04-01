package edu.northwestern.cbits.intellicare.mantra;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.zip.Inflater;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
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
//	public static final String INTENT_KEY_ORIGINATING_ACTIVITY = "originatingActivity";
	private ProgressBar progress;
	private String currentProgressActionTextValue;
	private TextView currentProgressActionText = null;
	private View progressBarView;

	
	public GetImagesTask(ProgressBar p) {
		progress = p;
	}

	
	@Override
	protected Object doInBackground(GetImagesTaskParams... arg0) {
		final Activity activity = arg0[0].activity;
		Map<String, Integer> imagesToDownload = arg0[0].imagesToDownload;
//		progress = arg0[0].progress;
		progressBarView = arg0[0].progressBarView;
		
		currentProgressActionText = (TextView) progressBarView.findViewById(R.id.currentProgressAction);
		Log.d(CN+".doInBackground", "currentProgressActionText = " + currentProgressActionText);
				
		int count = imagesToDownload.keySet().size();
		int totalSize = 0;

		// create the temp folder if it doesn't exist
		// http://developer.android.com/reference/android/os/Environment.html#getExternalStoragePublicDirectory(java.lang.String)
		// and: http://stackoverflow.com/questions/7592800/android-how-to-use-and-create-temporary-folder
		final File mantraTempFolder = new File(Paths.MANTRA_IMAGES_TMP);
		Log.d(CN+".GetImagesTask.doInBackground", "folder must exist: " + mantraTempFolder);
		if(!mantraTempFolder.exists()) {
			Log.d(CN+".GetImagesTask.doInBackground", "folder doesn't exist; creating: " + mantraTempFolder);
			mantraTempFolder.mkdirs();
		}
		
		// fetch and save each image, updating progress and scanning the image (for gallery-viewability) along the way.
		long startTime = System.currentTimeMillis();
		int i = 0, imageCount = imagesToDownload.keySet().size();
		Log.d(CN+".doInBackground", "imageCount = " + imageCount);

		// set the max progress bar value
		progress.setMax(imageCount + 1);	// +1 for the image-size download
		currentProgressActionTextValue = "Getting image file sizes...;";
		publishProgress();

		String[] fullFilePathsToScan = new String[imageCount];
		Thread thread = null;
		for(String url : imagesToDownload.keySet()) {
			Log.d(CN+".GetImagesTask.doInBackground", "saving image to (" + mantraTempFolder + ") from: " + url);
	        
			int imageSize = imagesToDownload.get(url);
			totalSize += imageSize;
			currentProgressActionTextValue = "(" + imageSize + " bytes) Downloading: " + url;
	        activity.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					publishProgress();
				}
			});
			
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
		mediaScannerIntent.putExtra(MediaScannerService.INTENT_KEY_FILE_PATHS_TO_SCAN, fullFilePathsToScan);
		activity.startService(mediaScannerIntent);
		
		activity.finish();
		
		long endTime = System.currentTimeMillis();
		Log.d(CN+".GetImagesTask.doInBackground", "exiting; ELAPSED TIME (ms) = " + ((double)endTime - startTime));

		return null;
	}
	
	@Override
	protected void onProgressUpdate(Void... values) {
		super.onProgressUpdate(values);
		Log.d(CN+".onProgressUpdate", "entered");

		progress.incrementProgressBy(1);
		Log.d(CN+".onProgressUpdate", "Should set progress bar text to: " + currentProgressActionTextValue);
		currentProgressActionText.setText(currentProgressActionTextValue);
//		currentProgressActionText.endBatchEdit();
//		currentProgressActionText.invalidate();
	}

}