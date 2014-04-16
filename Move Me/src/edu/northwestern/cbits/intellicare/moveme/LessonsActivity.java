package edu.northwestern.cbits.intellicare.moveme;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class LessonsActivity extends ConsentedActivity 
{
	protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_lessons);
        
        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setTitle(R.string.title_lessons);
        
        final LessonsActivity me = this;
        
        final String[] titles = this.getResources().getStringArray(R.array.array_lessons);
        final String[] urls = this.getResources().getStringArray(R.array.array_lesson_urls);
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.row_lesson, titles)
        {
        	public View getView (int position, View convertView, ViewGroup parent)
        	{
        		if (convertView == null)
        		{
    				LayoutInflater inflater = LayoutInflater.from(me);
    				convertView = inflater.inflate(R.layout.row_lesson, parent, false);
        		}
        		
        		TextView lessonName = (TextView) convertView.findViewById(R.id.lesson_name);
        		TextView lessonCategory = (TextView) convertView.findViewById(R.id.lesson_category);
        		
        		String title = this.getItem(position);
        		
        		if (title.endsWith("*"))
        		{
        			lessonName.setVisibility(View.GONE);
        			lessonCategory.setText(title.replace("*", "").trim());
        			lessonCategory.setVisibility(View.VISIBLE);
        		}
        		else
        		{
        			lessonCategory.setVisibility(View.GONE);
        			lessonName.setText(title);
        			lessonName.setVisibility(View.VISIBLE);
        		}
        		
        		return convertView;
        	}
        };
        
        ListView list = (ListView) this.findViewById(R.id.list_view);
        list.setAdapter(adapter);
        
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
    }
}
