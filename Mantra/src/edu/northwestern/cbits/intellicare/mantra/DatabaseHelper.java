package edu.northwestern.cbits.intellicare.mantra;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.drawable.Drawable;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "mantra.sqlite";

	private static final String TABLE_MANTRA_BOARDS = "mantra_boards";
	private static final String COLUMN_MANTRA_BOARD_ID = "_id";
	private static final String COLUMN_MANTRA_BOARD_MANTRA = "mantra";

	private static final String TABLE_MANTRA_IMAGES = "mantra_images";
	private static final String COLUMN_MANTRA_IMAGE_ID = "_id";
	private static final String COLUMN_MANTRA_IMAGE_FOCUS_BOARD_ID = "focus_board_id";
	private static final String COLUMN_MANTRA_IMAGE_PATH = "path";
	private static final String COLUMN_MANTRA_IMAGE_CAPTION = "caption";

	DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TABLE_MANTRA_BOARDS + "("
				+ COLUMN_MANTRA_BOARD_ID
				+ " integer primary key autoincrement, "
				+ COLUMN_MANTRA_BOARD_MANTRA + " text)"
				);
		db.execSQL("CREATE TABLE " + TABLE_MANTRA_IMAGES + "("
				+ COLUMN_MANTRA_IMAGE_ID
				+ " integer primary key autoincrement, "
				+ COLUMN_MANTRA_IMAGE_FOCUS_BOARD_ID + " integer, "
				+ COLUMN_MANTRA_IMAGE_PATH + " text, "
				+ COLUMN_MANTRA_IMAGE_CAPTION  + " text)"
				);
	}

	/**
	 * Database upgrade per-version.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
	}


	/*** image CRUD ***/ 
	
	public FocusImageCursor queryFocusImages(long focusBoardId) {
		String selection = COLUMN_MANTRA_IMAGE_FOCUS_BOARD_ID + " = ?";
		String[] selectionArgs = new String[] { String.valueOf(focusBoardId) };
		Cursor wrapped = getReadableDatabase().query(TABLE_MANTRA_IMAGES, null,
				selection, selectionArgs, null, null, null);
		return new FocusImageCursor(wrapped);
	}

	public FocusImageCursor queryFocusImage(long id) {
		String selection = COLUMN_MANTRA_IMAGE_ID + " = ?";
		String[] selectionArgs = new String[] { String.valueOf(id) };
		String limit = "1";
		Cursor wrapped = getReadableDatabase().query(TABLE_MANTRA_IMAGES, null,
				selection, selectionArgs, null, null, null, limit);
		return new FocusImageCursor(wrapped);
	}

	public long insertFocusImage(MantraImage mantraImage) {
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_MANTRA_IMAGE_FOCUS_BOARD_ID, mantraImage.getFocusBoardId());
		cv.put(COLUMN_MANTRA_IMAGE_PATH, mantraImage.getPath());
		cv.put(COLUMN_MANTRA_IMAGE_CAPTION, mantraImage.getCaption());
		return getWritableDatabase().insert(TABLE_MANTRA_IMAGES, null, cv);
	}

	public long updateFocusImage(MantraImage mantraImage) {
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_MANTRA_IMAGE_FOCUS_BOARD_ID, mantraImage.getFocusBoardId());
		cv.put(COLUMN_MANTRA_IMAGE_PATH, mantraImage.getPath());
		cv.put(COLUMN_MANTRA_IMAGE_CAPTION, mantraImage.getCaption());
		return getWritableDatabase().update(
				TABLE_MANTRA_IMAGES, cv, "_id=?", 
				new String[] { ((Long) mantraImage.getId()).toString() }
			);
	}
	
	public int deleteFocusImage(Long id) {
		return getWritableDatabase().delete(
				TABLE_MANTRA_IMAGES, "_id=?",
				new String[] { id.toString() }
			);
	}

		
	/*** mantra board CRUD ***/ 
	
	public FocusBoardCursor queryFocusBoards() {
		Cursor wrapped = getReadableDatabase().query(TABLE_MANTRA_BOARDS, null,
				null, null, null, null, COLUMN_MANTRA_BOARD_MANTRA);
		return new FocusBoardCursor(wrapped);
	}

	public FocusBoardCursor queryFocusBoard(long id) {
		String selection = COLUMN_MANTRA_BOARD_ID + " = ?";
		String[] selectionArgs = new String[] { String.valueOf(id) };
		String limit = "1";
		Cursor wrapped = getReadableDatabase().query(TABLE_MANTRA_BOARDS, null,
				selection, selectionArgs, null, null, null, limit);
		return new FocusBoardCursor(wrapped);
	}

	public long insertFocusBoard(MantraBoard mantraBoard) {
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_MANTRA_BOARD_MANTRA, mantraBoard.getMantra());
		return getWritableDatabase().insert(TABLE_MANTRA_BOARDS, null, cv);
	}
	
	public long updateFocusBoard(MantraBoard mantraBoard) {
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_MANTRA_BOARD_MANTRA, mantraBoard.getMantra());
		return getWritableDatabase()
			.update(
				TABLE_MANTRA_BOARDS, cv, "_id=?", 
				new String[] { ((Long) mantraBoard.getId()).toString() }
			);
	}

	public int deleteFocusBoard(Long id) {
		return getWritableDatabase().delete(
				TABLE_MANTRA_BOARDS, "_id=?", 
				new String[] { id.toString() }
		);
	}

	
	/*** cursors over domain objects ***/
	
	public static class FocusBoardCursor extends CursorWrapper {
		public FocusBoardCursor(Cursor c) {
			super(c);
		}

		public MantraBoard getFocusBoard() {
			if (isBeforeFirst() || isAfterLast()) {
				return null;
			}
			MantraBoard mantraBoard = new MantraBoard();
			long focusBoardId = getLong(getColumnIndex(COLUMN_MANTRA_BOARD_ID));
			mantraBoard.setId(focusBoardId);
			String mantra = getString(getColumnIndex(COLUMN_MANTRA_BOARD_MANTRA));
			mantraBoard.setMantra(mantra);
			return mantraBoard;
		}
	}

	public static class FocusImageCursor extends CursorWrapper {
		public FocusImageCursor(Cursor c) {
			super(c);
		}

		public MantraImage getFocusImage() {
			if (isBeforeFirst() || isAfterLast()) {
				return null;
			}
			MantraImage mantraImage = new MantraImage();
			long focusImageId = getLong(getColumnIndex(COLUMN_MANTRA_IMAGE_ID));
			mantraImage.setId(focusImageId);
			long focusBoardId = getLong(getColumnIndex(COLUMN_MANTRA_IMAGE_FOCUS_BOARD_ID));
			mantraImage.setFocusBoardId(focusBoardId);
			String imagePath = getString(getColumnIndex(COLUMN_MANTRA_IMAGE_PATH));
			mantraImage.setPath(imagePath);
			String imageCaption = getString(getColumnIndex(COLUMN_MANTRA_IMAGE_CAPTION));
			mantraImage.setCaption(imageCaption);
			
			return mantraImage;
		}
	}

}
