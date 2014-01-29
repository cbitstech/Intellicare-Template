package edu.northwestern.cbits.intellicare.slumbertime;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class HomeActivity extends PortraitActivity 
{
	private class Tool
	{
		public String name;
		public String description;
		public int icon;
		public Intent launchIntent;
		
		public Tool(String name, String description, int icon, Intent launchIntent)
		{
			this.name = name;
			this.description = description;
			this.icon = icon;
			this.launchIntent = launchIntent;
		}
	}

	@SuppressLint("SetJavaScriptEnabled")
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_home);
		
		final HomeActivity me = this;
		
		ArrayList<Tool> tools = new ArrayList<Tool>();
		
		tools.add(new Tool(this.getString(R.string.tool_sleep_log_notes), this.getString(R.string.desc_sleep_log_notes), R.drawable.clock_log_dark, new Intent(this, SleepLogActivity.class)));
		tools.add(new Tool(this.getString(R.string.tool_bedtime_checklist), this.getString(R.string.desc_bedtime_checklist), R.drawable.clock_checklist_dark, new Intent(this, BedtimeChecklistActivity.class)));
		tools.add(new Tool(this.getString(R.string.tool_sleep_diaries), this.getString(R.string.desc_sleep_diaries), R.drawable.clock_checklist_dark, new Intent(this, SleepDiaryActivity.class)));

		ListView toolsList = (ListView) this.findViewById(R.id.list_tools);
		
		final ArrayAdapter<Tool> adapter = new ArrayAdapter<Tool>(this, R.layout.row_home_tool, tools)
		{
			public View getView (int position, View convertView, ViewGroup parent)
			{
				if (convertView == null)
				{
    				LayoutInflater inflater = LayoutInflater.from(parent.getContext());
    				convertView = inflater.inflate(R.layout.row_home_tool, parent, false);
				}
				
				TextView name = (TextView) convertView.findViewById(R.id.label_tool_name);
				TextView desc = (TextView) convertView.findViewById(R.id.label_tool_description);
				
				Tool t = this.getItem(position);
				
				name.setText(t.name);
				desc.setText(t.description);
				
				ImageView icon = (ImageView) convertView.findViewById(R.id.icon_tool);
				
				if (t.icon != 0)
					icon.setImageDrawable(me.getResources().getDrawable(t.icon));
				
				return convertView;
			}
		};
		
		toolsList.setAdapter(adapter);

		toolsList.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int which, long id) 
			{
				Tool t = adapter.getItem(which);
				
				if (t.launchIntent != null)
					me.startActivity(t.launchIntent);
			}
		});
		
		WebView graphView = (WebView) this.findViewById(R.id.graph_web_view);
		graphView.getSettings().setJavaScriptEnabled(true);
		
		graphView.setWebChromeClient(new WebChromeClient() 
		{
			  public void onConsoleMessage(String message, int lineNumber, String sourceID) 
			  {
				  Log.d("ST", message + " -- From line " + lineNumber + " of " + sourceID);
			  }
		});
		
		graphView.loadUrl("file:///android_asset/home_graph.html");
	}

	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.home, menu);

		return true;
	}
}
