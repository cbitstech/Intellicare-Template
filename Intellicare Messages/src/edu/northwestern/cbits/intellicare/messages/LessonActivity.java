package edu.northwestern.cbits.intellicare.messages;

import java.util.ArrayList;

import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.StatusNotificationManager;

public class LessonActivity extends ConsentedActivity 
{
	public final static String LESSON_ID = "LESSON_ID";
	public final static String LESSON_TITLE = "LESSON_TITLE";
	
	private class NotificationRow
	{
		public String type = "";
		public String message = "";
		public int lessonId = 0;
	}

	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_lessons);
		
		ListView list = (ListView) this.findViewById(R.id.list_view);
		
		long lessonId = this.getIntent().getLongExtra(LessonActivity.LESSON_ID, 0);
		String lessonTitle = this.getIntent().getStringExtra(LessonActivity.LESSON_TITLE);
		
		this.getSupportActionBar().setTitle(lessonTitle);

		String selection = "lesson_id = ?";
		String[] selectionArgs = { "" + lessonId };
		
		Cursor cursor = this.getContentResolver().query(ContentProvider.MESSAGE_GROUPS_URI, null, selection, selectionArgs, "group_order");
		
		final ArrayList<NotificationRow> messages = new ArrayList<NotificationRow>();
		
		while (cursor.moveToNext())
		{
			NotificationRow prompt = new NotificationRow();
			prompt.type = this.getString(R.string.prompt_type);
			prompt.message = cursor.getString(cursor.getColumnIndex("prompt"));
			prompt.lessonId  = cursor.getInt(cursor.getColumnIndex("lesson_id"));
			messages.add(prompt);

			NotificationRow first = new NotificationRow();
			first.type = this.getString(R.string.message_type);
			first.message = cursor.getString(cursor.getColumnIndex("first"));
			first.lessonId  = cursor.getInt(cursor.getColumnIndex("lesson_id"));
			messages.add(first);

			NotificationRow second = new NotificationRow();
			second.type = this.getString(R.string.message_type);
			second.message = cursor.getString(cursor.getColumnIndex("second"));
			second.lessonId  = cursor.getInt(cursor.getColumnIndex("lesson_id"));
			messages.add(second);

			NotificationRow third = new NotificationRow();
			third.type = this.getString(R.string.message_type);
			third.message = cursor.getString(cursor.getColumnIndex("third"));
			third.lessonId  = cursor.getInt(cursor.getColumnIndex("lesson_id"));
			messages.add(third);

			NotificationRow fourth = new NotificationRow();
			fourth.type = this.getString(R.string.message_type);
			fourth.message = cursor.getString(cursor.getColumnIndex("fourth"));
			fourth.lessonId  = cursor.getInt(cursor.getColumnIndex("lesson_id"));
			messages.add(fourth);
		}
		
		cursor.close();
		
		ArrayAdapter<NotificationRow> adapter = new ArrayAdapter<NotificationRow>(this, R.layout.row_notification, messages)
		{
			public View getView(int position, View convertView, ViewGroup parent) 
			{
				if (convertView == null)
				{
					LayoutInflater inflater = LayoutInflater.from(parent.getContext());
					
					convertView = inflater.inflate(R.layout.row_notification, parent, false);
				}
				
				NotificationRow row = messages.get(position);
				
				TextView message = (TextView) convertView.findViewById(R.id.notification_text);
				message.setText(row.message);

				TextView messageType = (TextView) convertView.findViewById(R.id.notification_type);
				messageType.setText(row.type);
				
				return convertView;
			}
		};
		
		list.setAdapter(adapter);
		
		final LessonActivity me = this;
		
		list.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
			{
				NotificationRow row = messages.get(position);
				
				Intent intent = new Intent(me, LessonContentActivity.class);
				intent.setAction(Intent.ACTION_VIEW);
				intent.putExtra(LessonContentActivity.LESSON_ID, row.lessonId);
				intent.putExtra(LessonContentActivity.LESSON_TITLE, me.getSupportActionBar().getTitle());
				
				PendingIntent pi = PendingIntent.getActivity(me, 0, intent, 0);
				
				StatusNotificationManager note = StatusNotificationManager.getInstance(me);
				
				note.notifyBigText(12345, R.drawable.ic_notification, me.getIntent().getStringExtra(LessonActivity.LESSON_TITLE), row.message, pi);
			}
		});
	}
}
