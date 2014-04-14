package edu.northwestern.cbits.intellicare.dailyfeats.views;

import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import edu.northwestern.cbits.intellicare.dailyfeats.FeatsProvider;
import edu.northwestern.cbits.intellicare.dailyfeats.R;

public class CalendarView extends LinearLayout 
{
	public static abstract class DateChangeListener
	{
		public abstract void onDateChanged(Date date);
	}
	
	private static int[] _dateCells = { R.id.cell_11, R.id.cell_12, R.id.cell_13, R.id.cell_14, R.id.cell_15, R.id.cell_16, R.id.cell_17, 
										R.id.cell_21, R.id.cell_22, R.id.cell_23, R.id.cell_24, R.id.cell_25, R.id.cell_26, R.id.cell_27, 
										R.id.cell_31, R.id.cell_32, R.id.cell_33, R.id.cell_34, R.id.cell_35, R.id.cell_36, R.id.cell_37, 	
										R.id.cell_41, R.id.cell_42, R.id.cell_43, R.id.cell_44, R.id.cell_45, R.id.cell_46, R.id.cell_47, 
										R.id.cell_51, R.id.cell_52, R.id.cell_53, R.id.cell_54, R.id.cell_55, R.id.cell_56, R.id.cell_57,
										R.id.cell_61, R.id.cell_62, R.id.cell_63, R.id.cell_64, R.id.cell_65, R.id.cell_66, R.id.cell_67 };

	private DateChangeListener _dateChangeListener = null;

	private Date _date = null;

	private float _startX = -1;
	
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

	public void setDate(final Date date, boolean doCallbacks) 
	{
		this._date = date;
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		
		int todayMonth = calendar.get(Calendar.DAY_OF_MONTH);
		
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		
		int weekDay = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		int dayCount = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		
		final CalendarView me = this;

		for (int id : CalendarView._dateCells)
		{
			DayView dayView = (DayView) this.findViewById(id);
			dayView.reset();
		}
		
		for (int i = weekDay; i < weekDay + dayCount; i++)
		{
			int id = CalendarView._dateCells[i];

			final DayView dayView = (DayView) this.findViewById(id);
			
			final int dateDay = (i - weekDay + 1);
			
			calendar.set(Calendar.DAY_OF_MONTH, dateDay);
			
			int featsCount = FeatsProvider.featCountForDate(this.getContext(), calendar.getTime());

			if (featsCount > 0)
			{
				boolean metGoal = FeatsProvider.metGoalForDate(this.getContext(), calendar.getTime());

				int color = 0xffcc0000;
				
				if (metGoal)
					color = 0xff669900;
				
				dayView.setDay("" + featsCount, dateDay, color);
			}
			else
				dayView.setDay("", dateDay);
				
			if (i - weekDay + 1 == todayMonth)
			{
				Calendar nowCalendar = Calendar.getInstance();
				
				if (nowCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) && 
					nowCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR))
				{
					dayView.setIsToday(true);
				}
				
				if (doCallbacks)
					dayView.select();
			}
			
			dayView.setOnClickListener(new OnClickListener()
			{
				public void onClick(View view) 
				{
					if (me._dateChangeListener != null)
					{
						Calendar now = Calendar.getInstance();
						now.setTime(date);
						now.set(Calendar.DAY_OF_MONTH, dateDay);
						
						me._dateChangeListener.onDateChanged(now.getTime());
					}
					
					for (int id : CalendarView._dateCells)
					{
						DayView otherDayView = (DayView) me.findViewById(id);
						otherDayView.clearSelection();
					}

					dayView.select();
				}
			});
		}
		
		LinearLayout fiveWeek = (LinearLayout) this.findViewById(R.id.row_week_five);
		LinearLayout sixWeek = (LinearLayout) this.findViewById(R.id.row_week_six);

		if (weekDay + dayCount < 29)
			fiveWeek.setVisibility(View.GONE);
		else
			fiveWeek.setVisibility(View.VISIBLE);
		
		if (weekDay + dayCount < 36)
			sixWeek.setVisibility(View.GONE);
		else
			sixWeek.setVisibility(View.VISIBLE);
		
		if (this._dateChangeListener != null && doCallbacks)
			this._dateChangeListener.onDateChanged(date);
	}

	protected void clearSelection() 
	{
		for (int id : CalendarView._dateCells)
		{
			DayView dayView = (DayView) this.findViewById(id);
			dayView.reset();
		}
	}

	public void setOnDateChangeListener(DateChangeListener dateChangeListener) 
	{
		this._dateChangeListener  = dateChangeListener;
	}
	
	public boolean onInterceptTouchEvent(MotionEvent event)
	{
		int action = event.getAction();
		
		if (action == MotionEvent.ACTION_DOWN)
		{
			this._startX  = event.getX();
		}
		else if (action == MotionEvent.ACTION_MOVE)
		{
			float delta = event.getX() - this._startX;

			if (Math.abs(delta) > 512)
			{
				this._startX = event.getX();
				
				Calendar next = Calendar.getInstance();
				next.setTime(this._date);
				
				int month = next.get(Calendar.MONTH);
				int year = next.get(Calendar.YEAR);
				
				if (delta > 0)
					month -= 1;
				else
					month += 1;
				
				if (month < 0)
				{
					month = 11;
					year -= 1;
				}
				else if (month == 12)
				{
					month = 0;
					year += 1;
				}
				
				next.set(Calendar.MONTH, month);
				next.set(Calendar.YEAR, year);
				
				this.setDate(next.getTime());
			}
		}
		else if (action == MotionEvent.ACTION_UP)
			this._startX = -1;

	    return false;
	}

	public void setDate(Date date) 
	{
		this.setDate(date, true);
	}
}
