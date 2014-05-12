package edu.northwestern.cbits.intellicare.slumbertime;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.ErrorReason;
import com.google.android.youtube.player.YouTubePlayer.PlayerStateChangeListener;
import com.google.android.youtube.player.YouTubePlayerView;

public class YouTubeActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener, PlayerStateChangeListener 
{
	private static final int RECOVERY_DIALOG_REQUEST = 1;
	private static final String DEVELOPER_KEY = "AIzaSyDgYRpl_PyoNL68fEzwtZe3X1V1f-zSN1I";
	public static final String VIDEO_ID = "video_id";

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH) @Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
    	
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    	this.setContentView(R.layout.activity_youtube);

    	YouTubePlayerView youtube = (YouTubePlayerView) findViewById(R.id.youtube_view);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        {
		    youtube.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        }

    	youtube.initialize(YouTubeActivity.DEVELOPER_KEY, this);
	}

	public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) 
	{
		// Specify that we want to handle fullscreen behavior ourselves.
		player.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
		player.setPlayerStateChangeListener(this);
		
		if (!wasRestored) 
		{
			player.cueVideo(this.getIntent().getStringExtra(YouTubeActivity.VIDEO_ID));
			
			player.play();
		}	
	}
	
	@Override
	public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult errorReason)
	{
		if (errorReason.isUserRecoverableError()) 
		{
			errorReason.getErrorDialog(this, RECOVERY_DIALOG_REQUEST).show();
		}
		else 
		{
//		  String errorMessage = String.format(getString(R.string.error_player), errorReason.toString());
		  Toast.makeText(this, errorReason.toString(), Toast.LENGTH_LONG).show();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		if (requestCode == RECOVERY_DIALOG_REQUEST) 
		{
		    YouTubePlayerView youtube = (YouTubePlayerView) findViewById(R.id.youtube_view);

		    youtube.initialize(YouTubeActivity.DEVELOPER_KEY, this);
		}
	}

	@Override
	public void onAdStarted() 
	{

	}

	@Override
	public void onError(ErrorReason arg0) 
	{

	}

	@Override
	public void onLoaded(String arg0) 
	{

	}

	@Override
	public void onLoading() 
	{

	}

	@Override
	public void onVideoEnded() 
	{
		this.finish();
	}

	@Override
	public void onVideoStarted() 
	{

	}
}
