package edu.northwestern.cbits.intellicare.moveme;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class MotivatorsActivity extends ContentIndexActivity 
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		this.getSupportActionBar().setTitle(R.string.title_motivators);
		this.getSupportActionBar().setSubtitle(R.string.subtitle_motivators);
	}

	protected int titlesArrayId() 
	{
		return R.array.array_motivator_titles;
	}

	protected int urlsArrayId() 
	{
		return R.array.array_motivator_urls;
	}
	
	protected void openUri(Uri uri, String title) 
	{
		if ("get_motivated.pages".equalsIgnoreCase(uri.getLastPathSegment()))
		{
			Intent intent = new Intent(this, GetMotivatedActivity.class);
			this.startActivity(intent);
		}
		else if ("motivators.pages".equalsIgnoreCase(uri.getLastPathSegment()))
		{
			Intent intent = new Intent(this, MotivatorListActivity.class);
			this.startActivity(intent);
		}
		else if ("going.dialog".equalsIgnoreCase(uri.getLastPathSegment()))
			this.showGoingDialog();
		else
			super.openUri(uri, title);
	}

	private void showGoingDialog() 
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		builder.setTitle(R.string.title_past_motivators);
		builder.setMultiChoiceItems(R.array.array_past_motivations, null, new DialogInterface.OnMultiChoiceClickListener()
		{
			public void onClick(DialogInterface dialog, int which, boolean checked) 
			{

			}
		});
		
		builder.setPositiveButton(R.string.action_continue, new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog, int which) 
			{

			}
		});
		
		builder.create().show();
	}
}
