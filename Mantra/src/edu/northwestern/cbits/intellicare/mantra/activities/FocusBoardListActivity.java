package edu.northwestern.cbits.intellicare.mantra.activities;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.northwestern.cbits.intellicare.mantra.DatabaseHelper;
import edu.northwestern.cbits.intellicare.mantra.FocusBoardCursorAdapter;
import edu.northwestern.cbits.intellicare.mantra.FocusBoardManager;
import edu.northwestern.cbits.intellicare.mantra.DatabaseHelper.FocusBoardCursor;
import edu.northwestern.cbits.intellicare.mantra.ImageExtractor;
import edu.northwestern.cbits.intellicare.mantra.R;
import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class FocusBoardListActivity extends ListActivity {
	
	private FocusBoardCursor mCursor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.focus_board_list_activity);
		mCursor = FocusBoardManager.get(this).queryFocusBoards();
		FocusBoardCursorAdapter adapter = new FocusBoardCursorAdapter(this, mCursor);
		setListAdapter(adapter);
		
		
		// handle external intents; src: http://developer.android.com/training/sharing/receive.html
		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();
		
		Log.d("FocusBoardActivityList.onCreate", "action = " + action + "; type = " + type);
		if(Intent.ACTION_SEND.equals(action) && type != null) {
			if("text/plain".equals(type)) {
				handleSendText(intent);
			}
		}
	}
	
	private void handleSendText(Intent intent) {
		Log.d("FocusBoardActivityList.handleSendText", "entered; intent = " + intent);
		String urlFromBrowser = intent.getStringExtra(Intent.EXTRA_TEXT);
		new GetImageListAndSizesTask().execute(urlFromBrowser);
	}

	@Override
	protected void onDestroy() {
		mCursor.close();
		super.onDestroy();
	}
}



/**** Async tasks *****/

/**
 * Fetches the set of image URLs from webpage at a specified URL.
 * @author mohrlab
 *
 */
class GetImageListAndSizesTask extends AsyncTask<String, Void, Map<String, Integer>> {
	@Override
	protected Map<String, Integer> doInBackground(String... arg0) {
		try {
			Log.d("GetImageListAndSizesTask.doInBackground", "entered");
			long startTime = System.currentTimeMillis();
			Set<String> imageList = ImageExtractor.getImageList(arg0[0], false);
			long imageListTime = System.currentTimeMillis();
			Map<String,Integer> m = ImageExtractor.getRemoteContentLength(imageList);
			long endTime = System.currentTimeMillis();
			Log.d("GetImageListAndSizesTask.doInBackground", 
					"exiting; ELAPSED TIME (ms) = " + ((double)endTime - startTime) + 
					", getImageList (ms) = " + ((double)(imageListTime - startTime)) + 
					", getRemoteContentLength (ms) = " + ((double)(endTime - imageListTime))
					);
			return m;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	protected void onPostExecute(Map<String, Integer> imageInfo) {
		for(String key : imageInfo.keySet()) {
			Log.d("GetImageListAndSizesTask.onPostExecute", "size = " + imageInfo.get(key) + " for image " + key);
		}
	}
}
