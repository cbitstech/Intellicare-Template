package edu.northwestern.cbits.intellicare.slumbertime;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
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

	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_home);
		
		final HomeActivity me = this;
		
		ArrayList<Tool> tools = new ArrayList<Tool>();
		
		tools.add(new Tool("bEdTimE cHeckList", "bedtime checklist desc goes here...", 0, null));
		tools.add(new Tool(this.getString(R.string.tool_sleep_log_notes), this.getString(R.string.desc_sleep_log_notes), R.drawable.clock_log, new Intent(this, SleepLogActivity.class)));
		tools.add(new Tool("fOOd & exERCise", "FAE desc goes here...", 0, null));
		tools.add(new Tool("relAXinG cONTeNt", "RC desc goes here...", 0, null));

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
				else
					Toast.makeText(me, "ToDo: AssiGN laUNCh iNtenT to TooL...", Toast.LENGTH_SHORT).show();
			}
		});
	}

	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.home, menu);

		return true;
	}
}
