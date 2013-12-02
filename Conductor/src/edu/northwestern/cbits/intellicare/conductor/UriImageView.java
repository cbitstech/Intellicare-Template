package edu.northwestern.cbits.intellicare.conductor;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.ImageView;

public class UriImageView extends ImageView 
{
	public UriImageView(Context context, AttributeSet attrs, int defStyle) 
	{
		super(context, attrs, defStyle);
	}

	public UriImageView(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
	}
	
	public UriImageView(Context context) 
	{
		super(context);
	}

	public void setCachedImageUri(final Uri uri)
	{
		final UriImageView me = this;
		
		final Uri imageUri = ContentProvider.fetchCachedUri(this.getContext(), uri, new Runnable()
		{
			public void run() 
			{
				Activity activity = (Activity) me.getContext();
				
				activity.runOnUiThread(new Runnable()
				{
					public void run() 
					{
						Uri imageUri = ContentProvider.fetchCachedUri(me.getContext(), uri, null);
						
						me.setImageURI(imageUri);
						
						me.refreshDrawableState();
					}
				});
			}
		});
		
		if (imageUri != null)
		{
			this.setImageURI(imageUri);
			this.refreshDrawableState();
		}
	}
	
	public void setImageDrawable(Drawable drawable)
	{
		this.cleanupDrawable();
		super.setImageDrawable(drawable);
	}
	
	private void cleanupDrawable()
	{
		this.setImageResource(0);
	}
	
	public void setImageURI(Uri uri)
	{
		this.cleanupDrawable();
		
		super.setImageURI(uri);
	}
}