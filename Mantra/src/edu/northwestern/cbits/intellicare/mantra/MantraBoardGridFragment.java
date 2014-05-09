package edu.northwestern.cbits.intellicare.mantra;

import edu.northwestern.cbits.intellicare.mantra.DatabaseHelper.MantraBoardCursor;
import edu.northwestern.cbits.intellicare.mantra.activities.NewMantraBoardActivity;
import edu.northwestern.cbits.intellicare.mantra.activities.SingleMantraBoardActivity;
import android.app.Activity;
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

public class MantraBoardGridFragment extends Fragment {

	protected static final String CN = "MantraBoardGridFragment";
	private MantraBoardCursor mCursor;
	private Activity activity = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(CN+".onCreateView", "entered");
		View view = inflater.inflate(R.layout.focus_boards_grid, container,false);
		mCursor = MantraBoardManager.get(getActivity()).queryFocusBoards();
		Util.logCursor(mCursor);
		MantraBoardCursorAdapter adapter = new MantraBoardCursorAdapter(getActivity(), mCursor);
		GridView gv = (GridView) view.findViewById(R.id.gridview);
		gv.setAdapter(adapter);
		
		gv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				Intent intent = new Intent(getActivity(), SingleMantraBoardActivity.class);
				intent.putExtra(NewMantraBoardActivity.MANTRA_BOARD_ID, id);
				
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
		activity = getActivity();
		if(activity.getIntent().getData() != null) {
			Toast.makeText(getActivity(), activity.getString(R.string.now_tap_on_a_mantra_to_attach_your_selected_image_to_it_), Toast.LENGTH_LONG).show();
		}
		
		return view;
	}

	@Override
	public void onDestroy() {
		mCursor.close();
		super.onDestroy();
	}
}
