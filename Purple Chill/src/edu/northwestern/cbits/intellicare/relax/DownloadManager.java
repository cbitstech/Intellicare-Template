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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

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

public class DownloadManager
{
	public static final String DOWNLOAD_UPDATE = "chill_download_update";
	public static final String DOWNLOAD_ERROR = "chill_download_error";

	private static DownloadManager _instance = null;
	private Context _context = null;
	
	private ArrayList<String> _queue = new ArrayList<String>();
	private ArrayList<String> _remaining = new ArrayList<String>();
	
	HashMap<String, Long> _downloadSizes = new HashMap<String, Long>();
	HashMap<String, Long> _downloadProgress = new HashMap<String, Long>();

	private boolean _checking = false;
	
	public static class DownloadItem
	{
		public String url = null;
		public long size = -1;
		public long downloaded = -1;
	}

	public DownloadManager(Context context) 
	{
		this._context  = context;
		
		this.checkDownloads();
	}
	
	public boolean downloadsComplete()
	{
		boolean complete = (this._queue.size() > 0 && this._remaining.size() == 0);

		if (complete == false && this._queue.size() == 0)
		{
			File folder = this._context.getFilesDir();

			final String[] urls = this._context.getResources().getStringArray(R.array.remote_media_urls);
			
			for (String url : urls)
			{
				Uri u = Uri.parse(url);
				
				File f = new File(folder, u.getLastPathSegment());
				
				if (f.exists() == false)
					return false;
			}
			
			return true;
		}
		
		return complete;
	}

	private void checkDownloads() 
	{
		if (this._checking)
			return;
		
		this._checking  = true;
		
		final String[] urls = this._context.getResources().getStringArray(R.array.remote_media_urls);
		
		final DownloadManager me = this;

		final LocalBroadcastManager broadcast = LocalBroadcastManager.getInstance(me._context);

		Runnable r = new Runnable()
		{
			public void run() 
			{
				File folder = me._context.getFilesDir();

				for (String url : urls)
				{
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
								me._downloadSizes.put(url, Long.parseLong(header.getValue()));
								
								me._queue.add(url);
								
								if (Long.parseLong(header.getValue()) != fileSize)
									me._remaining.add(url);
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

				Intent intent = new Intent(DownloadManager.DOWNLOAD_UPDATE);

				broadcast.sendBroadcast(intent);

				while (me._remaining.size() > 0)
				{
					try 
					{
						String url = me._remaining.get(0);
						
						Uri uri = Uri.parse(url);

						String filename = uri.getLastPathSegment();
						URL u = new URL(url);
						
						URLConnection conn = u.openConnection();
						InputStream in = conn.getInputStream();

						File tempFile = File.createTempFile(filename, "tmp", me._context.getCacheDir());

						BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(tempFile));

						byte[] buffer = new byte[131072 * 4];
						int read = 0;
						
						long totalRead = 0;

						while ((read = in.read(buffer, 0, buffer.length)) != -1)
						{
							out.write(buffer, 0, read);
							
							totalRead += read;

							me._downloadProgress.put(url, totalRead);
							
							broadcast.sendBroadcast(intent);
						}

						out.flush();
						out.close();

						tempFile.renameTo(new File(folder, filename));
						
						me._remaining.remove(0);
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
				
				me._checking  = false;
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
	
	public List<DownloadItem> getCurrentDownloads()
	{
		ArrayList<DownloadItem> items = new ArrayList<DownloadItem>();
		
		for (String url : this._queue)
		{
			DownloadItem item = new DownloadItem();
			item.url = url;
			
			if (this._downloadSizes.containsKey(url))
				item.size = this._downloadSizes.get(url);

			if (this._downloadProgress.containsKey(url))
				item.downloaded = this._downloadProgress.get(url);
			
			items.add(item);
		}
		
		Collections.sort(items, new Comparator<DownloadItem>()
		{
			public int compare(DownloadItem one, DownloadItem two) 
			{
				long oneDiff = one.size - one.downloaded;
				long twoDiff = two.size - two.downloaded;
				
				if (oneDiff == 0 && twoDiff != 0)
					return -1;
				else if (oneDiff != 0 && twoDiff == 0)
					return -2;
				else if (oneDiff == 0 && twoDiff == 0)
					return Long.valueOf(two.size).compareTo(Long.valueOf(one.size));
				
				double oneRatio = (double) one.size / (double)one.downloaded;
				double twoRatio = (double) two.size / (double)two.downloaded;
					
				return Double.valueOf(twoRatio).compareTo(Double.valueOf(oneRatio));
			}
		});
		
		return items;
	}
}
