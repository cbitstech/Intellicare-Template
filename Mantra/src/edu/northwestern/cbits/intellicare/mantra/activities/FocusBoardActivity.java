package edu.northwestern.cbits.intellicare.mantra.activities;

import edu.northwestern.cbits.intellicare.mantra.FocusBoard;
import edu.northwestern.cbits.intellicare.mantra.FocusBoardGridFragment;
import edu.northwestern.cbits.intellicare.mantra.FocusBoardManager;
import edu.northwestern.cbits.intellicare.mantra.FocusImageGridFragment;
import edu.northwestern.cbits.intellicare.mantra.R;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class FocusBoardActivity extends ActionBarActivity {

	private FocusBoardManager mManager;
	private long mFocusBoardId;

	private static int RESULT_LOAD_IMAGE = 1;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.focus_board_activity);
		
		Log.d("FocusBoardActivity.onCreate", "entered");

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
		case R.id.existing_image_action:
			startBrowsePhotosActivity();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/* display image from gallery: BEGIN (src: http://viralpatel.net/blogs/pick-image-from-galary-android-app/) */
	private void startBrowsePhotosActivity() {
		Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(i, RESULT_LOAD_IMAGE);
	}

	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
        Log.d("onActivityResult", "entered; requestCode = " + requestCode + "; resultCode = " + resultCode + "; data = " + data);

        // if: existing image
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
	         Uri selectedImage = data.getData();
	         String[] filePathColumn = { MediaStore.Images.Media.DATA };
	 
	         Cursor cursor = getContentResolver().query(selectedImage,
	                 filePathColumn, null, null, null);
	         cursor.moveToFirst();
	 
	         int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
	         String picturePath = cursor.getString(columnIndex);
	         
	         cursor.close();
	                      
	         // String picturePath contains the path of selected Image
	         Log.d("onActivityResult", "picturePath = " + picturePath);
	     }
	}
	/* display image from gallery: END (src: http://viralpatel.net/blogs/pick-image-from-galary-android-app/) */

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
