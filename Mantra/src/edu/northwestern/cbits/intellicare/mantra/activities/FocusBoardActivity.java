package edu.northwestern.cbits.intellicare.mantra.activities;

import java.io.File;

import edu.northwestern.cbits.intellicare.mantra.DatabaseHelper.FocusBoardCursor;
import edu.northwestern.cbits.intellicare.mantra.DatabaseHelper.FocusImageCursor;
import edu.northwestern.cbits.intellicare.mantra.FocusBoard;
import edu.northwestern.cbits.intellicare.mantra.FocusBoardGridFragment;
import edu.northwestern.cbits.intellicare.mantra.FocusBoardManager;
import edu.northwestern.cbits.intellicare.mantra.FocusImageGridFragment;
import edu.northwestern.cbits.intellicare.mantra.R;
import android.content.Context;
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
import android.widget.Toast;

public class FocusBoardActivity extends ActionBarActivity {

	private static final String CN = "FocusBoardActivity";
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

		handleSelectedImageIntent();

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

	
	private void handleSelectedImageIntent() {
		Log.d(CN+".handleSelectedImageIntent", "entered");
		Intent intent = getIntent();
			
		if(intent != null) {
			// get a thumbnail of the image: http://stackoverflow.com/questions/14978566/how-to-get-selected-image-from-gallery-in-android
			Uri selectedImage = intent.getData();
			
			if(selectedImage != null && selectedImage.toString().length() > 0) {
				mFocusBoardId = intent.getLongExtra(NewFocusBoardActivity.FOCUS_BOARD_ID, -1);
				mManager = FocusBoardManager.get(this);
				FocusBoard focusBoard = mManager.getFocusBoard(mFocusBoardId);

				String filePathToImage = getRealPathFromURI(this, selectedImage).trim();
				
				// search the image set for a particular image path, and if the image isn't already associated with the board,
				// then associate it. 
				FocusImageCursor fic = mManager.queryFocusImages(mFocusBoardId);
				boolean imageAlreadyAssociated = false;
				while(fic.moveToNext()) {
					String path = fic.getString(FocusBoardManager.COL_INDEX_FILE_PATH).trim();
					Log.d(CN+".handleSelectedImageIntent", "path.equals(filePathToImage) = " + (path.equals(filePathToImage)) + "; path = \"" + path + "\"" + "; filePathToImage = \"" + filePathToImage + "\"");
					if(path.equals(filePathToImage)) {
						Toast.makeText(this, "Image already applied. Consider choosing a different one!", Toast.LENGTH_LONG).show();
						imageAlreadyAssociated = true;
						break;
					}
				}
				if(!imageAlreadyAssociated) {
					Log.d(CN+".handleSelectedImageIntent","for board " + mFocusBoardId + ", associating image = " + filePathToImage);
					Toast.makeText(this, "Image applied!", Toast.LENGTH_SHORT).show();
					mManager.createFocusImage(mFocusBoardId, filePathToImage);					
				}
			}
			else
				Log.d(CN+".handleSelectedImageIntent", "one of the conds is untrue: " + (selectedImage != null) + " && " + (selectedImage == null ? "<not evaluable>" : selectedImage.toString().length() > 0) + ")");
		}
		else
			Log.d(CN+".handleSelectedImageIntent", "intent is null");
	}
	
	// src: http://stackoverflow.com/questions/3401579/get-filename-and-path-from-uri-from-mediastore
	public String getRealPathFromURI(Context context, Uri contentUri) {
		  Cursor cursor = null;
		  try { 
		    String[] proj = { MediaStore.Images.Media.DATA };
		    cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
		    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		    cursor.moveToFirst();
		    return cursor.getString(column_index);
		  } finally {
		    if (cursor != null) {
		      cursor.close();
		    }
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
