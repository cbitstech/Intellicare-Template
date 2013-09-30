package edu.northwestern.cbits.intellicare;

import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import edu.northwestern.cbits.ic_template.R;

public class PhqFourActivity extends FormQuestionActivity 
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_phq_four);
		
		this.getSupportActionBar().setTitle(R.string.phq_title);
		this.getSupportActionBar().setSubtitle(R.string.phq_subtitle);
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
					
					me.didUpdate();
				}
			}
		};
		
		one.setOnCheckedChangeListener(listener);
		two.setOnCheckedChangeListener(listener);
		three.setOnCheckedChangeListener(listener);
		four.setOnCheckedChangeListener(listener);
	}

	protected String responsesKey() 
	{
		return "phq4_response";
	}
	
	protected boolean canSubmit() 
	{
		return this._payload.size() >= 4;
	}
}
