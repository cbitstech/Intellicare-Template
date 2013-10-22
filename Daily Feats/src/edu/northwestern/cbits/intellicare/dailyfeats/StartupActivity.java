package edu.northwestern.cbits.intellicare.dailyfeats;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;

import java.util.Calendar;
import java.util.Date;

public class StartupActivity extends Activity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(android.R.menu.startup, android.R.menu);
//        return true;
//    }

    protected void onResume() {
        super.onResume();

        /**
         * See if we have completed setup.
         * If we have, go on.
         * If not, launch setup.
         */
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // FOR-TESTING:
        // When you want to test setup, you can run it in a a constant loop
        // with this line:
//        prefs.edit().putInt(AppConstants.currentSetupKey, AppConstants.setupWelcome).commit();

        int currentSetupStep = prefs.getInt(AppConstants.currentSetupKey, AppConstants.setupWelcome );

        Intent i;
        if (currentSetupStep > AppConstants.setupConclusion) {
            /**
             * Is it checklist time?
             * If so, start checklist.  If not, go to homepage.
             */
            // FOR-PRODUCTTION:
//            boolean checklistTime = timeToCompleteChecklist();
            // FOR-TESTING you can use:
            boolean checklistTime = true;
//            boolean checklistTime = false;
            if ( checklistTime ) {
                Log.d("StartupActivity", "load checklist");
                i = new Intent(this, CompleteFeatsChecklistActivity.class);
            }
            else
            {
                Log.d("StartupActivity", "load home");
                i = new Intent(this, HomeActivity.class);
            }

        }
        else {
            // Launch Setup
            Log.d("StartupActivity", "load setup");
            i = new Intent(this, SetupActivity.class);
        }

        startActivity(i);
        this.finish();

    }

    private boolean timeToCompleteChecklist() {

        int reminderHour   = prefs.getInt(getString(R.string.preferredHourKey), AppConstants.defaultReminderHour);
        int reminderMinutes = prefs.getInt(getString(R.string.preferredMinutesKey), AppConstants.defaultReminderMinutes);

//      FOR-TESTING:
        boolean checklistCompletedToday = false;
        Calendar c = Calendar.getInstance();

        return checklistCompletedToday && (
                reminderHour > c.get(Calendar.HOUR_OF_DAY) ||
                            (
                                reminderHour == c.get(Calendar.HOUR_OF_DAY) &&
                                reminderMinutes > c.get(Calendar.MINUTE)
                            )
                );
    }
    
}