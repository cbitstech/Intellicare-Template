package edu.northwestern.cbits.intellicare.slumbertime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class ClockActivity extends Activity 
{
	protected static final String ACTIVE_BRIGHTNESS_OPTION = "active_brightness_level";
	protected static final String REST_BRIGHTNESS_OPTION = "rest_brightness_level";
	protected static final float DEFAULT_ACTIVE_BRIGHTNESS = 1.0f;
	protected static final float DEFAULT_REST_BRIGHTNESS = 0.25f;
	protected static final String DIM_DELAY_OPTION = "dim_delay_duration";
	protected static final int DEFAULT_DIM_DELAY = 4;
	protected static final String DIM_DARK_OPTION = "dim_when_dark";
	protected static final boolean DIM_DARK_DEFAULT = true;
	
	private Handler _handler = null;
	private long _lastEventQuery = 0;
	private Event _lastEvent = null;
	protected float _currentBrightness;
	
	private class Alarm
	{
		String ringtone = null;
		String dateString = null;
		
		public Alarm(String ringtone, String dateString)
		{
			this.ringtone = ringtone;
			this.dateString = dateString;
		}
	}
	
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        this.setContentView(R.layout.activity_clock);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        {
            View root = this.findViewById(R.id.clock_root);

            root.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        }
        
		final ClockActivity me = this;

        ImageView alarms = (ImageView) this.findViewById(R.id.button_alarms);
        ImageView log = (ImageView) this.findViewById(R.id.button_log);
        ImageView tips = (ImageView) this.findViewById(R.id.button_tips);
        ImageView settings = (ImageView) this.findViewById(R.id.button_settings);
        
        alarms.setColorFilter(0x80000000);
        log.setColorFilter(0x80000000);
        tips.setColorFilter(0x80000000);
        settings.setColorFilter(0x80000000);
        
        alarms.setOnClickListener(new OnClickListener()
        {
			public void onClick(View arg0) 
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(me);

				LayoutInflater inflater = LayoutInflater.from(me);
				View view = inflater.inflate(R.layout.view_clock_alarms, null, false);
				
				final ListView alarmsList = (ListView) view.findViewById(R.id.list_alarms);
				final TextView selectMessage = (TextView) view.findViewById(R.id.select_alarm);
				final LinearLayout alarmEditor = (LinearLayout) view.findViewById(R.id.editor_alarm);
				
				final ArrayList<Alarm> alarms = new ArrayList<Alarm>();
				alarms.add(new Alarm("'Bad', Michael Jackson", "4am (MTWThF)"));
				alarms.add(new Alarm("Klaxon", "8:30am (SaSu)"));
				alarms.add(new Alarm("'Mr. Sandman', The Chordettes", "1:30pm (TTh)"));

				alarmsList.setFocusable(false);
				alarmsList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
				
				alarmsList.setOnItemSelectedListener(new OnItemSelectedListener()
				{
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						
						Log.e("ST", "ALBUM SELECT: " + arg2);
						
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
						Log.e("ST", "NO SELECT: ");
						
					}
					
				});

				ArrayAdapter<Alarm> adapter = new ArrayAdapter<Alarm>(me, R.layout.row_alarm, alarms)
				{
					public View getView (int position, View convertView, ViewGroup parent)
					{
						if (convertView == null)
						{
		    				LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		    				convertView = inflater.inflate(R.layout.row_alarm, parent, false);
						}
						
						TextView title = (TextView) convertView.findViewById(R.id.title_alarm);
						title.setText(this.getItem(position).ringtone);

						TextView times = (TextView) convertView.findViewById(R.id.times_alarm);
						times.setText(this.getItem(position).dateString);

						return convertView;
					}
				};
				
				alarmsList.setAdapter(adapter);
				
				alarmsList.setOnItemClickListener(new OnItemClickListener()
				{
					public void onItemClick(AdapterView<?> arg0, View arg1, int which, long arg3) 
					{
						alarmEditor.setVisibility(View.VISIBLE);
						selectMessage.setVisibility(View.GONE);
						
						Alarm alarm = alarms.get(which);
						
						TextView ringtone = (TextView) alarmEditor.findViewById(R.id.label_alarm_tone);
						TextView time = (TextView) alarmEditor.findViewById(R.id.label_alarm_name);
						
						ringtone.setText(alarm.ringtone);
						time.setText(alarm.dateString);
					}
				});

				builder = builder.setView(view);

				builder.setNegativeButton(R.string.button_close, new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{

					}
				});

				AlertDialog d = builder.create();
				d.show();
				
				DisplayMetrics metrics = me.getResources().getDisplayMetrics();
				
				WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

				lp.copyFrom(d.getWindow().getAttributes());
				lp.width = (int) (480f * metrics.density);

				d.getWindow().setAttributes(lp);
			}
        });
        
        log.setOnClickListener(new OnClickListener()
        {
			public void onClick(View arg0) 
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(me);
				
				builder = builder.setTitle(R.string.title_clock_log);
				
				LayoutInflater inflater = LayoutInflater.from(me);
				View view = inflater.inflate(R.layout.view_clock_log, null, false);
				
				builder = builder.setView(view);
				builder.setNegativeButton(R.string.button_discard, new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{

					}
				});
				
				builder.setPositiveButton(R.string.button_save, new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						Toast.makeText(me, "tOdO: saVe sLeeP lOg", Toast.LENGTH_LONG).show();
					}
				});

				AlertDialog d = builder.create();
				d.show();
				
				
				DisplayMetrics metrics = me.getResources().getDisplayMetrics();
				
				WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

				lp.copyFrom(d.getWindow().getAttributes());
				lp.width = (int) (480f * metrics.density);

				d.getWindow().setAttributes(lp);
			}
        });

        tips.setOnClickListener(new OnClickListener()
        {
			public void onClick(View arg0) 
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(me);
				
				builder = builder.setTitle(R.string.title_clock_tips);
				
				LayoutInflater inflater = LayoutInflater.from(me);
				View view = inflater.inflate(R.layout.view_clock_tips, null, false);
				
				GridView contentGrid = (GridView) view.findViewById(R.id.root_grid);
				
				final ArrayList<String> testTitles = new ArrayList<String>();
				testTitles.add("A brief talk on sleeping well.");
				testTitles.add("Joe sings a lullaby.");
				testTitles.add("The sounds of the restful forest.");
				testTitles.add("A brief calming breathing exercise.");
				testTitles.add("Sleep exercises from Purple Chill.");
				testTitles.add("Mark Twain on sleep.");
				testTitles.add("How the experts sleep. (CNN)");

				ArrayAdapter<String> adapter = new ArrayAdapter<String>(me, R.layout.cell_tip, testTitles)
				{
					public View getView (int position, View convertView, ViewGroup parent)
					{
						if (convertView == null)
						{
		    				LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		    				convertView = inflater.inflate(R.layout.cell_tip, parent, false);
						}
						
						TextView title = (TextView) convertView.findViewById(R.id.title_tip);
						
						title.setText(this.getItem(position));
						
						return convertView;
					}
				};
				
				contentGrid.setAdapter(adapter);
				
				contentGrid.setOnItemClickListener(new OnItemClickListener()
				{
					public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) 
					{
						Toast.makeText(me, "tOdO: " + testTitles.get(arg2), Toast.LENGTH_SHORT).show();
					}
				});
				
				builder = builder.setView(view);
				builder.setNegativeButton(R.string.button_close, new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{

					}
				});

				AlertDialog d = builder.create();
				d.show();
				
				DisplayMetrics metrics = me.getResources().getDisplayMetrics();
				
				WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

				lp.copyFrom(d.getWindow().getAttributes());
				lp.width = (int) (480f * metrics.density);
				lp.height = (int) (320f * metrics.density);

				d.getWindow().setAttributes(lp);
			}
        });

        settings.setOnClickListener(new OnClickListener()
        {
			public void onClick(View arg0) 
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(me);
				
				builder = builder.setTitle(R.string.title_clock_settings);
				
				LayoutInflater inflater = LayoutInflater.from(me);
				View view = inflater.inflate(R.layout.view_clock_settings, null, false);
				
				final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(me);

				SeekBar activeBrightness = (SeekBar) view.findViewById(R.id.active_brightness);
				activeBrightness.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
				{
					public void onProgressChanged(SeekBar bar, int position, boolean fromUser) 
					{
						if (fromUser)
						{
							WindowManager.LayoutParams params = me.getWindow().getAttributes();
	
							params.screenBrightness = ((float) position) / 100f;
	
							me.getWindow().setAttributes(params);
						}
					}

					public void onStartTrackingTouch(SeekBar bar) 
					{
						WindowManager.LayoutParams params = me.getWindow().getAttributes();
						
						me._currentBrightness = params.screenBrightness;
					}

					public void onStopTrackingTouch(SeekBar bar)
					{
						WindowManager.LayoutParams params = me.getWindow().getAttributes();
						params.screenBrightness = me._currentBrightness;
						
						me.getWindow().setAttributes(params);
						
						Editor e = prefs.edit();
						e.putFloat(ClockActivity.ACTIVE_BRIGHTNESS_OPTION, ((float) bar.getProgress()) / 100f);
						e.commit();
					}
				});
				
				float defaultActiveBrightness = prefs.getFloat(ClockActivity.ACTIVE_BRIGHTNESS_OPTION, ClockActivity.DEFAULT_ACTIVE_BRIGHTNESS);
				activeBrightness.setProgress((int) (((float) activeBrightness.getMax()) * defaultActiveBrightness));

				SeekBar restBrightness = (SeekBar) view.findViewById(R.id.rest_brightness);
				restBrightness.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
				{
					public void onProgressChanged(SeekBar bar, int position, boolean fromUser) 
					{
						if (fromUser)
						{
							WindowManager.LayoutParams params = me.getWindow().getAttributes();
	
							params.screenBrightness = ((float) position) / 100f;
	
							me.getWindow().setAttributes(params);
						}
					}

					public void onStartTrackingTouch(SeekBar bar) 
					{
						WindowManager.LayoutParams params = me.getWindow().getAttributes();
						
						me._currentBrightness = params.screenBrightness;
					}

					public void onStopTrackingTouch(SeekBar bar)
					{
						WindowManager.LayoutParams params = me.getWindow().getAttributes();
						params.screenBrightness = me._currentBrightness;
						
						me.getWindow().setAttributes(params);
						
						SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(me);
						Editor e = prefs.edit();
						e.putFloat(ClockActivity.REST_BRIGHTNESS_OPTION, ((float) bar.getProgress()) / 100f);
						e.commit();
					}
				});

				float restActiveBrightness = prefs.getFloat(ClockActivity.REST_BRIGHTNESS_OPTION, ClockActivity.DEFAULT_REST_BRIGHTNESS);
				restBrightness.setProgress((int) (((float) activeBrightness.getMax()) * restActiveBrightness));

				final TextView dimLabel = (TextView) view.findViewById(R.id.dim_label);
				
				dimLabel.setText(me.getString(R.string.label_dim_delay, me.getTimeString(prefs.getInt(ClockActivity.DIM_DELAY_OPTION, ClockActivity.DEFAULT_DIM_DELAY) * 15)));

				SeekBar dimDelay = (SeekBar) view.findViewById(R.id.dim_delay);
				dimDelay.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
				{
					public void onProgressChanged(SeekBar bar, int position, boolean fromUser) 
					{
						dimLabel.setText(me.getString(R.string.label_dim_delay, me.getTimeString(position * 15)));
					}

					public void onStartTrackingTouch(SeekBar bar) 
					{

					}

					public void onStopTrackingTouch(SeekBar bar)
					{
						SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(me);
						Editor e = prefs.edit();
						e.putInt(ClockActivity.DIM_DELAY_OPTION, bar.getProgress());
						e.commit();
					}
				});
				
				dimDelay.setProgress(prefs.getInt(ClockActivity.DIM_DELAY_OPTION, ClockActivity.DEFAULT_DIM_DELAY));
				
				final CheckBox darkDim = (CheckBox) view.findViewById(R.id.dark_dim);
				darkDim.setOnCheckedChangeListener(new OnCheckedChangeListener()
				{
					public void onCheckedChanged(CompoundButton check,	boolean checked)
					{
						Editor e = prefs.edit();
						e.putBoolean(ClockActivity.DIM_DARK_OPTION, checked);
						e.commit();
					}
				});
				
				darkDim.setChecked(prefs.getBoolean(ClockActivity.DIM_DARK_OPTION, ClockActivity.DIM_DARK_DEFAULT));

				builder = builder.setView(view);
				builder = builder.setPositiveButton(R.string.button_close, new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{

					}
				});
				
				AlertDialog d = builder.create();
				d.show();
				
				DisplayMetrics metrics = me.getResources().getDisplayMetrics();
				
				WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

				lp.copyFrom(d.getWindow().getAttributes());
				lp.width = (int) (480f * metrics.density);

				d.getWindow().setAttributes(lp);
			}
        });
    }

	protected String getTimeString(int seconds) 
	{
		if (seconds % 60 == 0)
		{
			if (seconds == 0)
				return this.getString(R.string.dim_delay_immediately);
			else if (seconds == 60)
				return this.getString(R.string.dim_delay_minute);
			else
				return this.getString(R.string.dim_delay_minutes, seconds / 60);
		}
		
		return this.getString(R.string.dim_delay_seconds, seconds);
	}

	protected void onResume() 
	{
		super.onResume();
		
		if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
		{
			this.finish();
			
			Intent homeIntent = new Intent(this, HomeActivity.class);
			this.startActivity(homeIntent);
		}
		
		if (this._handler == null)
		{
			this._handler = new Handler();
			
			final ClockActivity me = this;
			
			this._handler.postDelayed(new Runnable()
			{
				public void run() 
				{
					me.updateClock();

					if (me._handler != null)
						me._handler.postDelayed(this, 250);
				}
				
			}, 250);
		}
	}
	
	protected void onPause()
	{
		super.onPause();
		
		this._handler.removeCallbacksAndMessages(null);
		this._handler = null;
	}

	protected void updateClock() 
	{
		TextView dateText = (TextView) this.findViewById(R.id.date_view);
		TextView timeText = (TextView) this.findViewById(R.id.time_view);
		TextView apptText = (TextView) this.findViewById(R.id.appointment_view);
		TextView ampmText = (TextView) this.findViewById(R.id.ampm_view);
		
		Date now = new Date();
		
		DateFormat dateFormat = new SimpleDateFormat("EEEE, LLLL d, yyyy");
		
		dateText.setText(dateFormat.format(now));
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		boolean useAmPm = prefs.getBoolean("display_am_pm", true);
		
		SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm");
		
		if (useAmPm)
		{
			SimpleDateFormat ampmFormat = new SimpleDateFormat("a");

			ampmText.setVisibility(View.VISIBLE);
			ampmText.setText(ampmFormat.format(now));
		}
		else
		{
			ampmText.setVisibility(View.GONE);
			
			timeFormat = new SimpleDateFormat("H:mm");
		}
		
		timeText.setText(timeFormat.format(now));
		
		Event event = this.getNextEvent();
		
		if (event != null)
		{
			SimpleDateFormat apptFormat = new SimpleDateFormat("EEEE, " + timeFormat.toPattern());

			apptText.setText(this.getString(R.string.label_upcoming_appointment, event.title, apptFormat.format(new Date(event.timestamp))));
		}
		else
			apptText.setText(R.string.label_no_appointments);
	}
	
	private class Event
	{
		public String title;
		public long timestamp;
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private Event getNextEvent() 
	{
		long now = System.currentTimeMillis();
		
		if (now - this._lastEventQuery  > 60000)
		{
			this._lastEventQuery = now;

	        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	        {
	        	String[] projection = { CalendarContract.Instances.BEGIN, CalendarContract.Instances.EVENT_ID };
			
	        	Cursor c = CalendarContract.Instances.query(this.getContentResolver(), projection, now, now + (1000 * 3600 * 24 * 6)); 
	        	
	        	String title = null;
	        	long timestamp = Long.MAX_VALUE;
	        	
	        	while (c.moveToNext())
	        	{
	        		long eventTime = c.getLong(c.getColumnIndex(CalendarContract.Instances.BEGIN));
	        		
	        		if (eventTime < timestamp)
	        		{
	        			timestamp = eventTime;
	        			
		        		long eventId = c.getLong(c.getColumnIndex(CalendarContract.Instances.EVENT_ID));
		        		
		        		String eventSelection = CalendarContract.Events._ID + " = ?";
		        		String[] eventArgs = { "" + eventId };
		        		Cursor eventCursor = this.getContentResolver().query(CalendarContract.Events.CONTENT_URI, null, eventSelection, eventArgs, null);
		        		
		        		if (eventCursor.moveToNext())
			        		title = eventCursor.getString(eventCursor.getColumnIndex(CalendarContract.Events.TITLE));
		        		
		        		eventCursor.close();
	        		}
	        	}
	        	
	        	c.close();
	        	
	        	if (title != null)
	        	{
	        		this._lastEvent = new Event();
	        		this._lastEvent.timestamp = timestamp;
	        		this._lastEvent.title = title;
	        		
	        		return this._lastEvent;
	        	}
	        	
	        	return null;
	        }
		}
		
		return this._lastEvent;
	}
}
