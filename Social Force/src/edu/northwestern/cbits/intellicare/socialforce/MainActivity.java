package edu.northwestern.cbits.intellicare.socialforce;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class MainActivity extends ConsentedActivity 
{
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        
        this.getSupportActionBar().setSubtitle("lIst NExT ConTACT EvEnT HeRE");
        
        final MainActivity me = this;
        
        View network = this.findViewById(R.id.network_visualization);
        
        network.setOnClickListener(new OnClickListener()
        {
			public void onClick(View arg0) 
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(me);
				
				builder.setTitle("SeLeCt sUppORTeR");
				
				List<ContactRecord> contacts = ContactCalibrationHelper.fetchContactRecords(me);
				
				final List<ContactRecord> positives = new ArrayList<ContactRecord>();
				
				for (ContactRecord record : contacts)
				{
					if (record.level >= 0 && record.level < 3)
						positives.add(record);
				}
				
				Collections.sort(positives, new Comparator<ContactRecord>()
				{
					public int compare(ContactRecord one, ContactRecord two) 
					{
						if (one.level < two.level)
							return -1;
						else if (one.level > two.level)
							return 1;
						
						return one.name.compareTo(two.name);
					}
				});

				String[] names = new String[positives.size()];
				
				for (int i = 0; i < positives.size(); i++)
				{
					ContactRecord record = positives.get(i);
					
					names[i] = record.name;
				}
				
				builder.setItems(names, new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						Intent intent = new Intent(me, ScheduleActivity.class);
						intent.putExtra(ScheduleActivity.CONTACT_KEY, positives.get(which).key);
						
						me.startActivity(intent);
					}
				});
				
				builder.create().show();
			}
        });
    }
    
    protected void onResume()
    {
    	super.onResume();
    	
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        if (prefs.getBoolean(IntroActivity.INTRO_SHOWN, false) == false)
        {
	        Intent introIntent = new Intent(this, IntroActivity.class);
	        this.startActivity(introIntent);
        }
        else if (prefs.getBoolean(RatingActivity.CONTACTS_RATED, false) == false)
        {
	        Intent introIntent = new Intent(this, RatingActivity.class);
	        this.startActivity(introIntent);
        }
        
        this.refreshBubbles();
    }

    @SuppressLint("SetJavaScriptEnabled")
	private void refreshBubbles() 
    {
		WebView graphView = (WebView) this.findViewById(R.id.network_visualization);
		graphView.getSettings().setJavaScriptEnabled(true);
		graphView.getSettings().setBuiltInZoomControls(true);
		graphView.getSettings().setLoadWithOverviewMode(true);
		graphView.getSettings().setUseWideViewPort(true);
		graphView.setInitialScale(1);
		
		graphView.loadDataWithBaseURL("file:///android_asset/viz/", MainActivity.generateBubbles(this), "text/html", null, null);
	}

	private static String generateBubbles(Context context) 
	{
	    StringBuilder buffer = new StringBuilder();
	    
		try 
		{
		    InputStream html = context.getAssets().open("viz/home_bubbles.html");

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

		try 
		{
			JSONObject graphValues = MainActivity.bubbleValues(context);

			graphString = graphString.replaceAll("VALUES_JSON", graphValues.toString());
		} 
		catch (JSONException e) 
		{
			LogManager.getInstance(context).logException(e);
		} 

		Log.e("SF", "RET: " + graphString);
		
		return graphString;
	}

	private static JSONObject bubbleValues(Context context) throws JSONException 
	{
		/*
		 * 
		 * var root = {
  "name": "contacts",
  "children": [
    {"name": "AgglomerativeCluster", "size": 3938},
    {"name": "CommunityStructure", "size": 3812},
    {"name": "HierarchicalCluster", "size": 3938},
    {"name": "MergeEdge", "size": 3938}
  ]
};
		 */
		
		JSONObject bubbles = new JSONObject();

		bubbles.put("name", "contacts");
		
		JSONArray children = new JSONArray();

		List<ContactRecord> contacts = ContactCalibrationHelper.fetchContactRecords(context);
		
		for (ContactRecord contact : contacts)
		{
			if (contact.level <= 2 && contact.level >= 0 && contact.name != null && contact.name.trim().length() > 0)
			{
				JSONObject bubble = new JSONObject();
				bubble.put("name", contact.name);
				bubble.put("size", contact.count);
				
				if (ContactCalibrationHelper.isAdvice(context, contact))
				{
					Log.e("SF", "ADVICE");
					bubble.put("color", "#0099CC");
				}
				else if (ContactCalibrationHelper.isCompanion(context, contact))
				{
					Log.e("SF", "COMPANION");
					bubble.put("color", "#9933CC");
				}
				else if (ContactCalibrationHelper.isEmotional(context, contact))
				{
					Log.e("SF", "EMOTIONAL");
					bubble.put("color", "#CC0000");
				}
				else if (ContactCalibrationHelper.isPractical(context, contact))
				{
					Log.e("SF", "PRACTICAL");
					bubble.put("color", "#669900");
				}
				else
				{
					Log.e("SF", "DEFAULT");
					bubble.put("color", "#808080");
				}
				
				children.put(bubble);
			}
		}
		
		bubbles.put("children", children);
		
		return bubbles;
	}

	public boolean onCreateOptionsMenu(Menu menu) 
    {
        this.getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	int itemId = item.getItemId();
    	
		if (itemId == R.id.action_settings)
		{
//			Intent settingsIntent = new Intent(this, SettingsActivity.class);
//			this.startActivity(settingsIntent);
		}
		else if (itemId == R.id.action_pleasure)
		{
			Intent intent = new Intent(this, EventReviewActivity.class);
			this.startActivity(intent);
		}
		else if (itemId == R.id.action_rating)
		{
			Intent intent = new Intent(this, RatingActivity.class);
			this.startActivity(intent);
		}
		else if (itemId == R.id.action_network)
		{
			Intent intent = new Intent(this, NetworkActivity.class);
			this.startActivity(intent);
		}
		else if (item.getItemId() == R.id.action_feedback)
			this.sendFeedback(this.getString(R.string.app_name));
		else if (item.getItemId() == R.id.action_faq)
			this.showFaq(this.getString(R.string.app_name));

        return super.onOptionsItemSelected(item);
    }
}
