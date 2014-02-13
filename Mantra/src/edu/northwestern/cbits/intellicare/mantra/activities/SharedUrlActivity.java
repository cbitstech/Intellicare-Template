package edu.northwestern.cbits.intellicare.mantra.activities;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.northwestern.cbits.intellicare.mantra.ImageExtractor;
import edu.northwestern.cbits.intellicare.mantra.R;
import edu.northwestern.cbits.intellicare.mantra.R.layout;
import edu.northwestern.cbits.intellicare.mantra.R.menu;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;

/**
 * Handles the passing of a URL from another Android app (e.g. Google Chrome) to this one.
 * @author mohrlab
 *
 */
public class SharedUrlActivity extends Activity {
	private static final String CN = "SharedUrlActivity";
	
	private static SharedUrlActivity self;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shared_url);
		
		self = this;
		handleExternalIntents();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.shared_url, menu);
		return true;
	}

	
	/**
	 * Handle external intents; src: http://developer.android.com/training/sharing/receive.html
	 */
	private void handleExternalIntents() {
		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();
		
		Log.d("FocusBoardActivityList.onCreate", "action = " + action + "; type = " + type);
		if(Intent.ACTION_SEND.equals(action) && type != null) {
			if("text/plain".equals(type)) {
				String urlFromBrowser = intent.getStringExtra(Intent.EXTRA_TEXT);
				promptConfirmDownloadPageImages(urlFromBrowser);
			}
		}
	}

	/**
	 * Prompts the user to confirm they wish to fetch the images at the URL they passed from some some other app (e.g. Chrome).
	 * @param url
	 */
	public void promptConfirmDownloadPageImages(final String url) {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        switch (which){
		        case DialogInterface.BUTTON_POSITIVE:
		            //Yes button clicked
		        	Log.d(CN + ".promptConfirmDownloadPageImages", "Yes path");
		        	new GetImageListAndSizesTask(self).execute(url);
		            break;

		        case DialogInterface.BUTTON_NEGATIVE:
		            //No button clicked
		        	Log.d(CN + ".promptConfirmDownloadPageImages", "No path");
		            break;
		        }
		    }
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Download and choose from the images at this URL?: \"" + url + "\"")
			.setPositiveButton("Yes", dialogClickListener)
		    .setNegativeButton("No", dialogClickListener)
		    .show();
	}
	
	/**
	 * Heuristic function to determine whether to download an image. 
	 * @param imageByteSize
	 * @return
	 */
	public static boolean shouldDownloadImage(int imageByteSize) {
		return imageByteSize >= 15000;		// if at least 15kBytes, then true. simplest heuristic ever. 
	}
}



/**** Async tasks *****/


class GetImageListAndSizesTaskBackgroundReturn {
	protected String url;
	protected Map<String, Integer> imageUrlsAndSizes;
	public GetImageListAndSizesTaskBackgroundReturn(String u, Map<String, Integer> m) { url = u; imageUrlsAndSizes = m; }
}

/**
 * Fetches the set of image URLs from webpage at a specified URL.
 * @author mohrlab
 *
 */
class GetImageListAndSizesTask extends AsyncTask<String, Void, GetImageListAndSizesTaskBackgroundReturn> {
	private static final String CN = "GetImageListAndSizesTask";
	public SharedUrlActivity activity;
		
	public GetImageListAndSizesTask(SharedUrlActivity sua) {
		activity = sua;
	}
	
	@Override
	protected GetImageListAndSizesTaskBackgroundReturn doInBackground(String... arg0) {
		try {
			String url = arg0[0];
			Log.d(CN + ".doInBackground", "entered for url = " + url);
			long startTime = System.currentTimeMillis();
			Set<String> imageList = ImageExtractor.getImageList(url, false);
			long imageListTime = System.currentTimeMillis();
			Map<String,Integer> m = ImageExtractor.getRemoteContentLength(imageList);
			long endTime = System.currentTimeMillis();
			Log.d(CN + ".doInBackground", 
					"exiting; ELAPSED TIME (ms) = " + ((double)endTime - startTime) + 
					", getImageList (ms) = " + ((double)(imageListTime - startTime)) + 
					", getRemoteContentLength (ms) = " + ((double)(endTime - imageListTime))
					);
			GetImageListAndSizesTaskBackgroundReturn ret = new GetImageListAndSizesTaskBackgroundReturn(url, m);
			return ret;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	protected void onPostExecute(GetImageListAndSizesTaskBackgroundReturn backgroundRet) {
		// select the set of images to download, using some heuristic function
		Map<String, Integer> imagesToDownload = new HashMap<String, Integer>();
		for(String key : backgroundRet.imageUrlsAndSizes.keySet()) {
			int sz = backgroundRet.imageUrlsAndSizes.get(key);
			Log.d(CN + ".onPostExecute", "size = " + sz + " for image " + key);
			if(SharedUrlActivity.shouldDownloadImage(sz)) {
				imagesToDownload.put(key, sz);
			}
		}

		// fetch the selected images from their URLs and save to the temp folder
		new GetImagesTask().execute(new GetImagesTaskParams(imagesToDownload, activity));
	}
}



class GetImagesTaskParams {
	Map<String,Integer> imagesToDownload;
	SharedUrlActivity activity;
	public GetImagesTaskParams(Map<String, Integer> d, SharedUrlActivity a) { imagesToDownload = d; activity = a; }
}
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
class GetImagesTask extends AsyncTask<GetImagesTaskParams, Void, Object> {
	public static final String CN = "GetImagesTask";

	@Override
	protected Object doInBackground(GetImagesTaskParams... arg0) {
		Map<String, Integer> imagesToDownload = arg0[0].imagesToDownload;
		int count = imagesToDownload.keySet().size();
		int totalSize = 0;

		// create the temp folder if it doesn't exist
		// http://developer.android.com/reference/android/os/Environment.html#getExternalStoragePublicDirectory(java.lang.String)
		// and: http://stackoverflow.com/questions/7592800/android-how-to-use-and-create-temporary-folder
		final File mantraTempFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/MantraTmp");
		Log.d(CN+".onPostExecute", "folder must exist: " + mantraTempFolder);
		if(!mantraTempFolder.exists()) {
			Log.d(CN+".onPostExecute", "folder doesn't exist; creating: " + mantraTempFolder);
			mantraTempFolder.mkdirs();
		}
		
		// fetch and save each image, updating progress along the way.
		long startTime = System.currentTimeMillis();
		for(String url : imagesToDownload.keySet()) {
			Log.d(CN + ".onPostExecute", "saving image to (" + mantraTempFolder + ") from: " + url);
	        int imageSize = imagesToDownload.get(url);
			totalSize += imageSize;
	        publishProgress((int) ((imageSize / (float) count) * 100));
	        // Escape early if cancel() is called
	        if (isCancelled()) break;
			long startTimeForImage = System.currentTimeMillis();
	        ImageExtractor.downloadAndSaveImage(mantraTempFolder.getAbsolutePath() + "/", url);
			long endTimeForImage = System.currentTimeMillis();
			Log.d(CN + ".doInBackground", "elapsed time to fetch image (" + url + ") (ms) = " + ((double)endTimeForImage - startTimeForImage));
		}
		long endTime = System.currentTimeMillis();
		Log.d(CN + ".doInBackground", "exiting; ELAPSED TIME (ms) = " + ((double)endTime - startTime));
		
		// run the media scanner so the images appear in the image gallery:
		// ATTEMPT 1: http://stackoverflow.com/questions/20523658/how-to-create-application-specific-folder-in-android-gallery
		final SharedUrlActivity activity = arg0[0].activity;
//		activity.sendBroadcast (
//			    new Intent(Intent.ACTION_MEDIA_MOUNTED, 
//			    Uri.parse("file://" + Environment.getExternalStorageDirectory()))
//			);

		// ATTEMPT 2: http://stackoverflow.com/questions/18624235/android-refreshing-the-gallery-after-saving-new-images
		MediaScannerConnection.scanFile(activity, new String[] { Environment.getExternalStorageDirectory().toString() }, null, new MediaScannerConnection.OnScanCompletedListener() {
            /*
             *   (non-Javadoc)
             * @see android.media.MediaScannerConnection.OnScanCompletedListener#onScanCompleted(java.lang.String, android.net.Uri)
             */
            public void onScanCompleted(String path, Uri uri) 
              {
                  Log.i("ExternalStorage", "Scanned " + path + ":");
                  Log.i("ExternalStorage", "-> uri=" + uri);
                  
//                  new GetImagesTaskBackgroundRet(mantraTempFolder.getAbsolutePath(), activity);
                  
          		// display the Android image gallery for the folder
          		Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
          		activity.startActivityForResult(i, 0);
              }
            });
		
//		return new GetImagesTaskBackgroundRet(mantraTempFolder.getAbsolutePath(), arg0[0].activity);
		return null;
	}
	
	private void publishProgress(int i) {
		Log.d(CN+".publishProgress", "progress: i = " + i);
	}

//	protected void onPostExecute(GetImagesTaskBackgroundRet imagesSavedRet) {
//		Log.d(CN+".onPostExecute", "entered; display image gallery now for imageSavePath = " + imagesSavedRet.saveFolderPath);
//		
//		// display the Android image gallery for the folder
//		Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
//		imagesSavedRet.activity.startActivityForResult(i, 0);
//	}
	
}