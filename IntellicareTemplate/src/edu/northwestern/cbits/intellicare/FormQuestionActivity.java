package edu.northwestern.cbits.intellicare;

import java.util.HashMap;

import edu.northwestern.cbits.ic_template.R;
import edu.northwestern.cbits.intellicare.logging.LogManager;

import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

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
			HashMap<String, Object> payload = new HashMap<String, Object>();
			payload.put("payload", this._payload);
			
			LogManager.getInstance(this).log(this.responsesKey(), payload);
			
			this.finish();
		}

		return true;
	}
	
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_rating, menu);

		MenuItem doneItem = menu.findItem(R.id.action_done);

		if (this.canSubmit() == false)
			doneItem.setVisible(false);

		return true;
	}
}
