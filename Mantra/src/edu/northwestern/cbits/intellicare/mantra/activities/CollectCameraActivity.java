package edu.northwestern.cbits.intellicare.mantra.activities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.InputStream;

import edu.northwestern.cbits.intellicare.mantra.CameraPreview;
import edu.northwestern.cbits.intellicare.mantra.GetImagesTask;
import edu.northwestern.cbits.intellicare.mantra.MantraBoardManager;
import edu.northwestern.cbits.intellicare.mantra.MantraImage;
import edu.northwestern.cbits.intellicare.mantra.Paths;
import edu.northwestern.cbits.intellicare.mantra.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class CollectCameraActivity extends Activity {


	 private long mFocusBoardId;
	 private MantraBoardManager mFocusBoardManager;
	
	  // src: http://www.vogella.com/tutorials/AndroidCamera/article.html
	  private static final int REQUEST_CODE = 1;
	  private Bitmap bitmap;
	  private ImageView imageView;
	  private Intent intent =null;

	  
	  @Override
	  public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.collect_from_camera_activity);
	    imageView = (ImageView) findViewById(R.id.result);
	    
		mFocusBoardId = (getIntent()).getLongExtra(SingleMantraBoardActivity.MANTRA_BOARD_ID, -1);
	    mFocusBoardManager = MantraBoardManager.get(this);

	    startCameraApp();
	  }


	  private void startMantraBoardActivity() {
// CJK		Intent intent = new Intent(CollectCameraActivity.this,
		Intent intent = new Intent(CollectCameraActivity.this, SingleMantraBoardActivity.class);
		intent.putExtra(SingleMantraBoardActivity.MANTRA_BOARD_ID, mFocusBoardId);
		startActivity(intent);
	  }
	
	
	  public void onClick(View View) {
	    startCameraApp();
	  }

	  
	  private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	  private Uri fileUri;

	  public static final int MEDIA_TYPE_IMAGE = 1;
	  public static final int MEDIA_TYPE_VIDEO = 2;
	  private static final String CN = "CollectCameraActivity";

	  /** Create a file Uri for saving an image or video 
	 * @param isThumbnail */
	  private static Uri getOutputMediaFileUri(int type, boolean isThumbnail){
	        return Uri.fromFile(getOutputMediaFile(type, isThumbnail));
	  }

	  /** Create a File for saving an image or video 
	 * @param isThumbnail */
	  private static File getOutputMediaFile(int type, boolean isThumbnail){
	      // To be safe, you should check that the SDCard is mounted
	      // using Environment.getExternalStorageState() before doing this.

	      File mediaStorageDir = isThumbnail
	    		? new File(Paths.MANTRA_IMAGES_TMP_THUMBNAILS)
      			: new File(Paths.MANTRA_IMAGES_TMP);
	      // This location works best if you want the created images to be shared
	      // between applications and persist after your app has been uninstalled.

	      // Create the storage directory if it does not exist
	      if (! mediaStorageDir.exists()){
	          if (! mediaStorageDir.mkdirs()){
	              Log.d(CN+"getOutputMediaFile", "failed to create directory");
	              return null;
	          }
	      }

	      // Create a media file name
	      String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	      File mediaFile = null;
	      if (type == MEDIA_TYPE_IMAGE){
	          mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	          "IMG_"+ timeStamp + ".jpg");
	      }
	      else if(type == MEDIA_TYPE_VIDEO) {
	          mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	          "VID_"+ timeStamp + ".mp4");
	      }

	      return mediaFile;
	  }

	  
	  public void startCameraApp() {
		// src: http://developer.android.com/guide/topics/media/camera.html
	    // create Intent to take a picture and return control to the calling application
	    intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

	    fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE, false); // create a file to save the image
	    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
	    intent.putExtra(SingleMantraBoardActivity.MANTRA_BOARD_ID, mFocusBoardId);

	    // start the image capture Intent
	    Log.d(CN+".startCameraApp", "fileUri = " + fileUri);
	    Log.d(CN+".startCameraApp", "intent = " + intent);
	    startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
	  }
	  
	  
	  /**
	   * Creates and saves a thumbnail image given an existing full-size image.
	   * @param fullSizeFileUri
	   * @return A File object to the new thumbnail file.
	   */
	  public static Uri createAndSaveThumbnail(Uri fullSizeFileUri) {

		  Uri thumbUri = Uri.parse(Paths.MANTRA_IMAGES_TMP_THUMBNAILS + "/" + fullSizeFileUri.getLastPathSegment());
		  
		  
		  
		  return thumbUri;
	  }

	
	  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    Log.d(CN+".onActivityResult", "entered; requestCode = " + requestCode + "; resultCode = " + resultCode + "; data = " + data);

	    String[] fullFilePathsToScan = new String[] { fileUri.getPath() };
	    GetImagesTask.scanFilePathsForImages(this, fullFilePathsToScan);

		MantraImage image = mFocusBoardManager.createMantraImage(
				mFocusBoardId, fileUri.getPath(), getString(R.string.some_image_caption)
			);
		
		// create thumbnail too
		createAndSaveThumbnail(fileUri);
		
	    startMantraBoardActivity();
		
		this.finish();
	  }
	  
	  
//	  //-------------------------------------------------------------------------------------------------------------------------
//	  
//	  
//	  static final int REQUEST_IMAGE_CAPTURE = 1;
//
//	  @Override
//	  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//	      if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//	          Bundle extras = data.getExtras();
//	          Bitmap imageBitmap = (Bitmap) extras.get("data");
////	          mImageView.setImageBitmap(imageBitmap);
//	      }
//	  }
//
//	  String mCurrentPhotoPath;
//
//	  private File createImageFile() throws IOException {
//	      // Create an image file name
//	      String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//	      String imageFileName = "JPEG_" + timeStamp + "_";
//	      File storageDir = Environment.getExternalStoragePublicDirectory(
//	              Environment.DIRECTORY_PICTURES);
//	      File image = File.createTempFile(
//	          imageFileName,  /* prefix */
//	          ".jpg",         /* suffix */
//	          storageDir      /* directory */
//	      );
//
//	      // Save a file: path for use with ACTION_VIEW intents
//	      mCurrentPhotoPath = "file:" + image.getAbsolutePath();
//	      return image;
//	  }
//	  
//	  static final int REQUEST_TAKE_PHOTO = 1;
//
//	  private void dispatchTakePictureIntent() {
//	      Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//	      // Ensure that there's a camera activity to handle the intent
//	      if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//	          // Create the File where the photo should go
//	          File photoFile = null;
//	          try {
//	              photoFile = createImageFile();
//	          } catch (IOException ex) {
//	              // Error occurred while creating the File
//	              Log.e(CN+".displatchTakePictureIntent", ex.getLocalizedMessage());
//	          }
//	          // Continue only if the File was successfully created
//	          if (photoFile != null) {
//	              takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
//	                      Uri.fromFile(photoFile));
//	              startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
//	          }
//	      }
//	  }
}
