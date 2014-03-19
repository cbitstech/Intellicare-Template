package edu.northwestern.cbits.intellicare.thoughtchallenger;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.ListView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class ReviewActivity extends ConsentedActivity 
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_review);

		this.getSupportActionBar().setTitle(R.string.title_review);
	}
	
	protected void onResume()
	{
		this.onResume();

		Cursor c = this.getContentResolver().query(ThoughtContentProvider.THOUGHT_PAIR_URI, null, null, null, null);
		
		if (c.getCount() == 1)
			this.getSupportActionBar().setSubtitle(R.string.subtitle_review_single);
		else
			this.getSupportActionBar().setSubtitle(this.getString(R.string.subtitle_review, c.getCount()));
		
		ListView list = (ListView) this.findViewById(R.id.list_review);

		SimpleCursorAdapter adapter = SimpleCursorAdapter(this, int layout, c, new String[0], new int[0], 0)
		{
			
		};
		
	}
}
