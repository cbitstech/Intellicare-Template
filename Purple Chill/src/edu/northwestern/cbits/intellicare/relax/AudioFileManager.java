package edu.northwestern.cbits.intellicare.relax;

import java.io.IOException;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.widget.MediaController.MediaPlayerControl;

public class AudioFileManager implements MediaPlayerControl
{
	private static AudioFileManager _instance = null;
	
	private static String PLACEHOLDER = "file:///android_asset/audio/silent.mp3";

	private Context _context = null;
	private MediaPlayer _player = null;
	
	private String _url = null;
	private String _title = null;
	private String _group = null;
	
	public AudioFileManager(Context context) 
	{
		this._context = context;
		
		this.setUrl(AudioFileManager.PLACEHOLDER, null, null, null);
	}

	public static AudioFileManager getInstance(Context context)
	{
		if (AudioFileManager._instance != null)
			return AudioFileManager._instance;
		
		AudioFileManager._instance = new AudioFileManager(context.getApplicationContext());
		
		return AudioFileManager._instance;
	}
	
	public void setUrl(String url, String title, String group, OnPreparedListener listener)
	{
		this._url = url;
		this._title = title;
		this._group = group;
		
		try 
		{
			AssetFileDescriptor afd = this._context.getAssets().openFd(this._url.replace("file:///android_asset/", ""));

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
			
			this._player.prepareAsync();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			
			this.setUrl(AudioFileManager.PLACEHOLDER, null, null, null);
		}
	}
	
	public boolean hasPlayer()
	{
		return (this._player != null);
	}
	
	public boolean canPause() 
	{
		if (AudioFileManager.PLACEHOLDER.equals(this._url))
			return false;
		
		return true;
	}

	public boolean canSeekBackward() 
	{
		if (AudioFileManager.PLACEHOLDER.equals(this._url))
			return false;

		return true;
	}

	public boolean canSeekForward() 
	{
		if (AudioFileManager.PLACEHOLDER.equals(this._url))
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
		if (AudioFileManager.PLACEHOLDER.equals(this._url))
			return 0;
		
		return this._player.getCurrentPosition();
	}

	public int getDuration() 
	{
		if (AudioFileManager.PLACEHOLDER.equals(this._url))
			return 0;

		return this._player.getDuration();
	}

	public boolean isPlaying() 
	{
		if (AudioFileManager.PLACEHOLDER.equals(this._url))
			return false;
		
		return this._player.isPlaying();
	}

	public void pause() 
	{
		this._player.pause();
	}

	public void seekTo(int location) 
	{
		this._player.seekTo(location);
	}

	public void start() 
	{
		if (AudioFileManager.PLACEHOLDER.equals(this._url))
			return;

		this._player.start();
	}

	public String currentTitle() 
	{
		return this._title;
	}

	public String currentGroup() 
	{
		return this._group;
	}
}
