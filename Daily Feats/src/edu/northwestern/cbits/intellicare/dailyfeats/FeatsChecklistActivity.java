package edu.northwestern.cbits.intellicare.dailyfeats;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class FeatsChecklistActivity extends ConsentedActivity
{
	// Renamed variables to get closer to Google's style guide for Java.
	
    private ArrayList<Feat> mCurrentFeats = null;
    private int mCurrentFeatsLevel = -1;

    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_complete_checklist);
    }

    protected void onResume() 
    {
        super.onResume();

        this.mCurrentFeats = this.filterFeats();

        ArrayAdapter<Feat> featsAdapter = new ArrayAdapter<Feat>(this, R.layout.row_feat_checkbox, this.mCurrentFeats) 
		{
            public View getView (int position, View convertView, ViewGroup parent)
            {
                if (convertView == null)
                {
                    LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.row_feat_checkbox, null);
                }

                final Feat f = getItem(position);

                CheckBox check = (CheckBox) convertView.findViewById(R.id.feat_checkbox);
                
                check.setText(f.getFeatLabel());
                check.setChecked(f.isCompleted());
                check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
                {
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b)
                    {
                        f.setCompletedTo(b);
                        String name = f.getFeatName();
                        Log.e("featClicked", "CLICKED " + f.getFeatLabel() + "; " + name + " is " + String.valueOf(f.isCompleted()));
                    }
                });

                return convertView;
            }
        };

        // Handle feats checklist
        final ListView featsList = (ListView) this.findViewById(R.id.feats_checklist);
        featsList.setAdapter(featsAdapter);
    }
    
    private ArrayList<Feat> filterFeats() 
    {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	
        this.mCurrentFeatsLevel = prefs.getInt(getString(R.string.currentFeatsLevelKey), 2);
        Feat currentFeat;
        ArrayList<Feat> filteredFeats = new ArrayList<Feat>();
        filteredFeats.addAll(AppConstants.AllFeats);

        Iterator<Feat> it = filteredFeats.iterator();
        if (this.mCurrentFeatsLevel <= 2) {
            while (it.hasNext()) {
                currentFeat = (Feat) it.next();
                if(currentFeat.getFeatLevel() > this.mCurrentFeatsLevel) {
                    it.remove();
                }
            }
        }
        else {
            while (it.hasNext()) {
                currentFeat = (Feat) it.next();
                if (this.mCurrentFeatsLevel == 4) {
                    if(currentFeat.getFeatLevel() < 3) {
                        it.remove();
                    }
                }
                else {
                    if(currentFeat.getFeatLevel() != 3) {
                        it.remove();
                    }
                }
            }
        }

        return filteredFeats;
    }

    private HashMap<String, Object> saveList() 
    {

        // Bookkeeping and return values.
        HashMap<String, Object> results = new HashMap<String, Object>();
        Uri createdChecklist = Uri.EMPTY;

        Date now = new Date();
        Log.d("onSave", "Saving Responses at ("+AppConstants.iso8601Format.format(now)+")...");

        EditText fosView = (EditText) this.findViewById(R.id.feat_of_strength);
        String featOfStrengthEntry = fosView.getText().toString();
        Log.d("FeatofStrength", "Entry -> "+featOfStrengthEntry);


        // Build Checklist Object & Save Using DailyFeatsStore
        ContentValues checklist = new ContentValues();
        checklist.put(AppConstants.DEPRESSION_LEVEL, this.mCurrentFeatsLevel );
        checklist.put(AppConstants.dateTakenKey,      AppConstants.isoDateOnlyFormat.format(now) );
        checklist.put(AppConstants.dateTimeTakenKey,  now.getTime() );
        checklist.put(AppConstants.featOfStrengthKey, featOfStrengthEntry );

        createdChecklist = getContentResolver().insert(DailyFeatsStore.CHECKLISTS_URI, checklist);

        // Save Individual feat responses.
        Iterator<Feat> it = this.mCurrentFeats.iterator();
        Feat f;
        ContentValues featResponse;
        Uri featResponseURI;
        while(it.hasNext()) {

            f = (Feat) it.next();
            featResponse = new ContentValues();
            featResponse.put(AppConstants.checklistIdKey, createdChecklist.getFragment().toString());
            featResponse.put(AppConstants.featLevelKey,     f.getFeatLevel());
            featResponse.put(AppConstants.featNameKey,      f.getFeatName());
            featResponse.put(AppConstants.featCompletedKey, f.isCompleted());

            featResponseURI = getContentResolver().insert(DailyFeatsStore.FEATRESPONSES_URI, featResponse);
            Log.d("onSave", "Created Feat: "+ featResponseURI.toString() );
        }

        results.put("checklist", createdChecklist);
        results.put("strength_feat", featOfStrengthEntry);
        updateStreakCount();

        return results;
    }

    /* private void shareList( HashMap<String, Object> saveResults ) {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        String username = prefs.getString("username", "Corky Templeshire");
        StringBuilder shareText = new StringBuilder();
        shareText.append("<h2>Today, "+username+":</h2>");
        shareText.append("<ul>");

        Iterator<Feat> it = this.mCurrentFeats.iterator();
        Feat f;
        while(it.hasNext()) {
            f = (Feat) it.next();
            if(f.isCompleted())
            {
                shareText.append("<li>"+AppConstants.shareFeatText.get(f.getFeatName())+"</li>");
            }
        }
        shareText.append("</ul>");

        Log.d("onShare", "Share Text: "+shareText.toString());
    } */

    private void updateStreakCount() 
    {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

    	Date now = new Date();
        Date lastCompleted;

        // Get the date of the last time a checklist was completed...
        Cursor checklistsByRecency = getContentResolver().query(DailyFeatsStore.CHECKLISTS_URI,
                null,
                null,
                null,
                AppConstants.dateTimeTakenKey+" DESC");

        checklistsByRecency.moveToFirst();
        int i = checklistsByRecency.getColumnIndex(AppConstants.dateTimeTakenKey);
        try {
            lastCompleted = new Date(checklistsByRecency.getInt(i));
        }
        catch (Exception e) {
            lastCompleted = new Date(0);
        }

        // If the last time a checklist was completed, was within a day of today,
        // then increase the streak, otherwise set it to zero. Save it in the preferences.
        int currentStreak;
        if (withinADay(now, lastCompleted)) {
            currentStreak = prefs.getInt(AppConstants.currentStreakKey, 0);
            currentStreak += 1;
        }
        else {
            currentStreak = 0;
        }
        prefs.edit().putInt(AppConstants.currentStreakKey, currentStreak).commit();

    }

    private boolean withinADay(Date date1, Date date2) 
    {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        int absoluteDiff = Math.abs( cal1.get(Calendar.DAY_OF_YEAR) - cal2.get(Calendar.DAY_OF_YEAR));
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && ( absoluteDiff <= 1 );
    }

	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_feats_checklist, menu);

		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.action_save:
                this.saveList();
                
                this.startActivity(new Intent(this, HomeActivity.class));

                this.finish();
				
				break;
		}

		return true;
	}

}