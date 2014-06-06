package edu.northwestern.cbits.intellicare.icope;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

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

        Intent libraryIntent = new Intent(this, LibraryActivity.class);
        this.startActivity(libraryIntent);
    }


}
