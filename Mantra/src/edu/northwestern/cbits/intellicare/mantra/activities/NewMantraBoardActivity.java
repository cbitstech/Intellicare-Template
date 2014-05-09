package edu.northwestern.cbits.intellicare.mantra.activities;

import edu.northwestern.cbits.intellicare.mantra.MantraBoard;
import edu.northwestern.cbits.intellicare.mantra.MantraBoardManager;
import edu.northwestern.cbits.intellicare.mantra.R;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

public class NewMantraBoardActivity extends Activity implements OnItemSelectedListener {

	public final static String CN = "NewMantraBoardActivity";
	public final static String FOCUS_BOARD_ID = "edu.northwestern.cbits.intellicare.mantra.FOCUS_BOARD_ID";
	private MantraBoardManager mFocusBoardManager;
	private static NewMantraBoardActivity self = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_focus_board_activity);
		self = this;
		
		Log.d(CN+".onCreate", "entered");
		
		mFocusBoardManager = MantraBoardManager.get(this);
		populateSpinner();
		
		addCancelListener();
		addSubmitListener();
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {
		EditText mMantraText = (EditText) findViewById(R.id.new_mantra_input);
		mMantraText.setText(parent.getItemAtPosition(pos).toString());
	}

	private void populateSpinner() {
		Spinner spinner = (Spinner) findViewById(R.id.new_mantra_examples_spinner);
		spinner.setOnItemSelectedListener(this);
		// Create an ArrayAdapter using the string array and a default spinner
		// layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.sample_mantras,
				android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(adapter);
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}

	private void addCancelListener() {
		Button cancelButton = (Button) findViewById(R.id.new_focus_board_cancel);
//		final Intent intent = new Intent(this, HomeActivity.class);
		final Intent intent = new Intent(this, IndexActivity.class);
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(intent);
			}
		});
	}

	private void addSubmitListener() {
		Button submitButton = (Button) findViewById(R.id.new_focus_board_submit);
		final Intent intent = new Intent(this, SingleMantraBoardActivity.class);
		submitButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText mMantraText = (EditText) findViewById(R.id.new_mantra_input);
				String mantra = mMantraText.getText().toString().trim();

				if (mantra.equals("")) {
					Toast.makeText(NewMantraBoardActivity.this, R.string.empty_mantra_toast, Toast.LENGTH_SHORT) .show();
				} else {
					MantraBoard mantraBoard = mFocusBoardManager.createFocusBoard(mantra);
					intent.putExtra(FOCUS_BOARD_ID, mantraBoard.getId());

					// handle image-URI-passing intent from HomeActivity
					Intent intentFromIndexActivity = getIntent();
					if(intentFromIndexActivity != null) {
						Uri uriFromImageBrowser = intentFromIndexActivity.getData();
						if(uriFromImageBrowser != null) {
							// get the URL returned by the image browser
							Log.d(CN+".addSubmitListener", "uriFromImageBrowser = " + uriFromImageBrowser.toString());
							intent.setData(intentFromIndexActivity.getData());
						}
					}
					
					startActivity(intent);
					self.finish();
				}
			}
		});
	}
}
