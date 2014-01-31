package edu.northwestern.cbits.intellicare.slumbertime;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
	
	private BroadcastReceiver _startAlarmReceiver = null;
	private BroadcastReceiver _endAlarmReceiver = null;
	private Uri _lastAudioUri = null;
	
	private AlertDialog _alarmDialog = null;
	
	protected float _currentBrightness;
	protected long _searchLastUpdate = 0;
	protected AlertDialog _toneListDialog = null;
	
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
													switch (which)
													{
														case 0:
															Intent playIntent = new Intent(Intent.ACTION_VIEW);
															playIntent.setDataAndType(Uri.fromFile(new File(data)), "audio/*");
															
															me.startActivity(playIntent);
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
							
							me.getContentResolver().insert(SlumberContentProvider.NOTES_URI, values);
							
							dialog.cancel();

							Toast.makeText(me, R.string.toast_note_saved, Toast.LENGTH_SHORT).show();
						}
						else
							Toast.makeText(me, R.string.toast_provide_note, Toast.LENGTH_SHORT).show();
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
							WindowManager.LayoutParams params = me.getWindow().getAttributes();
							params.screenBrightness = 1.0f;
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
					});
				}
			};
			
			IntentFilter filter = new IntentFilter(AlarmService.START_ALARM);
			filter.addDataScheme("file");
			
			broadcasts.registerReceiver(this._startAlarmReceiver, filter);
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
						}
					});
				}
			};
			
			IntentFilter filter = new IntentFilter(AlarmService.STOP_ALARM);
			
			broadcasts.registerReceiver(this._endAlarmReceiver, filter);
		}
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

			String date = apptFormat.format(new Date(event.timestamp));

			if (useAmPm == false)
			{
				SimpleDateFormat ampmFormat = new SimpleDateFormat("a");

				date += ampmFormat.format(new Date(event.timestamp)).toLowerCase();
			}
			
			apptText.setText(this.getString(R.string.label_upcoming_appointment, event.title, date));
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
