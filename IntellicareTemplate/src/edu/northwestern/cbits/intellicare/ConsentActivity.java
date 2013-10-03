package edu.northwestern.cbits.intellicare;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
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
		consentView.setOnBottomReachedListener(this, 40);
		
		LogManager.getInstance(this).log("consent_shown", null);
		
		ImageButton confirmButton = (ImageButton) this.findViewById(R.id.confirm_button);
		confirmButton.setEnabled(false);
		confirmButton.setVisibility(View.GONE);
		
		EditText nameField = (EditText) this.findViewById(R.id.name_field);
		nameField.setEnabled(false);
		nameField.setVisibility(View.GONE);
		
		SimpleDateFormat format = new SimpleDateFormat("EEEE, LLLL d, yyyy");
		
		TextView dateField = (TextView) this.findViewById(R.id.date_text_field); 
		dateField.setText(format.format(new Date()));
		dateField.setVisibility(View.GONE);
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
		final ImageButton confirmButton = (ImageButton) this.findViewById(R.id.confirm_button);
		confirmButton.setVisibility(View.VISIBLE);
		
		final Activity me = this;

		final EditText nameField = (EditText) this.findViewById(R.id.name_field);
		nameField.setEnabled(true);
		nameField.setVisibility(View.VISIBLE);

		TextView dateField = (TextView) this.findViewById(R.id.date_text_field); 
		dateField.setVisibility(View.VISIBLE);
		
		nameField.addTextChangedListener(new TextWatcher()
		{
			public void afterTextChanged(Editable text) 
			{
				if (text.length() > 2)
					confirmButton.setEnabled(true);
				else
					confirmButton.setEnabled(false);
			}
			
			public void beforeTextChanged(CharSequence s, int start, int count, int after) 
			{

			}
			
			public void onTextChanged(CharSequence s, int start, int before, int count) 
			{

			}
		});
		
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
					
					HashMap<String, Object> payload = new HashMap<String, Object>();
					payload.put("consent_name", nameField.getText().toString());
					
					LogManager.getInstance(me).log("consent_given", payload);

					me.finish();
				} 
				catch (FileNotFoundException e) 
				{
					e.printStackTrace();
				}
			}
		});
		
		confirmButton.setEnabled(false);
	}
	
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_consent, menu);

		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.action_cancel)
		{
			Intent result = new Intent();
			result.putExtra(ConsentActivity.CONSENTED, false);
			this.setResult(Activity.RESULT_CANCELED);

			LogManager.getInstance(this).log("consent_rejected", null);

			this.finish();
		}
		
		return true;
	}

}
