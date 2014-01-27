package edu.northwestern.cbits.intellicare.slumbertime;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;
import edu.northwestern.cbits.intellicare.ConsentedActivity;

public class BedtimeChecklistActivity extends ConsentedActivity
{
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_bedtime_checklist);
		
		this.getSupportActionBar().setTitle(R.string.tool_bedtime_checklist);
		this.getSupportActionBar().setIcon(R.drawable.ic_launcher_plain);
	}
	
	protected void onResume()
	{
		super.onResume();
		
		ListView checkList = (ListView) this.findViewById(R.id.list_checklist);
		
		String selection = SlumberContentProvider.CHECKLIST_ITEM_ENABLED + " = ?";
		String[] args = { "1" };
		
		Cursor c = this.getContentResolver().query(SlumberContentProvider.CHECKLIST_ITEMS_URI, null, selection, args, "category, name");

		this.startManagingCursor(c);
		int[] emptyInts = {};
		String[] emptyStrings = {};

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.row_checklist_item, c, emptyStrings, emptyInts, 0)
		{
			public void bindView (View view, Context context, Cursor cursor)
			{
				// label_category_name
				
				CheckBox item = (CheckBox) view.findViewById(R.id.check_item);
				
				item.setText(cursor.getString(cursor.getColumnIndex(SlumberContentProvider.CHECKLIST_ITEM_NAME)));
			}
		};
		
		checkList.setAdapter(adapter);
	}
}
