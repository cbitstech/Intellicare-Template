package edu.northwestern.cbits.intellicare.relax;

import java.io.IOException;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.MediaController;

public class AudioActivity extends ActionBarActivity implements OnPreparedListener, MediaController.MediaPlayerControl
{
	  private MediaController mediaController;
	  
	  private Handler handler = new Handler();

	  public void onCreate(Bundle savedInstanceState) 
	  {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_audio);

	    PlayerActivity._player.setOnPreparedListener(this);

	    mediaController = new MediaController(this);

		try {
			PlayerActivity._player.prepare();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	PlayerActivity._player.start();
	  }

	  @Override
	  protected void onStop() {
	    super.onStop();
	    mediaController.hide();
	  }

	  @Override
	  public boolean onTouchEvent(MotionEvent event) {
	    //the MediaController will hide after 3 seconds - tap the screen to make it appear again
	    mediaController.show();
	    return false;
	  }

	  //--MediaPlayerControl methods----------------------------------------------------
	  public void start() {
		  PlayerActivity._player.start();
	  }

	  public void pause() {
		  PlayerActivity._player.pause();
	  }

	  public int getDuration() {
	    return PlayerActivity._player.getDuration();
	  }

	  public int getCurrentPosition() {
	    return PlayerActivity._player.getCurrentPosition();
	  }

	  public void seekTo(int i) {
		  PlayerActivity._player.seekTo(i);
	  }

	  public boolean isPlaying() {
	    return PlayerActivity._player.isPlaying();
	  }

	  public int getBufferPercentage() {
	    return 0;
	  }

	  public boolean canPause() {
	    return true;
	  }

	  public boolean canSeekBackward() {
	    return true;
	  }

	  public boolean canSeekForward() {
	    return true;
	  }
	  //--------------------------------------------------------------------------------

	  public void onPrepared(MediaPlayer mediaPlayer) {
	    Log.e("PC", "onPrepared");
	    mediaController.setMediaPlayer(this);
	    mediaController.setAnchorView(findViewById(R.id.main_audio_view));
	    
	    handler.post(new Runnable() 
	    {
	      public void run() 
	      {
	    	  Log.e("PC", "POSTED");
	    	  
	        mediaController.setEnabled(true);
	        mediaController.show();
	      }
	    });
	  }

	@Override
	public int getAudioSessionId() 
	{
		return PlayerActivity._player.getAudioSessionId();
	}
}
