package edu.northwestern.cbits.intellicare.dailyfeats;

import android.os.Bundle;
import android.webkit.WebView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

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
}
