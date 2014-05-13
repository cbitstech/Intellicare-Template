package edu.northwestern.cbits.intellicare.socialforce;

import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class EventReviewActivity extends ConsentedActivity 
{
    public static final String EVENT_ID = "EVENT_ID";

	protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_event_review);
        
        this.getSupportActionBar().setTitle(R.string.title_review);
    }
    
    protected void onResume()
    {
    	super.onResume();
    	
    	Intent intent = this.getIntent();

    	long eventId = intent.getLongExtra(EventReviewActivity.EVENT_ID, -1);

    	Uri u = intent.getData();
    	
    	if (u != null)
    	{
    		List<String> segments = u.getPathSegments();
    		
    		eventId = Long.parseLong(segments.get(segments.size() - 1));
    	}
    	
		String selection = CalendarContract.Events._ID + " = ?";
		String[] args = { "" + eventId };

		Cursor c = this.getContentResolver().query(CalendarContract.Events.CONTENT_URI, null, selection, args, CalendarContract.Events._ID + " DESC");
		
		if (c.moveToNext())
		{
	    	final EventReviewActivity me = this;
	    	
	    	String title = c.getString(c.getColumnIndex(CalendarContract.Events.TITLE));
	    	
	    	String attSelection = CalendarContract.Attendees.EVENT_ID + " = ?";
	    	String[] attArgs = { "" + eventId };
	    	
	    	Cursor attendees = this.getContentResolver().query(CalendarContract.Attendees.CONTENT_URI, null, attSelection, attArgs, null);
	    	
	    	StringBuffer sb = new StringBuffer();
	    	
	    	while (attendees.moveToNext())
	    	{
	    		if (sb.length() > 0)
	    			sb.append(", ");
	    		
	    		sb.append(attendees.getString(attendees.getColumnIndex(CalendarContract.Attendees.ATTENDEE_NAME)));
	    	}
	    	
	    	attendees.close();
	    	
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		builder.setTitle(title);
    		
    		builder.setMessage(this.getString(R.string.message_event_check, sb.toString()));
    		
    		builder.setPositiveButton(R.string.action_yes, new OnClickListener()
    		{
				public void onClick(DialogInterface arg0, int arg1) 
				{

				}
    		});
    		
    		builder.setNegativeButton(R.string.action_no, new OnClickListener()
    		{
				public void onClick(DialogInterface dialog, int which) 
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(me);
					builder.setTitle(R.string.title_try_again);
					
					final String[] items = me.getResources().getStringArray(R.array.actions_event);
					
					builder.setItems(items, new OnClickListener()
					{
						public void onClick(DialogInterface dialog, int which) 
						{
							Toast.makeText(me, items[which], Toast.LENGTH_LONG).show();

							me.finish();
						}
					});
					
					builder.create().show();
				}
    		});
    		
    		builder.create().show();
		}
		else 
		{
			this.finish();
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
    	
		if (itemId == R.id.action_continue)
		{
			final EventReviewActivity me = this;
			
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		builder.setTitle(R.string.title_thanks_review);
    		builder.setMessage(R.string.message_thanks_review);
    		
    		builder.setPositiveButton(R.string.action_schedule_again, new OnClickListener()
    		{
				public void onClick(DialogInterface arg0, int arg1) 
				{
					Intent intent = new Intent(me, ScheduleActivity.class);
					
					me.startActivity(intent);
					
					me.finish();
				}
    		});
    		
    		builder.setNegativeButton(R.string.action_plan_activity, new OnClickListener()
    		{
				public void onClick(DialogInterface dialog, int which) 
				{
					Intent intent = new Intent(me, ScheduleActivity.class);
					
					me.startActivity(intent);
					
					me.finish();
				}
    		});
    		
    		builder.create().show();

		}
		
		return true;
    }

	public static Uri uriForEvent(long id) 
	{
		return Uri.parse("intellicare://social-force/event/" + id);
	}
}
