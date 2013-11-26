package edu.northwestern.cbits.intellicare.dailyfeats;

import edu.northwestern.cbits.intellicare.ConsentedActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Gabe on 9/16/13.
 */
public class HomeActivity extends ConsentedActivity 
{
    private Cursor matchingResponses;

    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    protected void onResume() 
    {
        super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // This one displays Feat labels and their total counts.
        // Defining an instance of an Array Adapter, anonymously sub-classed with a different view method
        ArrayAdapter<Feat> featsAdapter = new ArrayAdapter<Feat>(this, R.layout.p_feat_count, AppConstants.AllFeats) {

            public View getView (int position, View convertView, ViewGroup parent)
            {
                // Does this view already exist (i.e., recycling an old one),
                // or are we creating it for the first time?
                if (convertView == null)
                {
                    LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.p_feat_count, null);
                }

                final Feat f = getItem(position);


                TextView countView = (TextView) convertView.findViewById(R.id.feat_count);
                countView.setText(String.valueOf(getFeatCount(f)));

                TextView featLabel = (TextView) convertView.findViewById(R.id.feat_label);
                featLabel.setText(f.getFeatLabel());

                return convertView;
            }

        };

        final TextView streak = (TextView) this.findViewById(R.id.completion_streak);
        streak.setText( String.valueOf(getCompletionStreakCount()) );

        this.getSupportActionBar().setSubtitle(this.getString(R.string.subtitle_next_reminder, this.getReminderTimeString()));

        final TextView recentFeat = (TextView) this.findViewById(R.id.most_recent_feat);
        String rfText = getRecentStrengthFeat();
        if (!rfText.equals("")) {
            recentFeat.setText( rfText );
        }
        else {
            recentFeat.setText( getString(R.string.no_feats_of_strength) );
            final Button moreStrengths = (Button) this.findViewById(R.id.all_strength_feats_button);
            moreStrengths.setVisibility(View.GONE);
        }

        final Button viewFOSBtn = (Button) this.findViewById(R.id.all_strength_feats_button);
        viewFOSBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(HomeActivity.this, ViewFeatCollectionActivity.class);
                startActivity(i);
            }
        });

        final TextView featsTotal = (TextView) this.findViewById(R.id.feats_total);
        featsTotal.setText( String.valueOf(getTotalFeatCount()) );

        final ListView featsList = (ListView) this.findViewById(R.id.show_feats_list);
        featsList.setAdapter(featsAdapter);

    }

    private int getCompletionStreakCount() 
    {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	
        Log.d("HomeActivity", "Look up completion streak");
        int currentStreak = prefs.getInt(AppConstants.currentStreakKey, 0);
        return currentStreak;
    }

    private int getFeatCount(Feat feat) {
        matchingResponses = getContentResolver().query(DailyFeatsStore.FEATRESPONSES_URI,
                                        // columns
                                        new String[] {
                                                AppConstants.featNameKey,
                                                AppConstants.featCompletedKey
                                        },
                                        // where clause
                                        AppConstants.featCompletedKey+" = 1"+
                                        " AND "+
                                        AppConstants.featNameKey+" = ?",
                                        // where clause values:
                                        new String[]{ feat.getFeatName() },
                                        // sort order
                                        null);
        int total = matchingResponses.getCount();
        matchingResponses.close();
        return total;
    }

    private String getRecentStrengthFeat() {
        matchingResponses = listsWithFeatsOfStrength(new String[]{
                                AppConstants.featOfStrengthKey,
                                AppConstants.dateTimeTakenKey
                            }, AppConstants.dateTimeTakenKey+" DESC");

        String text = "";
        if (matchingResponses.getCount() > 0) {
            matchingResponses.moveToFirst();
            int i = matchingResponses.getColumnIndex(AppConstants.featOfStrengthKey);

            while ( text.equals("") && !matchingResponses.isAfterLast() ) {
                text = matchingResponses.getString(i);
                matchingResponses.moveToNext();
            }

        }

        matchingResponses.close();
        return text;
    }

    private int getTotalFeatCount() {
        int count;
        matchingResponses = completedFeats(new String[] {AppConstants.featCompletedKey}, null);
        count = matchingResponses.getCount();
        matchingResponses.close();
        return count;
    }

    /**
     *  getReminderTimeString
     *  duplicated between Setup Activity and HomeActivity
     *  out of expediency, and not sure how to share functions that depend on Android Context
     *  between Activities
     *  -Gabe
     **/
    private String getReminderTimeString() {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        String amPM;
        String minutes;
        int reminderHour = prefs.getInt(AppConstants.REMINDER_HOUR,    AppConstants.DEFAULT_HOUR);
        int reminderMins = prefs.getInt(AppConstants.REMINDER_MINUTE, AppConstants.DEFAULT_MINUTE);

        if (reminderHour > 12) {
            amPM = "PM";
            reminderHour = reminderHour - 12;
        }
        else {
            amPM = "AM";
        }

        if (reminderMins < 10) {
            minutes = "0"+String.valueOf(reminderMins);
        }
        else {
            minutes = String.valueOf(reminderMins);
        }

        return String.valueOf(reminderHour)+":"+minutes+" "+amPM;
    }

    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment(null);
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }


    public Cursor listsWithFeatsOfStrength(String[] projection, String order) {
        return getContentResolver().query(
                DailyFeatsStore.CHECKLISTS_URI,
                // columns
                projection,
                // where clause
                AppConstants.featOfStrengthKey+" IS NOT NULL AND "+
                        AppConstants.featOfStrengthKey+" != ''",
                // where clause values
                null,
                order);
    }

    public Cursor completedFeats(String[] projection, String order) {
        return getContentResolver().query(DailyFeatsStore.FEATRESPONSES_URI,
                projection,
                AppConstants.featCompletedKey+" = 1",
                null,
                order);
    }
    
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_home, menu);

		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.action_time:
				Toast.makeText(this, "sHoW tIme PicKer", Toast.LENGTH_LONG).show();
				
				break;
		}

		return true;
	}

}