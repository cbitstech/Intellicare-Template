package edu.northwestern.cbits.intellicare.slumbertime;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class TipsActivity extends ConsentedActivity 
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.view_clock_tips);
		
		this.getSupportActionBar().setTitle(R.string.tool_tip_video);
	}
	
	protected void onResume()
	{
		super.onResume();
		
		GridView contentGrid = (GridView) this.findViewById(R.id.root_grid);
		
		final String[] titles = this.getResources().getStringArray(R.array.youtube_titles);
		final String[] ids = this.getResources().getStringArray(R.array.youtube_ids);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.cell_tip, titles)
		{
			public View getView (int position, View convertView, ViewGroup parent)
			{
				if (convertView == null)
				{
    				LayoutInflater inflater = LayoutInflater.from(parent.getContext());
    				convertView = inflater.inflate(R.layout.cell_tip, parent, false);
				}
				
				String id = ids[position];
				String title = titles[position];
				
				TextView tipTitle = (TextView) convertView.findViewById(R.id.title_tip);
				tipTitle.setText(title);
				
				UriImageView icon = (UriImageView) convertView.findViewById(R.id.icon_tip);
				icon.setCachedImageUri(Uri.parse("http://img.youtube.com/vi/" + id + "/1.jpg"));
				
				return convertView;
			}
		};
		
		contentGrid.setAdapter(adapter);
		
		final TipsActivity me = this;

		contentGrid.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) 
			{
				Uri u = Uri.parse("http://www.youtube.com/watch?v=" + ids[arg2]);
				
				Intent intent = new Intent(Intent.ACTION_VIEW, u);
				
				me.startActivity(intent);
			}
		});
	}
}
