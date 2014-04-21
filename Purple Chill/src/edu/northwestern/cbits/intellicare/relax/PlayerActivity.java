package edu.northwestern.cbits.intellicare.relax;

import java.util.HashMap;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager.BadTokenException;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
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
	
	public void onResume() {

        super.onResume();


        this._trackUri = Uri.parse(this.getIntent().getStringExtra(AudioFileManager.TRACK_URI));
        this._trackTitle = this.getIntent().getStringExtra(AudioFileManager.TRACK_TITLE);
        this._trackDescription = this.getIntent().getStringExtra(AudioFileManager.TRACK_DESCRIPTION);

        this.getSupportActionBar().setTitle(this._trackTitle);

        this._controller = new PersistentMediaController(this);

        AudioFileManager audio = AudioFileManager.getInstance(this);
        this._controller.setMediaPlayer(audio);

        audio.setTrackUri(this._trackUri, this._trackTitle, this._trackDescription, this);

        HashMap<String, Object> payload = new HashMap<String, Object>();
        payload.put(GroupActivity.GROUP_NAME, this.getSupportActionBar().getTitle());
        LogManager.getInstance(this).log("viewed_track", payload);

        ViewPager pager = (ViewPager) this.findViewById(R.id.track_backgrounds);

        PagerAdapter adapter = new PagerAdapter() {
            public int getCount() {
                return 7;
            }

            public boolean isViewFromObject(View view, Object content) {
                ImageView image = (ImageView) view;
                Integer identifier = (Integer) content;

                return image.getTag().equals(identifier);
            }

            public void destroyItem(ViewGroup container, int position, Object content) {
                int toRemove = -1;

                for (int i = 0; i < container.getChildCount(); i++) {
                    View child = container.getChildAt(i);

                    if (this.isViewFromObject(child, content))
                        toRemove = i;
                }

                if (toRemove >= 0)
                    container.removeViewAt(toRemove);
            }

            public Object instantiateItem(ViewGroup container, int position) {
                ImageView image = new ImageView(container.getContext());

                int imageId = 0;

                switch (position) {
                    case 0:
                        imageId = R.drawable.boat;
                        break;
                    case 1:
                        imageId = R.drawable.butterfly;
                        break;
                    case 2:
                        imageId = R.drawable.flower;
                        break;
                    case 3:
                        imageId = R.drawable.steps;
                        break;
                    case 4:
                        imageId = R.drawable.stones;
                        break;
                    case 5:
                        imageId = R.drawable.sunset;
                        break;
                    case 6:
                        imageId = R.drawable.tree;
                        break;
                }

                image.setImageResource(imageId);
                image.setTag(Integer.valueOf(imageId));

                container.addView(image);

                LayoutParams layout = (LayoutParams) image.getLayoutParams();
                layout.height = LayoutParams.MATCH_PARENT;
                layout.width = LayoutParams.MATCH_PARENT;

                image.setLayoutParams(layout);

                image.setScaleType(ScaleType.CENTER_CROP);

                return Integer.valueOf(imageId);
            }
        };

        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(1);

        this._controller.setAnchorView(pager);

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
		else if (item.getItemId() == android.R.id.home)
		{
			if (this.isTaskRoot())
			{
				Intent intent = new Intent(this, IndexActivity.class);
				this.startActivity(intent);
			}

			this.finish();
		}
		
		return true;
	}
}
