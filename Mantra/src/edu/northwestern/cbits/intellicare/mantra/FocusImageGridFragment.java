package edu.northwestern.cbits.intellicare.mantra;

import edu.northwestern.cbits.intellicare.mantra.DatabaseHelper.FocusImageCursor;
import edu.northwestern.cbits.intellicare.mantra.activities.NewFocusBoardActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

public class FocusImageGridFragment extends Fragment {

	private FocusImageCursor mCursor;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.focus_images_grid, container,
				false);
		Intent intent = getActivity().getIntent();
		long focusBoardId = intent.getLongExtra(
				NewFocusBoardActivity.FOCUS_BOARD_ID, -1);
		mCursor = FocusBoardManager.get(getActivity()).queryFocusImages(focusBoardId);
		FocusImageCursorAdapter adapter = new FocusImageCursorAdapter(
				getActivity(), mCursor);
		GridView gv = (GridView) view.findViewById(R.id.gridview);
		gv.setAdapter(adapter);
		
		return view;
	}

	@Override
	public void onDestroy() {
		mCursor.close();
		super.onDestroy();
	}
}
