package edu.northwestern.cbits.intellicare.icope;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import edu.northwestern.cbits.intellicare.logging.LogManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ViewCardActivity extends Activity 
{
	protected static final String REMINDER_ID = "reminder_id";

	public static final Uri URI = Uri.parse("intellicare://icope/reminder");
	
	protected void onResume()
	{
		super.onResume();
		
		Intent intent = this.getIntent();
		
		if (intent.getData() != null)
		{
			Uri u = intent.getData();
			
			List<String> segments = u.getPathSegments();
			
			String last = segments.get(segments.size() - 1);
			
			try
			{
				long id = Long.parseLong(last);
				
				intent.putExtra(ViewCardActivity.REMINDER_ID, id);
			}
			catch (NumberFormatException e)
			{
				LogManager.getInstance(this).logException(e);
			}
		}
		
		final ViewCardActivity me = this;
		
		if (intent.hasExtra(ViewCardActivity.REMINDER_ID))
		{
			String selection = CopeContentProvider.ID + " = ?";
			String[] args = { "" + intent.getLongExtra(ViewCardActivity.REMINDER_ID, -1) };
			
			Cursor cursor = this.getContentResolver().query(CopeContentProvider.REMINDER_URI, null, selection, args, null);
			
			if (cursor.moveToNext())
			{
				ContextThemeWrapper wrapper = new ContextThemeWrapper(this, R.style.Theme_AppCompat_Light);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(wrapper);
				builder.setTitle(R.string.title_card_reminder);

				LayoutInflater inflater = LayoutInflater.from(wrapper);
				final View view = inflater.inflate(R.layout.view_reminder, null, false);

				DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(this);
				DateFormat dateFormat = android.text.format.DateFormat.getLongDateFormat(this);

				Calendar c = Calendar.getInstance();
				c.set(Calendar.HOUR_OF_DAY, cursor.getInt(cursor.getColumnIndex(CopeContentProvider.REMINDER_HOUR)));
				c.set(Calendar.MINUTE, cursor.getInt(cursor.getColumnIndex(CopeContentProvider.REMINDER_MINUTE)));

				Date date = c.getTime();
				
				TextView reminderTime = (TextView) view.findViewById(R.id.label_reminder_time);

				reminderTime.setText(dateFormat.format(date) + " @ " + timeFormat.format(date));
				
				selection = CopeContentProvider.CARD_ID + " = ?";
				String selectionArgs[] = { "" + cursor.getLong(cursor.getColumnIndex(CopeContentProvider.REMINDER_CARD_ID)) };
				
				Cursor cardCursor = this.getContentResolver().query(CopeContentProvider.CARD_URI, null, selection, selectionArgs, null);
				
				if (cardCursor.moveToNext())
				{
					TextView event = (TextView) view.findViewById(R.id.label_reminder_event);
					TextView reminder = (TextView) view.findViewById(R.id.label_reminder_reminder);
					TextView type = (TextView) view.findViewById(R.id.label_reminder_type);
					
					event.setText(cardCursor.getString(cardCursor.getColumnIndex(CopeContentProvider.CARD_EVENT)));
					reminder.setText(cardCursor.getString(cardCursor.getColumnIndex(CopeContentProvider.CARD_REMINDER)));
					type.setText(cardCursor.getString(cardCursor.getColumnIndex(CopeContentProvider.CARD_TYPE)));
				}
				
				cardCursor.close();
				
				builder.setPositiveButton(R.string.action_helpful, new OnClickListener()
				{
					public void onClick(DialogInterface arg0, int arg1) 
					{
						Log.e("IC", "TODO: LOG POSITIVE");
						
						me.finish();
					}
				});

				builder.setNegativeButton(R.string.action_not_helpful, new OnClickListener()
				{
					public void onClick(DialogInterface arg0, int arg1) 
					{
						Log.e("IC", "TODO: LOG NEGATIVE");

						me.finish();
					}
				});
				
				builder.setOnCancelListener(new OnCancelListener()
				{
					public void onCancel(DialogInterface arg0) 
					{
						me.finish();
					}
				});

				builder.setView(view);
				
				final AlertDialog dialog = builder.create();
				
		        dialog.setOnShowListener(new OnShowListener() 
		        {
		            public void onShow(DialogInterface dialogInterface) 
		            {
		        	    Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
		        	    positive.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_action_like, 0);

		        	    Button negative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
		        	    negative.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_dontlike, 0, 0, 0);
		            }
		        });
		        
		        dialog.show();
			}
			
			cursor.close();
		}
		else
		{
			this.finish();
		}
	}
}
