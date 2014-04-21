package edu.northwestern.cbits.intellicare.mantra;

import java.io.File;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.util.Log;

/**
 * Forces a MediaScanner scan operation for an image.
 * Src: http://stackoverflow.com/questions/4646913/android-how-to-use-mediascannerconnection-scanfile
 * @author mohrlab
 *
 */
public class SingleMediaScanner implements MediaScannerConnectionClient {
	public static final String CN = "SingleMediaScanner";
	
	private MediaScannerConnection mMs;
	private File mFile;
	public Boolean isDone = false;

	/**
	 * Ctor. isCompleted enables a wait-and-rejoin pattern in the caller, so galloping asynchronicity doesn't blow-up the caller on method-exit. 
	 * @param context
	 * @param f
	 * @param isCompleted
	 */
	public SingleMediaScanner(Context context, File f, Boolean isCompleted) {
		mFile = f;
		isDone = isCompleted;
		mMs = new MediaScannerConnection(context, this);
//		Log.d(CN+".ctor", "connecting for file: " + f.getAbsolutePath());
		mMs.connect();
	}

	@Override
	public void onMediaScannerConnected() {
		mMs.scanFile(mFile.getAbsolutePath(), null);
	}

	@Override
	public void onScanCompleted(String path, Uri uri) {
//		Log.d(CN+".onScanCompleted", "disconnecting for path: " + path + " and uri = " + uri);
		mMs.disconnect();
		isDone = true;
//		Log.d(CN+".onScanCompleted", "scanning file (end): " + path + "; isDone = " + isDone);
	}
}