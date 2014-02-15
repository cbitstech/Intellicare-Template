package edu.northwestern.cbits.intellicare.mantra.activities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.northwestern.cbits.intellicare.mantra.CameraPreview;
import edu.northwestern.cbits.intellicare.mantra.FocusBoardManager;
import edu.northwestern.cbits.intellicare.mantra.FocusImage;
import edu.northwestern.cbits.intellicare.mantra.R;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

public class CollectCameraActivity extends Activity {
	private static final String PICTURE_SUBDIRECTORY = "Mantra";
	private Camera mCamera;
	private CameraPreview mPreview;
	private PictureCallback mPicture = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			File pictureFile = getOutputMediaFile();
			if (pictureFile == null) {
				Log.d(TAG,
						"Error creating media file, check storage permissions");
				return;
			}

			try {
				FileOutputStream fos = new FileOutputStream(pictureFile);
				fos.write(data);
				fos.close();
				createFocusImage(pictureFile);
				startFocusBoardActivity();
			} catch (FileNotFoundException e) {
				Log.d(TAG, "File not found: " + e.getMessage());
			} catch (IOException e) {
				Log.d(TAG, "Error accessing file: " + e.getMessage());
			}
		}
	};
	private static final String TAG = "CollectCamera";
	private long mFocusBoardId;
	private FocusBoardManager mFocusBoardManager;

	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
		}
		return c; // returns null if camera is unavailable
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.collect_from_camera_activity);

		Log.d("CollectCameraActivity.onCreate", "entered");

		this.attachCameraPreview();
		this.addCaptureButtonListener();

		Intent intent = getIntent();
		mFocusBoardId = intent.getLongExtra(
				NewFocusBoardActivity.FOCUS_BOARD_ID, -1);

		mFocusBoardManager = FocusBoardManager.get(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		releaseCamera(); // release the camera immediately on pause event
	}

	private void attachCameraPreview() {
		// Create an instance of Camera
		mCamera = getCameraInstance();

		// Create our Preview view and set it as the content of our activity.
		mPreview = new CameraPreview(this, mCamera);
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.addView(mPreview);
	}

	private void addCaptureButtonListener() {
		// Add a listener to the Capture button
		Button captureButton = (Button) findViewById(R.id.button_capture);
		captureButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// get an image from the camera
				mCamera.takePicture(null, null, mPicture);
			}
		});
	}

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile() {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				PICTURE_SUBDIRECTORY);
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d("MyCameraApp", "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		File mediaFile;
		mediaFile = new File(mediaStorageDir.getPath() + File.separator
				+ "IMG_" + timeStamp + ".jpg");

		return mediaFile;
	}

	private void releaseCamera() {
		if (mCamera != null) {
			mCamera.release(); // release the camera for other applications
			mCamera = null;
		}
	}

	private void createFocusImage(File pictureFile) {
		FocusImage image = mFocusBoardManager.createFocusImage(mFocusBoardId,
				pictureFile.getAbsolutePath());
	}

	private void startFocusBoardActivity() {
		Intent intent = new Intent(CollectCameraActivity.this,
				FocusBoardActivity.class);
		intent.putExtra(NewFocusBoardActivity.FOCUS_BOARD_ID, mFocusBoardId);
		startActivity(intent);
	}
}
