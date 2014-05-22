package edu.northwestern.cbits.intellicare.mantra;

import edu.northwestern.cbits.intellicare.mantra.DatabaseHelper.MantraBoardCursor;
import edu.northwestern.cbits.intellicare.mantra.DatabaseHelper.MantraImageCursor;
import android.content.Context;

public class MantraBoardManager {

	public static final int COL_INDEX_FILE_PATH = 2;
	private static MantraBoardManager sMantraBoardManager;

	private Context mAppContext;
	private DatabaseHelper mDatabaseHelper;

	private MantraBoardManager(Context appContext) {
		mAppContext = appContext;
		mDatabaseHelper = new DatabaseHelper(mAppContext);
	}

	public static MantraBoardManager get(Context c) {
		if (sMantraBoardManager == null) {
			// use the app context to avoid leaking activities
			sMantraBoardManager = new MantraBoardManager(
					c.getApplicationContext());
		}
		return sMantraBoardManager;
	}

	public MantraBoard createMantraBoard(String mantra) {
		MantraBoard mantraBoard = new MantraBoard();
		mantraBoard.setMantra(mantra);
		mantraBoard.setId(mDatabaseHelper.insertMantraBoard(mantraBoard));
		return mantraBoard;
	}
	
	public MantraImage createMantraImage(long mantraBoardId, String imagePath, String imageCaption) {
		MantraImage mantraImage = new MantraImage();
		mantraImage.setFocusBoardId(mantraBoardId);
		mantraImage.setPath(imagePath);
		mantraImage.setCaption(imageCaption);
		mantraImage.setId(mDatabaseHelper.insertMantraImage(mantraImage));
		return mantraImage;
	}
	
	public MantraBoardCursor queryMantraBoards() {
		return mDatabaseHelper.queryMantraBoards();
	}
	
	public MantraBoard getMantraBoard(long id) {
		MantraBoard mantraBoard = null;
		MantraBoardCursor cursor = mDatabaseHelper.queryMantraBoard(id);
		cursor.moveToFirst();
		if (!cursor.isAfterLast()) {
			mantraBoard = cursor.getMantraBoard();
		}
		cursor.close();
		return mantraBoard;
	}
	
	public long setMantraBoard(MantraBoard mantraBoard) {
		MantraBoardCursor cursor = mDatabaseHelper.queryMantraBoard(mantraBoard.getId());
		cursor.moveToFirst();
		long ret = -1;
		if(!cursor.isAfterLast()) {
			ret = mDatabaseHelper.updateMantraBoard(mantraBoard);
		}
		cursor.close();
		return ret;
	}
	
	public int deleteMantraBoard(long id) {
		MantraBoardCursor cursor = mDatabaseHelper.queryMantraBoard(id);
		cursor.moveToFirst();
		int ret = -1;
		if(!cursor.isAfterLast()) {
			ret = mDatabaseHelper.deleteMantraBoard(id);
		}
		cursor.close();
		return ret;
	}
	
	
	public MantraImageCursor queryMantraImages(long focusBoardId) {
		return mDatabaseHelper.queryMantraImages(focusBoardId);
	}
	
	public MantraImage getMantraImage(long id) {
		MantraImage mantraImage = null;
		MantraImageCursor cursor = mDatabaseHelper.queryMantraImage(id);
		cursor.moveToFirst();
		if (!cursor.isAfterLast()) {
			mantraImage = cursor.getMantraImage();
		}
		cursor.close();
		return mantraImage;
	}
	
	public long setMantraImage(MantraImage mantraImage) {
		MantraImageCursor cursor = mDatabaseHelper.queryMantraImage(mantraImage.getId());
		cursor.moveToFirst();
		long ret = -1;
		if(!cursor.isAfterLast()) {
			ret = mDatabaseHelper.updateMantraImage(mantraImage);
		}
		cursor.close();
		return ret;
	}
	
	public int deleteMantraImage(long id) {
		MantraImageCursor cursor = mDatabaseHelper.queryMantraImage(id);
		cursor.moveToFirst();
		int ret = -1;
		if(!cursor.isAfterLast()) {
			ret = mDatabaseHelper.deleteMantraImage(id);
		}
		cursor.close();
		return ret;
	}
}
