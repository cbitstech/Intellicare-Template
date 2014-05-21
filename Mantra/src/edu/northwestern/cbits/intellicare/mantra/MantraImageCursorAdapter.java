package edu.northwestern.cbits.intellicare.mantra;

import edu.northwestern.cbits.intellicare.mantra.DatabaseHelper.MantraImageCursor;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class MantraImageCursorAdapter extends CursorAdapter {
	
	private MantraImageCursor mFocusImageCursor;

	public MantraImageCursorAdapter(Context context, MantraImageCursor cursor) {
		super(context, cursor, 0);
		mFocusImageCursor = cursor;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		return inflater.inflate(R.layout.image_item, parent, false);
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		MantraImage mantraImage = mFocusImageCursor.getMantraImage();
		ImageView imageView = (ImageView)view;
		imageView.setImageDrawable(drawablePhoto(mantraImage.getPath(), context));
	}
	
	private Drawable drawablePhoto(String path, Context context) {
		return PictureUtils.getScaledDrawable((Activity) context, path);
	}
}
