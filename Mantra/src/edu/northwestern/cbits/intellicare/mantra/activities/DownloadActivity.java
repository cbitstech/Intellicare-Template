package edu.northwestern.cbits.intellicare.mantra.activities;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.mantra.R;

public class DownloadActivity extends ConsentedActivity 
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_download);
		this.getSupportActionBar().setTitle(R.string.title_download_queue);

		DownloadManager.getInstance(this);
	}
	
	public void onResume()
	{
		super.onResume();

		final DownloadActivity me = this;
		final ListView list = (ListView) this.findViewById(R.id.list_view);

		LocalBroadcastManager broadcasts = LocalBroadcastManager.getInstance(this);
		
		IntentFilter filter = new IntentFilter(DownloadManager.DOWNLOAD_UPDATE);
		
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
	}
}
