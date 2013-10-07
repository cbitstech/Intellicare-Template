package edu.northwestern.cbits.intellicare;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import edu.northwestern.cbits.ic_template.R;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class RecruitmentActivity extends ActionBarActivity 
{
	private HashMap<String, Object> _payload = new HashMap<String, Object>();
	
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_recruitment);
		
		this.getSupportActionBar().setTitle(R.string.recruitment_title);
		
		final TextView contactLabel = (TextView) this.findViewById(R.id.recruitment_time_label);
		final TextView contactValue = (TextView) this.findViewById(R.id.recruitment_time_value);
		
		final RecruitmentActivity me = this;
		
		OnClickListener contactListener = new OnClickListener()
		{
			public void onClick(View view) 
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(me);
				
				builder.setTitle(R.string.recruitment_time_title);
				
				final String[] labels = me.getResources().getStringArray(R.array.recruitment_time_labels);
				
				builder.setItems(labels, new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						contactValue.setText(labels[which]);
						contactValue.setTextColor(contactLabel.getCurrentTextColor());
						
						me._payload.put("contact_time", labels[which]);
					}
				});
				
				builder.create().show();
			}
		};
		
		contactLabel.setOnClickListener(contactListener);
		contactValue.setOnClickListener(contactListener);
		
		EditText nameField = (EditText) this.findViewById(R.id.recruitment_name_field);

		nameField.addTextChangedListener(new TextWatcher()
		{
			public void afterTextChanged(Editable editable) 
			{
				me._payload.put("name", editable.toString());

			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) 
			{

			}

			public void onTextChanged(CharSequence s, int start, int before, int count) 
			{

			}
		});

		EditText emailField = (EditText) this.findViewById(R.id.recruitment_email_field);

		emailField.addTextChangedListener(new TextWatcher()
		{
			public void afterTextChanged(Editable editable) 
			{
				me._payload.put("email", editable.toString());
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) 
			{

			}

			public void onTextChanged(CharSequence s, int start, int before, int count) 
			{

			}
		});

		EditText phoneField = (EditText) this.findViewById(R.id.recruitment_phone_field);

		phoneField.addTextChangedListener(new TextWatcher()
		{
			public void afterTextChanged(Editable editable) 
			{
				me._payload.put("phone", editable.toString());
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) 
			{

			}

			public void onTextChanged(CharSequence s, int start, int before, int count) 
			{

			}
		});

		File root = Environment.getExternalStorageDirectory();
		
		File intellicare = new File(root, "Intellicare Shared");
		
		if (intellicare.exists() == false)
			intellicare.mkdirs();
		
		File consent = new File(intellicare, "Recruitment Record.txt");
		
		try 
		{
			TimeZone tz = TimeZone.getTimeZone("UTC");

			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
			df.setTimeZone(tz);
			
			PrintWriter out = new PrintWriter(consent);
			out.print(df.format(new Date()));
			out.close();
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() ==  R.id.action_done)
		{
			String name = null;
			
			if (this._payload.containsKey("name"))
				name = this._payload.get("name").toString();

			String email = null;
			
			if (this._payload.containsKey("email"))
				email = this._payload.get("email").toString();

			String phone = null;
			
			if (this._payload.containsKey("phone"))
				phone = this._payload.get("phone").toString();
			
			if (name == null || name.length() < 3)
				Toast.makeText(this, R.string.recruitment_name_message, Toast.LENGTH_LONG).show();
			else if (email == null || Patterns.EMAIL_ADDRESS.matcher(email).matches() == false)
				Toast.makeText(this, R.string.recruitment_email_message, Toast.LENGTH_LONG).show();
			else if (phone != null && Patterns.PHONE.matcher(phone).matches() == false)
				Toast.makeText(this, R.string.recruitment_phone_message, Toast.LENGTH_LONG).show();
			else
			{
				LogManager.getInstance(this).log("recruitment_response", this._payload);
			
				this.finish();
			}
		}
		else if (item.getItemId() == R.id.action_skip)
			this.finish();

		return true;
	}
	
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_recruitment, menu);

		return true;
	}
	
	public static boolean showedRecruitment()
	{
		File root = Environment.getExternalStorageDirectory();
		File intellicare = new File(root, "Intellicare Shared");

		if (intellicare.exists() == false)
			intellicare.mkdirs();
		
		File recruitment = new File(intellicare, "Recruitment Record.txt");
		
		return recruitment.exists();
	}
}
