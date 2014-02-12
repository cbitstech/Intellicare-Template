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

		//			Set<String> imageSet = ImageExtractor.getImageList(intent.getStringExtra(Intent.EXTRA_TEXT), false);
//			for(String s : imageSet) {
//				Log.d("FocusBoardActivityList.handleSendText", "image URL = " + s);
		new GetImageListTask().execute(intent.getStringExtra(Intent.EXTRA_TEXT));
//			}
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
class GetImageListTask extends AsyncTask<String, Void, Set<String>> {
	@Override
	protected Set<String> doInBackground(String... arg0) {
		try {
			return ImageExtractor.getImageList(arg0[0], false);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	protected void onPostExecute(Set<String> imageSet) {
//		for(String s : imageSet) {
//			Log.d("FocusBoardActivityList.handleSendText", "image URL = " + s);
//		}
		new GetRemoteContentLengthTask().execute(imageSet);
	}
}

/**
 * Fetches the size of images at a specified set of URLs.
 * @author mohrlab
 *
 */
class GetRemoteContentLengthTask extends AsyncTask<Set<String>, Void, Map<String, Integer>> {
	
	protected void onPostExecute(Map<String, Integer> imageSizes) {
		for(String key : imageSizes.keySet()) {
			Log.d("FocusBoardActivityList.handleSendText", "size = " + imageSizes.get(key) + " for image " + key);
		}		
	}

	@Override
	protected Map<String, Integer> doInBackground(Set<String>... params) {
		return ImageExtractor.getRemoteContentLength(params[0]);
	}
}



