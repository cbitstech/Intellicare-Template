package edu.northwestern.cbits.intellicare.messages;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class LessonContentActivity extends ConsentedActivity 
{
	protected static final String LESSON_ID = "LESSON_ID";
	protected static final String LESSON_TITLE = "LESSON_TITLE";
	protected static final String LESSON_ORDER = "LESSON_ORDER";
	
	private int _lessonId = 0;
	private String _lessonTitle = null;
	private float _lessonOrder = 0;
	
	private float _next = -1;
	private float _back = -1;

	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		this._lessonId = this.getIntent().getIntExtra(LessonContentActivity.LESSON_ID, 0);
		this._lessonTitle = this.getIntent().getStringExtra(LessonContentActivity.LESSON_TITLE);
		this._lessonOrder = this.getIntent().getFloatExtra(LessonContentActivity.LESSON_ORDER, 0);

		this.getSupportActionBar().setTitle(this._lessonTitle);

		this.setContentView(R.layout.activity_content);
		
		final LessonContentActivity me = this;

		WebView webView = (WebView) this.findViewById(R.id.web_view);
		
		try 
		{
			InputStream in = this.getAssets().open("lesson.html");
			String htmlString = IOUtils.toString(in);
			in.close();
			
			String selection = "lesson_id = ? AND page_order >= ?";
			String[] lessonArgs = { "" + this._lessonId, "" + this._lessonOrder };
 			
			Cursor cursor = this.getContentResolver().query(ContentProvider.PAGES_URI, null, selection, lessonArgs, "page_order");
			
			if (cursor.moveToNext())
			{
				this._lessonOrder  = cursor.getFloat(cursor.getColumnIndex("page_order"));
				
				htmlString = htmlString.replace("[[LESSON TEXT]]", cursor.getString(cursor.getColumnIndex("html")));
				
				WebSettings settings = webView.getSettings();
				settings.setDefaultTextEncodingName("utf-8");
				webView.loadData(htmlString, "text/html; charset=UTF-8", null);
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}

		
		String selection = "page_order < ?";
		String[] selectionArgs = { this._lessonOrder + "" };

		Cursor c = this.getContentResolver().query(ContentProvider.PAGES_URI, null, selection, selectionArgs, "page_order desc");
		
		if (c.moveToNext())
			this._back = c.getFloat(c.getColumnIndex("page_order"));
		
		c.close();
		
		selection = "page_order > ?";

		c = this.getContentResolver().query(ContentProvider.PAGES_URI, null, selection, selectionArgs, "page_order");
		
		if (c.moveToNext())
			this._next = c.getFloat(c.getColumnIndex("page_order"));
		
		c.close();
		
		Button back = (Button) this.findViewById(R.id.back_button);
		
		if (this._back >= 0.0)
		{
			back.setOnClickListener(new OnClickListener()
			{
				public void onClick(View view) 
				{
					Intent intent = new Intent(me, LessonContentActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
					intent.putExtra(LessonContentActivity.LESSON_TITLE, me._lessonTitle);
					intent.putExtra(LessonContentActivity.LESSON_ID, me._lessonId);
					intent.putExtra(LessonContentActivity.LESSON_ORDER, me._back);
	
					me.startActivity(intent);
					
					me.finish();
				}
			});
		}
		else
			back.setVisibility(View.INVISIBLE);

		Button next = (Button) this.findViewById(R.id.next_button);
		
		if (this._next >= 0.0)
		{
			next.setOnClickListener(new OnClickListener()
			{
				public void onClick(View view) 
				{
					Intent intent = new Intent(me, LessonContentActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
					intent.putExtra(LessonContentActivity.LESSON_TITLE, me._lessonTitle);
					intent.putExtra(LessonContentActivity.LESSON_ID, me._lessonId);
					intent.putExtra(LessonContentActivity.LESSON_ORDER, me._next);
	
					me.startActivity(intent);
					
					me.finish();
				}
			});
		}
		else
		{
			next.setText(R.string.button_close);
			
			next.setOnClickListener(new OnClickListener()
			{
				public void onClick(View view) 
				{
					me.finish();
				}
			});
		}
	}
}
