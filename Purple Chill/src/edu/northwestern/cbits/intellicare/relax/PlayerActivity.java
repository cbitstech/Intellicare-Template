package edu.northwestern.cbits.intellicare.relax;

import java.util.HashMap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager.BadTokenException;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class PlayerActivity extends ConsentedActivity implements OnPreparedListener
{
	public static String REQUEST_STRESS = "request_stress";
	
	private Uri _trackUri = null;
	private String _trackTitle = null;
	private String _trackDescription = null;
	private PersistentMediaController _controller = null;

	private Handler _handler = null;
	
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_player);

		this._handler = new Handler(Looper.getMainLooper());
	}
	
	protected void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);
		
		this.setIntent(intent);
	}
	
	final PlayerActivity me = this;

	private void fetchStress()
	{
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		View body = inflater.inflate(R.layout.view_stress_rating, null);
        
        final TextView ratingNumber = (TextView) body.findViewById(R.id.rating_number);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		builder = builder.setTitle(R.string.title_rate_stress);
		builder = builder.setPositiveButton(R.string.button_continue, new OnClickListener()
		{
			public void onClick(DialogInterface arg0, int arg1) 
			{
				try
				{
					int stressLevel = Integer.parseInt(ratingNumber.getText().toString());
					
            		HashMap<String,Object> payload = new HashMap<String, Object>();
            		payload.put(GroupActivity.STRESS_RATING, stressLevel);
            		payload.put(GroupActivity.TRACK_END, true);
            		LogManager.getInstance(me).log("rated_stress", payload);
				}
				catch (NumberFormatException e)
				{
					Toast.makeText(me, R.string.toast_rate_stress, Toast.LENGTH_LONG).show();
					
					me.fetchStress();
				}
			}
		});

        final SeekBar ratingBar = (SeekBar) body.findViewById(R.id.stress_rating);
        
        ratingBar.setMax(9);
        
        ratingBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
        {
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) 
                {
                    progress += 1;
                    
                    ratingNumber.setText("" + progress);
                }

                public void onStartTrackingTouch(SeekBar seekBar) 
                {

                }

                public void onStopTrackingTouch(SeekBar seekBar) 
                {

                }
        });

        builder = builder.setView(body);
		
		builder.create().show();
	}
	
	public void onResume()
	{
		super.onResume();

		this._trackUri = Uri.parse(this.getIntent().getStringExtra(AudioFileManager.TRACK_URI));
		this._trackTitle = this.getIntent().getStringExtra(AudioFileManager.TRACK_TITLE);
		this._trackDescription = this.getIntent().getStringExtra(AudioFileManager.TRACK_DESCRIPTION);

		this.getSupportActionBar().setTitle(this._trackTitle);

		this._controller = new PersistentMediaController(this);

		AudioFileManager audio = AudioFileManager.getInstance(this);
		this._controller.setMediaPlayer(audio);

		audio.setTrackUri(this._trackUri, this._trackTitle, this._trackDescription, this);

		HashMap<String,Object> payload = new HashMap<String, Object>();
		payload.put(GroupActivity.GROUP_NAME, this.getSupportActionBar().getTitle());
		LogManager.getInstance(this).log("viewed_track", payload);

		View anchor = this.findViewById(R.id.track_background);
		this._controller.setAnchorView(anchor);
		
		if (this.getIntent().getBooleanExtra(PlayerActivity.REQUEST_STRESS, false))
			this.fetchStress();
	}
	
	public void onPause()
	{
		super.onPause();

		this._controller.superHide();
	}
	
	public void onPrepared(final MediaPlayer player) 
	{
		final PlayerActivity me = this;

		this._handler.postDelayed(new Runnable()
		{
			public void run() 
			{
				me._controller.setEnabled(true);
				
				try
				{
					me._controller.show(0);
				}
				catch (BadTokenException e)
				{
					e.printStackTrace();
				}
			}
		}, 250);
	}
	
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_player, menu);

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.action_track_info)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder = builder.setTitle(this._trackTitle);
			builder = builder.setMessage(this._trackDescription);
			
			builder.create().show();
		}
		
		return true;
	}
}
