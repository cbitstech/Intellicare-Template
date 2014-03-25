package edu.northwestern.cbits.intellicare.thoughtchallenger;

import java.util.HashMap;

import android.app.AlertDialog;
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
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class CatchActivity extends ConsentedActivity 
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_catch);
		
		this.getSupportActionBar().setTitle(R.string.title_catch);
		this.getSupportActionBar().setSubtitle(R.string.subtitle_catch);

		final CatchActivity me = this;

		final EditText prompt = (EditText) this.findViewById(R.id.field_negative_thought);

		TextView helpLink = (TextView) this.findViewById(R.id.link_help);
		
		helpLink.setOnClickListener(new OnClickListener()
		{
			public void onClick(View arg0) 
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(me);
				
				builder.setTitle(R.string.title_negative_thoughts);
				builder.setSingleChoiceItems(R.array.list_negative_thoughts, -1, new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						String[] thoughts = me.getResources().getStringArray(R.array.list_negative_thoughts);
						
						prompt.setText(thoughts[which]);
						
						HashMap<String, Object> payload = new HashMap<String, Object>();
						payload.put("automatic_thought", thoughts[which]);
						LogManager.getInstance(me).log("selected_thought", payload);
					}
				});
				
				builder.setPositiveButton(R.string.action_continue, new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{

					}
				});
				
				builder.create().show();
				
				HashMap<String, Object> payload = new HashMap<String, Object>();
				LogManager.getInstance(me).log("showed_examples", payload);
			}
		});
	}
	
	protected void onResume()
	{
		super.onResume();
		
		HashMap<String, Object> payload = new HashMap<String, Object>();
		LogManager.getInstance(this).log("opened_catch", payload);
	}
	
	protected void onPause()
	{
		super.onPause();

		HashMap<String, Object> payload = new HashMap<String, Object>();
		LogManager.getInstance(this).log("closed_catch", payload);
	}
	
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_catch, menu);

		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int itemId = item.getItemId();
		
		switch (itemId)
		{
			case R.id.action_help:
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				
				builder.setTitle(R.string.title_catch_help);
				builder.setMessage(R.string.message_catch_help);
				
				builder.setPositiveButton(R.string.action_continue, new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{

					}
				});
				
				builder.create().show();
				
				HashMap<String, Object> payload = new HashMap<String, Object>();
				LogManager.getInstance(this).log("showed_catch_help", payload);

				break;

			case R.id.action_next:
				EditText prompt = (EditText) this.findViewById(R.id.field_negative_thought);
				
				String promptValue = prompt.getText().toString();
				
				if (promptValue.length() > 5)
				{
					Intent intent = new Intent(this, CheckActivity.class);
					intent.putExtra(ChangeActivity.THOUGHT_VALUE, promptValue);
					this.startActivity(intent);
				}
				else
					Toast.makeText(this, R.string.step_one_continue, Toast.LENGTH_LONG).show();

				break;
		}
		
		return true;
	}
}
