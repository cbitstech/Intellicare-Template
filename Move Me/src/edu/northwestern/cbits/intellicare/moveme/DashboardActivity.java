package edu.northwestern.cbits.intellicare.moveme;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class DashboardActivity extends ConsentedActivity 
{
    protected static final String WEEKLY_GOAL = "setting_weekly_goal";

	protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_dashboard);
        
        ActionBar actionBar = this.getSupportActionBar();
        
        actionBar.setTitle(R.string.title_dashboard);
        
        this.refreshToday();
        this.refreshWeek();
    }
    
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        this.getMenuInflater().inflate(R.menu.menu_dashboard, menu);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	if (item.getItemId() == R.id.action_log)
    	{
    		Intent intent = new Intent(this, LogActivity.class);
    		
    		this.startActivity(intent);
    		
    		return true;
    	}

        return super.onOptionsItemSelected(item);
    }
    
    @SuppressLint({ "SetJavaScriptEnabled", "JavascriptInterface" })
	private void refreshToday() 
    {
		WebView graphView = (WebView) this.findViewById(R.id.view_today);
		graphView.getSettings().setJavaScriptEnabled(true);
		graphView.getSettings().setBuiltInZoomControls(false);
		graphView.getSettings().setDisplayZoomControls(false);
		graphView.getSettings().setLoadWithOverviewMode(true);
		graphView.getSettings().setUseWideViewPort(true);
		graphView.setInitialScale(1);
		
		graphView.addJavascriptInterface(this, "android");
		// graphView.loadUrl("file:///android_asset/viz/view_today.html");
		graphView.loadDataWithBaseURL("file:///android_asset/viz/", DashboardActivity.generateGraph(this), "text/html", null, null);
	}

    @SuppressLint({ "SetJavaScriptEnabled", "JavascriptInterface" })
	private void refreshWeek() 
    {
		WebView graphView = (WebView) this.findViewById(R.id.view_week);
		graphView.getSettings().setJavaScriptEnabled(true);
		graphView.getSettings().setBuiltInZoomControls(false);
		graphView.getSettings().setDisplayZoomControls(false);
		graphView.getSettings().setLoadWithOverviewMode(true);
		graphView.getSettings().setUseWideViewPort(true);
		graphView.setInitialScale(1);
		
		graphView.addJavascriptInterface(this, "android");
		// graphView.loadUrl("file:///android_asset/viz/view_today.html");
		graphView.loadDataWithBaseURL("file:///android_asset/viz/", DashboardActivity.generateWeekGraph(this), "text/html", null, null);
	}
    
	private static String generateGraph(Context context) 
	{
	    StringBuilder buffer = new StringBuilder();
	    
		try 
		{
		    InputStream html = context.getAssets().open("viz/view_today.html");

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
		
		JSONArray graphValues = DashboardActivity.todayGraphValues(context);

		graphString = graphString.replaceAll("VALUES_JSON", graphValues.toString());
		
		return graphString;
	}

	private static String generateWeekGraph(Context context) 
	{
	    StringBuilder buffer = new StringBuilder();
	    
		try 
		{
		    InputStream html = context.getAssets().open("viz/view_week.html");

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
		
		JSONArray graphValues = DashboardActivity.weekGraphValues(context);
		
		try {
			Log.e("MM", "WEEK JSON: " + graphValues.toString(2));
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try 
		{
			graphString = graphString.replaceAll("VALUES_JSON", graphValues.toString());

			FileUtils.writeStringToFile(new File(Environment.getExternalStorageDirectory(), "graph.html"), graphString);
		} 
		catch (IOException e) 
		{
			LogManager.getInstance(context).logException(e);
		} 
		
		return graphString;
	}

	private static JSONArray todayGraphValues(Context context) 
	{
		JSONArray values = new JSONArray();
		
		long now = System.currentTimeMillis();

		int total = MoveProvider.goal(context, now) * 2;
		int today = MoveProvider.progress(context, now);
		
		try 
		{
			if (today > 0)
			{
				JSONObject todayObj = new JSONObject();
				todayObj.put("value", today);
				
				values.put(todayObj);
			}
			
			int remaining = total - today;
			
			if (remaining < 0)
				remaining = 0;
			
			if (remaining > 0)
			{
				JSONObject totalObj = new JSONObject();
				totalObj.put("value", remaining);
				
				values.put(totalObj);
			}
		} 
		catch (JSONException e) 
		{
			LogManager.getInstance(context);
		}

		return values;
	}
	
	private static JSONArray weekGraphValues(Context context)  
	{
		JSONArray series = new JSONArray();
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 12);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		int offset = cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET);
		
		Log.e("MM", "OFFSET: " + offset);
		
		try 
		{
			JSONObject completed = new JSONObject();
			completed.put("color", "#669900");
			completed.put("data", new JSONArray());

			JSONObject remaining = new JSONObject();
			remaining.put("color", "#CC0000");
			remaining.put("data", new JSONArray());
			
			cal.add(Calendar.DATE, -7);

			for (int i = 0; i < 7; i++)
			{
				long time = cal.getTimeInMillis();
				
				int complete = MoveProvider.progress(context, time);

				JSONObject completePoint = new JSONObject();
				completePoint.put("x", (time + offset) / 1000);
				completePoint.put("y", complete);
				
				completed.getJSONArray("data").put(completePoint);
				
				int goal = MoveProvider.goal(context, time);
				
				int left = goal - complete;
				
				if (left < 0)
					left = 0;
				
				JSONObject leftPoint = new JSONObject();
				leftPoint.put("x", (time + offset) / 1000);
				leftPoint.put("y", left);
				
				remaining.getJSONArray("data").put(leftPoint);
				
				cal.add(Calendar.DATE, 1);
			}
			
			series.put(completed);
			series.put(remaining);
		} 
		catch (JSONException e) 
		{
			LogManager.getInstance(context).logException(e);
		}
		
		return series;
	}
}
