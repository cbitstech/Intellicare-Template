package edu.northwestern.cbits.intellicare;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout.LayoutParams;
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

	protected void onResume()
	{
		super.onResume();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        Intent intent = this.getIntent();

        final String title = intent.getStringExtra(DialogActivity.DIALOG_TITLE);
        final String message = intent.getStringExtra(DialogActivity.DIALOG_MESSAGE);
        final String confirm = intent.getStringExtra(DialogActivity.DIALOG_CONFIRM_BUTTON);

        final DialogActivity me = this;
        
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(confirm, new DialogInterface.OnClickListener() 
        {
        	public void onClick(DialogInterface dialog, int id) 
        	{
        		me.finish();
        	}
        });
        
        if (intent.hasExtra(DialogActivity.DIALOG_CANCEL_BUTTON))
        {
            final String cancel = intent.getStringExtra(DialogActivity.DIALOG_CANCEL_BUTTON);

        	builder.setNegativeButton(cancel, new DialogInterface.OnClickListener() 
	        {
	        	public void onClick(DialogInterface dialog, int id) 
	        	{
	        		me.finish();
	        	}
	    	});
        }

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() 
        {
			public void onCancel(DialogInterface dialog) 
			{
        		me.finish();
			}
		});

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}