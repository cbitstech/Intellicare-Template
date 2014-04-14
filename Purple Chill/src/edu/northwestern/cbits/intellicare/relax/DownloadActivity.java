package edu.northwestern.cbits.intellicare.relax;

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
import android.widget.TextView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class DownloadActivity extends ConsentedActivity 
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_download);
		this.getSupportActionBar().setTitle(R.string.title_download_queue);
		this.getSupportActionBar().setSubtitle(R.string.subtitle_download_queue);

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
						TextView progress = (TextView) convertView.findViewById(R.id.label_progress);
						
						url.setText(item.url);
						progress.setText(item.downloaded + " / " + item.size);
						
						return convertView;
					}
				};

				list.setAdapter(adapter);
			}
		}, filter);
	}
}
