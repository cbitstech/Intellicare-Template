package edu.northwestern.cbits.intellicare.mantra;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

public class Paths {

	public static final String MANTRA_IMAGES_TMP = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/MantraTmp";
	public static final String MANTRA_IMAGES = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Mantra";
	public static final String CAMERA_IMAGES = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/Camera";
	
	// src: http://stackoverflow.com/questions/3401579/get-filename-and-path-from-uri-from-mediastore
	public static String getRealPathFromURI(Context context, Uri contentUri) {
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
}
