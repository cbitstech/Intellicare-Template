package edu.northwestern.cbits.intellicare.thoughtchallenger;

import org.json.JSONArray;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class ChangeActivity extends ConsentedActivity 
{
	public static final String THOUGHT_VALUE = "thought_value";

	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_change);
		
		this.getSupportActionBar().setTitle(R.string.title_change);
		this.getSupportActionBar().setSubtitle(R.string.subtitle_change);
		
		final ChangeActivity me = this;
		
		final EditText prompt = (EditText) this.findViewById(R.id.field_replacement_thought);

		TextView helpLink = (TextView) this.findViewById(R.id.link_help);
		
		helpLink.setOnClickListener(new OnClickListener()
		{
			public void onClick(View arg0) 
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(me);
				
				builder.setTitle(R.string.title_positive_thoughts);
				builder.setSingleChoiceItems(R.array.list_positive_thoughts, -1, new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						String[] thoughts = me.getResources().getStringArray(R.array.list_positive_thoughts);
						
						prompt.setText(thoughts[which]);
					}
				});
				
				builder.setPositiveButton(R.string.action_continue, new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{

					}
				});
				
				builder.create().show();
			}
		});
	}
	
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_change, menu);

		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int itemId = item.getItemId();
		
		switch (itemId)
		{
			case R.id.action_help:
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				
				builder.setTitle(R.string.title_change_help);
				builder.setMessage(R.string.message_change_help);
				
				builder.setPositiveButton(R.string.action_continue, new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
	
					}
				});
				
				builder.create().show();
				
				break;
			case R.id.action_done:
				String original = this.getIntent().getStringExtra(ChangeActivity.THOUGHT_VALUE);

				EditText prompt = (EditText) this.findViewById(R.id.field_replacement_thought);
				
				String promptValue = prompt.getText().toString();
				
				if (promptValue.length() > 5)
				{
					ContentValues values = new ContentValues();
					values.put(ThoughtContentProvider.PAIR_AUTOMATIC_THOUGHT, original);
					values.put(ThoughtContentProvider.PAIR_RATIONAL_RESPONSE, promptValue);
					values.put(ThoughtContentProvider.PAIR_DISTORTIONS, (new JSONArray()).toString());
					values.put(ThoughtContentProvider.PAIR_TAGS, (new JSONArray()).toString());
					
					this.getContentResolver().insert(ThoughtContentProvider.THOUGHT_PAIR_URI, values);
					
					Intent intent = new Intent(this, MainActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					this.startActivity(intent);
				}
				else
					Toast.makeText(this, R.string.step_three_continue, Toast.LENGTH_LONG).show();

				break;
		}
		
		return true;
	}
}
