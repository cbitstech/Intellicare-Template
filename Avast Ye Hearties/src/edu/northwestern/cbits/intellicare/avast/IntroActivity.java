package edu.northwestern.cbits.intellicare.avast;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class IntroActivity extends ConsentedActivity 
{
	public static final String INTRO_SHOWN = "intro_shown";

	private int _step = 0;

    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.activity_intro);

        final IntroActivity me = this;
        
        Button back = (Button) this.findViewById(R.id.back_button);
        back.setOnClickListener(new OnClickListener()
        {
			public void onClick(View view) 
			{
				if (me._step > 0)
					me._step -= 1;
				
				me.updateLayout();
			}
        });

        Button next = (Button) this.findViewById(R.id.next_button);
        next.setOnClickListener(new OnClickListener()
        {
			public void onClick(View view) 
			{
				if (me._step < 4)
					me._step += 1;
				else
				{
					// finish
				}
				
				me.updateLayout();
			}
        });
    }
    
    protected void onResume()
    {
    	super.onResume();
    	
    	this.updateLayout();
    }

    private void updateLayout() 
	{
    	switch(this._step)
    	{
			case 0: 
				Log.e("AYH", "SHOW INTRO TXT");
				break;
			case 1: 
				Log.e("AYH", "OPEN CONTACT CARD");
				break;
			case 2: 
				Log.e("AYH", "SHOW ME CARD");
				
				Log.e("AYH", "PLACES INTRO");
				break;
			case 3: 
				Log.e("AYH", "PLACES PICKER");
				break;
			case 4: 
				Log.e("AYH", "THANKS & DONE");
				break;
    	}
	}
}
