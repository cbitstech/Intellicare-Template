package edu.northwestern.cbits.intellicare.dailyfeats.views;

import java.util.Calendar;
import java.util.Date;

import edu.northwestern.cbits.intellicare.dailyfeats.R;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CalendarView extends LinearLayout 
{
	private static int[] _dateCells = { R.id.cell_11, R.id.cell_12, R.id.cell_13, R.id.cell_14, R.id.cell_15, R.id.cell_16, R.id.cell_17, 
										R.id.cell_21, R.id.cell_22, R.id.cell_23, R.id.cell_24, R.id.cell_25, R.id.cell_26, R.id.cell_27, 
										R.id.cell_31, R.id.cell_32, R.id.cell_33, R.id.cell_34, R.id.cell_35, R.id.cell_36, R.id.cell_37, 	
										R.id.cell_41, R.id.cell_42, R.id.cell_43, R.id.cell_44, R.id.cell_45, R.id.cell_46, R.id.cell_47, 
										R.id.cell_51, R.id.cell_52, R.id.cell_53, R.id.cell_54, R.id.cell_55, R.id.cell_56, R.id.cell_57,
										R.id.cell_61, R.id.cell_62, R.id.cell_63, R.id.cell_64, R.id.cell_65, R.id.cell_66, R.id.cell_67 };
	
	public CalendarView(Context context, AttributeSet attrs, int defStyle) 
	{
		super(context, attrs, defStyle);
	}

	public CalendarView(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
        inflater.inflate(R.layout.view_calendar, this);
	}
	
	public CalendarView(Context context) 
	{
		super(context);
	}

	public void setDate(Date date) 
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		
		int todayMonth = calendar.get(Calendar.DAY_OF_MONTH);
		
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		
		int weekDay = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		int dayCount = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		
		for (int i = weekDay; i < weekDay + dayCount; i++)
		{
			int id = CalendarView._dateCells[i];
			
			View v = this.findViewById(id);
			v.setBackgroundColor(0xffffffff);
			
			TextView dayLabel = (TextView) v.findViewById(R.id.label_day);
			dayLabel.setText("" + (i - weekDay + 1));
			
			if (i - weekDay + 1 == todayMonth)
			{
				dayLabel.setTypeface(null, Typeface.BOLD);
				dayLabel.setTextSize(14);
			}
		}
		
		LinearLayout sixWeek = (LinearLayout) this.findViewById(R.id.row_week_six);

		if (weekDay + dayCount < 35)
			sixWeek.setVisibility(View.GONE);
		else
			sixWeek.setVisibility(View.VISIBLE);
	}
}
