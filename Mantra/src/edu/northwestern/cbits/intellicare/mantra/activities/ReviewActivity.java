package edu.northwestern.cbits.intellicare.mantra.activities;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import edu.northwestern.cbits.intellicare.mantra.EventLogging;
import edu.northwestern.cbits.intellicare.mantra.MantraBoard;
import edu.northwestern.cbits.intellicare.mantra.MantraBoardManager;
import edu.northwestern.cbits.intellicare.mantra.NotificationAlarm;
import edu.northwestern.cbits.intellicare.mantra.R;
import edu.northwestern.cbits.intellicare.mantra.R.layout;
import edu.northwestern.cbits.intellicare.mantra.R.menu;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
		reviewDialog(self);
	}

	/**
	 * Sets the photo count.
	 * @return 
	 */
	public String setPhotoCount(Activity activity) {
		// set the photo count
		TextView tvPhotosCount = (TextView) activity.findViewById(R.id.txtPhotosCount);
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		Date todayAtMidnight = c.getTime();
		
		String tokenizedString = tvPhotosCount.getText().toString();
		Log.d(CN+".setPhotoCount", "tokenizedString = " + tokenizedString);
		String photosCountDisplayText = tokenizedString.replaceAll(
				"^(.*)%NPHOTOS%(.*)$",
				"$1" + (NotificationAlarm.getCameraImagesSinceDate(this, todayAtMidnight)).size() + "$2"
			);
		Log.d(CN+".onCreate", "photosCountDisplayText = " + photosCountDisplayText);
		tvPhotosCount.setText(photosCountDisplayText);
		return photosCountDisplayText;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.review, menu);
		return true;
	}

	public static Uri activityUri(Context context) 
	{
		return Uri.parse("intellicare://mantra/review");
	}
	
	
	/**
	 * Displays a dialog box in which the user reviews their experience using the Mantra app.
	 * @param activity
	 */
	public void reviewDialog(final Activity activity) 
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		
		String photoCountText = self.setPhotoCount(activity);
		Log.d(CN+".reviewDialog", "photoCountText = " + photoCountText);
		LayoutInflater inflater = LayoutInflater.from(activity);		
		builder.setTitle(activity.getString(R.string.activity_review_dialog_title));
		
		View reviewView = inflater.inflate(R.layout.activity_review, null);
		builder.setView(reviewView);
		TextView photosCountTxt = (TextView) reviewView.findViewById(R.id.txtPhotosCount);
		photosCountTxt.setText(photoCountText);

		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog, int which) 
			{
				EventLogging.log(self, "Clicked OK.", "reviewDialog.onClick", CN);
				activity.finish();
			}
		});

		builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog, int which) 
			{
				EventLogging.log(self, "Clicked Cancel.", "reviewDialog.onClick", CN);
				activity.finish();
			}
		});
		
		builder.create().show();
	}
}
