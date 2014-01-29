package edu.northwestern.cbits.intellicare.slumbertime;

import java.text.DateFormat;
import java.util.Calendar;

import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class AddSleepDiaryActivity extends ConsentedActivity
{
	protected int _bedHour = -1;
	protected int _bedMinute = -1;
	protected int _sleepHour = -1;
	protected int _sleepMinute = -1;
	protected int _wakeHour = -1;
	protected int _wakeMinute = -1;
	protected int _upHour = -1;
	protected int _upMinute = -1;
	protected int _sleepDelay = -1;
	protected int _wakeCount = -1;
	protected int _sleepQuality = -1;

	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_add_diary);
		
		this.getSupportActionBar().setTitle(R.string.tool_sleep_diary);

		final AddSleepDiaryActivity me = this;

		final boolean useAmPm = android.text.format.DateFormat.is24HourFormat(this);
		
		final DateFormat format = android.text.format.DateFormat.getTimeFormat(this);
		final Calendar calendar = Calendar.getInstance();
		
		final TextView bedTime = (TextView) this.findViewById(R.id.field_bed_time);
		bedTime.setOnClickListener(new OnClickListener()
		{
			public void onClick(View view) 
			{
				TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() 
				{
					public void onTimeSet(TimePicker view, int hourOfDay, int minute) 
					{
						calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
						calendar.set(Calendar.MINUTE, minute);
						
						bedTime.setText(format.format(calendar.getTime()));
						
						me._bedHour = hourOfDay;
						me._bedMinute = minute;
					}
				};
				
				TimePickerDialog picker = new TimePickerDialog(me, timeSetListener, 12, 00, useAmPm);
				picker.show();
			}
		});
		
		final TextView sleepTime = (TextView) this.findViewById(R.id.field_sleep_time);
		sleepTime.setOnClickListener(new OnClickListener()
		{
			public void onClick(View view) 
			{
				TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() 
				{
					public void onTimeSet(TimePicker view, int hourOfDay, int minute) 
					{
						calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
						calendar.set(Calendar.MINUTE, minute);
						
						sleepTime.setText(format.format(calendar.getTime()));
						
						me._sleepHour = hourOfDay;
						me._sleepMinute = minute;
					}
				};
				
				TimePickerDialog picker = new TimePickerDialog(me, timeSetListener, 12, 00, useAmPm);
				picker.show();
			}
		});

		final TextView wakeTime = (TextView) this.findViewById(R.id.field_wake_time);
		wakeTime.setOnClickListener(new OnClickListener()
		{
			public void onClick(View view) 
			{
				TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() 
				{
					public void onTimeSet(TimePicker view, int hourOfDay, int minute) 
					{
						calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
						calendar.set(Calendar.MINUTE, minute);
						
						wakeTime.setText(format.format(calendar.getTime()));
						
						me._wakeHour = hourOfDay;
						me._wakeMinute = minute;
					}
				};

				
				TimePickerDialog picker = new TimePickerDialog(me, timeSetListener, 12, 00, useAmPm);
				picker.show();
			}
		});

		final TextView upTime = (TextView) this.findViewById(R.id.field_up_time);
		upTime.setOnClickListener(new OnClickListener()
		{
			public void onClick(View view) 
			{
				TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() 
				{
					public void onTimeSet(TimePicker view, int hourOfDay, int minute) 
					{
						calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
						calendar.set(Calendar.MINUTE, minute);
						
						upTime.setText(format.format(calendar.getTime()));
						
						me._upHour = hourOfDay;
						me._upMinute = minute;
					}
				};

				
				TimePickerDialog picker = new TimePickerDialog(me, timeSetListener, 12, 00, useAmPm);
				picker.show();
			}
		});
		
		final TextView sleepLabel = (TextView) this.findViewById(R.id.label_sleep_delay);
		
		final SeekBar sleepDelay = (SeekBar) this.findViewById(R.id.field_sleep_delay);
		sleepDelay.setMax(8);
		
		sleepDelay.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
		{
			public void onProgressChanged(SeekBar bar, int position, boolean fromUser) 
			{
				switch (position)
				{
					case 0:
						sleepLabel.setText(R.string.value_immediately);
						me._sleepDelay  = 0;
						
						break;
					case 1:
						sleepLabel.setText(R.string.value_five_min);
						me._sleepDelay  = 5;
						
						break;
					case 2:
						sleepLabel.setText(R.string.value_fifteen_min);
						me._sleepDelay  = 15;
						
						break;
					case 3:
						sleepLabel.setText(R.string.value_thirty_min);
						me._sleepDelay  = 30;
						
						break;
					case 4:
						sleepLabel.setText(R.string.value_one_hour);
						me._sleepDelay  = 60;
						
						break;
					case 5:
						sleepLabel.setText(R.string.value_two_hour);
						me._sleepDelay  = 120;
						
						break;
					case 6:
						sleepLabel.setText(R.string.value_four_hour);
						me._sleepDelay  = 240;
						
						break;
					case 7:
						sleepLabel.setText(R.string.value_eight_hour);
						me._sleepDelay  = 480;
						
						break;
					case 8:
						sleepLabel.setText(R.string.value_never);
						me._sleepDelay  = 960;
						
						break;
				}
				
			}

			public void onStartTrackingTouch(SeekBar arg0) 
			{

			}

			public void onStopTrackingTouch(SeekBar arg0) 
			{

			}
		});

		final TextView wakeLabel = (TextView) this.findViewById(R.id.label_wake_count);
		
		final SeekBar wakeCount = (SeekBar) this.findViewById(R.id.field_wake_count);
		wakeCount.setMax(11);
		
		wakeCount.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
		{
			public void onProgressChanged(SeekBar bar, int position, boolean fromUser) 
			{
				me._wakeCount  = position;

				switch (position)
				{
					case 0:
						wakeLabel.setText(R.string.value_never);
						
						break;
					case 1:
						wakeLabel.setText(R.string.value_once);
						
						break;
					case 11:
						wakeLabel.setText(R.string.value_greater_ten);
						me._wakeCount  = 100;
						
						break;
					default:
						wakeLabel.setText(me.getString(R.string.value_multiple_times, position));
						
						break;
				}
			}

			public void onStartTrackingTouch(SeekBar arg0) 
			{

			}

			public void onStopTrackingTouch(SeekBar arg0) 
			{

			}
		});

		final TextView qualityLabel = (TextView) this.findViewById(R.id.label_sleep_quality);
		
		final SeekBar sleepQuality = (SeekBar) this.findViewById(R.id.field_sleep_quality);
		sleepQuality.setMax(4);
		
		sleepQuality.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
		{
			public void onProgressChanged(SeekBar bar, int position, boolean fromUser) 
			{
				me._sleepQuality   = position;

				switch (position)
				{
					case 0:
						qualityLabel.setText(R.string.value_terrible);
						break;
					case 1:
						qualityLabel.setText(R.string.value_poor);
						break;
					case 2:
						qualityLabel.setText(R.string.value_adequate);
						break;
					case 3:
						qualityLabel.setText(R.string.value_good);
						break;
					case 4:
						qualityLabel.setText(R.string.value_excellent);
						break;
				}
			}

			public void onStartTrackingTouch(SeekBar arg0) 
			{

			}

			public void onStopTrackingTouch(SeekBar arg0) 
			{

			}
		});
	}

	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_add_sleep_diary, menu);

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.action_save)
		{
			ContentValues values = new ContentValues();
			
			RadioGroup napRadios = (RadioGroup) this.findViewById(R.id.radios_nap);
			int napChecked = napRadios.getCheckedRadioButtonId();
			
			if (napChecked == -1)
			{
				Toast.makeText(this, R.string.message_complete_nap, Toast.LENGTH_SHORT).show();
				return true;
			}
			
			values.put(SlumberContentProvider.DIARY_NAP, (napChecked == R.id.nap_yes));

			RadioGroup earlierRadios = (RadioGroup) this.findViewById(R.id.radios_earlier);

			int earlierChecked = earlierRadios.getCheckedRadioButtonId();

			if (earlierChecked == -1)
			{
				Toast.makeText(this, R.string.message_complete_earlier, Toast.LENGTH_SHORT).show();
				return true;
			}

			values.put(SlumberContentProvider.DIARY_EARLIER, (earlierChecked == R.id.nap_yes));
			
			if (this._bedHour == -1 || this._bedMinute == -1)
			{
				Toast.makeText(this, R.string.message_complete_bedtime, Toast.LENGTH_SHORT).show();
				return true;
			}

			values.put(SlumberContentProvider.DIARY_BED_HOUR, this._bedHour);
			values.put(SlumberContentProvider.DIARY_BED_MINUTE, this._bedMinute);

			if (this._sleepHour == -1 || this._sleepMinute == -1)
			{
				Toast.makeText(this, R.string.message_complete_sleeptime, Toast.LENGTH_SHORT).show();
				return true;
			}

			values.put(SlumberContentProvider.DIARY_SLEEP_HOUR, this._bedHour);
			values.put(SlumberContentProvider.DIARY_SLEEP_MINUTE, this._bedMinute);

			
			if (this._wakeHour == -1 || this._wakeMinute == -1)
			{
				Toast.makeText(this, R.string.message_complete_waketime, Toast.LENGTH_SHORT).show();
				return true;
			}

			values.put(SlumberContentProvider.DIARY_WAKE_HOUR, this._bedHour);
			values.put(SlumberContentProvider.DIARY_WAKE_MINUTE, this._bedMinute);

			
			if (this._upHour == -1 || this._upMinute == -1)
			{
				Toast.makeText(this, R.string.message_complete_uptime, Toast.LENGTH_SHORT).show();
				return true;
			}

			values.put(SlumberContentProvider.DIARY_UP_HOUR, this._bedHour);
			values.put(SlumberContentProvider.DIARY_UP_MINUTE, this._bedMinute);

			if (this._sleepDelay == -1)
			{
				Toast.makeText(this, R.string.message_complete_sleep_delay, Toast.LENGTH_SHORT).show();
				return true;
			}

			values.put(SlumberContentProvider.DIARY_SLEEP_DELAY, this._sleepDelay);

			if (this._wakeCount == -1)
			{
				Toast.makeText(this, R.string.message_complete_wake_count, Toast.LENGTH_SHORT).show();
				return true;
			}

			values.put(SlumberContentProvider.DIARY_WAKE_COUNT, this._wakeCount);

			if (this._sleepQuality == -1)
			{
				Toast.makeText(this, R.string.message_complete_sleep_quality, Toast.LENGTH_SHORT).show();
				return true;
			}

			values.put(SlumberContentProvider.DIARY_SLEEP_QUALITY, this._sleepQuality);
			
			EditText comments = (EditText) this.findViewById(R.id.field_comments);

			values.put(SlumberContentProvider.DIARY_COMMENTS, comments.getEditableText().toString());
			
			values.put(SlumberContentProvider.DIARY_TIMESTAMP, System.currentTimeMillis());
			
			this.getContentResolver().insert(SlumberContentProvider.SLEEP_DIARIES_URI, values);
			
			Toast.makeText(this, R.string.toast_sleep_diary_recorded, Toast.LENGTH_SHORT).show();
			
			this.finish();
		}

		return true;
	}
}
