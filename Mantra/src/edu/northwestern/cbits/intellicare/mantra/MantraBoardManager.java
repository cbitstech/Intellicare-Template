package edu.northwestern.cbits.intellicare.mantra;

import edu.northwestern.cbits.intellicare.mantra.DatabaseHelper.FocusBoardCursor;
import edu.northwestern.cbits.intellicare.mantra.DatabaseHelper.FocusImageCursor;
import android.content.Context;

public class MantraBoardManager {

	public static final int COL_INDEX_FILE_PATH = 2;
	private static MantraBoardManager sFocusBoardManager;

	private Context mAppContext;
	private DatabaseHelper mDatabaseHelper;

	private MantraBoardManager(Context appContext) {
		mAppContext = appContext;
		mDatabaseHelper = new DatabaseHelper(mAppContext);
	}

	public static MantraBoardManager get(Context c) {
		if (sFocusBoardManager == null) {
			// use the app context to avoid leaking activities
			sFocusBoardManager = new MantraBoardManager(
					c.getApplicationContext());
		}
		return sFocusBoardManager;
	}

	public MantraBoard createFocusBoard(String mantra) {
		MantraBoard mantraBoard = new MantraBoard();
		mantraBoard.setMantra(mantra);
		mantraBoard.setId(mDatabaseHelper.insertFocusBoard(mantraBoard));
		return mantraBoard;
	}
	
	public MantraImage createFocusImage(long focusBoardId, String imagePath, String imageCaption) {
		MantraImage mantraImage = new MantraImage();
		mantraImage.setFocusBoardId(focusBoardId);
		mantraImage.setPath(imagePath);
		mantraImage.setCaption(imageCaption);
		mantraImage.setId(mDatabaseHelper.insertFocusImage(mantraImage));
		return mantraImage;
	}
	
	public FocusBoardCursor queryFocusBoards() {
		return mDatabaseHelper.queryFocusBoards();
	}
	
	public MantraBoard getFocusBoard(long id) {
		MantraBoard mantraBoard = null;
		FocusBoardCursor cursor = mDatabaseHelper.queryFocusBoard(id);
		cursor.moveToFirst();
		if (!cursor.isAfterLast()) {
			mantraBoard = cursor.getFocusBoard();
		}
		cursor.close();
		return mantraBoard;
	}
	
	public long setFocusBoard(MantraBoard mantraBoard) {
		FocusBoardCursor cursor = mDatabaseHelper.queryFocusBoard(mantraBoard.getId());
		cursor.moveToFirst();
		long ret = -1;
		if(!cursor.isAfterLast()) {
			ret = mDatabaseHelper.updateFocusBoard(mantraBoard);
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
	
	public MantraImage getFocusImage(long id) {
		MantraImage mantraImage = null;
		FocusImageCursor cursor = mDatabaseHelper.queryFocusImage(id);
		cursor.moveToFirst();
		if (!cursor.isAfterLast()) {
			mantraImage = cursor.getFocusImage();
		}
		cursor.close();
		return mantraImage;
	}
	
	public long setFocusImage(MantraImage mantraImage) {
		FocusImageCursor cursor = mDatabaseHelper.queryFocusImage(mantraImage.getId());
		cursor.moveToFirst();
		long ret = -1;
		if(!cursor.isAfterLast()) {
			ret = mDatabaseHelper.updateFocusImage(mantraImage);
		}
		cursor.close();
		return ret;
	}
	
	public int deleteFocusImage(long id) {
		FocusImageCursor cursor = mDatabaseHelper.queryFocusImage(id);
		cursor.moveToFirst();
		int ret = -1;
		if(!cursor.isAfterLast()) {
			ret = mDatabaseHelper.deleteFocusImage(id);
		}
		cursor.close();
		return ret;
	}
}
