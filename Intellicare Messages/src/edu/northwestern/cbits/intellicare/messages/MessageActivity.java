package edu.northwestern.cbits.intellicare.messages;

import java.util.HashMap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.TextView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.DialogActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class MessageActivity extends ConsentedActivity 
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_message);
	}
	
	protected void onNewIntent (Intent intent)
	{
		super.onNewIntent(intent);
		
		this.setIntent(intent);
	}

	public void onResume()
	{
		super.onResume();
		
		
		String title = this.getIntent().getStringExtra(DialogActivity.DIALOG_TITLE);
		String message = this.getIntent().getStringExtra(DialogActivity.DIALOG_MESSAGE);
		
		this.getSupportActionBar().setTitle(title);
		
		TextView messageText = (TextView) this.findViewById(R.id.message_text);
		messageText.setText(message);
	}
	
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_message, menu);

		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.action_close)
		{
			HashMap<String, Object> payload = new HashMap<String, Object>();
			
			Intent intent = this.getIntent(); 
			
			if (intent.hasExtra(ScheduleManager.MESSAGE_INDEX))
			{
				String descIndex = intent.getStringExtra(ScheduleManager.MESSAGE_INDEX);
				payload.put("message_index", descIndex);
			}
			
			if (this.getIntent().getBooleanExtra(ScheduleManager.IS_INSTRUCTION, false))
			{
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
				Editor e = prefs.edit();
				e.putBoolean(ScheduleManager.INSTRUCTION_COMPLETED, true);
				e.commit();
			}

			CheckBox interrupted = (CheckBox) this.findViewById(R.id.interrupt_check);
			
			if (interrupted.isChecked())
				payload.put("interrupted", "true");
			else
				payload.put("interrupted", "false");

			LogManager.getInstance(this).log("message_closed", payload);

			this.finish();
		}
		
		return true;
	}

}
