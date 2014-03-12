package edu.northwestern.cbits.intellicare.avast;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class WelcomeActivity extends ConsentedActivity 
{
	public static final String INTRO_SHOWN = "intro_shown";

    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.activity_welcome);
        
        WebView webView = (WebView) this.findViewById(R.id.web_view);
        
        webView.loadUrl("file:///android_asset/www/welcome.html");
    }
    
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_welcome, menu);

		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int itemId = item.getItemId();
		
		switch (itemId)
		{
			case R.id.action_next:
				Intent venueIntent = new Intent(this, VenueTypeActivity.class);
				
				this.startActivity(venueIntent);
				
				break;
		}
		
		return true;
	}
}
