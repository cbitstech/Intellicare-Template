package edu.northwestern.cbits.intellicare.mantra.activities;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import edu.northwestern.cbits.intellicare.mantra.NotificationAlarm;
import edu.northwestern.cbits.intellicare.mantra.R;
import edu.northwestern.cbits.intellicare.mantra.R.layout;
import edu.northwestern.cbits.intellicare.mantra.R.menu;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Enables the user to review their activity and rate their experience with Mantra.
 * 
 * Src biz req (Stephen, 20140303): "(3) End of Day Messages: Review based messages of daily activity (you took X pics today), encouragement to visit mantra boards, and rating of app."
 * @author mohrlab
 *
 */
public class ReviewActivity extends Activity {
	private String CN = "ReviewActivity";

	private final UUID reviewInstanceId = UUID.randomUUID();
	private final ReviewActivity self = this;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_review);
		
		// set the change listener on the rating bar
		RatingBar rb = (RatingBar) this.findViewById(R.id.mantraAppRatingBar);
		rb.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
			
			@Override
			public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
				Toast.makeText(self, "(via ReviewActivity.onCreate.onRatingChanged) TODO: integrate this rating bar's change with the Intellicare server.", Toast.LENGTH_LONG).show();
			}
		});
		
		// set the on-button-tap-to-review button
		Button btnGoHome = (Button) this.findViewById(R.id.btnGoHome);
		btnGoHome.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				self.startActivity(new Intent(self, IndexActivity.class));
				self.finish();
			}
		});
		
		// set the photo count
		TextView tvPhotosCount = (TextView) this.findViewById(R.id.txtPhotosCount);
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		Date todayAtMidnight = c.getTime();
		
		String photosCountDisplayText = tvPhotosCount.getText().toString().replaceAll(
				"^(.*)%NPHOTOS%(.*)$",
				"$1" + NotificationAlarm.getCameraImagesSinceDate(this, todayAtMidnight) + "$2"
			);
		Log.d(CN+".onCreate", "photosCountDisplayText = " + photosCountDisplayText);
		tvPhotosCount.setText(photosCountDisplayText);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.review, menu);
		return true;
	}

}
