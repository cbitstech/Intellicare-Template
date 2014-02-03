package edu.northwestern.cbits.intellicare.dailyfeats;

import java.util.Date;

import android.os.Bundle;
import edu.northwestern.cbits.intellicare.ConsentedActivity;
import edu.northwestern.cbits.intellicare.dailyfeats.views.CalendarView;

public class CalendarActivity extends ConsentedActivity 
{
	public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        this.setContentView(R.layout.activity_calendar);
        
        CalendarView calendar = (CalendarView) this.findViewById(R.id.view_calendar);
        calendar.setDate(new Date());
    }
}
