package edu.northwestern.cbits.intellicare;

import java.util.HashMap;

import edu.northwestern.cbits.ic_template.R;
import edu.northwestern.cbits.intellicare.logging.LogManager;

import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public abstract class FormQuestionActivity extends ActionBarActivity 
{
	protected abstract void setupListeners();
	protected abstract String responsesKey();
	protected abstract boolean canSubmit();

	protected HashMap<String, Object> _payload = new HashMap<String, Object>();

	protected void onResume()
	{
		super.onResume();
		
		this.setupListeners();
	}
	
	protected void didUpdate()
	{					
		this.supportInvalidateOptionsMenu();
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() ==  R.id.action_done)
		{
			if (this.canSubmit() == false)
				Toast.makeText(this, R.string.message_complete_form, Toast.LENGTH_LONG).show();
			else
			{
				HashMap<String, Object> payload = new HashMap<String, Object>();
				
				for (String key : this._payload.keySet())
				{
					payload.put(key, this._payload.get(key));
				}
				
				LogManager.getInstance(this).log(this.responsesKey(), payload);

				Toast.makeText(this, R.string.message_submitted_form, Toast.LENGTH_LONG).show();

				this.finish();
			}
		}

		return true;
	}
	
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_rating, menu);

		return true;
	}
}
