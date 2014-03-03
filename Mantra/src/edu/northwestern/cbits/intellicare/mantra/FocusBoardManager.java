package edu.northwestern.cbits.intellicare.mantra;

import edu.northwestern.cbits.intellicare.mantra.DatabaseHelper.FocusBoardCursor;
import edu.northwestern.cbits.intellicare.mantra.DatabaseHelper.FocusImageCursor;
import android.content.Context;

public class FocusBoardManager {

	public static final int COL_INDEX_FILE_PATH = 2;
	private static FocusBoardManager sFocusBoardManager;

	private Context mAppContext;
	private DatabaseHelper mDatabaseHelper;

	private FocusBoardManager(Context appContext) {
		mAppContext = appContext;
		mDatabaseHelper = new DatabaseHelper(mAppContext);
	}

	public static FocusBoardManager get(Context c) {
		if (sFocusBoardManager == null) {
			// use the app context to avoid leaking activities
			sFocusBoardManager = new FocusBoardManager(
					c.getApplicationContext());
		}
		return sFocusBoardManager;
	}

	public FocusBoard createFocusBoard(String mantra) {
		FocusBoard focusBoard = new FocusBoard();
		focusBoard.setMantra(mantra);
		focusBoard.setId(mDatabaseHelper.insertFocusBoard(focusBoard));
		return focusBoard;
	}
	
	public FocusImage createFocusImage(long focusBoardId, String imagePath) {
		FocusImage focusImage = new FocusImage();
		focusImage.setFocusBoardId(focusBoardId);
		focusImage.setPath(imagePath);
		focusImage.setId(mDatabaseHelper.insertFocusImage(focusImage));
		return focusImage;
	}
	
	public FocusBoardCursor queryFocusBoards() {
		return mDatabaseHelper.queryFocusBoards();
	}
	
	public FocusBoard getFocusBoard(long id) {
		FocusBoard focusBoard = null;
		FocusBoardCursor cursor = mDatabaseHelper.queryFocusBoard(id);
		cursor.moveToFirst();
		if (!cursor.isAfterLast()) {
			focusBoard = cursor.getFocusBoard();
		}
		cursor.close();
		return focusBoard;
	}
	
	public long setFocusBoard(FocusBoard focusBoard) {
		FocusBoardCursor cursor = mDatabaseHelper.queryFocusBoard(focusBoard.getId());
		cursor.moveToFirst();
		long ret = -1;
		if(!cursor.isAfterLast()) {
			ret = mDatabaseHelper.updateFocusBoard(focusBoard);
		}
		cursor.close();
		return ret;
	}
	
	public int deleteFocusBoard(long id) {
		FocusBoardCursor cursor = mDatabaseHelper.queryFocusBoard(id);
		cursor.moveToFirst();
		int ret = -1;
		if(!cursor.isAfterLast()) {
			ret = mDatabaseHelper.deleteFocusBoard(id);
		}
		cursor.close();
		return ret;
	}
	
	
	public FocusImageCursor queryFocusImages(long focusBoardId) {
		return mDatabaseHelper.queryFocusImages(focusBoardId);
	}
	
	public FocusImage getFocusImage(long id) {
		FocusImage focusImage = null;
		FocusImageCursor cursor = mDatabaseHelper.queryFocusImage(id);
		cursor.moveToFirst();
		if (!cursor.isAfterLast()) {
			focusImage = cursor.getFocusImage();
		}
		cursor.close();
		return focusImage;
	}
}
