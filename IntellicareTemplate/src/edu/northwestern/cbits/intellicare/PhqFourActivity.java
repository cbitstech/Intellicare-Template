package edu.northwestern.cbits.intellicare;

import edu.northwestern.cbits.ic_template.R;
import edu.northwestern.cbits.intellicare.views.StarRatingView;
import edu.northwestern.cbits.intellicare.views.StarRatingView.OnRatingChangeListener;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

public class PhqFourActivity extends ActionBarActivity 
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_phq_four);
		
		this.getSupportActionBar().setTitle("pHq-fOuR");
	}
}
