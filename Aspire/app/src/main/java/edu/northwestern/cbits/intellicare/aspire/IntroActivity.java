package edu.northwestern.cbits.intellicare.aspire;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import edu.northwestern.cbits.intellicare.SequentialPageActivity;

/**
 * Created by Gwen on 5/29/2014.
 */
public class IntroActivity extends SequentialPageActivity {

    public static final String INTRO_SHOWN = "intro_shown";

    @Override
    //calls URLS from string array
    public int pagesSequence() {
        return R.array.intro_content_urls;
    }

    @Override
    //calls titles from string array
    public int titlesSequence() {
        return R.array.intro_content_titles;
    }

    public void onSequenceComplete(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor e = prefs.edit();
        e.putBoolean(IntroActivity.INTRO_SHOWN, true);
        e.commit();

        Log.e("intro activity", INTRO_SHOWN);

        Intent mainIntent = new Intent(this, MainActivity.class);
        this.startActivity(mainIntent);
    }
}
