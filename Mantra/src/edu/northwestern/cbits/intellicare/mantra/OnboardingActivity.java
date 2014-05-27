package edu.northwestern.cbits.intellicare.mantra;

import edu.northwestern.cbits.intellicare.SequentialPageActivity;
import edu.northwestern.cbits.intellicare.mantra.activities.DownloadManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;

/**
 * Executes the onboarding content navigation and display.
 * @author mohrlab
 *
 */
public class OnboardingActivity extends SequentialPageActivity {

	public static final String ONBOARDING_COMPLETED = "ONBOARDING_COMPLETED";
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	public int pagesSequence() {
		return R.array.onboarding_slide_texts;
	}
	
	public int titlesSequence() {
		return R.array.onboarding_slide_titles;
	}

	public void onSequenceComplete() {
		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);
		Editor e = p.edit();
		e.putBoolean(ONBOARDING_COMPLETED, true);
		e.commit();
	}
	
}
