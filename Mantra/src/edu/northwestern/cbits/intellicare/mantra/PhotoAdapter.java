package edu.northwestern.cbits.intellicare.mantra;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class PhotoAdapter extends ArrayAdapter<File> {
	private ArrayList<File> mPhotoFiles;
	private Context mContext = null;

	/**
	 * Standard Data Adapter Construction
	 */
	public PhotoAdapter(Context context, int textViewResourceId,
			ArrayList<File> photoFiles) {
		super(context, textViewResourceId, photoFiles);
		this.mPhotoFiles = photoFiles;
		this.mContext = context;
	}

	/**
	 * Code invoked when container notifies data set of change.
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		TextView fileNameView = null;
		ImageView mPhotoView = null;
		File f = mPhotoFiles.get(position);
		
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.photo_row, null);
		}
		
		if (f != null) {
			fileNameView = (TextView) v.findViewById(R.id.filename);
			mPhotoView = (ImageView) v.findViewById(R.id.photoIcon);
			
			if (fileNameView != null && mPhotoView != null) {
				fileNameView.setText(f.getName());
				mPhotoView.setImageDrawable(drawablePhoto(f));
			}
		}
		return v;
	}
	
	private Drawable drawablePhoto(File f) {
		String path = f.getAbsolutePath();
		return PictureUtils.getScaledDrawable((Activity) mContext, path);
	}
}
