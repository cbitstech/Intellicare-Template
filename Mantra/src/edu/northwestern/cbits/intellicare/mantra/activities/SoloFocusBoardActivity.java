package edu.northwestern.cbits.intellicare.mantra.activities;

import java.io.File;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import edu.northwestern.cbits.intellicare.mantra.DatabaseHelper.FocusImageCursor;
import edu.northwestern.cbits.intellicare.mantra.FocusBoard;
import edu.northwestern.cbits.intellicare.mantra.FocusBoardManager;
import edu.northwestern.cbits.intellicare.mantra.FocusImage;
import edu.northwestern.cbits.intellicare.mantra.MediaScannerService;
import edu.northwestern.cbits.intellicare.mantra.Paths;
import edu.northwestern.cbits.intellicare.mantra.PictureUtils;
import edu.northwestern.cbits.intellicare.mantra.R;
import edu.northwestern.cbits.intellicare.mantra.Util;

public class SoloFocusBoardActivity extends ActionBarActivity {

	private static final String CN = "FocusBoardActivity";
	private final SoloFocusBoardActivity self = this;
	
	private FocusBoardManager mManager;
	private long mFocusBoardId = -1;

	private static int RESULT_LOAD_IMAGE = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("FocusBoardActivity.onCreate", "entered");

		this.setContentView(R.layout.solo_focus_board_activity);

		ActionBar actionBar = this.getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();
		Log.d(CN+".onResume", "entered");

		Intent intent = this.getIntent();

		this.mFocusBoardId = intent.getLongExtra(NewFocusBoardActivity.FOCUS_BOARD_ID, -1);
		mManager = FocusBoardManager.get(this);

		FocusBoard focusBoard = mManager.getFocusBoard(this.mFocusBoardId);

		this.getSupportActionBar().setTitle(focusBoard.getMantra());

		try 
		{
			handleSelectedImageIntent();
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		attachGridView();

	}
	
	/**
	 * Creates, binds to data, and fills the main view for this activity.
	 */
	private void attachGridView() {
		setContentView(R.layout.no_fragments_home_activity);
		final GridView gv = (GridView) this.findViewById(R.id.gridview);

		final FocusImageCursor cursor = FocusBoardManager.get(this).queryFocusImages(this.mFocusBoardId);
		Util.logCursor(cursor);
		
		@SuppressWarnings("deprecation")
		CursorAdapter adapter = new CursorAdapter(this, cursor) {

			public void bindView(View view, Context context, Cursor c) 
			{
				if (c instanceof FocusImageCursor)
				{
					FocusImageCursor mFocusImageCursor = (FocusImageCursor) c;
					FocusImage focusImage = mFocusImageCursor.getFocusImage();
					ImageView imageView = (ImageView) view.findViewById(R.id.imageThumb);
					Drawable d = PictureUtils.getScaledDrawable(self, focusImage.getPath());
					imageView.setImageDrawable(d);

					// view.setBackgroundColor(0x80ff0000);

					// set the caption
					TextView tv = (TextView) view.findViewById(R.id.imageCaption);
					String captionText = focusImage.getCaption();
					tv.setText(captionText);
				}
			}

			@Override
			public View newView(Context context, Cursor cursor, ViewGroup parent) 
			{
				LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				return inflater.inflate(R.layout.cell_image_item, parent, false);
			}
			
		};
		gv.setAdapter(adapter);
		
		
		// OPEN action.
		gv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int positionInView, long rowId) {
				Intent intent = new Intent();
				intent.setAction(android.content.Intent.ACTION_VIEW);
				cursor.moveToPosition(positionInView);
				String filePath = cursor.getString(FocusBoardManager.COL_INDEX_FILE_PATH);
				Log.d(CN+".onItemClick", "filePath = " + filePath);
				intent.setDataAndType(Uri.fromFile(new File(filePath)), "image/*");
				startActivity(intent);
			}
		});
		
		
		// EDIT OR REMOVE action.
		gv.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, final long id) {
				// options
				final String[] optionItems = new String[] { "Edit", "Remove" };
				
				// create dialog for list of options
				AlertDialog.Builder dlg = new Builder(self);
				dlg.setTitle("Modify Mantra");
				dlg.setItems(optionItems, new OnClickListener() {
					
					// on user clicking the Edit or Delete option...
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Toast.makeText(self, "You chose " + optionItems[which] + "; which = " + which, Toast.LENGTH_SHORT).show();
						
						// which option from the dialog menu did the user select?
						switch(which) {
						
							case 0:
								Log.d(CN+".onItemLongClick....onClick", "You chose " + optionItems[which]);
								
								// get the current caption
								// v2: via database
								final View v = self.getLayoutInflater().inflate(R.layout.edit_text_field, null);
								FocusImage fi = FocusBoardManager.get(self).getFocusImage(id);
								((EditText) v.findViewById(R.id.text_dialog)).setText(fi.getCaption());

								AlertDialog.Builder editTextDlg = new AlertDialog.Builder(self);
								editTextDlg.setMessage("Edit the text");
								editTextDlg.setPositiveButton("OK", new OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										// update the selected mantra's text
										Toast.makeText(self, "Mantra text should change.", Toast.LENGTH_SHORT).show();
										String newCaption = ((EditText) v.findViewById(R.id.text_dialog)).getText().toString();
										FocusImage fi = FocusBoardManager.get(self).getFocusImage(id);
										fi.setCaption(newCaption);
										long updateRet = FocusBoardManager.get(self).setFocusImage(fi);
										Log.d(CN+".onItemLongClick....onClick", "updateRet = " + updateRet);
										attachGridView();
									}
								});

								editTextDlg.setView(v);
								AlertDialog dlg = editTextDlg.create();
								dlg.show();
								break;

							case 1:
								Log.d(CN+".onItemLongClick....onClick", "You chose " + optionItems[which]);
								
								AlertDialog.Builder dlg1 = new AlertDialog.Builder(self);
								dlg1.setTitle("Confirm deletion");
								dlg1.setMessage("The image and caption will be removed from this mantra, but will not be permanently deleted from your device. Proceed?");
								dlg1.setPositiveButton("Yes", new OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										int rowsDeleted = FocusBoardManager.get(self).deleteFocusImage(id);
										attachGridView();
										Log.d(CN+".onItemLongClick....onClick", "deleted row = " + id + "; deleted row count = " + rowsDeleted);
									}
								});
								dlg1.setNegativeButton("No", new OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										Log.d(CN+".onItemLongClick....onClick", "not deleting " + id);
									}
								});
								dlg1.show();
								break;
						}
					}
				});
				dlg.create().show();
				
				return true;
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(CN+".onCreateOptionsMenu", "entered");
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.focus_board_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(CN+".onOptionsItemSelected", "entered");
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

				String filePathToImageInTmpFolder = Paths.getRealPathFromURI(this, selectedImage).trim();
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
				
				applyNewImageToMantra(imageFile);
				
				// clean-up the temp folder
				deleteAllFilesInImageFolder(Paths.MANTRA_IMAGES_TMP);
			}
			else
				Log.d(CN+".handleSelectedImageIntent", "one of the conds is untrue: " + (selectedImage != null) + " && " + (selectedImage == null ? "<not evaluable>" : selectedImage.toString().length() > 0) + ")");
		}
		else
			Log.d(CN+".handleSelectedImageIntent", "intent is null");
	}

	/**
	 * Applies an image to a mantra, if the image has not already been associated. 
	 * @param imageFile
	 */
	private void applyNewImageToMantra(File imageFile) {
		Log.d(CN+".applyNewImageToMantra", "entered");
		// search the image set for a particular image path, and if the image isn't already associated with the board,
		// then associate it. 
		FocusImageCursor fic = mManager.queryFocusImages(mFocusBoardId );
		boolean imageAlreadyAssociated = false;
		Util.logCursor(fic);
		while(fic.moveToNext()) {
			String path = fic.getString(FocusBoardManager.COL_INDEX_FILE_PATH).trim();
			Log.d(CN+".applyNewImageToMantra", "path.equals(filePathToImage) = " + (path.equals(imageFile.getAbsolutePath())) + "; path = \"" + path + "\"" + "; imageFile.getAbsolutePath() = \"" + imageFile.getAbsolutePath() + "\"");
			if(path.equals(imageFile.getAbsolutePath())) {
				Toast.makeText(this, "Image already applied. Consider choosing a different one!", Toast.LENGTH_LONG).show();
				imageAlreadyAssociated = true;
				break;
			}
		}
		if(!imageAlreadyAssociated) {
			Log.d(CN+".applyNewImageToMantra","for board " + mFocusBoardId + ", associating image = " + imageFile.getAbsolutePath());
			Toast.makeText(this, "Image applied!", Toast.LENGTH_SHORT).show();
			mManager.createFocusImage(mFocusBoardId, imageFile.getAbsolutePath(), "SOME IMAGE CAPTION");
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
		Util.logCursor(imagesThumbsCursor);
//		imagesThumbsCursor.moveToPosition(-1);
		Log.d(CN+".deleteAllFilesInImageFolder", "logging content provider contents for MediaStore.Images.Media.EXTERNAL_CONTENT_URI = " + MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		Cursor imagesMediaCursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
		Util.logCursor(imagesMediaCursor);
//		imagesMediaCursor.moveToPosition(-1);

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
		for(Integer  id :imageIdsToDelete) {
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
		Log.d(CN+".getImageFilePaths", "entered");
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

	
	/* display image from gallery: BEGIN (src: http://viralpatel.net/blogs/pick-image-from-galary-android-app/) */
	private void startBrowsePhotosActivity() {
		Log.d(CN+".startBrowsePhotosActivity", "entered");
		Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(i, RESULT_LOAD_IMAGE);
	}

	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
        Log.d(CN+"onActivityResult", "entered; requestCode = " + requestCode + "; resultCode = " + resultCode + "; data = " + data);

        // if: existing image
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
	         Uri selectedImage = data.getData();

	         String picturePath = Util.getImageFilePathViaContentUri(this, selectedImage);
	                      
	         Log.d(CN+"onActivityResult", "picturePath = " + picturePath);
	         applyNewImageToMantra(new File(picturePath));
	     }
	}


	private void startCollectCameraActivity() {
		Log.d(CN+".startCollectCameraActivity", "entered");
		Intent intent = new Intent(this, CollectCameraActivity.class);
		intent.putExtra(NewFocusBoardActivity.FOCUS_BOARD_ID, mFocusBoardId);
		startActivity(intent);
	}
}