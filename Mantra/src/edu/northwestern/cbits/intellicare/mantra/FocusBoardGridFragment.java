package edu.northwestern.cbits.intellicare.mantra;

import edu.northwestern.cbits.intellicare.mantra.DatabaseHelper.FocusBoardCursor;
import edu.northwestern.cbits.intellicare.mantra.activities.FocusBoardActivity;
import edu.northwestern.cbits.intellicare.mantra.activities.NewFocusBoardActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

public class FocusBoardGridFragment extends Fragment {

	protected static final String CN = "FocusBoardGridFragment";
	private FocusBoardCursor mCursor;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.focus_boards_grid, container,
				false);
		mCursor = FocusBoardManager.get(getActivity()).queryFocusBoards();
		FocusBoardCursorAdapter adapter = new FocusBoardCursorAdapter(
				getActivity(), mCursor);
		GridView gv = (GridView) view.findViewById(R.id.gridview);
		gv.setAdapter(adapter);
		
		gv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				Intent intent = new Intent(getActivity(), FocusBoardActivity.class);
				intent.putExtra(NewFocusBoardActivity.FOCUS_BOARD_ID, id);
				
				Uri uri = getActivity().getIntent().getData();
				if(uri != null) {
					Log.d(CN+".onItemClick", "uri.toString() = " + uri.toString());
					intent.setData(uri);
				}
				
				startActivity(intent);
	        }
		});
		
		// if this activity was opened by a response to the image gallery,
		// then inform the user they need to tap on a mantra with which they wish to associate an image.
		if(getActivity().getIntent().getData() != null) {
			Toast.makeText(getActivity(), "Now tap on a mantra to attach your selected image to it!", Toast.LENGTH_LONG).show();
		}
		
		return view;
	}

	@Override
	public void onDestroy() {
		mCursor.close();
		super.onDestroy();
	}
}
