package edu.northwestern.cbits.intellicare.dailyfeats.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import edu.northwestern.cbits.intellicare.dailyfeats.R;

public class DayView extends LinearLayout 
{
	private static final int EMPTY_COLOR = 0xffc0c0c0;
	private static final int DAY_COLOR = 0xffffffff;
	protected static final int SELECTED_COLOR = 0xff33B5E5;

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

	public void reset() 
	{
		this.setIsToday(false);
		this.setBackgroundColor(DayView.EMPTY_COLOR);
		this.setOnClickListener(null);

		TextView dayLabel = (TextView) this.findViewById(R.id.label_day);
		dayLabel.setText("");
	}

	public void setDay(String value, int dateDay, int color) 
	{
		this.setDay(value, dateDay);

		TextView valueLabel = (TextView) this.findViewById(R.id.label_value);
		valueLabel.setTextColor(color);
	}

	public void setDay(String value, int dateDay) 
	{
		TextView dayLabel = (TextView) this.findViewById(R.id.label_day);
		dayLabel.setText("" + dateDay);
		
		this.setBackgroundColor(DayView.DAY_COLOR);

		TextView valueLabel = (TextView) this.findViewById(R.id.label_value);
		valueLabel.setText(value);
	}

	public void setIsToday(boolean isToday) 
	{
		TextView dayLabel = (TextView) this.findViewById(R.id.label_day);

		if (isToday)
		{
			dayLabel.setTypeface(null, Typeface.BOLD);
			dayLabel.setTextSize(14);
		}
		else
		{
			dayLabel.setTypeface(null, Typeface.NORMAL);
			dayLabel.setTextSize(12);
		}
	}

	public void clearSelection() 
	{
		TextView dayLabel = (TextView) this.findViewById(R.id.label_day);
		
		if (dayLabel.getText().length() > 0)
			this.setBackgroundColor(DayView.DAY_COLOR);
		else
			this.setBackgroundColor(DayView.EMPTY_COLOR);
	}

	public void select() 
	{
		this.setBackgroundColor(DayView.SELECTED_COLOR);
	}
}
