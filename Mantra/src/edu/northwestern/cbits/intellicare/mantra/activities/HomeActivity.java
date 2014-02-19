package edu.northwestern.cbits.intellicare.mantra.activities;

import edu.northwestern.cbits.intellicare.mantra.FocusBoardGridFragment;
import edu.northwestern.cbits.intellicare.mantra.R;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class HomeActivity extends ActionBarActivity {
	
	private static final String CN = "HomeActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fragment);

		Log.d("HomeActivity.onCreate", "entered");

		FragmentManager fm = getSupportFragmentManager();
		Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);
		
		if (fragment == null) {
			fragment = new FocusBoardGridFragment();
			fm.beginTransaction()
				.add(R.id.fragmentContainer, fragment)
				.commit();
		}
	}
	
//	private void handleSelectedImageIntent() {
//		Intent intent = getIntent();
//			
//		if(intent != null) {
//			// get a thumbnail of the image: http://stackoverflow.com/questions/14978566/how-to-get-selected-image-from-gallery-in-android
//			Uri selectedImage = intent.getData();
//			
//			if(selectedImage != null && selectedImage.toString().length() > 0) {
////				String[] filePathColumn = { MediaStore.Images.Media.DATA };
////				Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
////				cursor.moveToFirst();
////				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
////				String picturePath = cursor.getString(columnIndex);
////				Log.d(CN+"onActivityResult", "picturePath = " + picturePath);
////				cursor.close();
////				Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));
//		
//				Log.d(CN+".handleSelectedImageIntent", "exiting");
//			}
//		}
//	}

	protected Fragment createFragment() {
		return new FocusBoardGridFragment();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.home_activity_actions, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.action_new_focus_board:
	            openNewFocusBoardActivity();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	private void openNewFocusBoardActivity() {
		Intent intent = new Intent(this, NewFocusBoardActivity.class);
		startActivity(intent);
	}
}
