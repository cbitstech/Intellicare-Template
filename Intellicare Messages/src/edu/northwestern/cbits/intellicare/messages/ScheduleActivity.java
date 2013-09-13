package edu.northwestern.cbits.intellicare.messages;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class ScheduleActivity extends ConsentedActivity 
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_list);
		this.getSupportActionBar().setTitle(R.string.action_schedule);
	}
	
	public void onResume()
	{
		super.onResume();
		
		ListView list = (ListView) this.findViewById(R.id.list_view);
		
		final ArrayList<JSONObject> items = new ArrayList<JSONObject>();
		
		String[] testItems = this.getResources().getStringArray(R.array.schedule_test_items);
	
		try 
		{
			for (String test : testItems)
				items.add(new JSONObject(test));
		} 
		catch (JSONException e) 
		{
			e.printStackTrace();
		}
		
		Log.e("P2P", "ITEMS: " + items.size());
		
		ArrayAdapter<JSONObject> adapter = new ArrayAdapter<JSONObject>(this, R.layout.row_schedule, items)
		{
			public View getView(int position, View convertView, ViewGroup parent)
			{
				if (convertView == null)
				{
					LayoutInflater inflater = LayoutInflater.from(parent.getContext());
					convertView = inflater.inflate(R.layout.row_schedule, parent, false);
				}

				TextView header = (TextView) convertView.findViewById(R.id.notification_header);
				LinearLayout texts = (LinearLayout) convertView.findViewById(R.id.schedule_texts);
 				ImageView completeIcon = (ImageView) convertView.findViewById(R.id.complete_icon);

				JSONObject item = items.get(position);

				if (item.has("header"))
				{
					header.setVisibility(View.VISIBLE);
					texts.setVisibility(View.GONE);
					completeIcon.setVisibility(View.GONE);
					
					try 
					{
						header.setText(item.getString("header"));
					}
					catch (JSONException e) 
					{
						e.printStackTrace();
					}
				}
				else
				{
					header.setVisibility(View.GONE);
					texts.setVisibility(View.VISIBLE);
					completeIcon.setVisibility(View.VISIBLE);

					try 
					{
						TextView title = (TextView) convertView.findViewById(R.id.notification_title);
						TextView description = (TextView) convertView.findViewById(R.id.notification_description);
					
						title.setText(item.getString("title"));

						if (item.getBoolean("completed"))
						{
							completeIcon.setVisibility(View.VISIBLE);
							completeIcon.setImageResource(R.drawable.ic_action_tick);
							description.setText("cOMPLETED: " + item.getString("time"));
						}
						else
						{
							completeIcon.setVisibility(View.INVISIBLE);
							description.setText("sCHEDULED: " + item.getString("scheduled_time"));
						}
					} 
					catch (JSONException e) 
					{
						e.printStackTrace();
					}
				}				

				return convertView;
			}
		};

		list.setAdapter(adapter);
	}
}
