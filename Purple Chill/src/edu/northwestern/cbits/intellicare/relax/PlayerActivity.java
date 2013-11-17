package edu.northwestern.cbits.intellicare.relax;

import java.util.HashMap;

import android.app.AlertDialog;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager.BadTokenException;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class PlayerActivity extends ConsentedActivity implements OnPreparedListener
{
	private Uri _trackUri = null;
	private String _trackTitle = null;
	private String _trackDescription = null;
	private PersistentMediaController _controller = null;

	private Handler _handler = null;
	
	
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_player);
		
		this._trackUri = Uri.parse(this.getIntent().getStringExtra(AudioFileManager.TRACK_URI));
		this._trackTitle = this.getIntent().getStringExtra(AudioFileManager.TRACK_TITLE);
		this._trackDescription = this.getIntent().getStringExtra(AudioFileManager.TRACK_DESCRIPTION);

		Log.e("PC", "EXTRAS " + this.getIntent().getExtras());
		this._controller = new PersistentMediaController(this);
		
		AudioFileManager audio = AudioFileManager.getInstance(this);
		this._controller.setMediaPlayer(audio);

		this._handler = new Handler(Looper.getMainLooper());
		
		audio.setTrackUri(this._trackUri, this._trackTitle, this._trackDescription, this);
	}
	
	public void onResume()
	{
		super.onResume();
		
		this.getSupportActionBar().setTitle(this._trackTitle);

		HashMap<String,Object> payload = new HashMap<String, Object>();
		payload.put(GroupActivity.GROUP_NAME, this.getSupportActionBar().getTitle());
		LogManager.getInstance(this).log("viewed_track", payload);

		View anchor = this.findViewById(R.id.track_background);
		this._controller.setAnchorView(anchor);
	}
	
	public void onDestroy()
	{
		super.onDestroy();

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
		if (item.getItemId() == R.id.action_help)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder = builder.setTitle(this._trackTitle);
			builder = builder.setMessage(this._trackDescription);
			
			builder.create().show();
		}
		
		return true;
	}
}
