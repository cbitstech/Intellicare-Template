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

	private static final String TABLE_FOCUS_BOARDS = "focus_boards";
	private static final String COLUMN_FOCUS_BOARD_ID = "_id";
	private static final String COLUMN_FOCUS_BOARD_MANTRA = "mantra";

	private static final String TABLE_FOCUS_IMAGES = "focus_images";
	private static final String COLUMN_FOCUS_IMAGE_ID = "_id";
	private static final String COLUMN_FOCUS_IMAGE_FOCUS_BOARD_ID = "focus_board_id";
	private static final String COLUMN_FOCUS_IMAGE_PATH = "path";
	private static final String COLUMN_FOCUS_IMAGE_CAPTION = "caption";

	DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TABLE_FOCUS_BOARDS + "("
				+ COLUMN_FOCUS_BOARD_ID
				+ " integer primary key autoincrement, "
				+ COLUMN_FOCUS_BOARD_MANTRA + " text)"
				);
		db.execSQL("CREATE TABLE " + TABLE_FOCUS_IMAGES + "("
				+ COLUMN_FOCUS_IMAGE_ID
				+ " integer primary key autoincrement, "
				+ COLUMN_FOCUS_IMAGE_FOCUS_BOARD_ID + " integer, "
				+ COLUMN_FOCUS_IMAGE_PATH + " text, "
				+ COLUMN_FOCUS_IMAGE_CAPTION  + " text)"
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
		String selection = COLUMN_FOCUS_IMAGE_FOCUS_BOARD_ID + " = ?";
		String[] selectionArgs = new String[] { String.valueOf(focusBoardId) };
		Cursor wrapped = getReadableDatabase().query(TABLE_FOCUS_IMAGES, null,
				selection, selectionArgs, null, null, null);
		return new FocusImageCursor(wrapped);
	}

	public FocusImageCursor queryFocusImage(long id) {
		String selection = COLUMN_FOCUS_IMAGE_ID + " = ?";
		String[] selectionArgs = new String[] { String.valueOf(id) };
		String limit = "1";
		Cursor wrapped = getReadableDatabase().query(TABLE_FOCUS_IMAGES, null,
				selection, selectionArgs, null, null, null, limit);
		return new FocusImageCursor(wrapped);
	}

	public long insertFocusImage(FocusImage focusImage) {
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_FOCUS_IMAGE_FOCUS_BOARD_ID, focusImage.getFocusBoardId());
		cv.put(COLUMN_FOCUS_IMAGE_PATH, focusImage.getPath());
		cv.put(COLUMN_FOCUS_IMAGE_CAPTION, focusImage.getCaption());
		return getWritableDatabase().insert(TABLE_FOCUS_IMAGES, null, cv);
	}

	public long updateFocusImage(FocusImage focusImage) {
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_FOCUS_IMAGE_FOCUS_BOARD_ID, focusImage.getFocusBoardId());
		cv.put(COLUMN_FOCUS_IMAGE_PATH, focusImage.getPath());
		cv.put(COLUMN_FOCUS_IMAGE_CAPTION, focusImage.getCaption());
		return getWritableDatabase().update(
				TABLE_FOCUS_IMAGES, cv, "_id=?", 
				new String[] { ((Long) focusImage.getId()).toString() }
			);
	}
	
	public int deleteFocusImage(Long id) {
		return getWritableDatabase().delete(
				TABLE_FOCUS_IMAGES, "_id=?",
				new String[] { id.toString() }
			);
	}

		
	/*** mantra board CRUD ***/ 
	
	public FocusBoardCursor queryFocusBoards() {
		Cursor wrapped = getReadableDatabase().query(TABLE_FOCUS_BOARDS, null,
				null, null, null, null, COLUMN_FOCUS_BOARD_MANTRA);
		return new FocusBoardCursor(wrapped);
	}

	public FocusBoardCursor queryFocusBoard(long id) {
		String selection = COLUMN_FOCUS_BOARD_ID + " = ?";
		String[] selectionArgs = new String[] { String.valueOf(id) };
		String limit = "1";
		Cursor wrapped = getReadableDatabase().query(TABLE_FOCUS_BOARDS, null,
				selection, selectionArgs, null, null, null, limit);
		return new FocusBoardCursor(wrapped);
	}

	public long insertFocusBoard(FocusBoard focusBoard) {
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_FOCUS_BOARD_MANTRA, focusBoard.getMantra());
		return getWritableDatabase().insert(TABLE_FOCUS_BOARDS, null, cv);
	}
	
	public long updateFocusBoard(FocusBoard focusBoard) {
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_FOCUS_BOARD_MANTRA, focusBoard.getMantra());
		return getWritableDatabase()
			.update(
				TABLE_FOCUS_BOARDS, cv, "_id=?", 
				new String[] { ((Long) focusBoard.getId()).toString() }
			);
	}

	public int deleteFocusBoard(Long id) {
		return getWritableDatabase().delete(
				TABLE_FOCUS_BOARDS, "_id=?", 
				new String[] { id.toString() }
		);
	}

	
	/*** cursors over domain objects ***/
	
	public static class FocusBoardCursor extends CursorWrapper {
		public FocusBoardCursor(Cursor c) {
			super(c);
		}

		public FocusBoard getFocusBoard() {
			if (isBeforeFirst() || isAfterLast()) {
				return null;
			}
			FocusBoard focusBoard = new FocusBoard();
			long focusBoardId = getLong(getColumnIndex(COLUMN_FOCUS_BOARD_ID));
			focusBoard.setId(focusBoardId);
			String mantra = getString(getColumnIndex(COLUMN_FOCUS_BOARD_MANTRA));
			focusBoard.setMantra(mantra);
			return focusBoard;
		}
	}

	public static class FocusImageCursor extends CursorWrapper {
		public FocusImageCursor(Cursor c) {
			super(c);
		}

		public FocusImage getFocusImage() {
			if (isBeforeFirst() || isAfterLast()) {
				return null;
			}
			FocusImage focusImage = new FocusImage();
			long focusImageId = getLong(getColumnIndex(COLUMN_FOCUS_IMAGE_ID));
			focusImage.setId(focusImageId);
			long focusBoardId = getLong(getColumnIndex(COLUMN_FOCUS_IMAGE_FOCUS_BOARD_ID));
			focusImage.setFocusBoardId(focusBoardId);
			String imagePath = getString(getColumnIndex(COLUMN_FOCUS_IMAGE_PATH));
			focusImage.setPath(imagePath);
			String imageCaption = getString(getColumnIndex(COLUMN_FOCUS_IMAGE_CAPTION));
			focusImage.setCaption(imageCaption);
			
			return focusImage;
		}
	}

}
