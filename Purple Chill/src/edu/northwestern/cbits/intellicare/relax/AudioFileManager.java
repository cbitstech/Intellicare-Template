package edu.northwestern.cbits.intellicare.relax;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.MediaController.MediaPlayerControl;

public class AudioFileManager implements MediaPlayerControl, OnCompletionListener
{
	public static final String TRACK_URI = "track_uri";
	public static final String TRACK_TITLE = "track_title";
	public static final String TRACK_DESCRIPTION = "track_description";
	private static final int NOTIFICATION_ID = 901;
	
	private static AudioFileManager _instance = null;
	
	private static String PLACEHOLDER = "file:///android_asset/audio/silent.mp3";

	private Context _context = null;
	private MediaPlayer _player = null;

	private Uri _currentTrack = null;
	private String _currentTitle = null;
	private String _currentDescription = null;
	
	public Intent launchIntentForCurrentTrack()
	{
		if (this._currentTrack != null)
			return this.launchIntentForUri(this._currentTrack, this._currentTitle, this._currentDescription);

		return null;
	}
	
	public Intent launchIntentForUri(Uri trackUri, String title, String description)
	{
		Intent intent = new Intent(this._context, PlayerActivity.class);

		intent.putExtra(AudioFileManager.TRACK_URI, trackUri.toString());
		intent.putExtra(AudioFileManager.TRACK_TITLE, title);
		intent.putExtra(AudioFileManager.TRACK_DESCRIPTION, description);
		
		return intent;
	}

	public AudioFileManager(Context context) 
	{
		this._context = context;
		
		this.setTrackUri(Uri.parse(AudioFileManager.PLACEHOLDER), null, null, null);
	}
	
	boolean isPlaceholder(Uri uri)
	{
		return Uri.parse(AudioFileManager.PLACEHOLDER).equals(uri);
	}

	public static AudioFileManager getInstance(Context context)
	{
		if (AudioFileManager._instance != null)
			return AudioFileManager._instance;
		
		AudioFileManager._instance = new AudioFileManager(context.getApplicationContext());
		
		return AudioFileManager._instance;
	}
	
	public boolean hasPlayer()
	{
		return (this._player != null);
	}
	
	public boolean canPause() 
	{
		if (this.isPlaceholder(this._currentTrack))
			return false;
		
		return true;
	}

	public boolean canSeekBackward() 
	{
		if (AudioFileManager.PLACEHOLDER.equals(this))
			return false;

		return true;
	}

	public boolean canSeekForward() 
	{
		if (this.isPlaceholder(this._currentTrack))
			return false;

		return true;
	}

	public int getAudioSessionId() 
	{
		return this._player.getAudioSessionId();
	}

	public int getBufferPercentage() 
	{
		return 0;
	}

	public int getCurrentPosition() 
	{
		if (this.isPlaceholder(this._currentTrack))
			return 0;
		
		return this._player.getCurrentPosition();
	}

	public int getDuration() 
	{
		if (this.isPlaceholder(this._currentTrack))
			return 0;

		return this._player.getDuration();
	}

	public boolean isPlaying() 
	{
		if (this.isPlaceholder(this._currentTrack))
			return false;
		
		return this._player.isPlaying();
	}

	public void pause() 
	{
		this._player.pause();
		
		NotificationManager manager = (NotificationManager) this._context.getSystemService(Context.NOTIFICATION_SERVICE);

		manager.cancel(AudioFileManager.NOTIFICATION_ID);
	}

	public void seekTo(int location) 
	{
		this._player.seekTo(location);
	}

	@SuppressLint("NewApi")
	public void start() 
	{
		if (this.isPlaceholder(this._currentTrack))
			return;

		this._player.start();

		NotificationManager manager = (NotificationManager) this._context.getSystemService(Context.NOTIFICATION_SERVICE);

		manager.cancel(ScheduleManager.NOTIFICATION_ID);
		
        PendingIntent pi = PendingIntent.getActivity(this._context, 0, this.launchIntentForCurrentTrack(), PendingIntent.FLAG_UPDATE_CURRENT);

		Notification note = null;
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
		{
			Notification.Builder builder = new Notification.Builder(this._context);
			
			builder.setContentIntent(pi);
			builder.setContentTitle(this._context.getString(R.string.app_name));
			builder.setContentText(this._currentTitle);
			builder.setSmallIcon(R.drawable.ic_reminder);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
				builder.setStyle(new Notification.BigTextStyle().bigText(this._currentTitle));

			builder.setTicker(this._currentTitle);

			note = builder.build();
		}
		else
		{
			NotificationCompat.Builder builder = new NotificationCompat.Builder(this._context);

			builder.setContentIntent(pi);
			builder.setContentTitle(this._context.getString(R.string.app_name));
			builder.setContentText(this._currentTitle);
			builder.setSmallIcon(R.drawable.ic_reminder);

			note = builder.build();
		}
		
		note.flags = Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;
		
		manager.notify(AudioFileManager.NOTIFICATION_ID, note);
	}

	public void setTrackUri(Uri uri, String title, String description, OnPreparedListener listener) 
	{
		if (uri.equals(this._currentTrack))
		{
			if (this._player != null)	
				this._player.setOnPreparedListener(listener);
			
			if (listener != null)
				listener.onPrepared(this._player);
		}
		else
		{
			this._currentTrack = uri;
			this._currentTitle = title;
			this._currentDescription = description;
			
			try 
			{
				String uriString = this._currentTrack.toString();
				
				AssetFileDescriptor afd = this._context.getAssets().openFd(uriString.replace("file:///android_asset/", ""));

				if (this._player != null)
				{
					this._player.stop();
					this._player.release();
					
					this._player = null;
				}
				
				this._player = new MediaPlayer();
				this._player.setAudioStreamType(AudioManager.STREAM_MUSIC);
				this._player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());

				this._player.setOnPreparedListener(listener);
				this._player.setOnCompletionListener(this);
				
				this._player.prepare();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
				
				this.setTrackUri(Uri.parse(AudioFileManager.PLACEHOLDER), null, null, null);
			}
		}
	}

	public String currentTrackTitle() 
	{
		return this._currentTitle;
	}

	public void clearPreparedListener() 
	{
		if (this._player != null)
			this._player.setOnPreparedListener(null);
	}

	public void resetController() 
	{
		if (this._player != null)
		{
			this._player.setOnPreparedListener(null);
		}
	}

	public void onCompletion(MediaPlayer player) 
	{
		NotificationManager manager = (NotificationManager) this._context.getSystemService(Context.NOTIFICATION_SERVICE);

		manager.cancel(AudioFileManager.NOTIFICATION_ID);
		
		Intent intent = this.launchIntentForCurrentTrack();
		intent.putExtra(PlayerActivity.REQUEST_STRESS, true);
		
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		this._context.startActivity(intent);
	}
}
