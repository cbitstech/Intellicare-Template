package edu.northwestern.cbits.intellicare.moveme;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

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
		graphView.loadUrl("file:///android_asset/viz/view_today.html");
		// graphView.loadDataWithBaseURL("file:///android_asset/viz/", MainActivity.generateBubbles(this, this._state), "text/html", null, null);
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
		graphView.loadUrl("file:///android_asset/viz/view_today.html");
		// graphView.loadDataWithBaseURL("file:///android_asset/viz/", MainActivity.generateBubbles(this, this._state), "text/html", null, null);
	}
}
