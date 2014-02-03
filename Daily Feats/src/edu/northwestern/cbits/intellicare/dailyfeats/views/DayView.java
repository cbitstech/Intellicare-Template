package edu.northwestern.cbits.intellicare.dailyfeats.views;

import edu.northwestern.cbits.intellicare.dailyfeats.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

public class DayView extends LinearLayout 
{
	public DayView(Context context, AttributeSet attrs, int defStyle) 
	{
		super(context, attrs, defStyle);
	}

	public DayView(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
        inflater.inflate(R.layout.view_day, this);
	}
	
	public DayView(Context context) 
	{
		super(context);
	}
}
