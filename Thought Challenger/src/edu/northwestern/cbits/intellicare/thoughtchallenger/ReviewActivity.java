package edu.northwestern.cbits.intellicare.thoughtchallenger;

import java.security.SecureRandom;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class ReviewActivity extends ConsentedActivity 
{
	private int _index = 0;
	
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_review);

		this.getSupportActionBar().setTitle(R.string.title_review);
	}
	
	protected void onResume()
	{
		super.onResume();
		
		SecureRandom r = new SecureRandom();
		
		Cursor c = this.getContentResolver().query(ThoughtContentProvider.THOUGHT_PAIR_URI, null, null, null, null);
		
		if (c.getCount() == 1)
			this.getSupportActionBar().setSubtitle(R.string.subtitle_review_single);
		else
			this.getSupportActionBar().setSubtitle(this.getString(R.string.subtitle_review, c.getCount()));
		
		this.showPair(r.nextInt(c.getCount()));
		
		c.close();
	}

	private void showPair(int index) 
	{
		Cursor c = this.getContentResolver().query(ThoughtContentProvider.THOUGHT_PAIR_URI, null, null, null, null);

		Log.e("TC", "INDEX 0: " + index);
		
		Log.e("TC", "COUNT: " + c.getCount());

		if (index < 0)
			this._index = c.getCount() - 1;
		else if (this._index >= c.getCount())
			this._index = 0;
		else
			this._index = index;
		
		Log.e("TC", "INDEX 1: " + index);
		
		if (c.moveToPosition(this._index))
		{
			TextView automatic = (TextView) this.findViewById(R.id.automatic_thought);
			TextView response = (TextView) this.findViewById(R.id.rational_response);
			
			automatic.setText(c.getString(c.getColumnIndex(ThoughtContentProvider.PAIR_AUTOMATIC_THOUGHT)));
			response.setText(c.getString(c.getColumnIndex(ThoughtContentProvider.PAIR_RATIONAL_RESPONSE)));
		}
		
		c.close();
	}

	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_review, menu);

		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int itemId = item.getItemId();
		
		switch (itemId)
		{
			case R.id.action_next:
				this._index += 1;
				this.showPair(this._index);

				break;
			case R.id.action_previous:
				this._index -= 1;
				this.showPair(this._index);
				
				break;
		}
		
		return true;
	}
}
