package edu.northwestern.cbits.intellicare.relax;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class PlayerActivity extends ActionBarActivity implements OnPreparedListener
{
	protected static final String GROUP_NAME = "group_name";	
	protected static final String GROUP_MEDIA = "group_media";
	protected static final String GROUP_TITLES = "group_titles";
	protected static final String GROUP_TIMES = "group_times";
	protected static final String GROUP_TRACK = "group_track";
	
	private String _groupName = null;
	
	private static String _currentGroupName = null;
	private static int _currentGroupTimes = -1;
	private static int _currentGroupTitles = -1;
	private static int _currentGroupMedia = -1;
	private static int _currentGroupTrack = -1;
	private static int _currentStressLevel = -1;
	
	private int _selectedIndex = -1;
	private PersistentMediaController _controller = null;

    private static String formatTime(String secondsString)
    {
        int seconds = Integer.parseInt(secondsString);
        
        int minutes = seconds / 60;
        seconds = seconds % 60;
        
        String formatted = minutes + ":";
        
        if (seconds < 10)
                formatted += "0";
        
        formatted += seconds;
        
        return formatted;
    }

	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_player);
		
		LinearLayout root = (LinearLayout) this.findViewById(R.id.layout_player);
		
	    this._controller = new PersistentMediaController(this);
	    this._controller.setMediaPlayer(AudioFileManager.getInstance(this));
	    this._controller.setAnchorView(root);
	}
	
	public static String playerTitle(Context context)
	{
		return AudioFileManager.getInstance(context).currentTitle();
	}

	public static String playerSubtitle(Context context)
	{
		return AudioFileManager.getInstance(context).currentGroup();
	}

	public static Intent launchIntentForCurrentTrack(Context context)
	{
		if (AudioFileManager.getInstance(context).hasPlayer())
		{
			Intent intent = new Intent(context, PlayerActivity.class);
			
			intent.putExtra(PlayerActivity.GROUP_NAME, PlayerActivity._currentGroupName);
			intent.putExtra(PlayerActivity.GROUP_MEDIA, PlayerActivity._currentGroupMedia);
			intent.putExtra(PlayerActivity.GROUP_TITLES, PlayerActivity._currentGroupTitles);
			intent.putExtra(PlayerActivity.GROUP_TIMES, PlayerActivity._currentGroupTimes);
			intent.putExtra(PlayerActivity.GROUP_TRACK, PlayerActivity._currentGroupTrack);
			
			return intent;
		}
		
		return null;
	}
	
	protected void onResume()
	{
		super.onResume();
	
        this._controller.setEnabled(false);

        final ArrayList<String> recordings = new ArrayList<String>();
        final ArrayList<String> titles = new ArrayList<String>();
        final ArrayList<String> times = new ArrayList<String>();
        
        final int titlesId = this.getIntent().getIntExtra(PlayerActivity.GROUP_TITLES, -1);
        final int mediaId = this.getIntent().getIntExtra(PlayerActivity.GROUP_MEDIA, -1);
        final int timesId = this.getIntent().getIntExtra(PlayerActivity.GROUP_TIMES, -1);

        if (titlesId != -1 && mediaId != -1 && timesId != -1)
        {
            String[] mediaUrls = this.getResources().getStringArray(mediaId);
            String[] mediaTitles = this.getResources().getStringArray(titlesId);
            String[] mediaTimes = this.getResources().getStringArray(timesId);
            
            for (int i = 0; i < mediaUrls.length; i++)
            {
                titles.add(mediaTitles[i]);
                recordings.add(mediaUrls[i]);
                times.add(PlayerActivity.formatTime(mediaTimes[i]));
            }
        }

		this._groupName = this.getIntent().getStringExtra(PlayerActivity.GROUP_NAME);
		
		this.getSupportActionBar().setTitle(this._groupName);
		
		final PlayerActivity me = this;

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.row_recording, recordings)
        {
        	public View getView(int position, View convertView, ViewGroup parent)
            {
                if (convertView == null)
                {
                    LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                    convertView = inflater.inflate(R.layout.row_recording, parent, false);
                }
                
                TextView title = (TextView) convertView.findViewById(R.id.recording_title);
                title.setText(titles.get(position));

                TextView time = (TextView) convertView.findViewById(R.id.recording_time);
                time.setText(times.get(position));

                Drawable d = me.getResources().getDrawable(R.drawable.ic_action_music_2);
                
                if (position == me._selectedIndex)
                        d = me.getResources().getDrawable(R.drawable.ic_action_playback_play);
                
                title.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);

                return convertView;
            }
        };
        
        final ListView recordingsList = (ListView) this.findViewById(R.id.recording_list);
        recordingsList.setAdapter(adapter);
        
        recordingsList.setOnItemClickListener(new OnItemClickListener()
        {
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long id) 
			{
				AudioFileManager.getInstance(me).setUrl(recordings.get(i), titles.get(i), me._groupName, me);
			}
        });

		final TextView ratingNumber = (TextView) this.findViewById(R.id.rating_number);
		final SeekBar ratingBar = (SeekBar) this.findViewById(R.id.stress_rating);
		
		ratingBar.setMax(9);
		
		ratingBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
		{
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) 
			{
				progress += 1;
				
				PlayerActivity._currentStressLevel = progress;
				
				ratingNumber.setText("" + progress);
			}

			public void onStartTrackingTouch(SeekBar seekBar) 
			{

			}

			public void onStopTrackingTouch(SeekBar seekBar) 
			{

			}
		});

		if (PlayerActivity._currentStressLevel != -1)
		{
			ratingBar.setProgress(PlayerActivity._currentStressLevel - 1);
			ratingBar.setEnabled(true);
		}
		
		if (PlayerActivity._currentGroupName != null && PlayerActivity._currentGroupTrack >= 0)
		{
			if (PlayerActivity._currentGroupTrack < titles.size())
			{
	    		me._selectedIndex = PlayerActivity._currentGroupTrack;
	    		
				ratingBar.setEnabled(false);
			}
		}
		
		HashMap<String,Object> payload = new HashMap<String, Object>();
		payload.put(PlayerActivity.GROUP_NAME, this._groupName);
		LogManager.getInstance(this).log("viewed_group", payload);
		
		String completedKey = this._groupName + "_completed";
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		if (prefs.getBoolean(completedKey, false) == false)
		{
			Intent introIntent = new Intent(this, IntroActivity.class);
			
			introIntent.putExtra(IntroActivity.SEQUENCE_URLS, this.getIntroUrls());
			introIntent.putExtra(IntroActivity.SEQUENCE_TITLES, this.getIntroTitles());
			introIntent.putExtra(IntroActivity.SEQUENCE_KEY, completedKey);
			
			this.startActivity(introIntent);
		}
	}
	
	protected void onPause()
	{
		super.onPause();

		HashMap<String,Object> payload = new HashMap<String, Object>();
		payload.put(PlayerActivity.GROUP_NAME, this._groupName);
		LogManager.getInstance(this).log("exited_group", payload);	
	}

	private int getIntroUrls() 
	{
		if (this._groupName.equals(this.getString(R.string.breathing_title)))
			return R.array.breathing_urls;
		else if (this._groupName.equals(this.getString(R.string.muscle_title)))
			return R.array.muscle_urls;
		else if (this._groupName.equals(this.getString(R.string.autogenic_title)))
			return R.array.autogenic_urls;
		else if (this._groupName.equals(this.getString(R.string.visualization_title)))
			return R.array.visualization_urls;

		return R.array.mindful_urls;
	}

	private int getIntroTitles() 
	{
		if (this._groupName.equals(this.getString(R.string.breathing_title)))
			return R.array.breathing_titles;
		else if (this._groupName.equals(this.getString(R.string.muscle_title)))
			return R.array.muscle_titles;
		else if (this._groupName.equals(this.getString(R.string.autogenic_title)))
			return R.array.autogenic_titles;
		else if (this._groupName.equals(this.getString(R.string.visualization_title)))
			return R.array.visualization_titles;

		return R.array.mindful_titles;
	}
	
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_player, menu);

		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.action_help)
		{
			String completedKey = this._groupName + "_completed";

			Intent introIntent = new Intent(this, IntroActivity.class);
			
			introIntent.putExtra(IntroActivity.SEQUENCE_URLS, this.getIntroUrls());
			introIntent.putExtra(IntroActivity.SEQUENCE_TITLES, this.getIntroTitles());
			introIntent.putExtra(IntroActivity.SEQUENCE_KEY, completedKey);
			
			this.startActivity(introIntent);
		}
		
		return true;
	}

	public void onPrepared(MediaPlayer player) 
	{
		Log.e("PC", "PREPARED");
		
		this._controller.setEnabled(true);
		this._controller.show();
	}
}
