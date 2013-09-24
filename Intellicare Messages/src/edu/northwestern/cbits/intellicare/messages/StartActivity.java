package edu.northwestern.cbits.intellicare.messages;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class StartActivity extends ConsentedActivity 
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_start);
		
		Button basicButton = (Button) this.findViewById(R.id.test_basic);
		basicButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View view) 
			{

			}
		});

		Button imageButton = (Button) this.findViewById(R.id.test_image);
		imageButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View view) 
			{

			}
		});

		Button actionButton = (Button) this.findViewById(R.id.test_action);
		actionButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View view) 
			{

			}
		});
	}
	
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_start, menu);

		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.action_settings)
		{
			Toast.makeText(this, "TODO: Show Settings", Toast.LENGTH_LONG).show();
		}
		else if (item.getItemId() == R.id.action_help)
		{
			Toast.makeText(this, "TODO: Show Help", Toast.LENGTH_LONG).show();
		}
		
		return true;
	}
}
