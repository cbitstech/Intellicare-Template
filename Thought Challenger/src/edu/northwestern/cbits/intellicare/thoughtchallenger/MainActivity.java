package edu.northwestern.cbits.intellicare.thoughtchallenger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.JSONArray;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class MainActivity extends ConsentedActivity 
{
	@SuppressLint("SetJavaScriptEnabled")
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
			}
		});
		
		WebView cloud = (WebView) this.findViewById(R.id.cloud_view);
		cloud.getSettings().setJavaScriptEnabled(true);
		
		cloud.loadDataWithBaseURL("file:///android_asset/www/", this.generatePage(), "text/html", null, null);

//		cloud.loadUrl("file:///android_asset/www/cloud.html");
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
		Log.e("GS", "GRAPH: " + graphString);
		
		return graphString;
	}
}
