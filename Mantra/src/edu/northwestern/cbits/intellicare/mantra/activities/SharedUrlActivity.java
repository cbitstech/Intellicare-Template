package edu.northwestern.cbits.intellicare.mantra.activities;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.northwestern.cbits.intellicare.mantra.ImageExtractor;
import edu.northwestern.cbits.intellicare.mantra.MediaScannerService;
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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

		Log.d("SharedUrlActivity.onCreate", "entered");

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
	 * Handles the response from the image gallery activity.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data); 
		Log.d(CN + ".onActivityResult", "requestCode = " + requestCode + "; resultCode = " + resultCode + "; data = " + data);
		
		if(requestCode == GetImagesTask.RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
//		if(requestCode == GetImagesTask.RESULT_LOAD_IMAGE && data != null) {
//			Log.d(CN + ".onActivityResult", "in conditional");
			
			// get a thumbnail of the image: http://stackoverflow.com/questions/14978566/how-to-get-selected-image-from-gallery-in-android
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };
			
			Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String picturePath = cursor.getString(columnIndex);
			cursor.close();
			Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));
			
		}
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
	
	public static final int RESULT_LOAD_IMAGE = 1;

	@Override
	protected Object doInBackground(GetImagesTaskParams... arg0) {
		final SharedUrlActivity activity = arg0[0].activity;
		Map<String, Integer> imagesToDownload = arg0[0].imagesToDownload;
		int count = imagesToDownload.keySet().size();
		int totalSize = 0;

		// create the temp folder if it doesn't exist
		// http://developer.android.com/reference/android/os/Environment.html#getExternalStoragePublicDirectory(java.lang.String)
		// and: http://stackoverflow.com/questions/7592800/android-how-to-use-and-create-temporary-folder
		final File mantraTempFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/MantraES");
		Log.d(CN+".onPostExecute", "folder must exist: " + mantraTempFolder);
		if(!mantraTempFolder.exists()) {
			Log.d(CN+".onPostExecute", "folder doesn't exist; creating: " + mantraTempFolder);
			mantraTempFolder.mkdirs();
		}
		
		// fetch and save each image, updating progress and scanning the image (for gallery-viewability) along the way.
		long startTime = System.currentTimeMillis();
		int i = 0, imageCount = imagesToDownload.keySet().size();

		String[] fullFilePathsToScan = new String[imageCount];
		Thread thread = null;
		for(String url : imagesToDownload.keySet()) {
			Log.d(CN + ".onPostExecute", "saving image to (" + mantraTempFolder + ") from: " + url);
	        int imageSize = imagesToDownload.get(url);
			totalSize += imageSize;
	        publishProgress((int) ((imageSize / (float) count) * 100));
	        // Escape early if cancel() is called
	        if (isCancelled()) break;
			long startTimeForImage = System.currentTimeMillis();
	        String fileName = ImageExtractor.downloadAndSaveImage(mantraTempFolder.getAbsolutePath() + "/", url);
	        fullFilePathsToScan[i] = mantraTempFolder.getAbsolutePath() + "/" + fileName;
			long endTimeForImage = System.currentTimeMillis();
			Log.d(CN + ".doInBackground", "elapsed time to fetch image (" + url + ") (ms) = " + ((double)endTimeForImage - startTimeForImage));

//			// ATTEMPT 2: http://stackoverflow.com/questions/18624235/android-refreshing-the-gallery-after-saving-new-images
//			
//			// Since scanFile runs async, and onScanCompleted runs for each file scanned, and there is no way currently to do e.g. "onAllFilesCompleted",
//			// I'm employing this threading-based hack to wait on all files' completion before resuming procedurally.
//			thread = new Thread(new ScanMyMedia(activity, new String[] { fullFilePathsToScan[i] }));
//			thread.start();
//	        i++;
		}
		
		// ATTEMPT 3: run the image-scanning in a service called via an intent.
		Intent mediaScannerIntent = new Intent(activity, MediaScannerService.class);
		mediaScannerIntent.putExtra("fullFilePathsToScan", fullFilePathsToScan);
		activity.startService(mediaScannerIntent);
		
		long endTime = System.currentTimeMillis();
		Log.d(CN + ".doInBackground", "exiting; ELAPSED TIME (ms) = " + ((double)endTime - startTime));

//		try {
//			thread.join();
//			Log.d(CN+".doInBackground", "thread.join() called; did the 2 file scans complete first?");
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
				
		Log.d(CN + ".GetImagesTask.doInBackground", "past MediaScannerConnection.scanFile");

		return null;
	}
	
	/**
	 * Class to pass to a thread to scan a media file into the Android DB.
	 * @author mohrlab
	 *
	 */
	class ScanMyMedia implements Runnable {
		
		SharedUrlActivity activity;
		String[] fullFilePathsToScan;
		
		public ScanMyMedia(SharedUrlActivity p1, String[] p2) { activity = p1; fullFilePathsToScan = p2; } 

		@Override
		public void run() {
			MediaScannerConnection.scanFile(activity, fullFilePathsToScan, null, new MediaScannerConnection.OnScanCompletedListener() {
				/*
	             *   (non-Javadoc)
	             * @see android.media.MediaScannerConnection.OnScanCompletedListener#onScanCompleted(java.lang.String, android.net.Uri)
	             */
	            public void onScanCompleted(String path, Uri uri) 
	            {
					Log.i(CN + ".MediaScannerConnection.onScanCompleted", "Scanned " + path + ":");
					Log.i(CN + ".MediaScannerConnection.onScanCompleted", "-> uri=" + uri);
	      
					// display the Android image gallery for the folder
					// ATTEMPT 1; displays Gallery or Photos, but only allows picking 1 image
					Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
					activity.startActivityForResult(intent, RESULT_LOAD_IMAGE);
	                
//		                // ATTEMPT 2: displays image picker for multiple images, but requires API 18 (Android >= 4.3)
//		          		Intent i = new Intent();
//		          		i.setType("image/*");
//		          		i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
//		          		i.setAction(Intent.ACTION_GET_CONTENT);
//		          		activity.startActivityForResult(Intent.createChooser(i,"Select Picture"), RESULT_LOAD_IMAGE);
					
	            }
	        });
		}
		
	}
	
	private void publishProgress(int i) {
		Log.d(CN+".publishProgress", "progress: i = " + i);
	}

}