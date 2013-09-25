package edu.northwestern.cbits.intellicare;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import edu.northwestern.cbits.ic_template.R;
import edu.northwestern.cbits.intellicare.logging.LogManager;
import edu.northwestern.cbits.intellicare.views.ConsentWebView;

public class ConsentActivity extends ActionBarActivity implements ConsentWebView.OnBottomReachedListener 
{
	public static final String FORM_URL = "FORM_URL";
	public static final String DEFAULT_FORM_URL = "file:///android_asset/www/default_consent_form.html";
	protected static final String CONSENTED = "CONSENTED";
	
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_consent);
		
		this.getSupportActionBar().setTitle(R.string.title_consent);
		
		ConsentWebView consentView = (ConsentWebView) this.findViewById(R.id.consent_web_view);
		
		String formUrl = this.getIntent().getStringExtra(ConsentActivity.FORM_URL);
		
		if (formUrl == null)
			formUrl = ConsentActivity.DEFAULT_FORM_URL;
		
		consentView.loadUrl(formUrl);

		consentView.setVerticalScrollBarEnabled(true);
		consentView.setOnBottomReachedListener(this, 10);

		Button quitButton = (Button) this.findViewById(R.id.quit_button);
		quitButton.setEnabled(true);
		
		final Activity me = this;
		
		quitButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View view) 
			{
				Intent result = new Intent();
				result.putExtra(ConsentActivity.CONSENTED, false);
				me.setResult(Activity.RESULT_CANCELED);

				LogManager.getInstance(me).log("consent_rejected", null);

				me.finish();
			}
		});
		
		LogManager.getInstance(this).log("consent_shown", null);
	}
	
	public static boolean isConsented()
	{
		File root = Environment.getExternalStorageDirectory();
		File intellicare = new File(root, "Intellicare Shared");

		if (intellicare.exists() == false)
			intellicare.mkdirs();
		
		File consent = new File(intellicare, "Consent Record.txt");
		
		return consent.exists();
	}

	public void onBottomReached(View v) 
	{
		Button confirmButton = (Button) this.findViewById(R.id.confirm_button);
		
		final Activity me = this;
		
		confirmButton.setOnClickListener(new OnClickListener()
		{
			@SuppressLint("SimpleDateFormat")
			public void onClick(View view) 
			{
				File root = Environment.getExternalStorageDirectory();
				
				File intellicare = new File(root, "Intellicare Shared");
				
				if (intellicare.exists() == false)
					intellicare.mkdirs();
				
				File consent = new File(intellicare, "Consent Record.txt");
				
				try 
				{
					TimeZone tz = TimeZone.getTimeZone("UTC");

					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
					df.setTimeZone(tz);
					
					PrintWriter out = new PrintWriter(consent);
					out.print(df.format(new Date()));
					out.close();

					Intent result = new Intent();
					result.putExtra(ConsentActivity.CONSENTED, true);
					me.setResult(Activity.RESULT_OK);
					
					LogManager.getInstance(me).log("consent_given", null);

					me.finish();
				} 
				catch (FileNotFoundException e) 
				{
					e.printStackTrace();
				}
			}
		});
		
		confirmButton.setEnabled(true);
	}
}
