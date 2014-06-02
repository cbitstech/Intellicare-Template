package edu.northwestern.cbits.intellicare.views;

import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.SSLException;

import edu.northwestern.cbits.intellicare.logging.LogManager;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.StatFs;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

public class UriImageView extends ImageView 
{
    private static HashMap<String, String> _cachedHashes = new HashMap<String, String>();
	private static SparseArray<Drawable> _cachedResources = new SparseArray<Drawable>();
	private static boolean _useLargeImage = true;

	private Uri _originalUri = null;
    
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

/*	public void setCachedImageUri(final Uri uri)
	{
		final UriImageView me = this;
		
		final Uri imageUri = UriImageView.fetchCachedUri(this.getContext(), uri, new Runnable()
		{
			public void run() 
			{
				Activity activity = (Activity) me.getContext();
				
				activity.runOnUiThread(new Runnable()
				{
					public void run() 
					{
						Uri imageUri = UriImageView.fetchCachedUri(me.getContext(), uri, null);
						
						me.setImageURI(imageUri);
						
						me.refreshDrawableState();
					}
				});
			}
		});
		
		Log.e("IC", "USING CACHED URI " + imageUri + " FOR " + uri);
		
		if (imageUri != null)
		{
			this.setImageURI(imageUri);
			this.refreshDrawableState();
		}
	}
*/
	
	@SuppressWarnings("deprecation")
	public void setCachedImageUri(final Uri uri, final int resId, final boolean doFill)
	{
		this._originalUri = uri;

		final UriImageView me = this;

		LayoutParams params = this.getLayoutParams();

		if (params == null)
			params = new LayoutParams(144, 144);

		if (params.width < 0 || params.height < 0)
		{
			Context context = this.getContext();

			WindowManager wm  = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

			Display d = wm.getDefaultDisplay();

			params.width = d.getWidth();
			params.height = d.getHeight();

			if (context instanceof Activity)
			{
				Activity a = (Activity) context;

				Window w = a.getWindow();

				Rect visible = new Rect();
				w.getDecorView().getWindowVisibleDisplayFrame(visible);

				params.width = visible.width();
				params.height = visible.height();
			}
		}

		final int width = params.width;
		final int height = params.height;

		final Uri imageUri = uri;

		Runnable r = new Runnable()
		{
			public void run() 
			{
				if (imageUri.equals(me._originalUri))
				{
					final Uri cachedUri = UriImageView.fetchResizedImage(me.getContext(), uri, width, height, null);

					final Context context = me.getContext();

					if (context instanceof Activity)
					{
						Activity activity = (Activity) context;

						activity.runOnUiThread(new Runnable()
						{
							public void run() 
							{
								try
								{
									if (cachedUri != null)
									{
										me.setImageURI(cachedUri);

										if (doFill)
											me.setScaleType(ScaleType.CENTER_CROP);
										else
											me.setScaleType(ScaleType.FIT_CENTER);

										me.invalidate();
									}
									else
										me.setImageDrawable(UriImageView.fetchResourceDrawable(context, resId));
								}
								catch (OutOfMemoryError e)
								{
									UriImageView._useLargeImage = false;

									me.setImageDrawable(UriImageView.fetchResourceDrawable(context, resId));
								} 

								me.refreshDrawableState();
							}
						});
					}
				}
			}
		};

		try
		{
			Uri cachedUri = UriImageView.fetchResizedImage(this.getContext(), uri, width, height, r);

			if (cachedUri != null)
			{
				this.setImageURI(cachedUri);

				if (doFill)
					me.setScaleType(ScaleType.CENTER_CROP);
				else
					me.setScaleType(ScaleType.FIT_CENTER);
			}
			else if (resId != -1)
				this.setImageDrawable(UriImageView.fetchResourceDrawable(this.getContext(), resId));
		}
		catch (OutOfMemoryError e)
		{
			UriImageView._useLargeImage  = false;

			this.setImageDrawable(UriImageView.fetchResourceDrawable(this.getContext(), resId));
		}
	}
	
	public static Uri fetchResizedImage(final Context context, final Uri uri, final int width, final int height, final Runnable next) 
	{
		if (uri == null)
			return null;

		Uri resized = Uri.parse(uri.toString() + "/" + height + "-" + width);

		String hashString = UriImageView.createHash(context, resized.toString());

		final File cachedFile = new File(UriImageView.fetchCacheDir(context), hashString);

		if (cachedFile.exists())
		{
			cachedFile.setLastModified(System.currentTimeMillis());

			if (next != null)
				next.run();

			return Uri.fromFile(cachedFile);
		}
		else
		{
			Runnable r = new Runnable()
			{
				public void run() 
				{
					Uri cached = UriImageView.fetchCachedUri(context, uri, false);

					if (cached != null)
					{
				        BitmapFactory.Options opts = new BitmapFactory.Options();
						opts.inJustDecodeBounds = true;
						opts.inInputShareable = true;
						opts.inPurgeable = true;

						BitmapFactory.decodeFile(cached.getPath(), opts);

						int scale = 1;

						int thisWidth = width;
						int thisHeight = height;

						while ((thisWidth > 0 && thisHeight > 0) && (opts.outWidth > thisWidth || opts.outHeight > thisHeight))
						{
							scale *= 2;

							thisWidth = thisWidth * 2;
							thisHeight = thisHeight * 2;
						}

						if (UriImageView._useLargeImage || (width * height) < (128 * 128))
							scale = scale / 2;

						if (scale < 1)
							scale = 1;

						opts = new BitmapFactory.Options();		
						opts.inDither = false;
						opts.inPurgeable = true;
						opts.inInputShareable = true;
						opts.inSampleSize = scale;

						try
						{
							Bitmap bitmap = BitmapFactory.decodeFile(cached.getPath(), opts);

							if (bitmap != null)
							{
								cachedFile.createNewFile();

								FileOutputStream fout = new FileOutputStream(cachedFile);

								bitmap.compress(CompressFormat.PNG, 100, fout);

								fout.close();

								bitmap.recycle();
							}
							else
							{
								File f = new File(cached.getPath());

								if (f.exists())
									f.delete();

								return;
							}

							if (next != null)
								next.run();
						}
						catch (OutOfMemoryError e)
						{
							UriImageView._useLargeImage = false;
						}
						catch (FileNotFoundException e) 
						{
							e.printStackTrace();
						} 
						catch (IOException e) 
						{
							e.printStackTrace();
						}
					}
				}
			};

			Thread t = new Thread(r);
			t.start();
		}

		return null;
	}


	public static Drawable fetchResourceDrawable(Context context, int resId)
	{
		Drawable d = UriImageView._cachedResources.get(resId);

		if (d == null)
		{
			d = context.getResources().getDrawable(resId);
			UriImageView._cachedResources.put(resId, d);
		}

		return d;
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

	public static Uri fetchCachedUri(final Context context, final Uri uri, boolean async) 
	{
		if ("file".equals(uri.getScheme().toLowerCase(Locale.ENGLISH)))
			return uri;

		if (uri == null || uri.getScheme() == null)
			return null;

		Uri.Builder builder = uri.buildUpon();

		if ("https".equals(uri.getScheme().toLowerCase(Locale.getDefault())) && "freshcomics.us".equals(uri.getHost()))
			builder.scheme("http");

		final Uri fetchUri = builder.build();

		final String hashString = UriImageView.createHash(context, fetchUri.toString());

		final File cacheDir = UriImageView.fetchCacheDir(context);
		final File cachedFile = new File(cacheDir, hashString);

		if (cachedFile.exists() && cachedFile.length() > 0)
		{
			cachedFile.setLastModified(System.currentTimeMillis());
			return Uri.fromFile(cachedFile);
		}

		Runnable r = new Runnable()
		{
			public void run() 
			{
				File tempFile = null;

				try 
				{
					long freeSpace = UriImageView.cacheSpace(context);

					if (freeSpace < 4096 * 4096)
					{
						UriImageView.cleanCache(context);

						freeSpace = UriImageView.cacheSpace(context);

						if (freeSpace < 4096 * 4096)
							UriImageView.cleanCache(context);
					}

					freeSpace = UriImageView.cacheSpace(context);

					if (freeSpace > 2048 * 2048)
					{
						InputStream in = context.getContentResolver().openInputStream(fetchUri);
						
						tempFile = File.createTempFile(hashString, "tmp", cacheDir);

						BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(tempFile));

						byte[] buffer = new byte[8192];
						int read = 0;

						while ((read = in.read(buffer, 0, buffer.length)) != -1)
						{
							out.write(buffer, 0, read);
						}

						out.flush();
						out.close();

						tempFile.renameTo(cachedFile);
					}
				} 
				catch (SocketTimeoutException e)
				{
					e.printStackTrace();
					// Logger.logThrowable(e);
				}
				catch (SocketException e)
				{
					e.printStackTrace();
					// Logger.logThrowable(e);
				}
				catch (SSLException e)
				{
					e.printStackTrace();
					// Logger.logThrowable(e);
				}
				catch (UnknownHostException e)
				{
					e.printStackTrace();
					// Logger.logThrowable(e);
				}
				catch (EOFException e)
				{
					e.printStackTrace();
					// Logger.logThrowable(e);
				}
				catch (FileNotFoundException e) 
				{
					LogManager.getInstance(context).logException(e);
				}
				catch (IOException e) 
				{
					LogManager.getInstance(context).logException(e);
				} 
//				catch (InterruptedException e) 
//				{
//					e.printStackTrace();
//				}
				finally
				{
					if (tempFile != null && tempFile.exists())
						tempFile.delete();
				}
			}
		};

		if (async)
		{
			Thread t = new Thread(r);
			t.start();

			return null;
		}
		else
		{
			r.run();

			if (cachedFile.exists() && cachedFile.length() > 0)
			{
				cachedFile.setLastModified(System.currentTimeMillis());
				return Uri.fromFile(cachedFile);
			}
		}

		return null;
	}

    public static Uri yeOldeFetchCachedUri(final Context context, final Uri uri, final Runnable next) 
    {
        if (uri == null || uri.getScheme() == null)
            return null;
        
        final String hashString = UriImageView.createHash(context, uri.toString());
        
        final File cacheDir = UriImageView.fetchCacheDir(context);
        final File cachedFile = new File(cacheDir, hashString);
        
        if (cachedFile.exists() && cachedFile.length() > 0)
        {
            cachedFile.setLastModified(System.currentTimeMillis());
            return Uri.fromFile(cachedFile);
        }
        
        Runnable r = new Runnable()
        {
            public void run() 
            {
                File tempFile = null;
                
                try 
                {
                    long freeSpace = UriImageView.cacheSpace(context);
                    
                    if (freeSpace < 4096 * 4096)
                    {
                        UriImageView.cleanCache(context);
                    }
                    
                    freeSpace = UriImageView.cacheSpace(context);

                    if (freeSpace > 2048 * 2048)
                    {
                        URL u = new URL(uri.toString());
                        
                        URLConnection conn = u.openConnection();
                        InputStream in = conn.getInputStream();
                        
                        tempFile = File.createTempFile(hashString, "tmp", cacheDir);

                        FileOutputStream out = new FileOutputStream(tempFile);

                        byte[] buffer = new byte[8192];
                        int read = 0;
                        
                        while ((read = in.read(buffer, 0, buffer.length)) != -1)
                        {
                            out.write(buffer, 0, read);
                        }
                        
                        out.close();
                        
                        tempFile.renameTo(cachedFile);
                    }
                } 
                catch (SocketTimeoutException e)
                {

                }
                catch (SocketException e)
                {

                }
                catch (SSLException e)
                {

                }
                catch (UnknownHostException e)
                {

                }
                catch (EOFException e)
                {

                }
                catch (FileNotFoundException e) 
                {
                	LogManager.getInstance(context).logException(e);
                }
                catch (IOException e) 
                {
                	LogManager.getInstance(context).logException(e);
                }
                finally
                {
                    if (tempFile != null && tempFile.exists())
                        tempFile.delete();
                }

                if (next != null)
                	next.run();
            }
        };
        
        Thread t = new Thread(r);
        t.start();

        return null;
    }
    
    private static String createHash(Context context, String string)
    {
        if (string == null)
            return null;
        
        String hash = UriImageView._cachedHashes.get(string);
        
        if (hash != null)
            return hash;

        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(string.getBytes("UTF-8"));

            hash = (new BigInteger(1, digest)).toString(16);

            while (hash.length() < 32)
            {
                    hash = "0" + hash;
            }
        }
        catch (NoSuchAlgorithmException e)
        {
        	LogManager.getInstance(context).logException(e);
        }
        catch (UnsupportedEncodingException e)
        {
        	LogManager.getInstance(context).logException(e);
        }
        
        if (UriImageView._cachedHashes.size() > 256)
            UriImageView._cachedHashes.clear();
        
        UriImageView._cachedHashes.put(string, hash);

        return hash;
    }
    
    private static File fetchCacheDir(Context context)
    {
    	File cache = context.getExternalCacheDir(); // .getCacheDir();
    
    	Log.e("IC", "IMAGE: CACHE: " + cache);
    	
        if (cache.exists() == false)
            cache.mkdirs();
            
        return cache;
    }

    @SuppressWarnings("deprecation")
	public static synchronized long cacheSpace(Context context)
    {
        File cache = context.getCacheDir();
        
        if (cache.exists() == false)
        	cache.mkdirs();
        
        StatFs stat = new StatFs(cache.getAbsolutePath());
        
        return ((long) stat.getBlockSize()) * ((long) stat.getAvailableBlocks());
    }

    public static synchronized void cleanCache(Context context) 
    {
        File cache = context.getCacheDir();
        
        int cacheSize = 16 * 1024 * 1024;

        if (cache.exists() == false)
            cache.mkdirs();
        
        synchronized(context.getApplicationContext())
        {
            List<File> files = new ArrayList<File>();
            
            for (File f : cache.listFiles())
            {
                files.add(f);
            }

            try
            {
                Collections.sort(files, new Comparator<File>()
                {
                    public int compare(File first, File second) 
                    {
                        if (first.lastModified() < second.lastModified())
                            return 1;
                        else if (first.lastModified() > second.lastModified())
                            return -1;

                        return first.getName().compareTo(second.getName());
                    }
                });
            }
            catch (IllegalArgumentException e)
            {
            	LogManager.getInstance(context).logException(e);
            }
            
            List<File> toRemove = new ArrayList<File>();
            
            long totalCached = 0;
            
            for (File f : files)
            {
                if (totalCached < cacheSize)
                    totalCached += f.length();
                else
                    toRemove.add(f);
            }
            
            for (File f : toRemove)
            {
                f.delete();
            }
        }
    }
}