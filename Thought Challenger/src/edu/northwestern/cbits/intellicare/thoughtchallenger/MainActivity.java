package edu.northwestern.cbits.intellicare.thoughtchallenger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.JSONArray;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.widget.Button;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class MainActivity extends ConsentedActivity 
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_main);
		
		final MainActivity me = this;
		
		Button challenge = (Button) this.findViewById(R.id.button_challenge);
		
		challenge.setOnClickListener(new OnClickListener()
		{
			public void onClick(View arg0) 
			{
				Intent intent = new Intent(me, CatchActivity.class);
				
				me.startActivity(intent);
			}
		});

		Button review = (Button) this.findViewById(R.id.button_review);
		
		review.setOnClickListener(new OnClickListener()
		{
			public void onClick(View arg0) 
			{
				Cursor c = me.getContentResolver().query(ThoughtContentProvider.THOUGHT_PAIR_URI, null, null, null, null);
				
				if (c.getCount() > 0)
				{
					Intent intent = new Intent(me, ReviewActivity.class);
					
					me.startActivity(intent);
				}
				else
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(me);
					builder.setTitle(R.string.title_challenges_needed);
					builder.setMessage(R.string.message_challenges_needed);
					
					builder.setPositiveButton(R.string.action_challenge, new DialogInterface.OnClickListener() 
					{
						public void onClick(DialogInterface dialog, int which) 
						{
							Intent intent = new Intent(me, CatchActivity.class);
							me.startActivity(intent);
						}
					});

					builder.setNegativeButton(R.string.action_close, new DialogInterface.OnClickListener() 
					{
						public void onClick(DialogInterface dialog, int which) 
						{

						}
					});
					
					builder.create().show();
				}
				
				c.close();
			}
		});
	}
	
	@SuppressLint("SetJavaScriptEnabled")
	protected void onResume()
	{
		super.onResume();
		
		WebView cloud = (WebView) this.findViewById(R.id.cloud_view);
		cloud.getSettings().setJavaScriptEnabled(true);
		cloud.setVerticalScrollBarEnabled(false);
		cloud.setHorizontalScrollBarEnabled(false);

		cloud.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);

		//To disabled the horizontal and vertical scrolling:
		cloud.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View arg0, MotionEvent arg1) 
			{
				return false;
			}
		});
		cloud.loadDataWithBaseURL("file:///android_asset/www/", this.generatePage(), "text/html", null, null);
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
		
		JSONArray wordsList = ThoughtContentProvider.positiveWordArray(this);

		graphString = graphString.replaceAll("WORDS_LIST", wordsList.toString());
		
		return graphString;
	}
	
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item)
	{
		int itemId = item.getItemId();
		
		switch (itemId)
		{
			case R.id.action_settings:
				Intent settingsIntent = new Intent(this, SettingsActivity.class);
				this.startActivity(settingsIntent);
				
				break;
			case R.id.action_feedback:
				this.sendFeedback(this.getString(R.string.app_name));
					
				break;
		}
		
		return true;
	}

}
