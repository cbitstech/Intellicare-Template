package edu.northwestern.cbits.intellicare.moveme;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class LessonActivity extends ConsentedActivity 
{
	public static final String TITLE = "title";
	public static final String URL = "url";
	
	protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_lesson);
        
        Intent intent = this.getIntent();
        
        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setTitle(intent.getStringExtra(LessonActivity.TITLE));
        
        WebView webView = (WebView) this.findViewById(R.id.web_view);
        webView.loadUrl("file:///android_asset/www/" + intent.getStringExtra(LessonActivity.URL));
    }

    public boolean onCreateOptionsMenu(Menu menu) 
    {
        this.getMenuInflater().inflate(R.menu.menu_lesson, menu);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	if (item.getItemId() == R.id.action_close)
    		this.finish();

    	return true;
    }
}
