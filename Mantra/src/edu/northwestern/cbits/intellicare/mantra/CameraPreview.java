package edu.northwestern.cbits.intellicare.mantra;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

/** A basic Camera preview class */
public class CameraPreview extends SurfaceView implements
		SurfaceHolder.Callback {
	private SurfaceHolder mHolder;
	private Camera mCamera;
	private static final String TAG = "CameraPreview";
	private boolean isPreviewRunning;

	public CameraPreview(Context context, Camera camera) {
		super(context);
		mCamera = camera;

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);
		// deprecated setting, but required on Android versions prior to 3.0
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, now tell the camera where to draw the
		// preview.
		try {
			mCamera.setPreviewDisplay(holder);
			previewCamera();
		} catch (IOException e) {
			Log.d(TAG, "Error setting camera preview: " + e.getMessage());
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// empty. Take care of releasing the Camera preview in your activity.
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// see:
		// http://stackoverflow.com/questions/3841122/android-camera-preview-is-sideways
		if (isPreviewRunning) {
			mCamera.stopPreview();
		}

		Display display = ((WindowManager) this.getContext().getSystemService(
				Context.WINDOW_SERVICE)).getDefaultDisplay();

		switch (display.getRotation()) {
		case Surface.ROTATION_0:
			mCamera.setDisplayOrientation(90);
			break;
		case Surface.ROTATION_270:
			mCamera.setDisplayOrientation(180);
			break;
		}

		previewCamera();
	}

	private void previewCamera() {
		try {
			mCamera.setPreviewDisplay(mHolder);
			mCamera.startPreview();
			isPreviewRunning = true;
		} catch (Exception e) {
			Log.d(TAG, "Cannot start preview", e);
		}
	}
}
