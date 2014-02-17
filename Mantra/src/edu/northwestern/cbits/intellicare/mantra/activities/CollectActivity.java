package edu.northwestern.cbits.intellicare.mantra.activities;

import edu.northwestern.cbits.intellicare.mantra.R;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class CollectActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.collect_activity);
		Log.d("CollectActivity.onCreate", "entered");
	}
}
