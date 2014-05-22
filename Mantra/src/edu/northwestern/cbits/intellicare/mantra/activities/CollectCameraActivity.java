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
//	private static final String PICTURE_SUBDIRECTORY = "Mantra";
//	private Camera mCamera;
//	private CameraPreview mPreview;
//	private PictureCallback mPicture = new PictureCallback() {
//
//		@Override
//		public void onPictureTaken(byte[] data, Camera camera) {
//
//			File pictureFile = getOutputMediaFile();
//			if (pictureFile == null) {
//				Log.d(TAG,
//						"Error creating media file, check storage permissions");
//				return;
//			}
//
//			try {
//				FileOutputStream fos = new FileOutputStream(pictureFile);
//				fos.write(data);
//				fos.close();
//				createMantraImage(pictureFile);
//				startMantraBoardActivity();
//			} catch (FileNotFoundException e) {
//				Log.d(TAG, "File not found: " + e.getMessage());
//			} catch (IOException e) {
//				Log.d(TAG, "Error accessing file: " + e.getMessage());
//			}
//		}
//	};
//	private static final String TAG = "CollectCamera";
	private long mFocusBoardId;
	private MantraBoardManager mFocusBoardManager;
//
//	/** A safe way to get an instance of the Camera object. */
//	public static Camera getCameraInstance() {
//		Camera c = null;
//		try {
//			c = Camera.open(); // attempt to get a Camera instance
//		} catch (Exception e) {
//			// Camera is not available (in use or does not exist)
//		}
//		return c; // returns null if camera is unavailable
//	}
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.collect_from_camera_activity);
//
//		Log.d("CollectCameraActivity.onCreate", "entered");
//
//		this.attachCameraPreview();
//		this.addCaptureButtonListener();
//
//		Intent intent = getIntent();
//		mFocusBoardId = intent.getLongExtra(
//				SingleMantraBoardActivity.MANTRA_BOARD_ID, -1);
//
//		mFocusBoardManager = MantraBoardManager.get(this);
//	}
//
//	@Override
//	protected void onDestroy() {
//		releaseCamera(); // release the camera immediately on pause event
//		super.onDestroy();
//	}
//
//	private void attachCameraPreview() {
//		// Create an instance of Camera
//		mCamera = getCameraInstance();
//
//		// Create our Preview view and set it as the content of our activity.
//		mPreview = new CameraPreview(this, mCamera);
//		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
//		preview.addView(mPreview);
//	}
//
//	private void addCaptureButtonListener() {
//		// Add a listener to the Capture button
//		Button captureButton = (Button) findViewById(R.id.button_capture);
//		captureButton.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				// get an image from the camera
//				mCamera.takePicture(null, null, mPicture);
//			}
//		});
//	}
//
//	/** Create a File for saving an image or video */
//	private File getOutputMediaFile() {
//		// To be safe, you should check that the SDCard is mounted
//		// using Environment.getExternalStorageState() before doing this.
//
//		File mediaStorageDir = new File(
//				Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
//				PICTURE_SUBDIRECTORY);
//		// This location works best if you want the created images to be shared
//		// between applications and persist after your app has been uninstalled.
//
//		// Create the storage directory if it does not exist
//		if (!mediaStorageDir.exists()) {
//			if (!mediaStorageDir.mkdirs()) {
//				Log.d("MyCameraApp", "failed to create directory");
//				return null;
//			}
//		}
//
//		// Create a media file name
//		String timeStamp = new SimpleDateFormat(this.getString(R.string.media_file_timestamp_format))
//				.format(new Date());
//		File mediaFile;
//		mediaFile = new File(mediaStorageDir.getPath() + File.separator
//				+ "IMG_" + timeStamp + ".jpg");
//
//		return mediaFile;
//	}
//
//	private void releaseCamera() {
//		if (mCamera != null) {
//			mCamera.release(); // release the camera for other applications
//			mCamera = null;
//		}
//	}
//
//	private void createMantraImage(File pictureFile) {
//		MantraImage image = mFocusBoardManager.createMantraImage(
//				mFocusBoardId, pictureFile.getAbsolutePath(), getString(R.string.some_image_caption)
//			);
//	}
//

	
	// src: http://www.vogella.com/tutorials/AndroidCamera/article.html
	  private static final int REQUEST_CODE = 1;
	  private Bitmap bitmap;
	  private ImageView imageView;

	  
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

	  /** Create a file Uri for saving an image or video */
	  private static Uri getOutputMediaFileUri(int type){
	        return Uri.fromFile(getOutputMediaFile(type));
	  }

	  /** Create a File for saving an image or video */
	  private static File getOutputMediaFile(int type){
	      // To be safe, you should check that the SDCard is mounted
	      // using Environment.getExternalStorageState() before doing this.

	      File mediaStorageDir = new File(Paths.MANTRA_IMAGES_TMP);
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

	  
	  Intent intent =null;
	  
	  /**
	 * 
	 */
	public void startCameraApp() {
//		Intent intent = new Intent();
//	    intent.setType("image/*");
//	    intent.setAction(Intent.ACTION_GET_CONTENT);
//	    intent.addCategory(Intent.CATEGORY_OPENABLE);
//	    startActivityForResult(intent, REQUEST_CODE);
		
		// src: http://developer.android.com/guide/topics/media/camera.html
	    // create Intent to take a picture and return control to the calling application
	    intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

	    fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE); // create a file to save the image
	    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
	    intent.putExtra(SingleMantraBoardActivity.MANTRA_BOARD_ID, mFocusBoardId);

	    // start the image capture Intent
	    Log.d(CN+".startCameraApp", "fileUri = " + fileUri);
	    Log.d(CN+".startCameraApp", "intent = " + intent);
	    startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
	}

//	  @Override
	  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    Log.d(CN+".onActivityResult", "entered; requestCode = " + requestCode + "; resultCode = " + resultCode + "; data = " + data);
//	    InputStream stream = null;
//	    
//	    if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK)
//	      try {
//	        // recyle unused bitmaps
//	        if (bitmap != null) {
//	          bitmap.recycle();
//	        }
//	        stream = getContentResolver().openInputStream(data.getData());
//	        bitmap = BitmapFactory.decodeStream(stream);
//
//	        imageView.setImageBitmap(bitmap);
//	        Log.d(CN+".onActivityResult", "finished loading bitmap");
//	      } catch (FileNotFoundException e) {
//	        e.printStackTrace();
//	      }
//	    finally{
//	        if (stream != null)
//	          try {
//	            stream.close();
//	          } catch (IOException e) {
//	            e.printStackTrace();
//	          }
//	  }
	    
	    String[] fullFilePathsToScan = new String[] { fileUri.getPath() };
	    
	    GetImagesTask.scanFilePathsForImages(this, fullFilePathsToScan);

		MantraImage image = mFocusBoardManager.createMantraImage(
				mFocusBoardId, fileUri.getPath(), getString(R.string.some_image_caption)
			);
	    startMantraBoardActivity();
		
		this.finish();
	}
	
}
