package edu.northwestern.cbits.intellicare.mantra.activities;

import java.io.File;
import java.util.ArrayList;

import edu.northwestern.cbits.intellicare.mantra.DatabaseHelper.FocusBoardCursor;
import edu.northwestern.cbits.intellicare.mantra.DatabaseHelper.FocusImageCursor;
import edu.northwestern.cbits.intellicare.mantra.FocusBoard;
import edu.northwestern.cbits.intellicare.mantra.FocusBoardGridFragment;
import edu.northwestern.cbits.intellicare.mantra.FocusBoardManager;
import edu.northwestern.cbits.intellicare.mantra.FocusImageGridFragment;
import edu.northwestern.cbits.intellicare.mantra.GetImagesTask;
import edu.northwestern.cbits.intellicare.mantra.MediaScannerService;
import edu.northwestern.cbits.intellicare.mantra.Paths;
import edu.northwestern.cbits.intellicare.mantra.R;
import edu.northwestern.cbits.intellicare.mantra.SingleMediaScanner;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
		
		try {
			handleSelectedImageIntent();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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


	/**
	 * Attaches a selected image to the mantra board, if it isn't already attached. Also cleans-up the temp folder. 
	 * @throws Exception
	 */
	private void handleSelectedImageIntent() throws Exception {
		Log.d(CN+".handleSelectedImageIntent", "entered");
		Intent intent = getIntent();
			
		if(intent != null) {
			// get a thumbnail of the image: http://stackoverflow.com/questions/14978566/how-to-get-selected-image-from-gallery-in-android
			Uri selectedImage = intent.getData();
			
			if(selectedImage != null && selectedImage.toString().length() > 0) {
				mFocusBoardId = intent.getLongExtra(NewFocusBoardActivity.FOCUS_BOARD_ID, -1);
				mManager = FocusBoardManager.get(this);
				FocusBoard focusBoard = mManager.getFocusBoard(mFocusBoardId);

				String filePathToImageInTmpFolder = getRealPathFromURI(this, selectedImage).trim();
				File imageFileTmp = new File(filePathToImageInTmpFolder);
				String fileName = imageFileTmp.getName();
				String newFilePath = Paths.MANTRA_IMAGES + "/" + fileName;
				File imageFile = new File(newFilePath);
				if(imageFileTmp.exists()) { 
					if(!imageFileTmp.renameTo(imageFile)) {
						throw new Exception("Couldn't rename file from \"" + filePathToImageInTmpFolder + "\" to \"" + newFilePath + "\""); 
					}
					else
						Log.d(CN+".handleSelectedImageIntent", "Renamed file from \"" + filePathToImageInTmpFolder + "\" to \"" + newFilePath + "\"");
				}
				
				// search the image set for a particular image path, and if the image isn't already associated with the board,
				// then associate it. 
				FocusImageCursor fic = mManager.queryFocusImages(mFocusBoardId);
				boolean imageAlreadyAssociated = false;
				while(fic.moveToNext()) {
					String path = fic.getString(FocusBoardManager.COL_INDEX_FILE_PATH).trim();
					Log.d(CN+".handleSelectedImageIntent", "path.equals(filePathToImage) = " + (path.equals(imageFile.getAbsolutePath())) + "; path = \"" + path + "\"" + "; imageFile.getAbsolutePath() = \"" + imageFile.getAbsolutePath() + "\"");
					if(path.equals(imageFile.getAbsolutePath())) {
						Toast.makeText(this, "Image already applied. Consider choosing a different one!", Toast.LENGTH_LONG).show();
						imageAlreadyAssociated = true;
						break;
					}
				}
				if(!imageAlreadyAssociated) {
					Log.d(CN+".handleSelectedImageIntent","for board " + mFocusBoardId + ", associating image = " + imageFile.getAbsolutePath());
					Toast.makeText(this, "Image applied!", Toast.LENGTH_SHORT).show();
					mManager.createFocusImage(mFocusBoardId, imageFile.getAbsolutePath());
				}
				
				// clean-up the temp folder
				deleteAllFilesInImageFolder(Paths.MANTRA_IMAGES_TMP);
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
	
	
	/**
	 * Src: http://helpdesk.objects.com.au/java/how-to-delete-all-files-in-a-directory
	 * @param folderPath
	 */
	private void deleteAllFilesInImageFolder(String folderPath) {
		Log.d(CN+".deleteAllFilesInImageFolder", "entered; folderPath = " + folderPath);

		// ATTEMPT 4: query the content provider and log the contents of the thumbnail and media images sets.
		Log.d(CN+".deleteAllFilesInImageFolder", "logging content provider contents for MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI = " + MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI);
		Cursor imagesThumbsCursor = managedQuery(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, null, null, null, null);
		logCursor(imagesThumbsCursor);
		imagesThumbsCursor.moveToPosition(-1);
		Log.d(CN+".deleteAllFilesInImageFolder", "logging content provider contents for MediaStore.Images.Media.EXTERNAL_CONTENT_URI = " + MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		Cursor imagesMediaCursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
		logCursor(imagesMediaCursor);
		imagesMediaCursor.moveToPosition(-1);

		// get the set of images to delete
		ArrayList<Integer> imageIdsToDelete = getImageFilePaths(folderPath, imagesMediaCursor);

		// delete the images
		deleteImages(imageIdsToDelete);
		
		// close the cursors
		Log.d(CN+".deleteAllFilesInImageFolder", "closing cursors");
		imagesThumbsCursor.close();
		imagesMediaCursor.close();
		
		// finally, re-index the files in the Mantra folder.
		indexMantraFolder();
	}

	/**
	 * Deletes images from the Android media store.
	 * @param imageIdsToDelete
	 */
	private void deleteImages(ArrayList<Integer> imageIdsToDelete) {
		// Delete the images from the Media and Thumbnails media stores.
		// Note that this deletes both the row in the media database and the file on the filesystem!
		Log.d(CN+".deleteImages", "imageIdsToDelete = " + imageIdsToDelete);
		for(Integer id : imageIdsToDelete) {
			Log.d(CN+".deleteImages", "For (MediaStore.Images.Media.EXTERNAL_CONTENT_URI): deleting image ID = " + id);
			getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "_id=?", new String[]{ id.toString() });
			Log.d(CN+".deleteImages", "For (MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI): deleting image ID = " + id);
			getContentResolver().delete(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, "image_id=?", new String[]{ id.toString() });
		}
	}

	/**
	 * Gets image file paths.
	 * @param folderPath
	 * @param imagesMediaCursor
	 * @return
	 */
	private ArrayList<Integer> getImageFilePaths(String folderPath, Cursor imagesMediaCursor) {
		ArrayList<Integer> imageIdsToDelete = new ArrayList<Integer>();
		while(imagesMediaCursor.moveToNext()) {
			String filePath = imagesMediaCursor.getString(imagesMediaCursor.getColumnIndex("_data"));
			Log.d(CN+".getImageFilePaths", "filePath = " + filePath + "; folderPath = " + folderPath);
			if(filePath.contains(folderPath)) {
				Log.d(CN+".getImageFilePaths", "Adding to list of images to delete: " + filePath);
				imageIdsToDelete.add(imagesMediaCursor.getInt(imagesMediaCursor.getColumnIndex("_id")));
			}
		}
		return imageIdsToDelete;
	}

	/**
	 * Indexes the Mantra folder.
	 */
	private void indexMantraFolder() {
		Log.d(CN+".deleteAllFilesInFolder", "re-indexing files in the folder: " + Paths.MANTRA_IMAGES);
		File mantraFolder = new File(Paths.MANTRA_IMAGES);
		Intent mediaScannerIntent = new Intent(this, MediaScannerService.class);
		File[] pathsToFilesInMantraFolder = mantraFolder.listFiles();
		String[] filesToScan = new String[pathsToFilesInMantraFolder.length]; for(int i = 0; i < pathsToFilesInMantraFolder.length; i++) { filesToScan[i] = pathsToFilesInMantraFolder[i].getAbsolutePath(); }
		StringBuilder sb = new StringBuilder(); for(String f : filesToScan) { sb.append("\r\n\t" + f); } Log.d(CN+".deleteAllFilesInFolder", "scanning files:" + sb.toString());
		mediaScannerIntent.putExtra(MediaScannerService.INTENT_KEY_FILE_PATHS_TO_SCAN, filesToScan);
		ArrayList<String> receiverArrayListExtras = new ArrayList<String>(); receiverArrayListExtras.add("true");
		mediaScannerIntent.putExtra(MediaScannerService.INTENT_KEY_TO_RECEIVER_STRINGARRAY, receiverArrayListExtras);
		Log.d(CN+".deleteAllFilesInFolder", "starting mediaScannerIntent");
		startService(mediaScannerIntent);
	}

	
	/**
	 * Logs the contents of a table referenced by a Cursor.
	 * @param cursor
	 */
	private void logCursor(Cursor cursor) {
		StringBuilder sb = new StringBuilder();
		for(String cn : cursor.getColumnNames()) { sb.append("; " + cn); }
		Log.d(CN+".logCursor", "col names = " + sb.toString());
		while(cursor.moveToNext()) {
			sb.delete(0, sb.length());
			for(int j = 0; j < cursor.getColumnCount(); j++) {
				sb.append("; " + cursor.getString(j));
			}
			Log.d(CN+".logCursor", "row values = " + sb.toString());
		}
		Log.d(CN+".logCursor", "row count = " + cursor.getCount() + "; col count = " + cursor.getColumnCount());
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
			Log.d(CN+".addImageGridFragment", "loading images");
			fm.beginTransaction()
				.add(R.id.fragmentContainer, fragment)
				.commit();
		}
		Log.d(CN+".addImageGridFragment", "exiting");
	}
}
