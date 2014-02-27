package edu.northwestern.cbits.intellicare.mantra;

import java.io.File;

import edu.northwestern.cbits.intellicare.mantra.DatabaseHelper.FocusImageCursor;
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
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;

public class FocusImageGridFragment extends Fragment {

	private static final String CN = "FocusImageGridFragment";
	private FocusImageCursor mCursor;
	
	LayoutInflater inflater; ViewGroup container;

	@Override
	public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle savedInstanceState) {
		Log.d(CN+".onCreateView", "entered");
		inflater = li; container = vg;
		return renderSetup(inflater, container);
	}

	/**
	 * @param inflater
	 * @param container
	 * @return
	 */
	private View renderSetup(LayoutInflater inflater, ViewGroup container) {
		View view = inflater.inflate(R.layout.focus_images_grid, container,false);
		Intent intent = getActivity().getIntent();
		long focusBoardId = intent.getLongExtra(NewFocusBoardActivity.FOCUS_BOARD_ID, -1);
		mCursor = FocusBoardManager.get(getActivity()).queryFocusImages(focusBoardId);
		Util.logCursor(mCursor);
		FocusImageCursorAdapter adapter = new FocusImageCursorAdapter(getActivity(), mCursor);
		GridView gv = (GridView) view.findViewById(R.id.gridview);
		gv.setAdapter(adapter);
		
		// when the user taps an image, display it
		gv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int positionInView, long rowId) {
				Intent intent = new Intent();
				intent.setAction(android.content.Intent.ACTION_VIEW);
				mCursor.moveToPosition(positionInView);
				String filePath = mCursor.getString(FocusBoardManager.COL_INDEX_FILE_PATH);
				Log.d(CN+".onItemClick", "filePath = " + filePath);
				intent.setDataAndType(Uri.fromFile(new File(filePath)), "image/*");
				startActivity(intent);
			}
		});
		
		return view;
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		renderSetup(inflater, container);
	}

	@Override
	public void onDestroy() {
		mCursor.close();
		super.onDestroy();
	}
}
