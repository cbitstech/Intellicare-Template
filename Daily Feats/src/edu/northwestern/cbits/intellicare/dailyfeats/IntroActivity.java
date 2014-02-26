package edu.northwestern.cbits.intellicare.dailyfeats;

import java.security.SecureRandom;
import java.util.HashMap;

import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TimePicker;
import android.widget.Toast;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class IntroActivity extends ConsentedActivity 
{
	public static final String INTRO_SHOWN = "intro_shown";

	private int mStep = 0;

	protected int _dialogId = 0;
	
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.activity_intro);

        final IntroActivity me = this;
        
        Button back = (Button) this.findViewById(R.id.back_button);
        back.setOnClickListener(new OnClickListener()
        {
			public void onClick(View view) 
			{
				if (me.mStep > 0)
					me.mStep -= 1;
				
				me.updateLayout();
			}
        });

        Button next = (Button) this.findViewById(R.id.next_button);
        next.setOnClickListener(new OnClickListener()
        {
			public void onClick(View view) 
			{
		        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(me.getApplicationContext());

				if (me.mStep == 1 && prefs.contains(FeatsProvider.DEPRESSION_LEVEL) == false)
				{
					Toast.makeText(me, R.string.depression_toast, Toast.LENGTH_LONG).show();
					return;
				}
				else if (me.mStep == 4)
				{
			    	int hour = prefs.getInt(ScheduleManager.REMINDER_HOUR, ScheduleManager.DEFAULT_HOUR);
			        int minutes = prefs.getInt(ScheduleManager.REMINDER_MINUTE, ScheduleManager.DEFAULT_MINUTE);

					TimePickerDialog timeDialog = new TimePickerDialog(me, new OnTimeSetListener()
					{
						public void onTimeSet(TimePicker arg0, int hour, int minute) 
						{
					        Editor editor = prefs.edit();
					        
					        editor.putInt(ScheduleManager.REMINDER_HOUR, hour);
					        editor.putInt(ScheduleManager.REMINDER_MINUTE, minute);
					        editor.commit();

							HashMap<String, Object> payload = new HashMap<String, Object>();
							payload.put("hour", hour);
							payload.put("minute", minute);
							payload.put("source", "intro");
							
							LogManager.getInstance(me).log("set_reminder_time", payload);

							me.mStep += 1;
							me.updateLayout();
						}
						
					}, hour, minutes, DateFormat.is24HourFormat(me));
					
					timeDialog.show();

					return;
				}
				
				if (me.mStep < (me.getResources().getStringArray(R.array.intro_urls).length - 1))
				{
					me.mStep += 1;
					me.updateLayout();
				}
				else
				{
					Editor e = prefs.edit();
					e.putBoolean(IntroActivity.INTRO_SHOWN, true);
					e.putLong(FeatsProvider.START_FEATS_DATE, System.currentTimeMillis());
					e.commit();
					
					LogManager.getInstance(me).log("completed_intro", null);

					me.startActivity(new Intent(me, CalendarActivity.class));
					me.finish();
				}
			}
        });
        
        RadioGroup depression = (RadioGroup) this.findViewById(R.id.depression_level);
        depression.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
			public void onCheckedChanged(RadioGroup group, int id) 
			{
		        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(me.getApplicationContext());
				Editor e = prefs.edit();
				
				switch(id)
				{
					case R.id.depression_question_0:
						e.putInt(FeatsProvider.DEPRESSION_LEVEL, 1);
						break;
					case R.id.depression_question_1:
						e.putInt(FeatsProvider.DEPRESSION_LEVEL, 2);
						break;
					case R.id.depression_question_2:
						e.putInt(FeatsProvider.DEPRESSION_LEVEL, 3);
						break;
					case R.id.depression_question_3:
						e.putInt(FeatsProvider.DEPRESSION_LEVEL, 4);
						break;
				}
				
				e.commit();
			}
        });
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        if (prefs.contains("settings_full_mode") == false)
        {
        	Editor e = prefs.edit();
        	
        	SecureRandom random = new SecureRandom();
        	random.setSeed(System.currentTimeMillis());
        	
        	boolean isFull = random.nextBoolean(); 
        	e.putBoolean("settings_full_mode", isFull);
        	e.commit();

			HashMap<String, Object> payload = new HashMap<String, Object>();
			payload.put("full_mode", isFull);
        	
        	LogManager.getInstance(this).log("set_mode", payload);
        }
    }
    
    protected void onResume()
    {
    	super.onResume();
    	
    	this.updateLayout();
    }

    private void updateLayout() 
	{
		LinearLayout webLayout = (LinearLayout) this.findViewById(R.id.web_layout);
		LinearLayout moodLayout = (LinearLayout) this.findViewById(R.id.mood_layout);
		
		webLayout.setVisibility(View.GONE);
		moodLayout.setVisibility(View.GONE);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());

        Button back = (Button) this.findViewById(R.id.back_button);
        
        if (this.mStep == 0)
        	back.setVisibility(View.INVISIBLE);
        else
        	back.setVisibility(View.VISIBLE);
		
		WebView webView = (WebView) webLayout.findViewById(R.id.web_view);
		
		String[] urls = this.getResources().getStringArray(R.array.intro_urls);
		String[] titles = this.getResources().getStringArray(R.array.intro_titles);

		this.getSupportActionBar().setTitle(titles[this.mStep]);

		switch (this.mStep)
		{
			case 1:
				Editor e = prefs.edit();
				e.remove(FeatsProvider.DEPRESSION_LEVEL);
				e.commit();
				
				moodLayout.setVisibility(View.VISIBLE);
				break;
			case 3:
				webLayout.setVisibility(View.VISIBLE);
				
				int level = prefs.getInt(FeatsProvider.DEPRESSION_LEVEL, 0); 

				switch (level)
				{
					case 4:
						webView.loadUrl("file:///android_asset/help_3_4.html");
						break;
					case 3:
						webView.loadUrl("file:///android_asset/help_3_3.html");
						break;
					default:
						webView.loadUrl("file:///android_asset/help_3_12.html");
						break;
				}
				
				FeatsProvider.updateLevel(this, level);

				break;
			default:
				webLayout.setVisibility(View.VISIBLE);
				webView.loadUrl(urls[this.mStep]);
				break;
		}
	}
}
