package edu.northwestern.cbits.intellicare.relax;

import java.util.List;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
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

public class DownloadActivity extends ConsentedActivity 
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_download);
		this.getSupportActionBar().setTitle(R.string.title_download_queue);

        if (checkConnection(this) == false) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setMessage(R.string.connect_to_internet);
            builder.setTitle(R.string.no_internet);

            final DownloadActivity me = this;

            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    Intent wireless = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                    me.startActivity(wireless);

                    me.finish();
                }
                // where should the ok button take the user?
            });

            AlertDialog dialog = builder.create();
            dialog.show();

            return;
        }

        final DownloadActivity me = this;
        final ListView list = (ListView) this.findViewById(R.id.list_view);

        LocalBroadcastManager broadcasts = LocalBroadcastManager.getInstance(this);

        IntentFilter filter = new IntentFilter(DownloadManager.DOWNLOAD_UPDATE);

        broadcasts.registerReceiver(new BroadcastReceiver()
        {
            public void onReceive(final Context context, Intent intent)
            {
                Log.e("PC", "INTENT: " + intent);

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

                        String[] titles = context.getResources().getStringArray(R.array.remote_media_titles);

                        TextView url = (TextView) convertView.findViewById(R.id.label_url);
                        url.setText(titles[position]);

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

    private boolean checkConnection(Context context) {

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }
	
	public void onResume()
	{
		super.onResume();

        if (DownloadManager.getInstance(this).downloadsComplete())
        {
            Intent indexIntent = new Intent(this, IndexActivity.class);
            this.startActivity(indexIntent);

            this.finish();
        }
        else
            DownloadManager.getInstance(this).checkDownloads();
    }
}
