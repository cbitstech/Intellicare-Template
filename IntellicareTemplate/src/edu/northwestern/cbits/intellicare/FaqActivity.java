package edu.northwestern.cbits.intellicare;

import java.io.IOException;
import java.io.InputStream;

import edu.northwestern.cbits.ic_template.R;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

public class FaqActivity extends ConsentedActivity 
{
	static final String APP_NAME = "app_name";

	@SuppressLint("SimpleDateFormat")
	public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        this.setContentView(R.layout.activity_faq);
        
        this.getSupportActionBar().setTitle(R.string.title_faq);
        this.getSupportActionBar().setSubtitle(this.getIntent().getStringExtra(FaqActivity.APP_NAME));
        
        AssetManager assets = this.getAssets();
        
        try
        {
        	InputStream in = assets.open("www/faq.html");
        	
        	in.close();

            WebView webView = (WebView) this.findViewById(R.id.web_view);
        	webView.loadUrl("file:///android_asset/www/faq.html");
        }
        catch (IOException e)
        {
        	final FaqActivity me = this;
        	
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	builder.setTitle(R.string.title_missing_faq);
        	builder.setMessage(R.string.message_missing_faq);
        	
        	builder.setPositiveButton(R.string.action_close, new OnClickListener()
        	{
				public void onClick(DialogInterface arg0, int arg1) 
				{

				}
        	});
        	
        	builder.setOnDismissListener(new OnDismissListener()
        	{
				public void onDismiss(DialogInterface arg0)
				{
					me.finish();
				}
        	});
        	
        	builder.create().show();
        }
    }
	
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_faq, menu);

		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.action_cancel)
			this.finish();
		
		return true;
	}
	
}
