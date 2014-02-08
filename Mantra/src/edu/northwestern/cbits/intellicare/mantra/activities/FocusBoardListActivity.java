package edu.northwestern.cbits.intellicare.mantra.activities;

import java.io.IOException;
import java.util.Set;

import edu.northwestern.cbits.intellicare.mantra.DatabaseHelper;
import edu.northwestern.cbits.intellicare.mantra.FocusBoardCursorAdapter;
import edu.northwestern.cbits.intellicare.mantra.FocusBoardManager;
import edu.northwestern.cbits.intellicare.mantra.DatabaseHelper.FocusBoardCursor;
import edu.northwestern.cbits.intellicare.mantra.ImageExtractor;
import edu.northwestern.cbits.intellicare.mantra.R;
import android.app.ListActivity;
import android.content.Intent;
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

		try {
			Set<String> imageSet = ImageExtractor.getImageList(intent.getStringExtra(Intent.EXTRA_TEXT), false);
			for(String s : imageSet) {
				Log.d("FocusBoardActivityList.handleSendText", "image URL = " + s);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void onDestroy() {
		mCursor.close();
		super.onDestroy();
	}
}
