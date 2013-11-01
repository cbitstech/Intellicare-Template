package edu.northwestern.cbits.intellicare.messages;

import java.util.ArrayList;
import java.util.HashMap;

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
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class TestActivity extends ConsentedActivity 
{
	private class NotificationRow
	{
		public String type = "";
		public String message = "";
		public String image = null;
		@SuppressWarnings("unused")
		public int lessonId = 0;
	}

	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_lessons);
		
		ListView list = (ListView) this.findViewById(R.id.list_view);
		
		this.getSupportActionBar().setTitle(R.string.action_test);

		Cursor cursor = this.getContentResolver().query(ContentProvider.MESSAGE_GROUPS_URI, null, null, null, "group_order");
		
		final ArrayList<NotificationRow> messages = new ArrayList<NotificationRow>();
		
		while (cursor.moveToNext())
		{
			NotificationRow prompt = new NotificationRow();
			prompt.type = this.getString(R.string.prompt_type);
			prompt.message = cursor.getString(cursor.getColumnIndex("prompt"));
			prompt.image = cursor.getString(cursor.getColumnIndex("prompt_img"));
			prompt.lessonId  = cursor.getInt(cursor.getColumnIndex("lesson_id"));
			messages.add(prompt);

			NotificationRow first = new NotificationRow();
			first.type = this.getString(R.string.message_type);
			first.message = cursor.getString(cursor.getColumnIndex("first"));
			first.image = cursor.getString(cursor.getColumnIndex("first_img"));
			first.lessonId  = cursor.getInt(cursor.getColumnIndex("lesson_id"));
			messages.add(first);

			NotificationRow second = new NotificationRow();
			second.type = this.getString(R.string.message_type);
			second.message = cursor.getString(cursor.getColumnIndex("second"));
			second.image = cursor.getString(cursor.getColumnIndex("second_img"));
			second.lessonId  = cursor.getInt(cursor.getColumnIndex("lesson_id"));
			messages.add(second);

			NotificationRow third = new NotificationRow();
			third.type = this.getString(R.string.message_type);
			third.message = cursor.getString(cursor.getColumnIndex("third"));
			third.image = cursor.getString(cursor.getColumnIndex("third_img"));
			third.lessonId  = cursor.getInt(cursor.getColumnIndex("lesson_id"));
			messages.add(third);

			NotificationRow fourth = new NotificationRow();
			fourth.type = this.getString(R.string.message_type);
			fourth.message = cursor.getString(cursor.getColumnIndex("fourth"));
			fourth.image = cursor.getString(cursor.getColumnIndex("fourth_img"));
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
		
		final TestActivity me = this;
		
		list.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
			{
				NotificationRow row = messages.get(position);
				
				if ("prompt".equals(row.type.toLowerCase()))
				{
					Intent intent = new Intent(me, TaskActivity.class);
					intent.putExtra(TaskActivity.MESSAGE, row.message);
					intent.putExtra(TipActivity.IMAGE, row.image);
					
					me.startActivity(intent);
				}
				else
				{
					String descIndex = position / 35 + "." + (position % 35);
					
					NotificationRow prompt = messages.get(position);
					
					while (position > 0 && "prompt".equals(prompt.type.toLowerCase()) == false)
					{
						position -= 1;
						prompt = messages.get(position);
					}
					
					Intent intent = new Intent(me, TipActivity.class);
					intent.putExtra(TipActivity.MESSAGE, row.message);
					intent.putExtra(TipActivity.IMAGE, row.image);
					intent.putExtra(TipActivity.TASK, prompt.message);
					intent.putExtra(TipActivity.INDEX, descIndex);
					
					me.startActivity(intent);

					HashMap<String, Object> payload = new HashMap<String, Object>();
					payload.put("message_index", descIndex);
					LogManager.getInstance(me).log("notification_shown", payload);
				}
			}
		});
	}
}
