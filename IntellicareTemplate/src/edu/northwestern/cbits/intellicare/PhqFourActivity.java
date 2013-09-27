package edu.northwestern.cbits.intellicare;

import java.util.HashMap;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import edu.northwestern.cbits.ic_template.R;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class PhqFourActivity extends FormQuestionActivity 
{
	private HashMap<String, Object> _payload = new HashMap<String, Object>();
	
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
				boolean transmit = true;
				
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

					if (selected == -1)
						transmit = false;
					else if (selected == R.id.radio_zero)
						value = 0;
					else if (selected == R.id.radio_one)
						value = 1;
					else if (selected == R.id.radio_two)
						value = 2;
					else if (selected == R.id.radio_three)
						value = 3;
					
					if (value != -1)
						me._payload.put(key, Integer.valueOf(value));
					
					if (transmit == true)
					{
						Button continueButton = (Button) me.findViewById(R.id.continue_button);
						
						continueButton.setEnabled(true);
						
						continueButton.setOnClickListener(new OnClickListener()
						{
							public void onClick(View view) 
							{
								LogManager.getInstance(me).log("phq4_complete", me._payload);
								
								Log.e("IT", "GO TO NEXT VIEW...");
								
								me.finish();
							}							
						});
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
