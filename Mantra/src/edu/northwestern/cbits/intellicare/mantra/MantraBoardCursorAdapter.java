package edu.northwestern.cbits.intellicare.mantra;

import edu.northwestern.cbits.intellicare.mantra.DatabaseHelper.MantraBoardCursor;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MantraBoardCursorAdapter extends CursorAdapter {
	
	private MantraBoardCursor mFocusBoardCursor;

	public MantraBoardCursorAdapter(Context context, MantraBoardCursor cursor) {
		super(context, cursor, 0);
		mFocusBoardCursor = cursor;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		return inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		MantraBoard mantraBoard = mFocusBoardCursor.getFocusBoard();
		TextView mantraTextView = (TextView)view;
		mantraTextView.setText(mantraBoard.getMantra());
	}
}
