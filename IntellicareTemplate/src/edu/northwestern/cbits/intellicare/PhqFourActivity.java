package edu.northwestern.cbits.intellicare;

import java.util.HashMap;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import edu.northwestern.cbits.ic_template.R;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class PhqFourActivity extends FormQuestionActivity 
{
	private HashMap<String, Object> _payload = new HashMap<String, Object>();
	
	private boolean _continue = false;
	
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_phq_four);
		
		this.getSupportActionBar().setTitle(R.string.phq_title);
		this.getSupportActionBar().setSubtitle(R.string.phq_subtitle);
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() ==  R.id.action_done)
		{
			HashMap<String, Object> payload = new HashMap<String, Object>();
			payload.put("payload", this._payload);
			
			LogManager.getInstance(this).log("phq4_submitted", payload);
			
			this.finish();
		}

		return true;
	}
	
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.getMenuInflater().inflate(R.menu.menu_rating, menu);

		MenuItem doneItem = menu.findItem(R.id.action_done);

		if (this._continue == false)
			doneItem.setVisible(false);

		return true;
	}


	protected void setupListeners() 
	{
		RadioGroup one = (RadioGroup) this.findViewById(R.id.phq_one);
		RadioGroup two = (RadioGroup) this.findViewById(R.id.phq_two);
		RadioGroup three = (RadioGroup) this.findViewById(R.id.phq_three);
		RadioGroup four = (RadioGroup) this.findViewById(R.id.phq_four);
		
		final RadioGroup[] groups =  { one, two, three, four };
		
		final PhqFourActivity me = this;
		
		OnCheckedChangeListener listener = new OnCheckedChangeListener()
		{
			public void onCheckedChanged(RadioGroup group, int id) 
			{
				for (RadioGroup radioGroup : groups)
				{
					int selected = radioGroup.getCheckedRadioButtonId();
					
					String key = null;
					
					if (radioGroup.getId() == R.id.phq_one)
						key = "anxious";
					else if (radioGroup.getId() == R.id.phq_two)
						key = "worry";
					else if (radioGroup.getId() == R.id.phq_three)
						key = "hopeless";
					else if (radioGroup.getId() == R.id.phq_four)
						key = "interest";
					
					int value = -1;

					if (selected == R.id.radio_zero)
						value = 0;
					else if (selected == R.id.radio_one)
						value = 1;
					else if (selected == R.id.radio_two)
						value = 2;
					else if (selected == R.id.radio_three)
						value = 3;
					
					if (value != -1)
						me._payload.put(key, Integer.valueOf(value));
					
					if (me._payload.size() >= 4)
					{
						me._continue = true;
						me.supportInvalidateOptionsMenu();
					}
				}
			}
		};
		
		one.setOnCheckedChangeListener(listener);
		two.setOnCheckedChangeListener(listener);
		three.setOnCheckedChangeListener(listener);
		four.setOnCheckedChangeListener(listener);
	}
}
