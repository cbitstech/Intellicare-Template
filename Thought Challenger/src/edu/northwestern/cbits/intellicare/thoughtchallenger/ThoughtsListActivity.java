package edu.northwestern.cbits.intellicare.thoughtchallenger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.JSONArray;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class ThoughtsListActivity extends ConsentedActivity 
{
	private static final String SELECTED_ITEMS = "thoughts_selected_items";

	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_list);
		
		this.getSupportActionBar().setTitle(R.string.title_thoughts_list);
		
		Cursor c = this.getContentResolver().query(ThoughtContentProvider.THOUGHT_PAIR_URI, null, null, null, null);
		
		if (c.getCount() == 1)
			this.getSupportActionBar().setSubtitle(R.string.subtitle_thoughts_single);
		else
			this.getSupportActionBar().setSubtitle(this.getString(R.string.subtitle_thoughts, c.getCount()));

		c.close();
		
		this.refreshList();
	}
	
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_list, menu);

		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int itemId = item.getItemId();
		
		final ThoughtsListActivity me = this;
		
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		switch (itemId)
		{
			case R.id.action_close:
				this.finish();
				break;
			case R.id.action_display:
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.title_display_options);
				
				String[] options = this.getResources().getStringArray(R.array.list_display_options);
				
				builder.setSingleChoiceItems(options, prefs.getInt(ThoughtsListActivity.SELECTED_ITEMS, 0), new OnClickListener()
				{
					public void onClick(DialogInterface arg0, int which) 
					{
						Editor e = prefs.edit();
						e.putInt(ThoughtsListActivity.SELECTED_ITEMS, which);
						e.commit();
						
						me.refreshList();
					}
				});
				
				builder.setPositiveButton(R.string.action_close, new OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which) 
					{

					}
				});
				
				builder.create().show();

				break;
		}
		
		
		return true;
	}

	@SuppressLint("SetJavaScriptEnabled")
	@SuppressWarnings("deprecation")
	protected void refreshList() 
	{
		Display display = this.getWindowManager().getDefaultDisplay();

		ListView listView = (ListView) this.findViewById(R.id.list_view);
		WebView cloud = (WebView) this.findViewById(R.id.cloud_view);

		if (display.getWidth() > display.getHeight())
		{
			listView.setVisibility(View.GONE);
			cloud.setVisibility(View.VISIBLE);

			cloud.getSettings().setJavaScriptEnabled(true);
			cloud.setVerticalScrollBarEnabled(false);
			cloud.setHorizontalScrollBarEnabled(false);

			cloud.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);

			//To disabled the horizontal and vertical scrolling:
			cloud.setOnTouchListener(new View.OnTouchListener() 
			{
				public boolean onTouch(View arg0, MotionEvent arg1) 
				{
					return false;
				}
			});
			
			cloud.loadDataWithBaseURL("file:///android_asset/www/", this.generatePage(), "text/html", null, null);
		}
		else
		{
			listView.setVisibility(View.VISIBLE);
			cloud.setVisibility(View.GONE);

			Cursor c = this.getContentResolver().query(ThoughtContentProvider.THOUGHT_PAIR_URI, null, null, null, ThoughtContentProvider.ID);
	
			String[] from = { ThoughtContentProvider.PAIR_AUTOMATIC_THOUGHT, ThoughtContentProvider.PAIR_RATIONAL_RESPONSE };
			int[] to = { R.id.label_thought, R.id.label_response };
	
			final ThoughtsListActivity me = this;
	
			SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.row_pair, c, from, to, 0)
			{
				public void bindView (View view, Context context, Cursor cursor)
				{
					super.bindView(view, context, cursor);
					
					TextView thought = (TextView) view.findViewById(R.id.label_thought);
					TextView response = (TextView) view.findViewById(R.id.label_response);
	
					thought.setVisibility(View.VISIBLE);
					response.setVisibility(View.VISIBLE);
	
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(me);
					
					switch (prefs.getInt(ThoughtsListActivity.SELECTED_ITEMS, 0))
					{
						case 1:
							response.setVisibility(View.GONE);
							break;
						case 2:
							thought.setVisibility(View.GONE);
							break;
					}
				}
			};
			
			listView.setAdapter(adapter);
			
			listView.setOnItemClickListener(new OnItemClickListener()
			{
				public void onItemClick(AdapterView<?> arg0, View view, int which, long id) 
				{
					Intent data = new Intent();
					data.putExtra(ReviewActivity.THOUGHT_ID, id);
					
					me.setResult(Activity.RESULT_OK, data);
					
					me.finish();
				}
			});
		}
	}

	private String generatePage() 
	{
	    StringBuilder buffer = new StringBuilder();
	    
		try 
		{
		    InputStream html = this.getAssets().open("www/cloud.html");

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
			LogManager.getInstance(this).logException(e);
		}

		String graphString = buffer.toString();
		
		JSONArray wordsList = new JSONArray();
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		switch (prefs.getInt(ThoughtsListActivity.SELECTED_ITEMS, 0))
		{
			case 0: 
				wordsList = ThoughtContentProvider.fullWordArray(this);
				break;
			case 1: 
				wordsList = ThoughtContentProvider.negativeWordArray(this);
				break;
			case 2: 
				wordsList = ThoughtContentProvider.positiveWordArray(this);
				break;
		}

		

		graphString = graphString.replaceAll("WORDS_LIST", wordsList.toString());
		
		return graphString;
	}
}
