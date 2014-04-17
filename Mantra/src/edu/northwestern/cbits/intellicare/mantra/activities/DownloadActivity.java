package edu.northwestern.cbits.intellicare.mantra.activities;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Set;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.mantra.Constants;
import edu.northwestern.cbits.intellicare.mantra.GetImageListAndSizesTask;
import edu.northwestern.cbits.intellicare.mantra.MediaScannerService;
import edu.northwestern.cbits.intellicare.mantra.R;

public class DownloadActivity extends ConsentedActivity 
{
	protected static final String CN = "DownloadActivity";
	private final ConsentedActivity self = this;
	

	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_download);
		this.getSupportActionBar().setTitle(R.string.title_download_queue);

//		DownloadManager.getInstance(this);
	}
	
	public void onResume()
	{
		super.onResume();

		final DownloadActivity me = this;
		final ListView list = (ListView) this.findViewById(R.id.list_view);

		LocalBroadcastManager broadcasts = LocalBroadcastManager.getInstance(this);
		
		IntentFilter filter = new IntentFilter(Constants.BROADCAST_ACTION);
		
		broadcasts.registerReceiver(new BroadcastReceiver()
		{
			public void onReceive(Context context, Intent intent) 
			{
				List<DownloadManager.DownloadItem> items = DownloadManager.getInstance(me).getCurrentDownloads();
				
				ArrayAdapter<DownloadManager.DownloadItem> adapter = new ArrayAdapter<DownloadManager.DownloadItem>(me, R.layout.row_download, items)
				{
					public View getView(int position, View convertView, ViewGroup parent)
					{
						if (convertView == null)
						{
							LayoutInflater inflater = LayoutInflater.from(parent.getContext());
							convertView = inflater.inflate(R.layout.row_download, parent, false);
						}
						
						DownloadManager.DownloadItem item = this.getItem(position);
						
						TextView url = (TextView) convertView.findViewById(R.id.label_url);
						url.setText(item.url);
						
						ProgressBar progress = (ProgressBar) convertView.findViewById(R.id.progress);
						
						int downloaded = (int) item.downloaded;
						int size = (int) item.size;
						
						progress.setMax(size);
						progress.setProgress(downloaded);
						
						return convertView;
					}
				};

				list.setAdapter(adapter);
				
				if (DownloadManager.getInstance(me).downloadsComplete())
				{
					Intent indexIntent = new Intent(me, IndexActivity.class);
					me.startActivity(indexIntent);
					
					me.finish();
				}

				int remaining = 0;
				
				for (DownloadManager.DownloadItem item : items)
				{
					if (item.size != item.downloaded)
						remaining += 1;
				}
				
				me.getSupportActionBar().setSubtitle(me.getString(R.string.subtitle_download_queue, remaining, items.size()));
			}
		}, filter);
		
		handleExternalIntents();
	}
	
	/**
	 * Handle external intents; src: http://developer.android.com/training/sharing/receive.html
	 */
	private void handleExternalIntents() {
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		String action = intent.getAction();
		String type = intent.getType();
		
		Log.d(CN+".handleExternalIntents", "action = " + action + "; type = " + type);
		if(Intent.ACTION_SEND.equals(action) && type != null) {
			if("text/plain".equals(type)) {
				final String urlFromBrowser = intent.getStringExtra(Intent.EXTRA_TEXT);
//				promptConfirmDownloadPageImages(urlFromBrowser);
				
//				// start the download manager
//				DownloadManager.urls = new String[] { urlFromBrowser };
//				DownloadManager.getInstance(this);

//				final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar1);
//				final View progressBarView = self.getLayoutInflater().inflate(R.layout.activity_progress, null);
//				
//				Log.d(CN+".promptConfirmDownloadPageImages", "progressBarView == null = " + (progressBarView == null));
//				new GetImageListAndSizesTask(self, progressBar, progressBarView).execute(urlFromBrowser);
				
				final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar1);
				final View progressBarView = self.getLayoutInflater().inflate(R.layout.activity_progress, null);
				
				Log.d(CN+".handleExternalIntents", "progressBarView == null = " + (progressBarView == null));
				
				Thread thread = new Thread(new Runnable() {
					
					@Override
					public void run() {
						Set<String> imagesToDownload = null;
						try {
							GetImageListAndSizesTask gilast = new GetImageListAndSizesTask(self, progressBar, progressBarView);
							imagesToDownload = (gilast.getFilteredSetOfImageUrlsFromHtmlUrl(urlFromBrowser)).imagesToDownload.keySet();
						} catch (SocketTimeoutException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (UnknownHostException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						DownloadManager.urls = imagesToDownload.toArray(new String[imagesToDownload.size()]);
						Log.d(CN+".handleExternalIntents", "DownloadManager.urls = " + DownloadManager.urls.toString());
						DownloadManager.activity = self;
						DownloadManager.getInstance(self);
					}
				});
				
				thread.start();
			}
		}
		else if(extras.getBoolean(MediaScannerService.INTENT_KEY_TO_RECEIVER_STRINGARRAY)) {
			Log.d(CN+".handleExternalIntents", "intent from new-images notification alarm");
			SoloFocusBoardActivity.startBrowsePhotosActivity(this);
		}
	}

//	/**
//	 * Prompts the user to confirm they wish to fetch the images at the URL they passed from some some other app (e.g. Chrome).
//	 * @param url
//	 */
//	public void promptConfirmDownloadPageImages(final String url) {
//		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
//		    @Override
//		    public void onClick(DialogInterface dialog, int which) {
//		        switch (which){
//		        case DialogInterface.BUTTON_POSITIVE:
//		            //Yes button clicked
//		        	Log.d(CN + ".promptConfirmDownloadPageImages", "Yes path");
//
//					final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar1);
//					final View progressBarView = self.getLayoutInflater().inflate(R.layout.activity_progress, null);
//					
//					Log.d(CN+".promptConfirmDownloadPageImages", "progressBarView == null = " + (progressBarView == null));
//					new GetImageListAndSizesTask(self, progressBar, progressBarView).execute(url);
//
//		        	break;
//
//		        case DialogInterface.BUTTON_NEGATIVE:
//		            //No button clicked
//		        	Log.d(CN + ".promptConfirmDownloadPageImages", "No path");
//		            break;
//		        }
//		    }
//		};
//
//		AlertDialog.Builder builder = new AlertDialog.Builder(this);
//		builder.setMessage("Download and choose from the images at this URL?: \"" + url + "\"")
//			.setPositiveButton("Yes", dialogClickListener)
//		    .setNegativeButton("No", dialogClickListener)
//		    .show();
//	}
}
