package edu.northwestern.cbits.intellicare.mantra.activities;

import java.io.File;

import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.mantra.Constants;
import edu.northwestern.cbits.intellicare.mantra.GetImageListAndSizesTask;
import edu.northwestern.cbits.intellicare.mantra.GetImagesTask;
import edu.northwestern.cbits.intellicare.mantra.MediaScannerService;
import edu.northwestern.cbits.intellicare.mantra.Paths;
import edu.northwestern.cbits.intellicare.mantra.R;
import edu.northwestern.cbits.intellicare.mantra.Util;
import edu.northwestern.cbits.intellicare.mantra.R.layout;
import edu.northwestern.cbits.intellicare.mantra.R.menu;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;


/**
 * Handles the passing of a URL from another Android app (e.g. Google Chrome) to this one.
 * 
 * Also displays a progress bar.
 * @author mohrlab
 *
 */
public class ProgressActivity extends ConsentedActivity {

	/**
	 * Handles a response from the MediaScannerService by loading an image viewer of the user's choice (or default assignment).
	 * Src: https://developer.android.com/training/run-background-service/report-status.html
	 * @author mohrlab
	 *
	 */
	private class MediaScannerServiceResponseReceiver extends BroadcastReceiver {
		public static final String CN = "MediaScannerServiceResponseReceiver";
		
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			Log.d(CN+".onReceive", "arg1 = " + arg1);
			
			Bundle extras = arg1.getExtras();
			if(extras != null) {
				Log.d(CN+".onReceive", "intent not null");
				Log.d(CN+".onReceive", "message = " + arg1.getStringExtra("message"));
				
				if(!extras.containsKey(MediaScannerService.INTENT_KEY_TO_RECEIVER_STRINGARRAY)) {
					// display the Android image gallery for the folder
					// ATTEMPT 1; displays Gallery or Photos, but only allows picking 1 image
//					Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);

					// attempt to open the Mantra images folder, specifically
					Intent intent = new Intent(Intent.ACTION_PICK);
					intent.setDataAndType(Util.getContentUriViaImageFilePath(arg0, Paths.MANTRA_IMAGES_TMP), "image/*");
					
					self.startActivityForResult(intent, GetImagesTask.RESULT_LOAD_IMAGE);
				}
			}
			else {
				Log.d(CN+".onReceive", "intent is null");
			}
		}
	}

	private static final String CN = "ProgressActivity";
	private ProgressActivity self;
	private MediaScannerServiceResponseReceiver mediaScannerServiceResponseReceiver;
	
	public static final String INTENT_KEY_URL = "url"; 
	public static final String INTENT_KEY_TYPE_GETIMAGESANDSIZES = "getImagesAndSizes";
	public static final int INTENT_VAL_TYPE_GETIMAGESANDSIZES = 1;

	View progressBarView; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_progress);
		progressBarView = this.findViewById(R.id.activityProgressLayout);
		self = this;

		// set-up the image-scanner service + response.
		mediaScannerServiceResponseReceiver = new MediaScannerServiceResponseReceiver();
		LocalBroadcastManager
			.getInstance(this)
			.registerReceiver(
					mediaScannerServiceResponseReceiver, 
					new IntentFilter(Constants.BROADCAST_ACTION)
			);

		// setup external intent-handler
		handleExternalIntents();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.d(CN+".onResume", "entered");

		Intent receivedIntent = this.getIntent();
		int type = receivedIntent.getIntExtra(INTENT_KEY_TYPE_GETIMAGESANDSIZES, 0);
		String url = receivedIntent.getStringExtra("url");
		
		switch(type) {
			case INTENT_VAL_TYPE_GETIMAGESANDSIZES:
				final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar1);
				final View progressBarView = this.getLayoutInflater().inflate(R.layout.activity_progress, null);
				
				Log.d(CN+".onResume", "progressBarView == null = " + (progressBarView == null));
				new GetImageListAndSizesTask(this, progressBar, progressBarView).execute(url);
				break;
		}

		Log.d(CN+".onResume", "exiting");
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.progress, menu);
//		return true;
//	}

	
	@Override
	protected void onDestroy() {
		Log.d(CN+".onDestroy", "entered");
		LocalBroadcastManager
			.getInstance(this)
			.unregisterReceiver(mediaScannerServiceResponseReceiver);
		super.onDestroy();
		Log.d(CN+".onDestroy", "exiting");
	}


	/**
	 * Handle external intents; src: http://developer.android.com/training/sharing/receive.html
	 */
	private void handleExternalIntents() {
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		String action = intent.getAction();
		String type = intent.getType();
		
		Log.d(CN+".handleExternalIntents", "action = " + action + "; type = " + type);
		if(Intent.ACTION_SEND.equals(action) && type != null) {
			if("text/plain".equals(type)) {
				String urlFromBrowser = intent.getStringExtra(Intent.EXTRA_TEXT);
				promptConfirmDownloadPageImages(urlFromBrowser);
			}
		}
//		else if(extras != null && extras.getBoolean(MediaScannerService.INTENT_KEY_TO_RECEIVER_STRINGARRAY)) {
		else if(extras == null) {
			Log.d(CN+".handleExternalIntents", "intent from new-images notification alarm");
			SingleMantraBoardActivity.startBrowsePhotosActivity(this);
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

					final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar1);
//					final View progressBarView = self.getLayoutInflater().inflate(R.layout.activity_progress, null);
					
					Log.d(CN+".promptConfirmDownloadPageImages", "progressBarView == null = " + (progressBarView == null));
					
					new GetImageListAndSizesTask(self, progressBar, progressBarView).execute(url);

		        	break;

		        case DialogInterface.BUTTON_NEGATIVE:
		            //No button clicked
		        	Log.d(CN + ".promptConfirmDownloadPageImages", "No path");
		        	self.finish();
		            break;
		        }
		    }
		};

//		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.Theme_AppCompat_Light));
		builder
			.setTitle(self.getString(R.string.title_fetch_images))
			.setMessage(self.getString(R.string.download_and_choose_from_the_images_at_this_url_) + ": \n\n" + url)
			.setPositiveButton(self.getString(R.string.yes), dialogClickListener)
		    .setNegativeButton(self.getString(R.string.no), dialogClickListener)
		    .show();
	}
	
	
	/**
	 * Handles the response from the image gallery activity.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data); 
		Log.d(CN + ".onActivityResult", "requestCode = " + requestCode + "; resultCode = " + resultCode + "; data = " + data);
		
		updateActionBarSubtitle("Done!");
		if(requestCode == GetImagesTask.RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
			Intent intent = new Intent(this, IndexActivity.class);
			Uri uriFromImageBrowser = data.getData();
			Log.d(CN+".onActivityResult", "uriFromImageBrowser = " + uriFromImageBrowser.toString());
			intent.setData(uriFromImageBrowser);
			this.startActivity(intent);
        	self.finish();
		}
		else if (data == null) {
			self.finish();
		}
	}

	
	/**
	 * Heuristic function to determine whether to download an image. 
	 * @param imageByteSize
	 * @return
	 */
	public static boolean shouldDownloadImage(int imageByteSize) {
		return imageByteSize >= 15000;		// if at least 15kBytes, then true. simplest heuristic ever. 
	}

	public static Uri activityUri(Context context) 
	{
		return Uri.parse("intellicare://mantra/add_photos");
	}
	
	
	/**
	 * Updates the action bar subtitle. 
	 */
	public void updateActionBarSubtitle(final String msg) {
        self.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				self.getActionBar().setSubtitle(msg);
			}
		});
	}

	
}
