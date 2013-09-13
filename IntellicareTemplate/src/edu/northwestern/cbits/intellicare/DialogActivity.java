package edu.northwestern.cbits.intellicare;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.HashMap;

import edu.northwestern.cbits.ic_template.R;

public class DialogActivity extends Activity
{
	public static String DIALOG_MESSAGE = "dialog_message";
	public static String DIALOG_TITLE = "dialog_title";
	public static String DIALOG_CONFIRM_BUTTON = "dialog_confirm";
	public static String DIALOG_CANCEL_BUTTON= "dialog_cancel";

	public static String DIALOG_CONFIRM_SCRIPT = "dialog_confirm_script";
	public static String DIALOG_CANCEL_SCRIPT = "dialog_cancel_script";
	
	private static ArrayList<DialogActivity> _dialogStack = new ArrayList<DialogActivity>();

	protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.activity_dialog);

        getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        
        while (DialogActivity._dialogStack.size() > 4)
        {
        	DialogActivity activity = DialogActivity._dialogStack.remove(0);
        	
        	activity.finish();
        }
        
        DialogActivity._dialogStack.add(this);
    }
	
	protected void onStop()
	{
		super.onStop();
		
		DialogActivity._dialogStack.remove(this);
	}
	
	protected void onPause()
	{
		super.onPause();

        Intent intent = this.getIntent();

        final String title = intent.getStringExtra(DialogActivity.DIALOG_TITLE);
        final String message = intent.getStringExtra(DialogActivity.DIALOG_MESSAGE);

        HashMap <String, Object> payload = new HashMap<String, Object>();
		payload.put("title", title);
		payload.put("message", message);
	}

	protected void onResume()
	{
		super.onResume();

		final DialogActivity me = this;

        final TextView messageText = (TextView) this.findViewById(R.id.text_dialog_message);

        Intent intent = this.getIntent();

        final String title = intent.getStringExtra(DialogActivity.DIALOG_TITLE);
        final String message = intent.getStringExtra(DialogActivity.DIALOG_MESSAGE);

        final String confirmScript = intent.getStringExtra(DialogActivity.DIALOG_CONFIRM_SCRIPT);

        this.setTitle(title);

        messageText.setText(message);

        Button confirmButton = (Button) this.findViewById(R.id.button_dialog_confirm);
        confirmButton.setText(intent.getStringExtra(DialogActivity.DIALOG_CONFIRM_BUTTON));

		HashMap <String, Object> payload = new HashMap<String, Object>();
		payload.put("title", title);
		payload.put("message", message);
		
        confirmButton.setOnClickListener(new OnClickListener()
        {
			public void onClick(View v)
			{
				// TODO: Confirm activity...

				me.finish();
			}
        });

        Button cancelButton = (Button) this.findViewById(R.id.button_dialog_cancel);

        if (intent.hasExtra(DialogActivity.DIALOG_CANCEL_BUTTON))
        {
			cancelButton.setText(intent.getStringExtra(DialogActivity.DIALOG_CANCEL_BUTTON));

            if (intent.hasExtra(DialogActivity.DIALOG_CANCEL_SCRIPT))
            {
	            final String cancelScript = intent.getStringExtra(DialogActivity.DIALOG_CANCEL_SCRIPT);
	
	            cancelButton.setOnClickListener(new OnClickListener()
	            {
	    			public void onClick(View v)
	    			{
	    				// TODO: Cancel activity...
	    				
	    				me.finish();
	    			}
	            });
            }

            cancelButton.setVisibility(View.VISIBLE);
        }
        else
            cancelButton.setVisibility(View.GONE);
        	
    }
}