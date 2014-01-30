package edu.northwestern.cbits.intellicare.mantra.activities;

import edu.northwestern.cbits.intellicare.mantra.R;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class NewMantraActivity extends Activity implements OnItemSelectedListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_mantra_activity);
		Spinner spinner = (Spinner) findViewById(R.id.spinner1);
		spinner.setOnItemSelectedListener(this);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
		        R.array.sample_mantras, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home_activity_actions, menu);
		return true;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {
		EditText textField = (EditText) findViewById(R.id.new_mantra_input);
		textField.setText(parent.getItemAtPosition(pos).toString());
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
		
	}

}
