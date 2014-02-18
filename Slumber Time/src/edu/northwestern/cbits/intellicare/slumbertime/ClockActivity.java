package edu.northwestern.cbits.intellicare.slumbertime;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import edu.northwestern.cbits.intellicare.logging.LogManager;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.provider.MediaStore.Audio;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class ClockActivity extends Activity implements SensorEventListener 
{
	protected static final String ACTIVE_BRIGHTNESS_OPTION = "active_brightness_level";
	protected static final String REST_BRIGHTNESS_OPTION = "rest_brightness_level";
	protected static final float DEFAULT_ACTIVE_BRIGHTNESS = 1.0f;
	protected static final float DEFAULT_REST_BRIGHTNESS = 0.25f;
	protected static final String DIM_DELAY_OPTION = "dim_delay_duration";
	protected static final int DEFAULT_DIM_DELAY = 4;
	protected static final String DIM_DARK_OPTION = "dim_when_dark";
	protected static final boolean DIM_DARK_DEFAULT = true;
	protected static final long SAMPLE_RATE = 60000;
	
	private long _lastLightReading = 0;
	private long _lastTemperatureReading = 0;
	
	private Handler _handler = null;
	private long _lastEventQuery = 0;
	private Event _lastEvent = null;
	
	private BroadcastReceiver _startAlarmReceiver = null;
	private BroadcastReceiver _endAlarmReceiver = null;
	private Uri _lastAudioUri = null;
	
	private AlertDialog _alarmDialog = null;
	
	private boolean _sampleAudio = true;
	
	protected float _currentBrightness;
	protected long _searchLastUpdate = 0;
	protected AlertDialog _toneListDialog = null;
	private SharedPreferences _cachedPreferences = null;
	private long _lastDimCheck = 0;
	private long _lastInteraction = 0;
	private float _lastBrightness = -99;
	private float _lastTemperature = -278;
	
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
			public void onClick(final View alarmView) 
			{
				HashMap<String, Object> payload = new HashMap<String, Object>();
				LogManager.getInstance(me).log("launched_alarm_view", payload);

				AlertDialog.Builder builder = new AlertDialog.Builder(me);

				LayoutInflater inflater = LayoutInflater.from(me);
				View view = inflater.inflate(R.layout.view_clock_alarms, null, false);
				
				final ListView alarmsList = (ListView) view.findViewById(R.id.list_alarms);
				final TextView selectMessage = (TextView) view.findViewById(R.id.select_alarm);
				final LinearLayout alarmEditor = (LinearLayout) view.findViewById(R.id.editor_alarm);
				
				alarmsList.setFocusable(false);
				alarmsList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
				
				final String[] emptyString = {};
				final int[] emptyInt = {};

				Cursor alarmsCursor = me.getContentResolver().query(SlumberContentProvider.ALARMS_URI, null, null, null, null);
				
				if (alarmsCursor.getCount() == 0)
				{
					alarmsCursor.close();
					
					ContentValues values = new ContentValues();
					values.put(SlumberContentProvider.ALARM_NAME, me.getString(R.string.name_new_alarm));
					
					me.getContentResolver().insert(SlumberContentProvider.ALARMS_URI, values);

					alarmsCursor = me.getContentResolver().query(SlumberContentProvider.ALARMS_URI, null, null, null, null);
				}
				
				final String[] columnNames = { "_id", "ADD_ITEM" };
				final String[] columnObjects = { "0", "ADD_ITEM" };
				
				MatrixCursor addItemCursor = new MatrixCursor(columnNames);
				addItemCursor.addRow(columnObjects);
				
				Cursor[] cursors = { alarmsCursor, addItemCursor };
				
				final MergeCursor merged = new MergeCursor(cursors);
				
				final SimpleCursorAdapter adapter = new SimpleCursorAdapter(me, R.layout.row_alarm, merged,  emptyString, emptyInt, 0)
				{
					public void bindView (View view, Context context, Cursor cursor)
					{
						LinearLayout albumCell = (LinearLayout) view.findViewById(R.id.album_cell);
						TextView addAlbumCell = (TextView) view.findViewById(R.id.add_album_cell);
						
						if (cursor.getColumnIndex("ADD_ITEM") != -1)
						{
							albumCell.setVisibility(View.GONE);
							addAlbumCell.setVisibility(View.VISIBLE);
						}
						else
						{
							albumCell.setVisibility(View.VISIBLE);
							addAlbumCell.setVisibility(View.GONE);

							final long id = cursor.getLong(cursor.getColumnIndex("_id"));

							final SimpleCursorAdapter meAdapter = this;
							
							TextView title = (TextView) view.findViewById(R.id.title_alarm);
							title.setText(cursor.getString(cursor.getColumnIndex(SlumberContentProvider.ALARM_NAME)));
	
							TextView times = (TextView) view.findViewById(R.id.times_alarm);
							times.setText(SlumberContentProvider.dateStringForAlarmCursor(me, cursor));
							
							CheckBox enabled = (CheckBox) view.findViewById(R.id.alarm_enabled);

							enabled.setOnCheckedChangeListener(new OnCheckedChangeListener()
							{
								public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
								{

								}
							});
							
							enabled.setChecked(cursor.getInt(cursor.getColumnIndex(SlumberContentProvider.ALARM_ENABLED)) > 0);
							
							enabled.setOnCheckedChangeListener(new OnCheckedChangeListener()
							{
								public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
								{
									ContentValues values = new ContentValues();
									values.put(SlumberContentProvider.ALARM_ENABLED, isChecked);
									
									String where = "_id = ?";
									String[] args = { "" + id };
									
									me.getContentResolver().update(SlumberContentProvider.ALARMS_URI, values, where, args);

									Cursor alarmsCursor = me.getContentResolver().query(SlumberContentProvider.ALARMS_URI, null, null, null, null);

									MatrixCursor addItemCursor = new MatrixCursor(columnNames);
									addItemCursor.addRow(columnObjects);
									
									Cursor[] cursors = { alarmsCursor, addItemCursor };
									final MergeCursor newMerged = new MergeCursor(cursors);
									
									Cursor old = meAdapter.swapCursor(newMerged);
									old.close();
									
									if (isChecked)
									{
										HashMap<String, Object> payload = new HashMap<String, Object>();
										LogManager.getInstance(me).log("enabled_alarm", payload);
									}
									else
									{
										HashMap<String, Object> payload = new HashMap<String, Object>();
										LogManager.getInstance(me).log("disabled_alarm", payload);
									}
								}
							});
						}
					}
				};
				
				alarmsList.setAdapter(adapter);
				
				alarmsList.setOnItemLongClickListener(new OnItemLongClickListener()
				{
					public boolean onItemLongClick(AdapterView<?> parent, View view, int position, final long id) 
					{
						Cursor c = adapter.getCursor();

						if (c.getColumnIndex("ADD_ITEM") == -1)
						{
							final String name = c.getString(c.getColumnIndex(SlumberContentProvider.ALARM_NAME));
							
							AlertDialog.Builder builder = new AlertDialog.Builder(me);
							builder = builder.setTitle(R.string.title_delete_alarm);
							builder = builder.setMessage(me.getString(R.string.message_delete_alarm, name));
							
							builder = builder.setPositiveButton(R.string.button_remove, new DialogInterface.OnClickListener() 
							{
								public void onClick(DialogInterface dialog, int which) 
								{
									String selection = "_id = ?";
									String[] args = { "" + id };
									
									me.getContentResolver().delete(SlumberContentProvider.ALARMS_URI, selection, args);

									Cursor alarmsCursor = me.getContentResolver().query(SlumberContentProvider.ALARMS_URI, null, null, null, null);

									MatrixCursor addItemCursor = new MatrixCursor(columnNames);
									addItemCursor.addRow(columnObjects);
									
									Cursor[] cursors = { alarmsCursor, addItemCursor };
									final MergeCursor newMerged = new MergeCursor(cursors);
									
									Cursor old = adapter.swapCursor(newMerged);
									old.close();
									
									HashMap<String, Object> payload = new HashMap<String, Object>();
									LogManager.getInstance(me).log("deleted_alarm", payload);
								}
							});

							builder = builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() 
							{
								public void onClick(DialogInterface dialog, int which) 
								{

								}
							});

							builder.create().show();
						}
						
						return true;
					}
				});
				
				alarmsList.setOnItemClickListener(new OnItemClickListener()
				{
					public void onItemClick(final AdapterView<?> parent, final View view, final int which, final long alarmId) 
					{
						Cursor c = adapter.getCursor();

						if (c.getColumnIndex("ADD_ITEM") != -1)
						{
							ContentValues values = new ContentValues();
							values.put(SlumberContentProvider.ALARM_NAME, me.getString(R.string.name_new_alarm));
							
							me.getContentResolver().insert(SlumberContentProvider.ALARMS_URI, values);

							Cursor alarmsCursor = me.getContentResolver().query(SlumberContentProvider.ALARMS_URI, null, null, null, null);

							MatrixCursor addItemCursor = new MatrixCursor(columnNames);
							addItemCursor.addRow(columnObjects);
							
							Cursor[] cursors = { alarmsCursor, addItemCursor };
							final MergeCursor newMerged = new MergeCursor(cursors);
							
							Cursor old = adapter.swapCursor(newMerged);
							old.close();

							return;
						}
						else
						{
							HashMap<String, Object> payload = new HashMap<String, Object>();
							LogManager.getInstance(me).log("selected_alarm", payload);

							final OnItemClickListener clickListener = this;
							
							alarmEditor.setVisibility(View.VISIBLE);
							selectMessage.setVisibility(View.GONE);
							
							String toneName = c.getString(c.getColumnIndex(SlumberContentProvider.ALARM_NAME));
							
							TextView ringtone = (TextView) alarmEditor.findViewById(R.id.label_alarm_tone);
							final TextView time = (TextView) alarmEditor.findViewById(R.id.label_alarm_name);
							
							CheckBox sunday = (CheckBox) alarmEditor.findViewById(R.id.check_sunday);
							CheckBox monday = (CheckBox) alarmEditor.findViewById(R.id.check_monday);
							CheckBox tuesday = (CheckBox) alarmEditor.findViewById(R.id.check_tuesday);
							CheckBox wednesday = (CheckBox) alarmEditor.findViewById(R.id.check_wednesday);
							CheckBox thursday = (CheckBox) alarmEditor.findViewById(R.id.check_thursday);
							CheckBox friday = (CheckBox) alarmEditor.findViewById(R.id.check_friday);
							CheckBox saturday = (CheckBox) alarmEditor.findViewById(R.id.check_saturday);
	
							sunday.setChecked(c.getInt(c.getColumnIndex(SlumberContentProvider.ALARM_SUNDAY)) > 0);
							monday.setChecked(c.getInt(c.getColumnIndex(SlumberContentProvider.ALARM_MONDAY)) > 0);
							tuesday.setChecked(c.getInt(c.getColumnIndex(SlumberContentProvider.ALARM_TUESDAY)) > 0);
							wednesday.setChecked(c.getInt(c.getColumnIndex(SlumberContentProvider.ALARM_WEDNESDAY)) > 0);
							thursday.setChecked(c.getInt(c.getColumnIndex(SlumberContentProvider.ALARM_THURSDAY)) > 0);
							friday.setChecked(c.getInt(c.getColumnIndex(SlumberContentProvider.ALARM_FRIDAY)) > 0);
							saturday.setChecked(c.getInt(c.getColumnIndex(SlumberContentProvider.ALARM_SATURDAY)) > 0);
							
							sunday.setOnCheckedChangeListener(new OnCheckedChangeListener()
							{
								public void onCheckedChanged(CompoundButton button, boolean checked) 
								{
									ContentValues values = new ContentValues();
									values.put(SlumberContentProvider.ALARM_SUNDAY, checked);
									
									String where = "_id = ?";
									String[] args = { "" + alarmId };
									
									me.getContentResolver().update(SlumberContentProvider.ALARMS_URI, values, where, args);
									
									Cursor alarmsCursor = me.getContentResolver().query(SlumberContentProvider.ALARMS_URI, null, null, null, null);

									MatrixCursor addItemCursor = new MatrixCursor(columnNames);
									addItemCursor.addRow(columnObjects);
									
									Cursor[] cursors = { alarmsCursor, addItemCursor };
									final MergeCursor newMerged = new MergeCursor(cursors);
									
									Cursor old = adapter.swapCursor(newMerged);
									old.close();
	
									clickListener.onItemClick(parent, view, which, alarmId);
									
									if (checked)
									{
										HashMap<String, Object> payload = new HashMap<String, Object>();
										LogManager.getInstance(me).log("selected_sunday", payload);
									}
									else
									{
										HashMap<String, Object> payload = new HashMap<String, Object>();
										LogManager.getInstance(me).log("deselected_sunday", payload);
									}
								}
							});
							
							monday.setOnCheckedChangeListener(new OnCheckedChangeListener()
							{
								public void onCheckedChanged(CompoundButton button, boolean checked) 
								{
									ContentValues values = new ContentValues();
									values.put(SlumberContentProvider.ALARM_MONDAY, checked);
									
									String where = "_id = ?";
									String[] args = { "" + alarmId };
									
									me.getContentResolver().update(SlumberContentProvider.ALARMS_URI, values, where, args);
	
									Cursor alarmsCursor = me.getContentResolver().query(SlumberContentProvider.ALARMS_URI, null, null, null, null);

									MatrixCursor addItemCursor = new MatrixCursor(columnNames);
									addItemCursor.addRow(columnObjects);
									
									Cursor[] cursors = { alarmsCursor, addItemCursor };
									final MergeCursor newMerged = new MergeCursor(cursors);
									
									Cursor old = adapter.swapCursor(newMerged);
									old.close();
	
									clickListener.onItemClick(parent, view, which, alarmId);
									
									if (checked)
									{
										HashMap<String, Object> payload = new HashMap<String, Object>();
										LogManager.getInstance(me).log("selected_monday", payload);
									}
									else
									{
										HashMap<String, Object> payload = new HashMap<String, Object>();
										LogManager.getInstance(me).log("deselected_monday", payload);
									}

								}
							});
	
							tuesday.setOnCheckedChangeListener(new OnCheckedChangeListener()
							{
								public void onCheckedChanged(CompoundButton button, boolean checked) 
								{
									ContentValues values = new ContentValues();
									values.put(SlumberContentProvider.ALARM_TUESDAY, checked);
									
									String where = "_id = ?";
									String[] args = { "" + alarmId };
									
									me.getContentResolver().update(SlumberContentProvider.ALARMS_URI, values, where, args);
	
									Cursor alarmsCursor = me.getContentResolver().query(SlumberContentProvider.ALARMS_URI, null, null, null, null);

									MatrixCursor addItemCursor = new MatrixCursor(columnNames);
									addItemCursor.addRow(columnObjects);
									
									Cursor[] cursors = { alarmsCursor, addItemCursor };
									final MergeCursor newMerged = new MergeCursor(cursors);
									
									Cursor old = adapter.swapCursor(newMerged);
									old.close();

									clickListener.onItemClick(parent, view, which, alarmId);
									
									if (checked)
									{
										HashMap<String, Object> payload = new HashMap<String, Object>();
										LogManager.getInstance(me).log("selected_tuesday", payload);
									}
									else
									{
										HashMap<String, Object> payload = new HashMap<String, Object>();
										LogManager.getInstance(me).log("deselected_tuesday", payload);
									}
								}
							});
	
							wednesday.setOnCheckedChangeListener(new OnCheckedChangeListener()
							{
								public void onCheckedChanged(CompoundButton button, boolean checked) 
								{
									ContentValues values = new ContentValues();
									values.put(SlumberContentProvider.ALARM_WEDNESDAY, checked);
									
									String where = "_id = ?";
									String[] args = { "" + alarmId };
									
									me.getContentResolver().update(SlumberContentProvider.ALARMS_URI, values, where, args);
	
									Cursor alarmsCursor = me.getContentResolver().query(SlumberContentProvider.ALARMS_URI, null, null, null, null);

									MatrixCursor addItemCursor = new MatrixCursor(columnNames);
									addItemCursor.addRow(columnObjects);
									
									Cursor[] cursors = { alarmsCursor, addItemCursor };
									final MergeCursor newMerged = new MergeCursor(cursors);
									
									Cursor old = adapter.swapCursor(newMerged);
									old.close();

									clickListener.onItemClick(parent, view, which, alarmId);
									
									if (checked)
									{
										HashMap<String, Object> payload = new HashMap<String, Object>();
										LogManager.getInstance(me).log("selected_wednesday", payload);
									}
									else
									{
										HashMap<String, Object> payload = new HashMap<String, Object>();
										LogManager.getInstance(me).log("deselected_wednesday", payload);
									}
								}
							});
	
							thursday.setOnCheckedChangeListener(new OnCheckedChangeListener()
							{
								public void onCheckedChanged(CompoundButton button, boolean checked) 
								{
									ContentValues values = new ContentValues();
									values.put(SlumberContentProvider.ALARM_THURSDAY, checked);
									
									String where = "_id = ?";
									String[] args = { "" + alarmId };
									
									me.getContentResolver().update(SlumberContentProvider.ALARMS_URI, values, where, args);
	
									Cursor alarmsCursor = me.getContentResolver().query(SlumberContentProvider.ALARMS_URI, null, null, null, null);

									MatrixCursor addItemCursor = new MatrixCursor(columnNames);
									addItemCursor.addRow(columnObjects);
									
									Cursor[] cursors = { alarmsCursor, addItemCursor };
									final MergeCursor newMerged = new MergeCursor(cursors);
									
									Cursor old = adapter.swapCursor(newMerged);
									old.close();

									clickListener.onItemClick(parent, view, which, alarmId);
									
									if (checked)
									{
										HashMap<String, Object> payload = new HashMap<String, Object>();
										LogManager.getInstance(me).log("selected_thursday", payload);
									}
									else
									{
										HashMap<String, Object> payload = new HashMap<String, Object>();
										LogManager.getInstance(me).log("deselected_thursday", payload);
									}
								}
							});
	
							friday.setOnCheckedChangeListener(new OnCheckedChangeListener()
							{
								public void onCheckedChanged(CompoundButton button, boolean checked) 
								{
									ContentValues values = new ContentValues();
									values.put(SlumberContentProvider.ALARM_FRIDAY, checked);
									
									String where = "_id = ?";
									String[] args = { "" + alarmId };
									
									me.getContentResolver().update(SlumberContentProvider.ALARMS_URI, values, where, args);
	
									Cursor alarmsCursor = me.getContentResolver().query(SlumberContentProvider.ALARMS_URI, null, null, null, null);

									MatrixCursor addItemCursor = new MatrixCursor(columnNames);
									addItemCursor.addRow(columnObjects);
									
									Cursor[] cursors = { alarmsCursor, addItemCursor };
									final MergeCursor newMerged = new MergeCursor(cursors);
									
									Cursor old = adapter.swapCursor(newMerged);
									old.close();

									clickListener.onItemClick(parent, view, which, alarmId);
									
									if (checked)
									{
										HashMap<String, Object> payload = new HashMap<String, Object>();
										LogManager.getInstance(me).log("selected_friday", payload);
									}
									else
									{
										HashMap<String, Object> payload = new HashMap<String, Object>();
										LogManager.getInstance(me).log("deselected_friday", payload);
									}
								}
							});
	
							saturday.setOnCheckedChangeListener(new OnCheckedChangeListener()
							{
								public void onCheckedChanged(CompoundButton button, boolean checked) 
								{
									ContentValues values = new ContentValues();
									values.put(SlumberContentProvider.ALARM_SATURDAY, checked);
									
									String where = "_id = ?";
									String[] args = { "" + alarmId };
									
									me.getContentResolver().update(SlumberContentProvider.ALARMS_URI, values, where, args);
	
									Cursor alarmsCursor = me.getContentResolver().query(SlumberContentProvider.ALARMS_URI, null, null, null, null);

									MatrixCursor addItemCursor = new MatrixCursor(columnNames);
									addItemCursor.addRow(columnObjects);
									
									Cursor[] cursors = { alarmsCursor, addItemCursor };
									final MergeCursor newMerged = new MergeCursor(cursors);
									
									Cursor old = adapter.swapCursor(newMerged);
									old.close();

									clickListener.onItemClick(parent, view, which, alarmId);
									
									if (checked)
									{
										HashMap<String, Object> payload = new HashMap<String, Object>();
										LogManager.getInstance(me).log("selected_saturday", payload);
									}
									else
									{
										HashMap<String, Object> payload = new HashMap<String, Object>();
										LogManager.getInstance(me).log("deselected_saturday", payload);
									}
								}
							});
	
							ringtone.setText(toneName);
							time.setText(SlumberContentProvider.dateStringForAlarmCursor(me, c));
							
							final int hour = c.getInt(c.getColumnIndex(SlumberContentProvider.ALARM_HOUR));
							final int minute = c.getInt(c.getColumnIndex(SlumberContentProvider.ALARM_MINUTE));
							
							time.setOnClickListener(new OnClickListener()
							{
								public void onClick(View timeView)
								{
									boolean useAmPm = android.text.format.DateFormat.is24HourFormat(me);
	
									TimePickerDialog picker = new TimePickerDialog(me, new OnTimeSetListener()
									{
										public void onTimeSet(TimePicker picker, int hour, int minute) 
										{
											ContentValues values = new ContentValues();
											values.put(SlumberContentProvider.ALARM_HOUR, hour);
											values.put(SlumberContentProvider.ALARM_MINUTE, minute);

											HashMap<String, Object> payload = new HashMap<String, Object>();
											payload.put("hour", hour);
											payload.put("minute", minute);
											LogManager.getInstance(me).log("set_time", payload);

											String where = "_id = ?";
											String[] args = { "" + alarmId };
											
											me.getContentResolver().update(SlumberContentProvider.ALARMS_URI, values, where, args);
	
											Cursor alarmsCursor = me.getContentResolver().query(SlumberContentProvider.ALARMS_URI, null, null, null, null);

											MatrixCursor addItemCursor = new MatrixCursor(columnNames);
											addItemCursor.addRow(columnObjects);
											
											Cursor[] cursors = { alarmsCursor, addItemCursor };
											final MergeCursor newMerged = new MergeCursor(cursors);
											
											Cursor old = adapter.swapCursor(newMerged);
											old.close();

											clickListener.onItemClick(parent, view, which, alarmId);
										}
									}, hour, minute, useAmPm);
									
									picker.show();
								}
							});
	
							ringtone.setOnClickListener(new OnClickListener()
							{
								public void onClick(View toneView)
								{
									HashMap<String, Object> payload = new HashMap<String, Object>();
									LogManager.getInstance(me).log("browsed_audio", payload);

									AlertDialog.Builder builder = new AlertDialog.Builder(me);
	
									LayoutInflater inflater = LayoutInflater.from(me);
									View searchView = inflater.inflate(R.layout.view_clock_tone_search, null, false);
									
									ListView tonesList = (ListView) searchView.findViewById(R.id.list_tones);
									
									String[] projection = { Audio.AudioColumns.TITLE, Audio.AudioColumns.ARTIST, Audio.AudioColumns.DATA, Audio.AudioColumns._ID };
									String selection = Audio.AudioColumns.IS_MUSIC + " = ? OR " + Audio.AudioColumns.IS_RINGTONE + " = ?";
									String[] args = { "1", "1"};
									
									Cursor internal = me.getContentResolver().query(Audio.Media.INTERNAL_CONTENT_URI, projection, selection, args, null);
									Cursor external = me.getContentResolver().query(Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, args, null);
									
									Cursor[] cursors = { internal, external };
									
									MergeCursor c = new MergeCursor(cursors);
									
									final SimpleCursorAdapter toneAdapter = new SimpleCursorAdapter(me, R.layout.row_tone, c, emptyString, emptyInt, 0)
									{
										public void bindView (View matchView, Context context, Cursor cursor)
										{
											TextView title = (TextView) matchView.findViewById(R.id.label_title);
											title.setText(cursor.getString(cursor.getColumnIndex(Audio.AudioColumns.TITLE)));
	
											TextView artist = (TextView) matchView.findViewById(R.id.label_artist);
											artist.setText(cursor.getString(cursor.getColumnIndex(Audio.AudioColumns.ARTIST)));
										}
									};
									
									tonesList.setAdapter(toneAdapter);
									
									tonesList.setOnItemClickListener(new OnItemClickListener()
									{
										public void onItemClick(AdapterView<?> arg0, View matchView, int position, long id) 
										{
											Cursor c = toneAdapter.getCursor();
											
											c.moveToPosition(position);
	
											final String title = c.getString(c.getColumnIndex(Audio.AudioColumns.TITLE));
											final String data = c.getString(c.getColumnIndex(Audio.AudioColumns.DATA));
											
											AlertDialog.Builder builder = new AlertDialog.Builder(me);
											builder = builder.setTitle(title);
	
											String[] items = { me.getString(R.string.action_audio_open), me.getString(R.string.action_audio_use) };
											builder = builder.setItems(items, new DialogInterface.OnClickListener() 
											{
												public void onClick(DialogInterface dialog, int which) 
												{
													HashMap<String, Object> payload = new HashMap<String, Object>();

													switch (which)
													{
														case 0:
															Intent playIntent = new Intent(Intent.ACTION_VIEW);
															playIntent.setDataAndType(Uri.fromFile(new File(data)), "audio/*");
															
															me.startActivity(playIntent);

															LogManager.getInstance(me).log("previewed_audio", payload);

															break;
														case 1:
	
															ContentValues values = new ContentValues();
															values.put(SlumberContentProvider.ALARM_NAME, title);
															values.put(SlumberContentProvider.ALARM_CONTENT_URI, Uri.fromFile(new File(data)).toString());
															
															String where = "_id = ?";
															String[] args = { "" + alarmId };
															
															me.getContentResolver().update(SlumberContentProvider.ALARMS_URI, values, where, args);
	
															Cursor alarmsCursor = me.getContentResolver().query(SlumberContentProvider.ALARMS_URI, null, null, null, null);

															MatrixCursor addItemCursor = new MatrixCursor(columnNames);
															addItemCursor.addRow(columnObjects);
															
															Cursor[] cursors = { alarmsCursor, addItemCursor };
															final MergeCursor newMerged = new MergeCursor(cursors);
															
															Cursor old = adapter.swapCursor(newMerged);
															old.close();

															clickListener.onItemClick(parent, view, which, alarmId);
															
															dialog.cancel();
															me._toneListDialog.cancel();

															LogManager.getInstance(me).log("chose_audio", payload);

															break;
													}
												}
											});
											
											builder = builder.setNegativeButton(R.string.button_close, new DialogInterface.OnClickListener() 
											{
												public void onClick(DialogInterface dialog, int which) 
												{
	
												}
											});
											
											builder.create().show();
										}
									});
									
									EditText searchField = (EditText) searchView.findViewById(R.id.text_search);
									searchField.addTextChangedListener(new TextWatcher()
									{
										public void afterTextChanged(final Editable editable) 
										{
											me._searchLastUpdate  = System.currentTimeMillis();
											
											me._handler.postDelayed(new Runnable()
											{
												public void run() 
												{
													long now = System.currentTimeMillis();
													
													if (now - me._searchLastUpdate > 900)
													{
														String[] projection = { Audio.AudioColumns.TITLE, Audio.AudioColumns.ARTIST, Audio.AudioColumns.DATA, Audio.AudioColumns._ID };
														String selection = "(" + Audio.AudioColumns.IS_MUSIC + " = ? OR " + Audio.AudioColumns.IS_RINGTONE + " = ?) AND (" + Audio.AudioColumns.TITLE + " LIKE ? OR " + Audio.AudioColumns.ARTIST + " LIKE ?)";
														
														String likeString = "%" + editable.toString() + "%";
														
														String[] args = { "1", "1", likeString, likeString } ;
														
														Cursor internal = me.getContentResolver().query(Audio.Media.INTERNAL_CONTENT_URI, projection, selection, args, null);
														Cursor external = me.getContentResolver().query(Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, args, null);
														
														Cursor[] cursors = { internal, external };
														
														MergeCursor c = new MergeCursor(cursors);
														
														toneAdapter.changeCursor(c);
														
														HashMap<String, Object> payload = new HashMap<String, Object>();
														LogManager.getInstance(me).log("searched_audio", payload);

													}
												}
											}, 1000);
										}
	
										public void beforeTextChanged(CharSequence s, int start, int count, int after) 
										{
	
										}
	
										public void onTextChanged(CharSequence s, int start, int count, int after) 
										{
	
										}
									});
									
									builder = builder.setView(searchView);
	
									me._toneListDialog = builder.create();
									me._toneListDialog.show();
									
									DisplayMetrics metrics = me.getResources().getDisplayMetrics();
									
									WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
	
									lp.copyFrom(me._toneListDialog.getWindow().getAttributes());
									lp.width = (int) (480f * metrics.density);
									lp.height = (int) (320f * metrics.density);
	
									me._toneListDialog.getWindow().setAttributes(lp);
								}
							});
						}
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
				lp.height = (int) (300f * metrics.density);

				d.getWindow().setAttributes(lp);
			}
        });
        
        log.setOnClickListener(new OnClickListener()
        {
			public void onClick(View arg0) 
			{
				HashMap<String, Object> payload = new HashMap<String, Object>();
				LogManager.getInstance(me).log("opened_log_view", payload);

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
				
				final EditText logField = (EditText) view.findViewById(R.id.field_log_text);
				
				builder.setPositiveButton(R.string.button_save, new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						String logText = logField.getEditableText().toString().trim();
						
						if (logText.length() > 0)
						{
							long now = System.currentTimeMillis();
							
							ContentValues values = new ContentValues();
							values.put(SlumberContentProvider.NOTE_TEXT, logText);
							values.put(SlumberContentProvider.NOTE_TIMESTAMP, now);
							
							HashMap<String, Object> payload = new HashMap<String, Object>();
							LogManager.getInstance(me).log("created_log", payload);
							
							me.getContentResolver().insert(SlumberContentProvider.NOTES_URI, values);
							
							dialog.cancel();

							Toast.makeText(me, R.string.toast_note_saved, Toast.LENGTH_SHORT).show();
						}
						else
							Toast.makeText(me, R.string.toast_provide_note, Toast.LENGTH_SHORT).show();
						
						HashMap<String, Object> payload = new HashMap<String, Object>();
						LogManager.getInstance(me).log("closed_log_view", payload);
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
				HashMap<String, Object> payload = new HashMap<String, Object>();
				LogManager.getInstance(me).log("opened_tips_view", payload);

				AlertDialog.Builder builder = new AlertDialog.Builder(me);
				
				builder = builder.setTitle(R.string.title_clock_tips);
				
				LayoutInflater inflater = LayoutInflater.from(me);
				View view = inflater.inflate(R.layout.view_clock_tips, null, false);
				
				GridView contentGrid = (GridView) view.findViewById(R.id.root_grid);
				
				final String[] titles = me.getResources().getStringArray(R.array.youtube_titles);
				final String[] ids = me.getResources().getStringArray(R.array.youtube_ids);

				ArrayAdapter<String> adapter = new ArrayAdapter<String>(me, R.layout.cell_tip, titles)
				{
					public View getView (int position, View convertView, ViewGroup parent)
					{
						if (convertView == null)
						{
		    				LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		    				convertView = inflater.inflate(R.layout.cell_tip, parent, false);
						}
						
						String id = ids[position];
						String title = titles[position];
						
						TextView tipTitle = (TextView) convertView.findViewById(R.id.title_tip);
						tipTitle.setText(title);
						
						UriImageView icon = (UriImageView) convertView.findViewById(R.id.icon_tip);
						icon.setCachedImageUri(Uri.parse("http://img.youtube.com/vi/" + id + "/1.jpg"));
						
						return convertView;
					}
				};
				
				contentGrid.setAdapter(adapter);
				
				builder = builder.setView(view);
				builder.setNegativeButton(R.string.button_close, new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int which) 
					{
						HashMap<String, Object> payload = new HashMap<String, Object>();
						LogManager.getInstance(me).log("closed_tips_view", payload);
					}
				});

				final AlertDialog d = builder.create();
				d.show();
				
				DisplayMetrics metrics = me.getResources().getDisplayMetrics();
				
				WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

				lp.copyFrom(d.getWindow().getAttributes());
				lp.width = (int) (480f * metrics.density);
				lp.height = (int) (320f * metrics.density);

				d.getWindow().setAttributes(lp);

				contentGrid.setOnItemClickListener(new OnItemClickListener()
				{
					public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) 
					{
						Uri u = Uri.parse("http://www.youtube.com/watch?v=" + ids[arg2]);
						
						Intent intent = new Intent(Intent.ACTION_VIEW, u);
						
						me.startActivity(intent);
						
						d.dismiss();
						
						HashMap<String, Object> payload = new HashMap<String, Object>();
						LogManager.getInstance(me).log("viewed_tip", payload);
					}
				});
			}
        });

        settings.setOnClickListener(new OnClickListener()
        {
			public void onClick(View arg0) 
			{
				HashMap<String, Object> payload = new HashMap<String, Object>();
				LogManager.getInstance(me).log("opened_settings_view", payload);

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
						HashMap<String, Object> payload = new HashMap<String, Object>();
						LogManager.getInstance(me).log("closed_settings_view", payload);
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

	@SuppressLint("InlinedApi")
	protected void onResume() 
	{
		super.onResume();
	
		HashMap<String, Object> payload = new HashMap<String, Object>();
		LogManager.getInstance(this).log("launched_clock_activity", payload);

		this._lastInteraction = System.currentTimeMillis();
		this._sampleAudio = true;

		if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
		{
			this.finish();
			
			Intent homeIntent = new Intent(this, HomeActivity.class);
			this.startActivity(homeIntent);
		}

		final ClockActivity me = this;
		
		if (this._handler == null)
		{
			this._handler = new Handler();
			
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
		
		LocalBroadcastManager broadcasts = LocalBroadcastManager.getInstance(this.getApplicationContext());
		
		if (this._startAlarmReceiver == null)
		{
			this._startAlarmReceiver = new BroadcastReceiver()
			{
				public void onReceive(final Context context, final Intent intent) 
				{
					me.runOnUiThread(new Runnable()
					{
						public void run() 
						{
							Uri intentUri = intent.getData();
							
							if (intentUri.equals(me._lastAudioUri) == false)
							{
								WindowManager.LayoutParams params = me.getWindow().getAttributes();
								
								SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(me);

								float active = prefs.getFloat(ClockActivity.ACTIVE_BRIGHTNESS_OPTION, ClockActivity.DEFAULT_ACTIVE_BRIGHTNESS);
								params.screenBrightness = active;
								me.getWindow().setAttributes(params);

								if (me._alarmDialog != null)
								{
									me._alarmDialog.dismiss();
									me._alarmDialog = null;
								}
								
								String name = intent.getStringExtra(SlumberContentProvider.ALARM_NAME);
	
								AlertDialog.Builder builder = new AlertDialog.Builder(me);
								
								builder = builder.setTitle(R.string.title_cancel_alarm);
								builder = builder.setMessage(context.getString(R.string.message_cancel_alarm, name));
								builder = builder.setPositiveButton(R.string.button_cancel_alarm, new DialogInterface.OnClickListener()
								{
									public void onClick(DialogInterface dialog, int which) 
									{
										Intent stopIntent = new Intent(AlarmService.STOP_ALARM, null, context, AlarmService.class);
										context.startService(stopIntent);
									}
								});
								
								me._alarmDialog = builder.create();
								me._alarmDialog.show();					
							}
						}
					});
				}
			};
			
			IntentFilter infoFilter = new IntentFilter(AlarmService.BROADCAST_TRACK_INFO);
			infoFilter.addDataScheme("file");

			broadcasts.registerReceiver(this._startAlarmReceiver, infoFilter);

			IntentFilter startFilter = new IntentFilter(AlarmService.START_ALARM);
			startFilter.addDataScheme("file");

			broadcasts.registerReceiver(this._startAlarmReceiver, startFilter);
		}

		if (this._endAlarmReceiver == null)
		{
			this._endAlarmReceiver = new BroadcastReceiver()
			{
				public void onReceive(final Context context, final Intent intent) 
				{
					me.runOnUiThread(new Runnable()
					{
						public void run() 
						{
							me._alarmDialog.dismiss();
							
							me._alarmDialog = null;
							me._lastAudioUri = null;
						}
					});
				}
			};
			
			IntentFilter filter = new IntentFilter(AlarmService.STOP_ALARM);
			
			broadcasts.registerReceiver(this._endAlarmReceiver, filter);
		}

		Intent fetchTrackInfo = new Intent(AlarmService.BROADCAST_TRACK_INFO, null, this, AlarmService.class);
		this.startService(fetchTrackInfo);
		
		SensorManager sensors = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
		
		sensors.registerListener(this, sensors.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			sensors.registerListener(this, sensors.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE), SensorManager.SENSOR_DELAY_NORMAL);
		
		Runnable r = new Runnable()
		{
			@SuppressWarnings("deprecation")
			public void run() 
			{
				while (me._sampleAudio)
				{
					int bufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);

					AudioRecord recorder = null;

					int[] rates = new int[] { 44100, 22050, 11025, 8000 };

					for (int rate : rates)
					{
						if (recorder == null)
						{
							AudioRecord newRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, rate, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

							if (newRecorder.getState() == AudioRecord.STATE_INITIALIZED)
								recorder = newRecorder;

							else
								newRecorder.release();
						}
					}						

					if (recorder != null)
					{
						double[] samples = new double[32768];
						
						recorder.startRecording();

						short[] buffer = new short[bufferSize];

						int index = 0;

						int read = 0;

						while (index < samples.length && 0 <= (read = recorder.read(buffer, 0, bufferSize)))
						{
							for (int i = 0; i < read; i++)
							{
								if (index < samples.length)
								{
									samples[index] = (double) buffer[i];
									index += 1;
								}
							}
						}

						recorder.stop();
						recorder.release();

						FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);

						Complex[] values = fft.transform(samples, TransformType.FORWARD);

						double maxFrequency = 0;
						double maxMagnitude = 0;

						double minMagnitude = Double.MAX_VALUE;

						for (int i = 0; i < values.length / 2; i++) 
						{
							Complex value = values[i];

							double magnitude = value.abs();

							if (magnitude > maxMagnitude)
							{
								maxMagnitude = magnitude;
								maxFrequency = (i * 44100.0) / (double) samples.length;
							}

							if (magnitude < minMagnitude)
								minMagnitude = magnitude;
						}
						
						me.logSensorValue(SlumberContentProvider.AUDIO_FREQUENCY, maxFrequency);
						me.logSensorValue(SlumberContentProvider.AUDIO_MAGNITUDE, maxMagnitude);
					}
					
					try 
					{
						// TODO: Make configurable?
						Thread.sleep(ClockActivity.SAMPLE_RATE);
					} 
					catch (InterruptedException e) 
					{

					}
				}
			}
		};
		
		Thread t = new Thread(r);
		t.start();
	}
	
	public boolean dispatchTouchEvent (MotionEvent ev)
	{
		this._lastInteraction = System.currentTimeMillis();

		if (this._cachedPreferences == null)
			this._cachedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		WindowManager.LayoutParams params = this.getWindow().getAttributes();

		float active = this._cachedPreferences.getFloat(ClockActivity.ACTIVE_BRIGHTNESS_OPTION, ClockActivity.DEFAULT_ACTIVE_BRIGHTNESS);
		params.screenBrightness = active;
		this.getWindow().setAttributes(params);

		return super.dispatchTouchEvent(ev);
	}

	protected void logSensorValue(String name, double value)
	{
		this.logSensorValue(name, value, System.currentTimeMillis());
	}

	protected void logSensorValue(String name, double value, long timestamp)
	{
		ContentValues values = new ContentValues();
		values.put(SlumberContentProvider.READING_NAME, name);
		values.put(SlumberContentProvider.READING_VALUE, value);
		values.put(SlumberContentProvider.READING_RECORDED, timestamp);
		
		this.getContentResolver().insert(SlumberContentProvider.SENSOR_READINGS_URI, values);
	}

	protected void onPause()
	{
		super.onPause();

		LocalBroadcastManager broadcasts = LocalBroadcastManager.getInstance(this.getApplicationContext());

		if (this._startAlarmReceiver != null)
		{
			broadcasts.unregisterReceiver(this._startAlarmReceiver);
			this._startAlarmReceiver = null;
		}

		if (this._endAlarmReceiver != null)
		{
			broadcasts.unregisterReceiver(this._endAlarmReceiver);
			this._endAlarmReceiver = null;
		}
		
		if (this._alarmDialog != null)
		{
			this._alarmDialog.dismiss();
			this._alarmDialog = null;
		}

		this._handler.removeCallbacksAndMessages(null);
		this._handler = null;
		
		SensorManager sensors = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
		sensors.unregisterListener(this);
		
		this._sampleAudio = false;
		
		HashMap<String, Object> payload = new HashMap<String, Object>();
		LogManager.getInstance(this).log("closed_clock_activity", payload);
	}

	@SuppressLint({ "SimpleDateFormat", "DefaultLocale" })
	protected void updateClock() 
	{
		TextView dateText = (TextView) this.findViewById(R.id.date_view);
		TextView timeText = (TextView) this.findViewById(R.id.time_view);
		TextView apptText = (TextView) this.findViewById(R.id.appointment_view);
		TextView ampmText = (TextView) this.findViewById(R.id.ampm_view);
		
		Date now = new Date();
		
		DateFormat dateFormat = new SimpleDateFormat("EEEE, LLLL d, yyyy");
		
		dateText.setText(dateFormat.format(now));
		
		boolean useAmPm = android.text.format.DateFormat.is24HourFormat(this);
		
		SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm");
		
		if (useAmPm == false)
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

			if (event.allDay == false)
			{
				String date = apptFormat.format(new Date(event.timestamp));
	
				if (useAmPm == false)
				{
					SimpleDateFormat ampmFormat = new SimpleDateFormat("a");
	
					date += ampmFormat.format(new Date(event.timestamp)).toLowerCase();
				}

				apptText.setText(this.getString(R.string.label_upcoming_appointment, event.title, date));
			}
			else
			{
				apptFormat = new SimpleDateFormat("EEEE");

				String date = apptFormat.format(new Date(event.timestamp));

				apptText.setText(this.getString(R.string.label_upcoming_appointment, event.title, date));
			}
		}
		else
			apptText.setText(R.string.label_no_appointments);
		
		this.manageBrightness();
	}
	
	@SuppressLint("InlinedApi")
	private void manageBrightness() 
	{
		long now = System.currentTimeMillis();
		
		if (now - this._lastDimCheck > 1000)
		{
			if (this._cachedPreferences == null)
				this._cachedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
			
			int dimDuration = this._cachedPreferences.getInt(ClockActivity.DIM_DELAY_OPTION, ClockActivity.DEFAULT_DIM_DELAY) * 15;
			
			if (now - this._lastInteraction  > dimDuration * 1000)
			{
				WindowManager.LayoutParams params = this.getWindow().getAttributes();
				
				float restBrightness = this._cachedPreferences.getFloat(ClockActivity.REST_BRIGHTNESS_OPTION, ClockActivity.DEFAULT_REST_BRIGHTNESS);

				if (params.screenBrightness != restBrightness)
				{
					params.screenBrightness = restBrightness;
					this.getWindow().setAttributes(params);
				}
				
				this._lastInteraction = Long.MAX_VALUE;
				
		        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
		        {
		            View root = this.findViewById(R.id.clock_root);

		            root.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		        }
			}
		}		
	}

	private class Event
	{
		public String title;
		public long timestamp;
		public boolean allDay;
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
	        	String[] projection = { CalendarContract.Instances.BEGIN, CalendarContract.Instances.EVENT_ID, CalendarContract.Instances.ALL_DAY };
			
	        	Cursor c = CalendarContract.Instances.query(this.getContentResolver(), projection, now, now + (1000 * 3600 * 24 * 6)); 
	        	
	        	String title = null;
	        	long timestamp = Long.MAX_VALUE;
	        	boolean allDay = false;
	        	boolean thisAllDay = false;
	        	
	        	while (c.moveToNext())
	        	{
	        		long eventTime = c.getLong(c.getColumnIndex(CalendarContract.Instances.BEGIN));

	        		allDay = c.getInt(c.getColumnIndex(CalendarContract.Instances.ALL_DAY)) != 0;

	        		if (allDay)
	        		{
	        			eventTime -= TimeZone.getDefault().getRawOffset();
	        			
	        			eventTime += (1000 * 60 * 60 * 24) - 1;
	        		}

	        		if (eventTime < timestamp)
	        		{
		        		timestamp = eventTime;
		        		thisAllDay = allDay;
		        		
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
	        		this._lastEvent.allDay = thisAllDay;
	        		this._lastEvent.title = title;
	        		
	        		return this._lastEvent;
	        	}
	        	
	        	return null;
	        }
		}
		
		return this._lastEvent;
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) 
	{

	}

	public void onSensorChanged(SensorEvent event) 
	{
		long now = System.currentTimeMillis();
		
		switch (event.sensor.getType())
		{
			case Sensor.TYPE_LIGHT:
				if (event.values[0] < 10)
				{
					if (this._cachedPreferences == null)
						this._cachedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
					
					if (this._cachedPreferences.getBoolean(ClockActivity.DIM_DARK_OPTION, ClockActivity.DIM_DARK_DEFAULT))
					{
						this._lastInteraction = 0;
						this._lastDimCheck = 0;
							
						this.manageBrightness();
					}
				}
				
				if (Math.abs(event.values[0] - this._lastBrightness ) > 1)
					this._lastLightReading = 0;
				
				this._lastBrightness = event.values[0];
				
				if (now - this._lastLightReading > ClockActivity.SAMPLE_RATE)
				{
					this.logSensorValue(SlumberContentProvider.LIGHT_LEVEL, event.values[0], event.timestamp / (1000 * 1000));
					
					this._lastLightReading = now;
				}
				
				break;
			case Sensor.TYPE_AMBIENT_TEMPERATURE:
				if (Math.abs(event.values[0] - this._lastTemperature ) > 1)
					this._lastTemperatureReading = 0;
				
				this._lastTemperature  = event.values[0];

				if (now - this._lastTemperatureReading > ClockActivity.SAMPLE_RATE)
				{
					this.logSensorValue(SlumberContentProvider.TEMPERATURE, event.values[0], event.timestamp / (1000 * 1000));
					
					this._lastTemperatureReading = now;
				}

				break;
		}
	}
}
