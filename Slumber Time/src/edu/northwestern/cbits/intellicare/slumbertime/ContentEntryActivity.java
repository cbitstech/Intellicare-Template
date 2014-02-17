package edu.northwestern.cbits.intellicare.slumbertime;
import java.util.List;

import org.markdownj.MarkdownProcessor;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;


public class ContentEntryActivity extends ConsentedActivity
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_content);
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
			
			Log.e("ST", "HTML: " + html);
			
			webView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", null, null);
		}
		
		c.close();
	}
}
