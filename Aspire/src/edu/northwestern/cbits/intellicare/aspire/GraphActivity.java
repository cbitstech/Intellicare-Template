package edu.northwestern.cbits.intellicare.aspire;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class GraphActivity extends ConsentedActivity 
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_graph);
	}

	@SuppressLint("SetJavaScriptEnabled")
	protected void onResume()
	{
		super.onResume();
		
		WebView graphView = (WebView) this.findViewById(R.id.graph_web_view);
		graphView.getSettings().setJavaScriptEnabled(true);
		
		graphView.loadDataWithBaseURL("file:///android_asset/", GraphActivity.generateGraph(this), "text/html", null, null);
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
		
		try 
		{
			JSONArray graphValues = GraphActivity.graphValues(context, false);

			graphString = graphString.replaceAll("VALUES_JSON", graphValues.toString());
		}
		catch (JSONException e) 
		{
			LogManager.getInstance(context).logException(e);
		}

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
	
	private static JSONArray graphValues(Context context, boolean includeAll) throws JSONException 
	{
		ArrayList<String> cards = new ArrayList<String>();
		ArrayList<Long> cardIds = new ArrayList<Long>();
		
		Cursor c = context.getContentResolver().query(AspireContentProvider.ASPIRE_PATH_URI, null, null, null, null);
		
		while (c.moveToNext())
		{
			String where = AspireContentProvider.ID + " = ?";
			String[] args = {"" + c.getLong(c.getColumnIndex(AspireContentProvider.PATH_CARD_ID)) };
			
			Cursor cardCursor = context.getContentResolver().query(AspireContentProvider.ASPIRE_CARD_URI, null, where, args, null);
			
			if (cardCursor.moveToNext())
			{
				String cardName = cardCursor.getString(cardCursor.getColumnIndex(AspireContentProvider.CARD_NAME));
				
				if (cards.contains(cardName) == false)
				{
					cards.add(cardName);
					cardIds.add(cardCursor.getLong(cardCursor.getColumnIndex(AspireContentProvider.ID)));
				}
			}
			
			cardCursor.close();
		}
		
		c.close();
		
		JSONArray values = new JSONArray();
		
		String[] colorWheel = { "#0099CC", "#9933CC", "#669900", "#FF8800", "#CC0000" };

		long day = 1000 * 60 * 60 * 24;
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		long start = calendar.getTimeInMillis() - (day * 6);

		for (int j = 0; j < cards.size(); j++)
		{
			String cardName = cards.get(j);
			long cardId = cardIds.get(j);
			
			JSONObject dataObj = new JSONObject();
			dataObj.put("color", colorWheel[j % colorWheel.length]);
			dataObj.put("name", cardName);
			
			JSONArray points = new JSONArray();
			
			for (int i = 0; i < 7; i++)
			{
				long cellStart = start + (day * j);
				long cellEnd = start + (day * (j + 1));
				
				long cellMid = (cellStart + cellEnd) / 2;
				
				JSONObject point = new JSONObject();
				
				point.put("x", cellMid / 1000);
				point.put("y", i % 2);  // TODO: Query
				
				points.put(point);
			}
			
			dataObj.put("data", points);
			
			values.put(dataObj);
		}
		
		/*
		 * 		{
			data: [ { x: 0, y: 40 }, { x: 1, y: 49 }, { x: 2, y: 38 }, { x: 3, y: 30 }, { x: 4, y: 32 } ],
			color: '#4682b4'
		}, {
			data: [ { x: 0, y: 20 }, { x: 1, y: 24 }, { x: 2, y: 19 }, { x: 3, y: 15 }, { x: 4, y: 16 } ],
			color: '#9cc1e0'

		 */

		Log.e("AS", "RETURNING " + values.toString(2));

		return values;
	}
}
