package edu.northwestern.cbits.intellicare.slumbertime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.CrashManagerListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import edu.northwestern.cbits.intellicare.logging.LogManager;

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
	protected static final String SELECTED_TIME_RANGE = "setting_selected_time_range";
	
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
		tools.add(new Tool(this.getString(R.string.tool_tip_video), this.getString(R.string.desc_tip_video), R.drawable.clock_youtube_dark, new Intent(this, TipsActivity.class)));

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
		
		graphView.loadDataWithBaseURL("file:///android_asset/", HomeActivity.generateGraph(this), "text/html", null, null);
		
		HashMap<String, Object> payload = new HashMap<String, Object>();
		LogManager.getInstance(this).log("launched_home_activity", payload);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		int durationIndex = prefs.getInt(HomeActivity.SELECTED_TIME_RANGE, -1);
		
		if (durationIndex != -1)
		{
			String duration = this.getResources().getStringArray(R.array.graph_time_intervals)[durationIndex];
			
			this.getSupportActionBar().setSubtitle(this.getString(R.string.home_subtitle, duration));
		}
	}

	protected void onPause()
	{
		HashMap<String, Object> payload = new HashMap<String, Object>();
		LogManager.getInstance(this).log("closed_home_activity", payload);
		
		super.onPause();
	}

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

		    while ((str = in.readLine()) != null) 
		    {
		    	buffer.append(str);
		    	buffer.append(System.getProperty("line.separator"));
		    }

		    in.close();
		} 
		catch (IOException e) 
		{
			LogManager.getInstance(context).logException(e);
		}

		String graphString = buffer.toString();
		
		JSONArray graphValues = HomeActivity.graphValues(context, false);

		graphString = graphString.replaceAll("VALUES_JSON", graphValues.toString());

/*		try 
		{
			graphString = graphString.replaceAll("VALUES_JSON", graphValues.toString());

			FileUtils.writeStringToFile(new File(Environment.getExternalStorageDirectory(), "graph.html"), graphString);
		} 
		catch (IOException e) 
		{
			LogManager.getInstance(context).logException(e);
		} 
		catch (JSONException e) 
		{
			LogManager.getInstance(context).logException(e);
		}
*/
		
		return graphString;
	}

	private static JSONArray graphValues(Context context, boolean includeAll) 
	{
		JSONArray values = new JSONArray();

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		long startTime = 0;
		
		int durationIndex = prefs.getInt(HomeActivity.SELECTED_TIME_RANGE, -1);
		
		if (durationIndex != -1)
		{
			long duration = Long.parseLong(context.getResources().getStringArray(R.array.graph_time_values)[durationIndex]);
			
			if (duration > 0)
			{
				long now = System.currentTimeMillis();
				startTime = now - duration;
			}
		}

		String[] sensors = { SlumberContentProvider.TEMPERATURE, SlumberContentProvider.LIGHT_LEVEL, SlumberContentProvider.AUDIO_MAGNITUDE, SlumberContentProvider.AUDIO_FREQUENCY };
		String[] units = { " °C", " SI lux", " ?", " Hz" };
		
		for (int index = 0; index < sensors.length; index ++)
		{
			String sensor = sensors[index];
			
			try 
			{
				Cursor c = SlumberContentProvider.fetchNormalizedSensorReadings(context, sensor, startTime);
				
				if (c.moveToNext())
				{
					JSONObject sensorObj = new JSONObject();
					sensorObj.put("key", SlumberContentProvider.nameForKey(context, sensor));
					sensorObj.put("color", HomeActivity.colorForKey(context, sensor));
					sensorObj.put("renderer", "custom-bars");
					sensorObj.put("base", c.getDouble(c.getColumnIndex(SlumberContentProvider.READING_MINIMUM)));
					sensorObj.put("multiplier", c.getDouble(c.getColumnIndex(SlumberContentProvider.READING_MULTIPLIER)));
					sensorObj.put("unit", units[index]);

					JSONArray sensorValues = new JSONArray();

					while (c.moveToNext())
					{
						JSONObject reading = new JSONObject();

						reading.put("x", c.getLong(c.getColumnIndex(SlumberContentProvider.READING_RECORDED)));
						reading.put("y", c.getDouble(c.getColumnIndex(SlumberContentProvider.READING_VALUE)));

						sensorValues.put(reading);
					}

					sensorObj.put("values", sensorValues);

					String key = "graph_" + sensor;

					if (includeAll || prefs.getBoolean(key, true) != false)
						values.put(sensorObj);
				}
				
				c.close();
			} 
			catch (JSONException e) 
			{
				LogManager.getInstance(context).logException(e);
			}
		}

		try 
		{
			JSONObject sleep = new JSONObject();
			sleep.put("key", context.getString(R.string.label_sleep_efficiency));
			sleep.put("color", HomeActivity.colorForKey(context, sleep.getString("key")));
			sleep.put("renderer", "scatterplot");
			JSONArray sleepValues = new JSONArray();
			
			String where = SlumberContentProvider.DIARY_TIMESTAMP + " > ?";
			String[] whereArgs = { "" + startTime }; 
			
			Cursor c = context.getContentResolver().query(SlumberContentProvider.SLEEP_DIARIES_URI, null, where, whereArgs, SlumberContentProvider.DIARY_TIMESTAMP);
			
			while (c.moveToNext())
			{
				JSONObject reading = new JSONObject();
				
				reading.put("x", c.getLong(c.getColumnIndex(SlumberContentProvider.DIARY_TIMESTAMP)));
				reading.put("y", SlumberContentProvider.scoreSleep(c, 1));
				
				sleepValues.put(reading);
			}

			c.close();

			sleep.put("base", 0);
			sleep.put("multiplier", 100);
			sleep.put("unit", "%");

			sleep.put("values", sleepValues);

			String key = "graph_" + sleep.getString("key");
			
			if (includeAll || prefs.getBoolean(key, true) != false)
				values.put(sleep);
		} 
		catch (JSONException e) 
		{
			LogManager.getInstance(context).logException(e);
		}

		return values;
	}

	private static String colorForKey(Context context, String sensor) 
	{
		if (context.getString(R.string.label_sleep_efficiency).equals(sensor))
			return "#33B5E5";
		else if (SlumberContentProvider.TEMPERATURE.equals(sensor))
			return "#AA66CC";
		else if (SlumberContentProvider.LIGHT_LEVEL.equals(sensor))
			return "#99CC00";
		else if (SlumberContentProvider.AUDIO_MAGNITUDE.equals(sensor))
			return "#FFBB33";
		else if (SlumberContentProvider.AUDIO_FREQUENCY.equals(sensor))
			return "#FF4444";

		return "#808080";
	}

	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.home, menu);

		return true;
	}

	public boolean onOptionsItemSelected(final MenuItem item)
	{
		final HomeActivity me = this;
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(me);
		
		if (item.getItemId() == R.id.action_settings)
		{
			Intent nativeIntent = new Intent(this, SettingsActivity.class);
			this.startActivity(nativeIntent);
		}
		else if (item.getItemId() == R.id.action_feedback)
			this.sendFeedback(this.getString(R.string.app_name));
		else if (item.getItemId() == R.id.action_chart)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder = builder.setTitle(R.string.action_chart_fields);
			
			builder = builder.setPositiveButton(R.string.button_close, new OnClickListener()
			{
				public void onClick(DialogInterface arg0, int arg1) 
				{
					WebView graphView = (WebView) me.findViewById(R.id.graph_web_view);
					
					graphView.loadDataWithBaseURL("file:///android_asset/", HomeActivity.generateGraph(me), "text/html", null, null);
				}
			});

			builder = builder.setNegativeButton(R.string.button_select_time, new OnClickListener()
			{
				public void onClick(DialogInterface arg0, int arg1) 
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(me);
					builder = builder.setTitle(R.string.action_chart_time);
					
					int selected = prefs.getInt(HomeActivity.SELECTED_TIME_RANGE, -1);
					
					builder.setSingleChoiceItems(R.array.graph_time_intervals, selected, new DialogInterface.OnClickListener() 
					{
						public void onClick(DialogInterface dialog, int which) 
						{
							Editor e = prefs.edit();
							e.putInt(HomeActivity.SELECTED_TIME_RANGE, which);
							e.commit();

							String duration = me.getResources().getStringArray(R.array.graph_time_intervals)[which];
								
							me.getSupportActionBar().setSubtitle(me.getString(R.string.home_subtitle, duration));
						}
					});
					
					builder = builder.setPositiveButton(R.string.button_close, new OnClickListener()
					{
						public void onClick(DialogInterface arg0, int arg1) 
						{
							WebView graphView = (WebView) me.findViewById(R.id.graph_web_view);
							
							graphView.loadDataWithBaseURL("file:///android_asset/", HomeActivity.generateGraph(me), "text/html", null, null);
						}
					});

					builder = builder.setNegativeButton(R.string.action_chart_fields, new OnClickListener()
					{
						public void onClick(DialogInterface arg0, int arg1) 
						{
							me.onOptionsItemSelected(item);
						}
					});

					builder.create().show();
				}
			});

			JSONArray graphValues = HomeActivity.graphValues(this, true);
			
			final String[] values = new String[graphValues.length()];
			final boolean[] checked = new boolean[graphValues.length()];
			
			for (int i = 0; i < graphValues.length(); i++)
			{
				try 
				{
					JSONObject obj = graphValues.getJSONObject(i);

					values[i] = obj.getString("key");

					String key = "graph_" + SlumberContentProvider.keyForName(me, values[i]);
					
					checked[i] = prefs.getBoolean(key, true);
				} 
				catch (JSONException e) 
				{
					LogManager.getInstance(this).logException(e);
				}
			}

			ListView lv = new ListView(this);
			
			ArrayAdapter<String> valuesAdapter = new ArrayAdapter<String>(this, R.layout.row_data_field, values)
			{
				public View getView (int position, View convertView, ViewGroup parent)
				{
					if (convertView == null)
					{
						LayoutInflater inflater = (LayoutInflater) me.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
						convertView = inflater.inflate(R.layout.row_data_field, null);

						final String key = "graph_" + SlumberContentProvider.keyForName(me, values[position]);
						
						CheckedTextView check = (CheckedTextView) convertView.findViewById(R.id.check_text);
						
						check.setChecked(checked[position]);
						
						check.setOnClickListener(new View.OnClickListener()
						{
							public void onClick(View v) 
							{
								CheckedTextView check = (CheckedTextView) v;

								check.setChecked(check.isChecked() == false);
								
								Editor e = prefs.edit();
								e.putBoolean(key, check.isChecked());
								e.commit();
							}
						});
					}

					CheckedTextView check = (CheckedTextView) convertView.findViewById(R.id.check_text);
					check.setText(values[position]);
					
					OvalShape oval = new OvalShape();
					
					ShapeDrawable circle = new ShapeDrawable(oval);
					circle.setIntrinsicHeight(32);
					circle.setIntrinsicWidth(32);
					circle.setBounds(0, 0, 32, 32);
					circle.getPaint().setColor(Color.parseColor(HomeActivity.colorForKey(me, SlumberContentProvider.keyForName(me, values[position]))));
					
					ImageView color = (ImageView) convertView.findViewById(R.id.check_color);
					color.setImageDrawable(circle);
				
					return convertView;
				}
			};
			
			lv.setAdapter(valuesAdapter);
	
			builder = builder.setView(lv);
			
			builder.create().show();
		}

		return true;
	}
}
