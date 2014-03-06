package edu.northwestern.cbits.intellicare;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
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
	protected static final String REASON = "REASON";
	protected static final String NAME = "consent_form_name";
	private boolean _isReview = false;
	
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_consent);
		
		this.getSupportActionBar().setTitle(R.string.title_consent);
		this.getSupportActionBar().setSubtitle(R.string.subtitle_consent);
		
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
		
		SimpleDateFormat format = new SimpleDateFormat("EEEE, MMMM d, yyyy");
		
		TextView dateField = (TextView) this.findViewById(R.id.date_text_field); 
		dateField.setText(format.format(new Date()));
		dateField.setVisibility(View.GONE);
	}
	
	public void onResume()
	{
		super.onResume();
		
		Uri data = this.getIntent().getData();
		
		if (data != null)
			this._isReview = data.toString().endsWith("review=true");
		else
			this._isReview = false;

		View signature = this.findViewById(R.id.view_signature);

		if (this._isReview)
		{
			signature.setVisibility(View.GONE);

			File root = Environment.getExternalStorageDirectory();
			File intellicare = new File(root, "Intellicare Shared");

			if (intellicare.exists() == false)
				intellicare.mkdirs();
			
			File consent = new File(intellicare, "Consent Record.txt");
			
			try 
			{
				String consentTimestamp = FileUtils.readFileToString(consent);

				TimeZone tz = TimeZone.getTimeZone("UTC");

				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
				df.setTimeZone(tz);
				
				Date consentDate = df.parse(consentTimestamp);
				
				java.text.DateFormat out = DateFormat.getDateFormat(this);
				
				this.getSupportActionBar().setSubtitle(this.getString(R.string.subtitle_consented_date, out.format(consentDate)));
			}
			catch (IOException e) 
			{
				LogManager.getInstance(this).logException(e);
			}
			catch (ParseException e) 
			{
				LogManager.getInstance(this).logException(e);
			}
		}
		else
		{
			signature.setVisibility(View.VISIBLE);
			
			final ConsentActivity me = this;
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder = builder.setMessage(R.string.message_research_disclosure);
			
			builder = builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface arg0, int arg1) 
				{

				}
			});
			
			builder = builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface arg0, int arg1) 
				{
					me.finish();
				}
			});
			
			builder.create().show();
		}
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
					
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(me);

					Editor e = prefs.edit();
					e.putString(ConsentActivity.NAME, payload.get("consent_name").toString());
					e.commit();

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
			this.onBackPressed();
		
		return true;
	}
	
	public void onBackPressed()
	{
		if (this._isReview)
		{
			this.finish();
			
			return;
		}
		
		final String[] items = 
		{ 
			this.getString(R.string.why_not_too_long), 
			this.getString(R.string.why_not_unclear), 
			this.getString(R.string.why_not_dont_want), 
			this.getString(R.string.why_not_something_else), 
			this.getString(R.string.why_not_unknown), 
		};

		final ConsentActivity me = this;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder = builder.setTitle(R.string.title_why_not_consent);
		builder = builder.setItems(items, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				Intent result = new Intent();
				result.putExtra(ConsentActivity.CONSENTED, false);
				
				me.setResult(Activity.RESULT_CANCELED);

				HashMap<String, Object> payload = new HashMap<String, Object>();
				payload.put("reason", items[which]);

				LogManager.getInstance(me).log("consent_rejected", payload);

				me.finish();
			}
		});
		
		builder = builder.setOnCancelListener(new OnCancelListener()
		{
			public void onCancel(DialogInterface dialog) 
			{
				Intent result = new Intent();
				result.putExtra(ConsentActivity.CONSENTED, false);
				
				me.setResult(Activity.RESULT_CANCELED);
				
				HashMap<String, Object> payload = new HashMap<String, Object>();
				payload.put("reason", "None provided.");

				LogManager.getInstance(me).log("consent_rejected", payload);

				me.finish();
			}
		});
		
		builder.create().show();
	}
}
