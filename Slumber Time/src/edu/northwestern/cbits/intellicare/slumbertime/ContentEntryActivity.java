package edu.northwestern.cbits.intellicare.slumbertime;
import java.util.HashMap;
import java.util.List;

import org.markdownj.MarkdownProcessor;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;


public class ContentEntryActivity extends ConsentedActivity
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_content);

		final ContentEntryActivity me = this;
		
		WebView webView = (WebView) this.findViewById(R.id.web_view);
		
		webView.setWebViewClient(new WebViewClient()
		{
			public boolean shouldOverrideUrlLoading (WebView view, String url)
			{
				Uri u = Uri.parse(url);
				
				Intent intent = new Intent(Intent.ACTION_VIEW, u);
				me.startActivity(intent);
				
				return true;
			}
		});
	}
	
	protected void onResume()
	{
		super.onResume();
		
		Uri uri = this.getIntent().getData();
		
		List<String> pathComponents = uri.getPathSegments();
		
		String slug = pathComponents.get(pathComponents.size() - 1);
		
		String selection = EntriesContentProvider.SLUG + " = ?";
		String[] args = { slug };
		
		Cursor c = this.getContentResolver().query(EntriesContentProvider.CONTENT_URI, null, selection, args, null);
		
		if (c.moveToNext())
		{
			this.getSupportActionBar().setTitle(c.getString(c.getColumnIndex(EntriesContentProvider.TITLE)));
			
			String body = c.getString(c.getColumnIndex(EntriesContentProvider.TEXT));
			
			WebView webView = (WebView) this.findViewById(R.id.web_view);
			
			MarkdownProcessor markdown = new MarkdownProcessor();
			
			String html = markdown.markdown(body);
			
			webView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", null, null);
		}
		
		c.close();

		HashMap<String, Object> payload = new HashMap<String, Object>();
		payload.put("slug", slug);
		
		LogManager.getInstance(this).log("launched_content_activity", payload);
	}
	
	protected void onPause()
	{
		HashMap<String, Object> payload = new HashMap<String, Object>();
		LogManager.getInstance(this).log("closed_content_activity", payload);
	}
}
