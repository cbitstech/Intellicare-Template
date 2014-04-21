package edu.northwestern.cbits.intellicare.moveme;

import java.text.DateFormat;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class LogActivity extends ConsentedActivity 
{
	protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_lessons);
        
        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setTitle(R.string.title_log);
        
    }
	
	public void onResume()
	{
		super.onResume();
		
		this.refreshList();
	}
	
	private void refreshList()
	{
		final LogActivity me = this;
        
        ListView list = (ListView) this.findViewById(R.id.list_view);
        
        Cursor c = this.getContentResolver().query(MoveProvider.EXERCISES_URI, null, null, null, "-" + MoveProvider.RECORDED);
        
        Log.e("MM", "ROWS: " + c.getCount());
        
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.row_exercise, c, new String[0], new int[0], 0)
        {
        	public void bindView (View view, Context context, Cursor cursor)
        	{
        		TextView moodChange = (TextView) view.findViewById(R.id.mood_change);
        		TextView recorded = (TextView) view.findViewById(R.id.recorded);
        		TextView duration = (TextView) view.findViewById(R.id.duration);
        		TextView moodRange = (TextView) view.findViewById(R.id.mood_range);
        		
        		int preMood = cursor.getInt(cursor.getColumnIndex(MoveProvider.PRE_MOOD));
        		int postMood = cursor.getInt(cursor.getColumnIndex(MoveProvider.POST_MOOD));
        		long exerciseRecorded = cursor.getLong(cursor.getColumnIndex(MoveProvider.RECORDED));
        		long exerciseDuration = cursor.getInt(cursor.getColumnIndex(MoveProvider.DURATION));
        		
        		int delta = postMood - preMood;
        		
        		if (delta > 0)
        		{
        			moodChange.setText("+" + delta);
        			moodChange.setTextColor(0xff669900);
        		}
        		else if (delta < 0)
        		{
        			moodChange.setText("" + delta);
        			moodChange.setTextColor(0xffCC0000);
        		}
        		else
        		{
        			moodChange.setText("" + delta);
        			moodChange.setTextColor(0xff000000);
        		}
        		
        		DateFormat day = android.text.format.DateFormat.getLongDateFormat(me);
        		DateFormat time = android.text.format.DateFormat.getTimeFormat(me);
        		
        		Date d = new Date(exerciseRecorded);
        		recorded.setText(day.format(d) + ": " + time.format(d));

        		exerciseDuration = exerciseDuration / 1000;
        		
        		long minutes = exerciseDuration / 60;
        		long seconds = exerciseDuration % 60;

        		if (seconds < 10)
        			duration.setText(me.getString(R.string.duration_label, "" + minutes + ":0" + seconds));
        		else
        			duration.setText(me.getString(R.string.duration_label, "" + minutes + ":" + seconds));

        		moodRange.setText(me.getString(R.string.mood_range, preMood, postMood));
        	}
        };
        
        list.setAdapter(adapter);

        /*
        list.setOnItemClickListener(new OnItemClickListener()
        {
			public void onItemClick(AdapterView<?> arg0, View view, int which, long id)
			{
				String title = titles[which];
				String url = urls[which];
				
				if (url.trim().length() > 0)
				{
					Intent intent = new Intent(me, LessonActivity.class);
					intent.putExtra(LessonActivity.TITLE, title);
					intent.putExtra(LessonActivity.URL, url);
        
					me.startActivity(intent);
				}
			}
        });
        */
    }
}
