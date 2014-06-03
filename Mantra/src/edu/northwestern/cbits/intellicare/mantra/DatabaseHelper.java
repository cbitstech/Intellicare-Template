package edu.northwestern.cbits.intellicare.mantra;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.drawable.Drawable;

/**
 * Defines the straight-SQL database interactions for Mantra. (A ContentProvider pattern was not used.)
 * @author mohrlab
 *
 */
public class DatabaseHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "mantra.sqlite";

	private static final String TABLE_MANTRA_BOARDS = "mantra_boards";
	private static final String COLUMN_MANTRA_BOARD_ID = "_id";
	private static final String COLUMN_MANTRA_BOARD_MANTRA = "mantra";

	private static final String TABLE_MANTRA_IMAGES = "mantra_images";
	private static final String COLUMN_MANTRA_IMAGE_ID = "_id";
	private static final String COLUMN_MANTRA_IMAGE_MANTRA_BOARD_ID = "focus_board_id";
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
				+ COLUMN_MANTRA_IMAGE_MANTRA_BOARD_ID + " integer, "
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
	
	public MantraImageCursor queryMantraImages(long focusBoardId) {
		String selection = COLUMN_MANTRA_IMAGE_MANTRA_BOARD_ID + " = ?";
		String[] selectionArgs = new String[] { String.valueOf(focusBoardId) };
		Cursor wrapped = getReadableDatabase().query(TABLE_MANTRA_IMAGES, null,
				selection, selectionArgs, null, null, null);
		return new MantraImageCursor(wrapped);
	}

	public MantraImageCursor queryMantraImage(long id) {
		String selection = COLUMN_MANTRA_IMAGE_ID + " = ?";
		String[] selectionArgs = new String[] { String.valueOf(id) };
		String limit = "1";
		Cursor wrapped = getReadableDatabase().query(TABLE_MANTRA_IMAGES, null,
				selection, selectionArgs, null, null, null, limit);
		return new MantraImageCursor(wrapped);
	}

	public long insertMantraImage(MantraImage mantraImage) {
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_MANTRA_IMAGE_MANTRA_BOARD_ID, mantraImage.getFocusBoardId());
		cv.put(COLUMN_MANTRA_IMAGE_PATH, mantraImage.getPath());
		cv.put(COLUMN_MANTRA_IMAGE_CAPTION, mantraImage.getCaption());
		return getWritableDatabase().insert(TABLE_MANTRA_IMAGES, null, cv);
	}

	public long updateMantraImage(MantraImage mantraImage) {
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_MANTRA_IMAGE_MANTRA_BOARD_ID, mantraImage.getFocusBoardId());
		cv.put(COLUMN_MANTRA_IMAGE_PATH, mantraImage.getPath());
		cv.put(COLUMN_MANTRA_IMAGE_CAPTION, mantraImage.getCaption());
		return getWritableDatabase().update(
				TABLE_MANTRA_IMAGES, cv, "_id=?", 
				new String[] { ((Long) mantraImage.getId()).toString() }
			);
	}
	
	public int deleteMantraImage(Long id) {
		return getWritableDatabase().delete(
				TABLE_MANTRA_IMAGES, "_id=?",
				new String[] { id.toString() }
			);
	}

		
	/*** mantra board CRUD ***/ 
	
	public MantraBoardCursor queryMantraBoards() {
		Cursor wrapped = getReadableDatabase().query(TABLE_MANTRA_BOARDS, null,
				null, null, null, null, COLUMN_MANTRA_BOARD_MANTRA);
		return new MantraBoardCursor(wrapped);
	}

	public MantraBoardCursor queryMantraBoard(long id) {
		String selection = COLUMN_MANTRA_BOARD_ID + " = ?";
		String[] selectionArgs = new String[] { String.valueOf(id) };
		String limit = "1";
		Cursor wrapped = getReadableDatabase().query(TABLE_MANTRA_BOARDS, null,
				selection, selectionArgs, null, null, null, limit);
		return new MantraBoardCursor(wrapped);
	}

	public long insertMantraBoard(MantraBoard mantraBoard) {
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_MANTRA_BOARD_MANTRA, mantraBoard.getMantra());
		return getWritableDatabase().insert(TABLE_MANTRA_BOARDS, null, cv);
	}
	
	public long updateMantraBoard(MantraBoard mantraBoard) {
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_MANTRA_BOARD_MANTRA, mantraBoard.getMantra());
		return getWritableDatabase()
			.update(
				TABLE_MANTRA_BOARDS, cv, "_id=?", 
				new String[] { ((Long) mantraBoard.getId()).toString() }
			);
	}

	public int deleteMantraBoard(Long id) {
		return getWritableDatabase().delete(
				TABLE_MANTRA_BOARDS, "_id=?", 
				new String[] { id.toString() }
		);
	}

	
	/*** cursors over domain objects ***/
	
	public static class MantraBoardCursor extends CursorWrapper {
		public MantraBoardCursor(Cursor c) {
			super(c);
		}

		public MantraBoard getMantraBoard() {
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

	public static class MantraImageCursor extends CursorWrapper {
		public MantraImageCursor(Cursor c) {
			super(c);
		}

		public MantraImage getMantraImage() {
			if (isBeforeFirst() || isAfterLast()) {
				return null;
			}
			MantraImage mantraImage = new MantraImage();
			long focusImageId = getLong(getColumnIndex(COLUMN_MANTRA_IMAGE_ID));
			mantraImage.setId(focusImageId);
			long focusBoardId = getLong(getColumnIndex(COLUMN_MANTRA_IMAGE_MANTRA_BOARD_ID));
			mantraImage.setFocusBoardId(focusBoardId);
			String imagePath = getString(getColumnIndex(COLUMN_MANTRA_IMAGE_PATH));
			mantraImage.setPath(imagePath);
			String imageCaption = getString(getColumnIndex(COLUMN_MANTRA_IMAGE_CAPTION));
			mantraImage.setCaption(imageCaption);
			
			return mantraImage;
		}
	}

}
