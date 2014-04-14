package edu.northwestern.cbits.intellicare.relax;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class DownloadManager
{
	public static final String DOWNLOAD_PROGRESS = "chill_download_progress";
	public static final String DOWNLOAD_SIZE = "chill_download_progress";
	public static final String DOWNLOAD_URL = "chill_download_url";

	private static DownloadManager _instance = null;
	private Context _context = null;
	
	private ArrayList<String> _toDownload = null;
	private boolean _checking = false;

	public DownloadManager(Context context) 
	{
		this._context  = context;
		
		this.checkDownloads();
	}
	
	public boolean downloadsComplete()
	{
		return (this._toDownload != null && this._toDownload.size() == 0);
	}

	private void checkDownloads() 
	{
		if (this._checking)
			return;
		
		this._checking  = true;
		
		final String[] urls = this._context.getResources().getStringArray(R.array.remote_media_urls);
		
		final DownloadManager me = this;
		
		Runnable r = new Runnable()
		{
			public void run() 
			{
				File folder = me._context.getFilesDir();

				me._toDownload = new ArrayList<String>();
				
				HashMap<String, Long> sizes = new HashMap<String, Long>();
				
				for (String url : urls)
				{
					Log.e("PC", "URL: " + url);

					try 
					{
						Uri uri = Uri.parse(url);
						
						String filename = uri.getLastPathSegment();
						
						File existingFile = new File(folder, filename);

						long fileSize = 0;
						
						if (existingFile.exists())
							fileSize = existingFile.length();
						
						HttpClient client = new DefaultHttpClient();
						HttpHead method = new HttpHead(url);

						HttpResponse response = client.execute(method);
						Header[] headers = response.getAllHeaders();
					
						for (Header header : headers)
						{
							if ("Content-Length".equalsIgnoreCase(header.getName()))
							{
								sizes.put(url, Long.parseLong(header.getValue()));
								
								if (Long.parseLong(header.getValue()) != fileSize)
									me._toDownload.add(url);
							}
						}
					} 
					catch (ClientProtocolException e) 
					{
						e.printStackTrace();
					} 
					catch (IOException e) 
					{
						e.printStackTrace();
					}
				}
				
				LocalBroadcastManager broadcast = LocalBroadcastManager.getInstance(me._context);

				while (me._toDownload.size() > 0)
				{
					try 
					{
						String url = me._toDownload.remove(0);
						
						Uri uri = Uri.parse(url);

						String filename = uri.getLastPathSegment();
						URL u = new URL(url);
						
						URLConnection conn = u.openConnection();
						InputStream in = conn.getInputStream();

						File tempFile = File.createTempFile(filename, "tmp", me._context.getCacheDir());

						BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(tempFile));

						byte[] buffer = new byte[131072];
						int read = 0;
						
						long totalSize = sizes.get(url);

						long totalRead = 0;

						while ((read = in.read(buffer, 0, buffer.length)) != -1)
						{
							out.write(buffer, 0, read);
							
							totalRead += read;
							
							Intent intent = new Intent(DownloadManager.DOWNLOAD_PROGRESS);
							intent.putExtra(DownloadManager.DOWNLOAD_PROGRESS, totalRead);
							intent.putExtra(DownloadManager.DOWNLOAD_SIZE, totalSize);
							intent.putExtra(DownloadManager.DOWNLOAD_URL, url);
							
							broadcast.sendBroadcast(intent);
						}

						out.flush();
						out.close();

						tempFile.renameTo(new File(folder, filename));
					} 
					catch (MalformedURLException e) 
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

	public static DownloadManager getInstance(Context context)
	{
		if (DownloadManager._instance == null)
			DownloadManager._instance = new DownloadManager(context.getApplicationContext());
		
		return DownloadManager._instance;
	}
}
