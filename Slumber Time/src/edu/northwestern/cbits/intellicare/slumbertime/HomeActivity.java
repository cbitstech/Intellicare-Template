package edu.northwestern.cbits.intellicare.slumbertime;

import java.util.ArrayList;

import edu.northwestern.cbits.ic_template.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

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
		
		ArrayList<Tool> tools = new ArrayList<Tool>();
		
		tools.add(new Tool("bEdTimE cHeckList", "bedtime checklist desc goes here...", 0, null));
		tools.add(new Tool("lAst MinuTE tHOUghTs", "LMT desc goes here...", 0, null));
		tools.add(new Tool("fOOd & exERCise", "FAE desc goes here...", 0, null));
		tools.add(new Tool("relAXinG cONTeNt", "RC desc goes here...", 0, null));

		ListView toolsList = (ListView) this.findViewById(R.id.list_tools);
		
		ArrayAdapter<Tool> adapter = new ArrayAdapter<Tool>(this, R.layout.row_home_tool, tools)
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
			}
		};
		
		toolsList.setAdapter(adapter);
	}

	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.home, menu);

		return true;
	}
}
