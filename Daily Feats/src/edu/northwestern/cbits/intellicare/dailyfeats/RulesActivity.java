package edu.northwestern.cbits.intellicare.dailyfeats;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class RulesActivity extends ConsentedActivity 
{
	public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        this.setContentView(R.layout.activity_rules);
        
        WebView webView = (WebView) this.findViewById(R.id.web_view);

        webView.loadUrl("file:///android_asset/rules.html");
        
        this.getSupportActionBar().setTitle(R.string.title_rules);
    }
	
	public void onResume()
	{
		super.onResume();
		
		LogManager.getInstance(this).log("opened_rules", null);
	}
	
	public void onPause()
	{
		LogManager.getInstance(this).log("closed_rules", null);
		
		super.onPause();
	}
	
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_rules, menu);

		return true;
	}
	
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int itemId = item.getItemId();
		
		if (itemId == R.id.action_cancel)
			this.finish();
		
		return true;
	}
}
