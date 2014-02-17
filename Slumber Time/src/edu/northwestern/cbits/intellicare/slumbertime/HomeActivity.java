package edu.northwestern.cbits.intellicare.slumbertime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.CrashManagerListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.northwestern.cbits.intellicare.logging.LogManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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

	private static final String APP_ID = "62e48583d6763b21b5ccf7186bd44089";
	
	@SuppressLint("SetJavaScriptEnabled")
	protected void onResume()
	{
		super.onResume();
		
		CrashManager.register(this, APP_ID, new CrashManagerListener() 
		{
			public boolean shouldAutoUploadCrashes() 
			{
				    return true;
			}
		});

		final HomeActivity me = this;
		
		ArrayList<Tool> tools = new ArrayList<Tool>();
		
		tools.add(new Tool(this.getString(R.string.tool_sleep_log_notes), this.getString(R.string.desc_sleep_log_notes), R.drawable.clock_log_dark, new Intent(this, SleepLogActivity.class)));
		tools.add(new Tool(this.getString(R.string.tool_bedtime_checklist), this.getString(R.string.desc_bedtime_checklist), R.drawable.clock_checklist_dark, new Intent(this, BedtimeChecklistActivity.class)));
		tools.add(new Tool(this.getString(R.string.tool_sleep_diaries), this.getString(R.string.desc_sleep_diaries), R.drawable.clock_diary_dark, new Intent(this, SleepDiaryActivity.class)));
		tools.add(new Tool(this.getString(R.string.tool_sleep_content), this.getString(R.string.desc_sleep_content), R.drawable.clock_question_dark, new Intent(this, SleepContentActivity.class)));
		tools.add(new Tool(this.getString(R.string.tool_tip_video), this.getString(R.string.desc_tip_video), R.drawable.clock_question_dark, new Intent(this, TipsActivity.class)));

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
		
		graphView.loadDataWithBaseURL("file:///android_asset/", HomeActivity.generateGraph(this), "text/html", null, null);
	}

	@SuppressLint("SetJavaScriptEnabled")
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		Intent startAlarms = new Intent(AlarmService.START_TIMER, null, this.getApplicationContext(), AlarmService.class);
		this.startService(startAlarms);

		this.setContentView(R.layout.activity_home);
	}

	private static String generateGraph(Context context) 
	{
	    StringBuilder buffer = new StringBuilder();
	    
		try 
		{
		    InputStream html = context.getAssets().open("home_graph.html");

		    BufferedReader in = new BufferedReader(new InputStreamReader(html));

		    String str = null;

		    while ((str=in.readLine()) != null) 
		    {
		    	buffer.append(str);
		    }

		    in.close();
		} 
		catch (IOException e) 
		{
			LogManager.getInstance(context).logException(e);
		}

		String graphString = buffer.toString();
		
		JSONArray graphValues = HomeActivity.graphValues(context);
		
		graphString = graphString.replaceAll("VALUES_JSON", graphValues.toString());
		
		Log.e("ST", "HTML: " + graphString);
		
		return graphString;
	}

	private static JSONArray graphValues(Context context) 
	{
		JSONArray values = new JSONArray();

		/* function sinAndCos()
		{
			var sin = [], cos = [];

			for (var i = 0; i < 100; i++) 
			{
				sin.push({x: i, y: Math.sin(i/10)});
				cos.push({x: i, y: .5 * Math.cos(i/10)});
			}

			return [
				{
					values: sin,
					key: 'Sine Wave',
					color: '#ff7f0e'
				},
				{
					values: cos,
					key: 'Cosine Wave',
					color: '#2ca02c'
				}
			];
		} */

		String[] colors = { "#33B5E5", "#AA66CC", "#99CC00", "#FFBB33", "#FF4444" };
		int colorIndex = 0;
		
		String[] sensors = { SlumberContentProvider.TEMPERATURE, SlumberContentProvider.LIGHT_LEVEL, SlumberContentProvider.AUDIO_MAGNITUDE, SlumberContentProvider.AUDIO_FREQUENCY };

		try 
		{
			JSONObject sleep = new JSONObject();
			sleep.put("key", context.getString(R.string.label_sleep_efficiency));
			sleep.put("color", colors[colorIndex++ % colors.length]);

			JSONArray sleepValues = new JSONArray();
			
			Cursor c = context.getContentResolver().query(SlumberContentProvider.SLEEP_DIARIES_URI, null, null, null, SlumberContentProvider.DIARY_TIMESTAMP);
			
			while (c.moveToNext())
			{
				JSONObject reading = new JSONObject();
				
				reading.put("x", c.getLong(c.getColumnIndex(SlumberContentProvider.DIARY_TIMESTAMP)));
				reading.put("y", SlumberContentProvider.scoreSleep(c, 1));
				
				sleepValues.put(reading);
			}

			c.close();
			
			sleep.put("values", sleepValues);
			
			values.put(sleep);
		} 
		catch (JSONException e) 
		{
			LogManager.getInstance(context).logException(e);
		}
		
		for (String sensor : sensors)
		{
			try 
			{
				Cursor c = SlumberContentProvider.fetchNormalizedSensorReadings(context, sensor);
				
				if (c.getCount() > 0)
				{
					JSONObject sensorObj = new JSONObject();
					sensorObj.put("key", sensor);
					sensorObj.put("color", colors[colorIndex++ % colors.length]);

					JSONArray sensorValues = new JSONArray();

					while (c.moveToNext())
					{
						JSONObject reading = new JSONObject();

						reading.put("x", c.getLong(c.getColumnIndex(SlumberContentProvider.READING_RECORDED)));
						reading.put("y", c.getLong(c.getColumnIndex(SlumberContentProvider.READING_VALUE)));

						sensorValues.put(reading);
					}

					sensorObj.put("values", sensorValues);

					values.put(sensorObj);
				}
				
				c.close();
			} 
			catch (JSONException e) 
			{
				LogManager.getInstance(context).logException(e);
			}
		}

		return values;
	}

	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.home, menu);

		return true;
	}
}
