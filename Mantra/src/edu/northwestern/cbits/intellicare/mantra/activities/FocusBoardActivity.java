package edu.northwestern.cbits.intellicare.mantra.activities;

import edu.northwestern.cbits.intellicare.mantra.FocusBoard;
import edu.northwestern.cbits.intellicare.mantra.FocusBoardGridFragment;
import edu.northwestern.cbits.intellicare.mantra.FocusBoardManager;
import edu.northwestern.cbits.intellicare.mantra.FocusImageGridFragment;
import edu.northwestern.cbits.intellicare.mantra.R;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class FocusBoardActivity extends ActionBarActivity {

	private FocusBoardManager mManager;
	private long mFocusBoardId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.focus_board_activity);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		addMantraText();
		addImageGridFragment();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.focus_board_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.new_image_action:
			startCollectCameraActivity();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void startCollectCameraActivity() {
		Intent intent = new Intent(this, CollectCameraActivity.class);
		intent.putExtra(NewFocusBoardActivity.FOCUS_BOARD_ID, mFocusBoardId);
		startActivity(intent);
	}

	private void addMantraText() {
		Intent intent = getIntent();
		mFocusBoardId = intent.getLongExtra(
				NewFocusBoardActivity.FOCUS_BOARD_ID, -1);
		mManager = FocusBoardManager.get(this);
		FocusBoard focusBoard = mManager.getFocusBoard(mFocusBoardId);
		TextView mantraText = (TextView) findViewById(R.id.focus_board_mantra);
		mantraText.setText(focusBoard.getMantra());
	}
	
	private void addImageGridFragment() {
		FragmentManager fm = getSupportFragmentManager();
		Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);
		
		if (fragment == null) {
			fragment = new FocusImageGridFragment();
			fm.beginTransaction()
				.add(R.id.fragmentContainer, fragment)
				.commit();
		}
	}
}
