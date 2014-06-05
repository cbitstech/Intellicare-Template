package edu.northwestern.cbits.intellicare.mantra;
import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;


public class Util {

	private static final String CN = "Util";

	/**
	 * Logs the contents of a table referenced by a Cursor.
	 * @param cursor
	 */
	public static void logCursor(Cursor cursor) {
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
		cursor.moveToPosition(-1);
	}
	
	

	/**
	 * Gets an image file path via a content:// URI.
	 * @param imageContentUri
	 * @return
	 */
	public static String getImageFilePathViaContentUri(Activity activity, Uri imageContentUri) {
		// get the file path from the media store database
		String[] filePathColumn = { MediaStore.Images.Media.DATA };
		Cursor cursor = activity.getContentResolver().query(imageContentUri, filePathColumn, null, null, null);
		cursor.moveToFirst();
		int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
		String picturePath = cursor.getString(columnIndex);
		cursor.close();
		return picturePath;
	}
	
	
	public static Uri getContentUriViaImageFilePath(Context arg0, String fullFilePath) {
//		String[] filePathColumn = { MediaStore.Images.Media.DATA };
//		Cursor cursor = activity.getContentResolver().query(imageContentUri, filePathColumn, null, null, null);
//		cursor.moveToFirst();
//		logCursor(cursor);
//		int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
////		String picturePath = cursor.getString(columnIndex);
//		Uri u = Uri.parse(cursor.getString(columnIndex));
//		cursor.close();
//		return u;
		
	    File imageFile = new File(fullFilePath);
		String filePath = imageFile.getAbsolutePath();
	    Cursor cursor = arg0.getContentResolver().query(
	            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
	            new String[] { MediaStore.Audio.Media._ID },
	            MediaStore.Audio.Media.DATA + "=? ",
	            new String[] { filePath }, null);

	    if (cursor != null && cursor.moveToFirst()) {
	        int id = cursor.getInt(cursor
	                .getColumnIndex(MediaStore.MediaColumns._ID));
	        Uri baseUri = Uri.parse("content://media/external/audio/media");
	        Log.d(CN+".getContentUriViaImageFilePath", "baseUri = " + baseUri.getPath());
	        Util.logCursor(cursor);
	        return Uri.withAppendedPath(baseUri, "" + id);
	    }
	    
	    cursor.close();
	    return null;
	}
}
