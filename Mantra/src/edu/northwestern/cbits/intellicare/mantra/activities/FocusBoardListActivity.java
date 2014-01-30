package edu.northwestern.cbits.intellicare.mantra.activities;

import edu.northwestern.cbits.intellicare.mantra.DatabaseHelper;
import edu.northwestern.cbits.intellicare.mantra.FocusBoardCursorAdapter;
import edu.northwestern.cbits.intellicare.mantra.FocusBoardManager;
import edu.northwestern.cbits.intellicare.mantra.DatabaseHelper.FocusBoardCursor;
import edu.northwestern.cbits.intellicare.mantra.R;
import android.app.ListActivity;
import android.os.Bundle;

public class FocusBoardListActivity extends ListActivity {
	
	private FocusBoardCursor mCursor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.focus_board_list_activity);
		mCursor = FocusBoardManager.get(this).queryFocusBoards();
		FocusBoardCursorAdapter adapter = new FocusBoardCursorAdapter(this, mCursor);
		setListAdapter(adapter);
	}
	
	@Override
	protected void onDestroy() {
		mCursor.close();
		super.onDestroy();
	}
}
