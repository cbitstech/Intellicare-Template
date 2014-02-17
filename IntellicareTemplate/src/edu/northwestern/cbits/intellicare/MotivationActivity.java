package edu.northwestern.cbits.intellicare;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;
import edu.northwestern.cbits.ic_template.R;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class MotivationActivity extends FormQuestionActivity 
{
	public static final String IS_PARTICIPANT = "is_research_participant";
	public static final boolean IS_PARTICIPANT_DEFAULT = false;

	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_motivation);
		
		this.getSupportActionBar().setTitle(R.string.motivation_title);
	}

	protected void setupListeners() 
	{
		CheckBox myQuality = (CheckBox) this.findViewById(R.id.reason_improve_me);
		CheckBox othersQuality = (CheckBox) this.findViewById(R.id.reason_improve_others);
		CheckBox curious = (CheckBox) this.findViewById(R.id.reason_curious);
		CheckBox research = (CheckBox) this.findViewById(R.id.reason_research);
		
		final MotivationActivity me = this;

		research.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			public void onCheckedChanged(CompoundButton button, boolean isChecked) 
			{
				me._payload.put("research", Boolean.valueOf(isChecked));
				
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(me);
				Editor e = prefs.edit();
				e.putBoolean(MotivationActivity.IS_PARTICIPANT, isChecked);
				e.commit();
			}
		});

		myQuality.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			public void onCheckedChanged(CompoundButton button, boolean isChecked) 
			{
				me._payload.put("my_quality", Boolean.valueOf(isChecked));
			}
		});

		othersQuality.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			public void onCheckedChanged(CompoundButton button, boolean isChecked) 
			{
				me._payload.put("others_quality", Boolean.valueOf(isChecked));
			}
		});
		
		curious.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			public void onCheckedChanged(CompoundButton button, boolean isChecked) 
			{
				me._payload.put("curious", Boolean.valueOf(isChecked));
			}
		});
		
		EditText others = (EditText) this.findViewById(R.id.reason_other);

		others.addTextChangedListener(new TextWatcher()
		{
			public void afterTextChanged(Editable text) 
			{
				me._payload.put("other_reason", text.toString());
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) 
			{

			}

			public void onTextChanged(CharSequence s, int start, int before, int count)
			{

			}
		});
	}

	protected String responsesKey() 
	{
		return "motivation";
	}

	protected boolean canSubmit() 
	{
		File root = Environment.getExternalStorageDirectory();
		
		File intellicare = new File(root, "Intellicare Shared");
		
		if (intellicare.exists() == false)
			intellicare.mkdirs();

		File consent = new File(intellicare, "Motivation Record.txt");
		
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
		
		return true;
	}
	
	public boolean onOptionsItemSelected(final MenuItem item)
	{
		if (item.getItemId() ==  R.id.action_done)
		{
			CheckBox myQuality = (CheckBox) this.findViewById(R.id.reason_improve_me);
			CheckBox othersQuality = (CheckBox) this.findViewById(R.id.reason_improve_others);
			CheckBox curious = (CheckBox) this.findViewById(R.id.reason_curious);
			CheckBox research = (CheckBox) this.findViewById(R.id.reason_research);

			EditText others = (EditText) this.findViewById(R.id.reason_other);
			
			final ArrayList<String> reasons = new ArrayList<String>();
			
			if (research.isChecked())
				reasons.add(this.getString(R.string.reason_research_study));

			if (myQuality.isChecked())
				reasons.add(this.getString(R.string.reason_improve_me));
			
			if (othersQuality.isChecked())
				reasons.add(this.getString(R.string.reason_improve_others));

			if (curious.isChecked())
				reasons.add(this.getString(R.string.reason_curious));
			
			String otherText = others.getText().toString();
			
			if (otherText != null && otherText.length() > 0)
				reasons.add(otherText);

			final MotivationActivity me = this;

			if (reasons.size() > 1)
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				
				builder.setTitle(R.string.title_primary_reason);

				builder.setItems(reasons.toArray(new String[0]), new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						me._payload.put("primary_reason", reasons.get(which));

						HashMap<String, Object> payload = new HashMap<String, Object>();
						
						for (String key : me._payload.keySet())
						{
							payload.put(key, me._payload.get(key));
						}
						
						LogManager.getInstance(me).log(me.responsesKey(), payload);
						
						if (me.canSubmit())
							me.finish();
					}
				});

				builder.create().show();
			}
			else if (reasons.size() == 1)
			{
				me._payload.put("primary_reason", reasons.get(0));

				HashMap<String, Object> payload = new HashMap<String, Object>();
				
				for (String key : me._payload.keySet())
				{
					payload.put(key, me._payload.get(key));
				}
				
				LogManager.getInstance(me).log(me.responsesKey(), payload);
				
				if (me.canSubmit())
					me.finish();
			}
			else
			{
				Toast.makeText(this, R.string.message_motivation_select_one, Toast.LENGTH_LONG).show();
			}
		}

		return true;
	}

	public static boolean showedQuestionnaire() 
	{
		File root = Environment.getExternalStorageDirectory();
		File intellicare = new File(root, "Intellicare Shared");

		if (intellicare.exists() == false)
			intellicare.mkdirs();
		
		File motivation = new File(intellicare, "Motivation Record.txt");
		
		return motivation.exists();
	}
}
