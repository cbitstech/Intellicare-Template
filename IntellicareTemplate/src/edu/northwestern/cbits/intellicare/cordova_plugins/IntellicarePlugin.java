package edu.northwestern.cbits.intellicare.cordova_plugins;

import org.apache.cordova.api.Plugin;
import org.apache.cordova.api.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.support.v7.app.ActionBarActivity;

@SuppressWarnings("deprecation")
public class IntellicarePlugin extends Plugin 
{
	public static final String SET_TITLE = "setTitle";
	public static final String SET_SUBTITLE = "setSubtitle";

	public PluginResult execute(String action, final JSONArray args, String callbackId) 
	{
		if (SET_TITLE.equals(action))
		{
			try 
			{
				final String titleString = args.getString(0);

				Context context = this.webView.getContext();
				
				if (context instanceof ActionBarActivity)
				{
					final ActionBarActivity activity = (ActionBarActivity) context;
					
					activity.runOnUiThread(new Runnable()
					{
						public void run() 
						{
							activity.getSupportActionBar().setTitle(titleString);
						}
					});
				}
				
				return new PluginResult(PluginResult.Status.OK, "Title set.");
			} 
			catch (JSONException e) 
			{
				e.printStackTrace();
				
				return new PluginResult(PluginResult.Status.ERROR, "JSON error encountered.");
			}
		}
		else if (SET_SUBTITLE.equals(action))
		{
			try 
			{
				final String titleString = args.getString(0);

				Context context = this.webView.getContext();
				
				if (context instanceof ActionBarActivity)
				{
					final ActionBarActivity activity = (ActionBarActivity) context;
					
					activity.runOnUiThread(new Runnable()
					{
						public void run() 
						{
							activity.getSupportActionBar().setSubtitle(titleString);
						}
					});
				}
				
				return new PluginResult(PluginResult.Status.OK, "Subtitle set.");
			} 
			catch (JSONException e) 
			{
				e.printStackTrace();
				
				return new PluginResult(PluginResult.Status.ERROR, "JSON error encountered.");
			}
		}
		
		return null;
	}

}
