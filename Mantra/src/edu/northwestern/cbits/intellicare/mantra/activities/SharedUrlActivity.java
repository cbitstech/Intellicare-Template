package edu.northwestern.cbits.intellicare.mantra.activities;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.northwestern.cbits.intellicare.mantra.Constants;
import edu.northwestern.cbits.intellicare.mantra.GetImagesTask;
import edu.northwestern.cbits.intellicare.mantra.GetImagesTaskParams;
import edu.northwestern.cbits.intellicare.mantra.ImageExtractor;
import edu.northwestern.cbits.intellicare.mantra.MediaScannerService;
//import edu.northwestern.cbits.intellicare.mantra.MediaScannerServiceResponseReceiver;
import edu.northwestern.cbits.intellicare.mantra.R;
import edu.northwestern.cbits.intellicare.mantra.R.layout;
import edu.northwestern.cbits.intellicare.mantra.R.menu;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;

/**
 * Handles the passing of a URL from another Android app (e.g. Google Chrome) to this one.
 * @author mohrlab
 *
 */
public class SharedUrlActivity extends Activity {
	
	/***** non-methods *****/
	private static final String CN = "SharedUrlActivity";
	
	private static SharedUrlActivity self;

	/**
	 * Handles a response from the MediaScannerService.
	 * Src: https://developer.android.com/training/run-background-service/report-status.html
	 * @author mohrlab
	 *
	 */
	private class MediaScannerServiceResponseReceiver extends BroadcastReceiver {
		public static final String CN = "MediaScannerServiceResponseReceiver";
		
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			Log.d(CN+".onReceive", "arg1 = " + arg1);
			
			Bundle extras = arg1.getExtras();
			if(extras != null) {
				Log.d(CN+".onReceive", "intent not null");
				Log.d(CN+".onReceive", "message = " + arg1.getStringExtra("message"));
				
				if(!extras.containsKey(MediaScannerService.INTENT_KEY_TO_RECEIVER_STRINGARRAY)) {
					// display the Android image gallery for the folder
					// ATTEMPT 1; displays Gallery or Photos, but only allows picking 1 image
					Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
					self.startActivityForResult(intent, GetImagesTask.RESULT_LOAD_IMAGE);
				}
			}
			else {
				Log.d(CN+".onReceive", "intent is null");
			}
		}
	}
	private MediaScannerServiceResponseReceiver mediaScannerServiceResponseReceiver;
	
	
	
	/***** methods *****/
	
	@Override
	protected void onDestroy() {
		LocalBroadcastManager
			.getInstance(this)
			.unregisterReceiver(mediaScannerServiceResponseReceiver);
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.shared_url, menu);
		return true;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shared_url);

		Log.d("SharedUrlActivity.onCreate", "entered");
		self = this;

		// setup external intent-handler
		handleExternalIntents();

		// set-up the image-scanner service + response.
		mediaScannerServiceResponseReceiver = new MediaScannerServiceResponseReceiver();
		LocalBroadcastManager
			.getInstance(this)
			.registerReceiver(
					mediaScannerServiceResponseReceiver, 
					new IntentFilter(Constants.BROADCAST_ACTION)
			);
	}

	
	/**
	 * Handles the response from the image gallery activity.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data); 
		Log.d(CN + ".onActivityResult", "requestCode = " + requestCode + "; resultCode = " + resultCode + "; data = " + data);
		
		if(requestCode == GetImagesTask.RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
			Intent intent = new Intent(this, HomeActivity.class);
			intent.setData(data.getData());
			this.startActivity(intent);
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
		        	new GetImageListAndSizesTask(self)
		        		.execute(url);
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
			try {
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
			}
			catch(SocketTimeoutException e) { displayNetworkExceptionMessage(url, e); }
			catch(UnknownHostException e) { displayNetworkExceptionMessage(url, e); }
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}

	/**
	 * @param url
	 * @param e 
	 */
	private void displayNetworkExceptionMessage(String url, IOException e) {
		e.printStackTrace();
		AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this.activity);
		dlgAlert.setMessage("Network error. Are you connected to the Internet? Error message for URL (" + url + "): " + e.getMessage());
		dlgAlert.setPositiveButton("OK",
			    new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int which) {
			          //dismiss the dialog  
			        }
			    });
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