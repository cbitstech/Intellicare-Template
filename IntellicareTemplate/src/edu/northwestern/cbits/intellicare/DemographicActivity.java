package edu.northwestern.cbits.intellicare;

import java.util.Calendar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import edu.northwestern.cbits.ic_template.R;

public class DemographicActivity extends FormQuestionActivity 
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_demographic);
		
		this.getSupportActionBar().setTitle(R.string.demographic_title);
		this.getSupportActionBar().setSubtitle(R.string.demographic_subtitle);
	}
	
	protected void setupListeners() 
	{
		final DemographicActivity me = this;
		
		final TextView birthLabel = (TextView) this.findViewById(R.id.birth_year_label);
		final TextView birthValue = (TextView) this.findViewById(R.id.birth_year_value);

		OnClickListener birthListener = new OnClickListener()
		{
			public void onClick(View view) 
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(me);
				
				builder.setTitle(R.string.birth_year_title);
				
				Calendar calendar = Calendar.getInstance();
				int year = calendar.get(Calendar.YEAR);
				
				final String[] years = new String[100];
				
				for (int i = 0; i < years.length; i++)
				{
					years[i] = "" + (year - 18 - i);
				}
				
				builder.setItems(years, new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						birthValue.setText(years[which]);
						birthValue.setTextColor(birthLabel.getCurrentTextColor());
						
						me._payload.put("birth_year", years[which]);
						
						me.didUpdate();
					}
				});
				
				builder.create().show();
			}
		};
		
		birthLabel.setOnClickListener(birthListener);
		birthValue.setOnClickListener(birthListener);

		// ---

		final TextView genderLabel = (TextView) this.findViewById(R.id.gender_label);
		final TextView genderValue = (TextView) this.findViewById(R.id.gender_value);

		OnClickListener genderListener = new OnClickListener()
		{
			public void onClick(View view) 
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(me);
				
				builder.setTitle(R.string.gender_title);
				
				final String[] genders = me.getResources().getStringArray(R.array.labels_gender);
				
				builder.setItems(genders, new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						genderValue.setText(genders[which]);
						genderValue.setTextColor(genderLabel.getCurrentTextColor());
						
						me._payload.put("gender", genders[which]);
						
						me.didUpdate();
					}
				});
				
				builder.create().show();
			}
		};
		
		genderLabel.setOnClickListener(genderListener);
		genderValue.setOnClickListener(genderListener);

		// ---
		
		final TextView ethnicityLabel = (TextView) this.findViewById(R.id.ethnicity_label);
		final TextView ethnicityValue = (TextView) this.findViewById(R.id.ethnicity_value);

		OnClickListener ethnicityListener = new OnClickListener()
		{
			public void onClick(View view) 
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(me);
				
				builder.setTitle(R.string.ethnicity_title);
				
				final String[] ethnics = me.getResources().getStringArray(R.array.labels_ethnicity);
				
				builder.setItems(ethnics, new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						ethnicityValue.setText(ethnics[which]);
						ethnicityValue.setTextColor(ethnicityLabel.getCurrentTextColor());
						
						me._payload.put("ethnicity", ethnics[which]);
						
						me.didUpdate();
					}
				});
				
				builder.create().show();
			}
		};
		
		ethnicityLabel.setOnClickListener(ethnicityListener);
		ethnicityValue.setOnClickListener(ethnicityListener);

		// ---
		
		final TextView raceLabel = (TextView) this.findViewById(R.id.race_label);
		final TextView raceValue = (TextView) this.findViewById(R.id.race_value);

		OnClickListener raceListener = new OnClickListener()
		{
			public void onClick(View view) 
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(me);
				
				builder.setTitle(R.string.race_title);
				
				final String[] races = me.getResources().getStringArray(R.array.labels_race);
				
				builder.setItems(races, new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						raceValue.setText(races[which]);
						raceValue.setTextColor(raceLabel.getCurrentTextColor());
						
						me._payload.put("race", races[which]);
						
						me.didUpdate();
					}
				});
				
				builder.create().show();
			}
		};
		
		raceLabel.setOnClickListener(raceListener);
		raceValue.setOnClickListener(raceListener);
		
		// ---

		final TextView educationLabel = (TextView) this.findViewById(R.id.education_label);
		final TextView educationValue = (TextView) this.findViewById(R.id.education_value);

		OnClickListener educationListener = new OnClickListener()
		{
			public void onClick(View view) 
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(me);
				
				builder.setTitle(R.string.education_title);
				
				final String[] educations = me.getResources().getStringArray(R.array.labels_education);
				
				builder.setItems(educations, new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						educationValue.setText(educations[which]);
						educationValue.setTextColor(educationLabel.getCurrentTextColor());
						
						me._payload.put("education", educations[which]);
						
						me.didUpdate();
					}
				});
				
				builder.create().show();
			}
		};
		
		educationLabel.setOnClickListener(educationListener);
		educationValue.setOnClickListener(educationListener);
	}

	protected String responsesKey() 
	{
		return "demographic_response";
	}
	
	protected boolean canSubmit() 
	{
		return this._payload.size() >= 5;
	}
}
