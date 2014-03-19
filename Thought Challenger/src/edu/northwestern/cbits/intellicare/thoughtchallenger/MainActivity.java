package edu.northwestern.cbits.intellicare.thoughtchallenger;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class MainActivity extends ConsentedActivity 
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_main);
		
		final MainActivity me = this;
		
		Button challenge = (Button) this.findViewById(R.id.button_challenge);
		
		challenge.setOnClickListener(new OnClickListener()
		{
			public void onClick(View arg0) 
			{
				Intent intent = new Intent(me, CatchActivity.class);
				
				me.startActivity(intent);
			}
		});

		Button review = (Button) this.findViewById(R.id.button_review);
		
		review.setOnClickListener(new OnClickListener()
		{
			public void onClick(View arg0) 
			{
				Cursor c = me.getContentResolver().query(ThoughtContentProvider.THOUGHT_PAIR_URI, null, null, null, null);
				
				if (c.getCount() > 0)
				{
					Intent intent = new Intent(me, ReviewActivity.class);
					
					me.startActivity(intent);
				}
				else
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(me);
					builder.setTitle(R.string.title_challenges_needed);
					builder.setMessage(R.string.message_challenges_needed);
					
					builder.setPositiveButton(R.string.action_challenge, new DialogInterface.OnClickListener() 
					{
						public void onClick(DialogInterface dialog, int which) 
						{
							Intent intent = new Intent(me, CatchActivity.class);
							me.startActivity(intent);
						}
					});

					builder.setNegativeButton(R.string.action_close, new DialogInterface.OnClickListener() 
					{
						public void onClick(DialogInterface dialog, int which) 
						{

						}
					});
					
					builder.create().show();
				}
			}
		});
	}
}
