package edu.northwestern.cbits.intellicare.mantra.activities;

import edu.northwestern.cbits.intellicare.mantra.GetImageListAndSizesTask;
import edu.northwestern.cbits.intellicare.mantra.R;
import edu.northwestern.cbits.intellicare.mantra.R.layout;
import edu.northwestern.cbits.intellicare.mantra.R.menu;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.widget.ProgressBar;

/**
 * Displays a progress bar.
 * @author mohrlab
 *
 */
public class ProgressActivity extends Activity {

	public static final String INTENT_KEY_URL = "url"; 
	public static final String INTENT_KEY_TYPE_GETIMAGESANDSIZES = "getImagesAndSizes";
	public static final int INTENT_VAL_TYPE_GETIMAGESANDSIZES = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_progress);
		
		Intent receivedIntent = this.getIntent();
		int type = receivedIntent.getIntExtra(INTENT_KEY_TYPE_GETIMAGESANDSIZES, 0);
		String url = receivedIntent.getStringExtra("url");
		
		switch(type) {
			case INTENT_VAL_TYPE_GETIMAGESANDSIZES:
				final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar1);
	        	new GetImageListAndSizesTask(this, progressBar).execute(url);
				break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.progress, menu);
		return true;
	}

}
