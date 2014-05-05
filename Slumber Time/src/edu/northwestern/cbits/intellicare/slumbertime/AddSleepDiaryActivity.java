package edu.northwestern.cbits.intellicare.slumbertime;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.HashMap;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class AddSleepDiaryActivity extends ConsentedActivity
{
	private static final String SELECTED_RADIO = "selected_radio";
	private static final String SELECTED_RADIO_ALCOHOL = "selected_alcohol";
	private static final String SELECTED_RADIO_CAFFEINE = "selected_caffeine";
	private static final String SELECTED_EARLIER = "selected_earlier";
	private static final String SELECTED_BED_HOUR = "selected_bed_hour";
	private static final String SELECTED_BED_MINUTE = "selected_bed_minute";
	private static final String SELECTED_SLEEP_HOUR = "selected_sleep_hour";
	private static final String SELECTED_SLEEP_MINUTE = "selected_sleep_minute";
	private static final String SELECTED_WAKE_HOUR = "selected_wake_hour";
	private static final String SELECTED_UP_HOUR = "selected_up_hour";
	private static final String SELECTED_WAKE_MINUTE = "selected_wake_minute";
	private static final String SELECTED_UP_MINUTE = "selected_up_minute";
	private static final String SELECTED_SLEEP_DELAY = "selected_sleep_delay";
	private static final String SELECTED_WAKE_COUNT = "selected_wake_count";
	private static final String SELECTED_SLEEP_QUALITY = "selected_sleep_quality";
	private static final String SELECTED_RESTED = "selected_rested";

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
	protected int _sleepRested = -1;
	protected String _napDuration = null;
	protected String _alcoholAmount = null;
	protected String _alcoholTime = null;
	protected String _caffeineTime = null;
	protected String _caffeineAmount = null;

	public static final Uri URI = Uri.parse("intellicare://slumber/sleep-diary");

	protected void onSaveInstanceState (Bundle outState)
	{
		super.onSaveInstanceState(outState);
		
		ContentValues values = new ContentValues();
		
		RadioGroup napRadios = (RadioGroup) this.findViewById(R.id.radios_nap);
		int napChecked = napRadios.getCheckedRadioButtonId();
		
		if (napChecked != -1)
			outState.putInt(AddSleepDiaryActivity.SELECTED_RADIO, napChecked);

		RadioGroup alcoholRadios = (RadioGroup) this.findViewById(R.id.radios_alcohol);
		int alcoholChecked = alcoholRadios.getCheckedRadioButtonId();
		
		if (alcoholChecked != -1)
			outState.putInt(AddSleepDiaryActivity.SELECTED_RADIO_ALCOHOL, alcoholChecked);
		
		RadioGroup caffeineRadios = (RadioGroup) this.findViewById(R.id.radios_caffeine);
		int caffeineChecked = caffeineRadios.getCheckedRadioButtonId();
		
		if (caffeineChecked != -1)
			outState.putInt(AddSleepDiaryActivity.SELECTED_RADIO_CAFFEINE, caffeineChecked);

		RadioGroup earlierRadios = (RadioGroup) this.findViewById(R.id.radios_earlier);
		int earlierChecked = earlierRadios.getCheckedRadioButtonId();

		if (earlierChecked != -1)
			outState.putInt(AddSleepDiaryActivity.SELECTED_EARLIER, napChecked);

		values.put(SlumberContentProvider.DIARY_EARLIER, (earlierChecked == R.id.nap_yes));
		
		if (this._bedHour != -1 || this._bedMinute != -1)
		{
			outState.putInt(AddSleepDiaryActivity.SELECTED_BED_HOUR, this._bedHour);
			outState.putInt(AddSleepDiaryActivity.SELECTED_BED_MINUTE, this._bedMinute);
		}

		if (this._sleepHour != -1 || this._sleepMinute != -1)
		{
			outState.putInt(AddSleepDiaryActivity.SELECTED_SLEEP_HOUR, this._sleepHour);
			outState.putInt(AddSleepDiaryActivity.SELECTED_SLEEP_MINUTE, this._sleepMinute);
		}

		if (this._wakeHour != -1 || this._wakeMinute != -1)
		{
			outState.putInt(AddSleepDiaryActivity.SELECTED_WAKE_HOUR, this._wakeHour);
			outState.putInt(AddSleepDiaryActivity.SELECTED_WAKE_MINUTE, this._wakeMinute);
		}
		
		if (this._upHour != -1 || this._upMinute != -1)
		{
			outState.putInt(AddSleepDiaryActivity.SELECTED_UP_HOUR, this._upHour);
			outState.putInt(AddSleepDiaryActivity.SELECTED_UP_MINUTE, this._upMinute);
		}

		if (this._sleepDelay != -1)
			outState.putInt(AddSleepDiaryActivity.SELECTED_SLEEP_DELAY, this._sleepDelay);

		if (this._wakeCount != -1)
			outState.putInt(AddSleepDiaryActivity.SELECTED_WAKE_COUNT, this._wakeCount);

		if (this._sleepQuality != -1)
			outState.putInt(AddSleepDiaryActivity.SELECTED_SLEEP_QUALITY, this._sleepQuality);

		if (this._sleepRested  != -1)
			outState.putInt(AddSleepDiaryActivity.SELECTED_RESTED, this._sleepRested);
	}

	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState == null)
			savedInstanceState = new Bundle();

		this.setContentView(R.layout.activity_add_diary);
		
		this.getSupportActionBar().setTitle(R.string.tool_sleep_diary);

		final AddSleepDiaryActivity me = this;

		final boolean useAmPm = android.text.format.DateFormat.is24HourFormat(this);
		
		final DateFormat format = android.text.format.DateFormat.getTimeFormat(this);
		final Calendar calendar = Calendar.getInstance();

		if (savedInstanceState.containsKey(AddSleepDiaryActivity.SELECTED_BED_HOUR))
			me._bedHour = savedInstanceState.getInt(AddSleepDiaryActivity.SELECTED_BED_HOUR);

		if (savedInstanceState.containsKey(AddSleepDiaryActivity.SELECTED_BED_MINUTE))
			me._bedMinute = savedInstanceState.getInt(AddSleepDiaryActivity.SELECTED_BED_MINUTE);
		
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
				
				TimePickerDialog picker = null;
				
				if (me._bedHour != -1 && me._bedMinute != -1)
					picker = new TimePickerDialog(me, timeSetListener, me._bedHour, me._bedMinute, useAmPm);
				else
					picker = new TimePickerDialog(me, timeSetListener, 12, 0, useAmPm);

				picker.show();
			}
		});

		if (this._bedHour != -1 && this._bedMinute != -1)
		{
			calendar.set(Calendar.HOUR_OF_DAY, me._bedHour);
			calendar.set(Calendar.MINUTE, me._bedMinute);
			
			bedTime.setText(format.format(calendar.getTime()));
		}

		if (savedInstanceState.containsKey(AddSleepDiaryActivity.SELECTED_SLEEP_HOUR))
			me._sleepHour = savedInstanceState.getInt(AddSleepDiaryActivity.SELECTED_SLEEP_HOUR);

		if (savedInstanceState.containsKey(AddSleepDiaryActivity.SELECTED_SLEEP_MINUTE))
			me._sleepMinute = savedInstanceState.getInt(AddSleepDiaryActivity.SELECTED_SLEEP_MINUTE);

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

				TimePickerDialog picker = null;
				
				if (me._sleepHour != -1 && me._sleepMinute != -1)
					picker = new TimePickerDialog(me, timeSetListener, me._sleepHour, me._sleepMinute, useAmPm);
				else
					picker = new TimePickerDialog(me, timeSetListener, 12, 0, useAmPm);

				picker.show();
			}
		});

		if (this._sleepHour != -1 && this._sleepMinute != -1)
		{
			calendar.set(Calendar.HOUR_OF_DAY, me._sleepHour);
			calendar.set(Calendar.MINUTE, me._sleepMinute);
			
			sleepTime.setText(format.format(calendar.getTime()));
		}

		if (savedInstanceState.containsKey(AddSleepDiaryActivity.SELECTED_WAKE_HOUR))
			me._wakeHour = savedInstanceState.getInt(AddSleepDiaryActivity.SELECTED_WAKE_HOUR);

		if (savedInstanceState.containsKey(AddSleepDiaryActivity.SELECTED_WAKE_MINUTE))
			me._wakeMinute = savedInstanceState.getInt(AddSleepDiaryActivity.SELECTED_WAKE_MINUTE);

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
				
				TimePickerDialog picker = null;
				
				if (me._wakeHour != -1 && me._wakeMinute != -1)
					picker = new TimePickerDialog(me, timeSetListener, me._wakeHour, me._wakeMinute, useAmPm);
				else
					picker = new TimePickerDialog(me, timeSetListener, 12, 0, useAmPm);

				picker.show();
			}
		});

		if (this._wakeHour != -1 && this._wakeMinute != -1)
		{
			calendar.set(Calendar.HOUR_OF_DAY, me._wakeHour);
			calendar.set(Calendar.MINUTE, me._wakeMinute);
			
			wakeTime.setText(format.format(calendar.getTime()));
		}

		if (savedInstanceState.containsKey(AddSleepDiaryActivity.SELECTED_UP_HOUR))
			me._upHour = savedInstanceState.getInt(AddSleepDiaryActivity.SELECTED_UP_HOUR);

		if (savedInstanceState.containsKey(AddSleepDiaryActivity.SELECTED_UP_MINUTE))
			me._upMinute = savedInstanceState.getInt(AddSleepDiaryActivity.SELECTED_UP_MINUTE);

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

				TimePickerDialog picker = null;
				
				if (me._upHour != -1 && me._upMinute != -1)
					picker = new TimePickerDialog(me, timeSetListener, me._upHour, me._upMinute, useAmPm);
				else
					picker = new TimePickerDialog(me, timeSetListener, 12, 0, useAmPm);

				picker.show();
			}
		});

		if (this._upHour != -1 && this._upMinute != -1)
		{
			calendar.set(Calendar.HOUR_OF_DAY, me._upHour);
			calendar.set(Calendar.MINUTE, me._upMinute);
			
			upTime.setText(format.format(calendar.getTime()));
		}

		final TextView sleepLabel = (TextView) this.findViewById(R.id.label_sleep_delay);

		final SeekBar sleepDelay = (SeekBar) this.findViewById(R.id.field_sleep_delay);
		sleepDelay.setMax(8);
		
		if (savedInstanceState.containsKey(AddSleepDiaryActivity.SELECTED_SLEEP_DELAY))
			sleepDelay.setProgress(savedInstanceState.getInt(AddSleepDiaryActivity.SELECTED_SLEEP_DELAY));

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
		
		if (savedInstanceState.containsKey(AddSleepDiaryActivity.SELECTED_WAKE_COUNT))
			wakeCount.setProgress(savedInstanceState.getInt(AddSleepDiaryActivity.SELECTED_WAKE_COUNT));

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

		if (savedInstanceState.containsKey(AddSleepDiaryActivity.SELECTED_SLEEP_QUALITY))
			sleepQuality.setProgress(savedInstanceState.getInt(AddSleepDiaryActivity.SELECTED_SLEEP_QUALITY));

		sleepQuality.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
		{
			public void onProgressChanged(SeekBar bar, int position, boolean fromUser) 
			{
				me._sleepQuality = position;

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
		
		final TextView restedLabel = (TextView) this.findViewById(R.id.label_rested);
		
		final SeekBar rested = (SeekBar) this.findViewById(R.id.field_rested);
		rested.setMax(4);

		if (savedInstanceState.containsKey(AddSleepDiaryActivity.SELECTED_RESTED))
			rested.setProgress(savedInstanceState.getInt(AddSleepDiaryActivity.SELECTED_RESTED));

		rested.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
		{
			public void onProgressChanged(SeekBar bar, int position, boolean fromUser) 
			{
				me._sleepRested  = position;

				switch (position)
				{
					case 0:
						restedLabel.setText(R.string.label_not_rested);
						break;
					case 1:
						restedLabel.setText(R.string.label_slightly_rested);
						break;
					case 2:
						restedLabel.setText(R.string.label_somewhat_rested);
						break;
					case 3:
						restedLabel.setText(R.string.label_well_rested);
						break;
					case 4:
						restedLabel.setText(R.string.label_very_rested);
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

		RadioGroup nap = (RadioGroup) this.findViewById(R.id.radios_nap);
		
		final View napLength = this.findViewById(R.id.question_nap_length);
		final TextView napLengthTap = (TextView) this.findViewById(R.id.field_nap_length);
		
		nap.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			public void onCheckedChanged(RadioGroup arg0, int id) 
			{
				if (id == R.id.nap_yes)
				{
					napLength.setVisibility(View.VISIBLE);
					napLengthTap.setVisibility(View.VISIBLE);
				}
				else
				{
					napLength.setVisibility(View.GONE);
					napLengthTap.setVisibility(View.GONE);
				}
			}
		});
		
		napLengthTap.setOnClickListener(new OnClickListener()
		{
			public void onClick(View arg0) 
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(me);
				builder.setTitle(R.string.question_nap_length);
				
				final String[] items = me.getResources().getStringArray(R.array.nap_labels);
				
				builder.setItems(items, new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						me._napDuration = items[which];
						napLengthTap.setText(items[which]);
					}
				});
				
				builder.create().show();
			}
		});

		if (savedInstanceState.containsKey(AddSleepDiaryActivity.SELECTED_RADIO))
			nap.check(savedInstanceState.getInt(AddSleepDiaryActivity.SELECTED_RADIO));

		RadioGroup alcohol = (RadioGroup) this.findViewById(R.id.radios_alcohol);
		
		final View alcoholAmount = this.findViewById(R.id.question_alcohol_quantity);
		final TextView alcoholAmountTap = (TextView) this.findViewById(R.id.field_alcohol_quantity);

		final View alcoholTime = this.findViewById(R.id.question_alcohol_time);
		final TextView alcoholTimeTap = (TextView) this.findViewById(R.id.field_alcohol_time);

		alcohol.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			public void onCheckedChanged(RadioGroup arg0, int id) 
			{
				if (id == R.id.alcohol_yes)
				{
					alcoholTime.setVisibility(View.VISIBLE);
					alcoholTimeTap.setVisibility(View.VISIBLE);
					alcoholAmount.setVisibility(View.VISIBLE);
					alcoholAmountTap.setVisibility(View.VISIBLE);
				}
				else
				{
					alcoholTime.setVisibility(View.GONE);
					alcoholTimeTap.setVisibility(View.GONE);
					alcoholAmount.setVisibility(View.GONE);
					alcoholAmountTap.setVisibility(View.GONE);
				}
			}
		});
		
		alcoholAmountTap.setOnClickListener(new OnClickListener()
		{
			public void onClick(View arg0) 
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(me);
				builder.setTitle(R.string.question_alcohol_amount);
				
				final String[] items = me.getResources().getStringArray(R.array.alcohol_amount_labels);
				
				builder.setItems(items, new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						me._alcoholAmount  = items[which];
						alcoholAmountTap.setText(items[which]);
					}
				});
				
				builder.create().show();
			}
		});

		alcoholTimeTap.setOnClickListener(new OnClickListener()
		{
			public void onClick(View arg0) 
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(me);
				builder.setTitle(R.string.question_alcohol_time);
				
				final String[] items = me.getResources().getStringArray(R.array.alcohol_time_labels);
				
				builder.setItems(items, new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						me._alcoholTime   = items[which];
						alcoholTimeTap.setText(items[which]);
					}
				});
				
				builder.create().show();
			}
		});

		if (savedInstanceState.containsKey(AddSleepDiaryActivity.SELECTED_RADIO_ALCOHOL))
			alcohol.check(savedInstanceState.getInt(AddSleepDiaryActivity.SELECTED_RADIO_ALCOHOL));


		RadioGroup caffeine = (RadioGroup) this.findViewById(R.id.radios_caffeine);
		
		final View caffeineAmount = this.findViewById(R.id.question_caffeine_quantity);
		final TextView caffeineAmountTap = (TextView) this.findViewById(R.id.field_caffeine_quantity);

		final View caffeineTime = this.findViewById(R.id.question_caffeine_time);
		final TextView caffeineTimeTap = (TextView) this.findViewById(R.id.field_caffeine_time);

		caffeine.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			public void onCheckedChanged(RadioGroup arg0, int id) 
			{
				if (id == R.id.caffeine_yes)
				{
					caffeineTime.setVisibility(View.VISIBLE);
					caffeineTimeTap.setVisibility(View.VISIBLE);
					caffeineAmount.setVisibility(View.VISIBLE);
					caffeineAmountTap.setVisibility(View.VISIBLE);
				}
				else
				{
					caffeineTime.setVisibility(View.GONE);
					caffeineTimeTap.setVisibility(View.GONE);
					caffeineAmount.setVisibility(View.GONE);
					caffeineAmountTap.setVisibility(View.GONE);
				}
			}
		});
		
		caffeineAmountTap.setOnClickListener(new OnClickListener()
		{
			public void onClick(View arg0) 
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(me);
				builder.setTitle(R.string.question_caffeine_amount);
				
				final String[] items = me.getResources().getStringArray(R.array.caffeine_amount_labels);
				
				builder.setItems(items, new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						me._caffeineAmount  = items[which];
						caffeineAmountTap.setText(items[which]);
					}
				});
				
				builder.create().show();
			}
		});

		caffeineTimeTap.setOnClickListener(new OnClickListener()
		{
			public void onClick(View arg0) 
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(me);
				builder.setTitle(R.string.question_caffeine_time);
				
				final String[] items = me.getResources().getStringArray(R.array.alcohol_time_labels);
				
				builder.setItems(items, new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						me._caffeineTime = items[which];
						caffeineTimeTap.setText(items[which]);
					}
				});
				
				builder.create().show();
			}
		});

		if (savedInstanceState.containsKey(AddSleepDiaryActivity.SELECTED_RADIO_CAFFEINE))
			caffeine.check(savedInstanceState.getInt(AddSleepDiaryActivity.SELECTED_RADIO_CAFFEINE));
		
		RadioGroup earlier = (RadioGroup) this.findViewById(R.id.radios_earlier);

		if (savedInstanceState.containsKey(AddSleepDiaryActivity.SELECTED_EARLIER))
			earlier.check(savedInstanceState.getInt(AddSleepDiaryActivity.SELECTED_EARLIER));
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
			HashMap<String, Object> payload = new HashMap<String, Object>();
			
			RadioGroup napRadios = (RadioGroup) this.findViewById(R.id.radios_nap);
			int napChecked = napRadios.getCheckedRadioButtonId();
			
			if (napChecked == -1)
			{
				Toast.makeText(this, R.string.message_complete_nap, Toast.LENGTH_SHORT).show();
				return true;
			}
			
			if (napChecked == R.id.nap_yes)
			{
				values.put(SlumberContentProvider.DIARY_NAP, true);
				
				if (this._napDuration != null)
					values.put(SlumberContentProvider.DIARY_NAP_DURATION, this._napDuration);
			}

			payload.put("napped", (napChecked == R.id.nap_yes));
			
			RadioGroup alcoholRadios = (RadioGroup) this.findViewById(R.id.radios_alcohol);
			int alcoholChecked = alcoholRadios.getCheckedRadioButtonId();
			
			if (alcoholChecked == -1)
			{
				Toast.makeText(this, R.string.message_complete_alcohol, Toast.LENGTH_SHORT).show();
				return true;
			}
			
			if (alcoholChecked == R.id.alcohol_yes)
			{
				values.put(SlumberContentProvider.DIARY_ALCOHOL, true);
				
				if (this._alcoholAmount != null)
					values.put(SlumberContentProvider.DIARY_ALCOHOL_AMOUNT, this._alcoholAmount);

				if (this._alcoholTime != null)
					values.put(SlumberContentProvider.DIARY_ALCOHOL_TIME, this._alcoholTime);
			}

			payload.put("alcohol", (alcoholChecked == R.id.alcohol_yes));

			RadioGroup caffeineRadios = (RadioGroup) this.findViewById(R.id.radios_caffeine);
			int caffeineChecked = caffeineRadios.getCheckedRadioButtonId();
			
			if (caffeineChecked == -1)
			{
				Toast.makeText(this, R.string.message_complete_caffeine, Toast.LENGTH_SHORT).show();
				return true;
			}
			
			if (caffeineChecked == R.id.caffeine_yes)
			{
				values.put(SlumberContentProvider.DIARY_CAFFEINE, true);
				
				if (this._caffeineAmount != null)
					values.put(SlumberContentProvider.DIARY_CAFFEINE_AMOUNT, this._caffeineAmount);

				if (this._caffeineTime != null)
					values.put(SlumberContentProvider.DIARY_CAFFEINE_TIME, this._caffeineTime);
			}

			payload.put("caffeine", (caffeineChecked == R.id.caffeine_yes));

			RadioGroup earlierRadios = (RadioGroup) this.findViewById(R.id.radios_earlier);

			int earlierChecked = earlierRadios.getCheckedRadioButtonId();

			if (earlierChecked == -1)
			{
				Toast.makeText(this, R.string.message_complete_earlier, Toast.LENGTH_SHORT).show();
				return true;
			}
			
			values.put(SlumberContentProvider.DIARY_EARLIER, (earlierChecked == R.id.earlier_yes));

			payload.put("earlier", (earlierChecked == R.id.earlier_yes));

			if (this._bedHour == -1 || this._bedMinute == -1)
			{
				Toast.makeText(this, R.string.message_complete_bedtime, Toast.LENGTH_SHORT).show();
				return true;
			}

			values.put(SlumberContentProvider.DIARY_BED_HOUR, this._bedHour);
			values.put(SlumberContentProvider.DIARY_BED_MINUTE, this._bedMinute);

			payload.put("bed_hour", this._bedHour);
			payload.put("bed_minute", this._bedMinute);

			if (this._sleepHour == -1 || this._sleepMinute == -1)
			{
				Toast.makeText(this, R.string.message_complete_sleeptime, Toast.LENGTH_SHORT).show();
				return true;
			}

			values.put(SlumberContentProvider.DIARY_SLEEP_HOUR, this._sleepHour);
			values.put(SlumberContentProvider.DIARY_SLEEP_MINUTE, this._sleepMinute);

			payload.put("sleep_hour", this._sleepHour);
			payload.put("sleep_minute", this._sleepMinute);
			
			if (this._wakeHour == -1 || this._wakeMinute == -1)
			{
				Toast.makeText(this, R.string.message_complete_waketime, Toast.LENGTH_SHORT).show();
				return true;
			}

			values.put(SlumberContentProvider.DIARY_WAKE_HOUR, this._wakeHour);
			values.put(SlumberContentProvider.DIARY_WAKE_MINUTE, this._wakeMinute);

			payload.put("wake_hour", this._wakeHour);
			payload.put("wake_minute", this._wakeMinute);
			
			if (this._upHour == -1 || this._upMinute == -1)
			{
				Toast.makeText(this, R.string.message_complete_uptime, Toast.LENGTH_SHORT).show();
				return true;
			}

			values.put(SlumberContentProvider.DIARY_UP_HOUR, this._upHour);
			values.put(SlumberContentProvider.DIARY_UP_MINUTE, this._upMinute);

			payload.put("up_hour", this._upHour);
			payload.put("up_minute", this._upMinute);

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

			payload.put("wake_count", this._wakeCount);

			if (this._sleepQuality == -1)
			{
				Toast.makeText(this, R.string.message_complete_sleep_quality, Toast.LENGTH_SHORT).show();
				return true;
			}

			values.put(SlumberContentProvider.DIARY_SLEEP_QUALITY, this._sleepQuality);

			payload.put("sleep_quality", this._sleepQuality);

			values.put(SlumberContentProvider.DIARY_RESTED, this._sleepRested);

			payload.put("sleep_rested", this._sleepRested);

			values.put(SlumberContentProvider.DIARY_TIMESTAMP, System.currentTimeMillis());
			
			this.getContentResolver().insert(SlumberContentProvider.SLEEP_DIARIES_URI, values);

			LogManager.getInstance(this).log("logged_diary", payload);
			
			Toast.makeText(this, R.string.toast_sleep_diary_recorded, Toast.LENGTH_SHORT).show();
			
			this.finish();
		}

		return true;
	}
	
	protected void onResume()
	{
		super.onResume();
		
		HashMap<String, Object> payload = new HashMap<String, Object>();
		LogManager.getInstance(this).log("launched_diary_activity", payload);
	}

	protected void onPause()
	{
		HashMap<String, Object> payload = new HashMap<String, Object>();
		LogManager.getInstance(this).log("closed_diary_activity", payload);

		super.onPause();
	}
}
