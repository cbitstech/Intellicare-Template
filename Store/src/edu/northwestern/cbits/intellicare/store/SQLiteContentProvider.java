package edu.northwestern.cbits.intellicare.store;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

/**
 * Provides access to a SQLite database for the IntellicareStore project.
 * @author mohrlab
 *
 */
public class SQLiteContentProvider extends ContentProvider {

	public static final String AUTHORITY = "edu.northwestern.cbits.intellicare.store";
	public static final String DATABASE_NAME = "intellicareStore.db";
	public static final int DATABASE_VERSION = 1;

	private UriMatcher _matcher = new UriMatcher(UriMatcher.NO_MATCH);
    private SQLiteDatabase _db = null;
    
	
	/**
	 * Encapsulates the set of URIs available and their formats. Each URI refers to a table, which is defined as one of the derivd table classes, below.
	 * @author mohrlab
	 *
	 */
	public static class StoreUri {
		private static final String PREFIX = "content://";
		private static final String SEP = "/";
		
		public static final Uri PRODUCTS = Uri.parse(PREFIX + AUTHORITY + SEP + Products.TABLE_NAME);
	}
	
	
	/* Table classes */
	private class BaseTable {
		public static final String COLNM_ID = "_id";
		private long id;
		
		public long getId() { return id; }
		public void setId() { this.id = id; }
	}

	public class Products extends BaseTable {
		public static final int TABLE_ID  = 1;
		public static final String TABLE_NAME = "Products";
		public static final String 
			 COLNM_USERHAZIT 	= "inInventory"					, COLTP_USERHAZIT = "smallint"
			,COLNM_BLOB 		= "blob"						, COLTP_BLOB = "blob"
			,COLNM_NAME 		= "name"						, COLTP_NAME = "text"
			,COLNM_DESC 		= "description"					, COLTP_DESC = "text"
			,COLNM_ICON 		= "icon"						, COLTP_ICON = "text"
			,COLNM_ATTACHLOC 	= "attachesOnAvatarAtLocation"	, COLTP_ATTACHLOC = "text"
			,COLNM_BUYPRICE 	= "buyPrice"					, COLTP_BUYPRICE = "decimal"
			;

		
	}
	

	
	public SQLiteContentProvider() {
		super();
		this._matcher.addURI(AUTHORITY, Products.TABLE_NAME, Products.TABLE_ID);
	}
	
	
	@Override
	public boolean onCreate() {
		final Context ctx = this.getContext().getApplicationContext();
		
		SQLiteOpenHelper sqliteOpenHelper = new SQLiteOpenHelper(ctx, DATABASE_NAME, null, DATABASE_VERSION)
		{

			@Override
			public void onCreate(SQLiteDatabase db) {
				db.execSQL("CREATE TABLE \"" + Products.TABLE_NAME + "\" (" +
					  "\"" + Products.COLNM_ID + "\" INTEGER PRIMARY KEY AUTOINCREMENT " +
					", \"" + Products.COLNM_USERHAZIT + "\" " + Products.COLTP_USERHAZIT +
					", \"" + Products.COLNM_NAME + "\" " + Products.COLTP_NAME +
					", \"" + Products.COLNM_DESC + "\" " + Products.COLTP_DESC +
					", \"" + Products.COLNM_ICON + "\" " + Products.COLTP_ICON +
					", \"" + Products.COLNM_ATTACHLOC + "\" " + Products.COLTP_ATTACHLOC + 
					", \"" + Products.COLNM_BUYPRICE + "\" " + Products.COLTP_BUYPRICE +
					", \"" + Products.COLNM_BLOB + "\" " + Products.COLTP_BLOB +
					");"
				);
				
			}

			@Override
			public void onUpgrade(SQLiteDatabase db, int oldVersion,
					int newVersion) {
				switch(oldVersion) {
					case 1:
						break;
					case 2:
						break;
					default:
						try {
							throw new Exception("Unhandled oldVersion version number.");
						} catch (Exception e) {
							e.printStackTrace();
						}
						break;
				}
			}
			
		};
		
		// sets the DB instance in memory AND creates it on-disk (if neccc.).
		this._db = sqliteOpenHelper.getWritableDatabase();
		
		return true;
	}


	@Override
	public String getType(Uri uri) {
		switch(this._matcher.match(uri)) {
		case Products.TABLE_ID:
			return "vnd.android.cursor.dir/" + AUTHORITY + ".products";
			default:
				try {
					throw new Exception("Unmatched uri: " + uri);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
		}
		return null;
	}

	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// switch on URI
		switch(this._matcher.match(uri)) {
			case Products.TABLE_ID:
				return this._db.delete(Products.TABLE_NAME, selection, selectionArgs);
			default:
				try {
					throw new Exception("Unmatched uri: " + uri);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
		}
		return 0;
	}

	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		long insertedId = 0;
		switch(this._matcher.match(uri)) {
		case Products.TABLE_ID:
			insertedId = this._db.insert(Products.TABLE_NAME, null, values);
			return Uri.withAppendedPath(StoreUri.PRODUCTS, "" + insertedId);
			default:
				try {
					throw new Exception("Unmatched uri: " + uri);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;				
		}
		return null;
	}

	
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return this._db.update(Products.TABLE_NAME, values, selection, selectionArgs);
	}


	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		switch(this._matcher.match(uri)) {
		case Products.TABLE_ID:
			return this._db.query(Products.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
		default:
			try {
				throw new Exception("Unmatched uri: " + uri);
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;			
		}
		return null;
	}

	
	/**
	 * Clears and repopulates the Products table.
	 */
	public static void deleteAndRepopulateAllProducts(Activity a) {
		// REFRESH ALL DB DATA: delete all data, then re-insert.
		a.getContentResolver().delete(SQLiteContentProvider.StoreUri.PRODUCTS, null, null);
		ContentValues values = new ContentValues();
		for(Product i : ProductData.getAllProducts()) {
			// TODO this is ugly and verbose; find a list+functional-ish way of dealing w/ the cols and values to insert.
	        values.put(SQLiteContentProvider.Products.COLNM_USERHAZIT, 0);
	        //values.put(SQLiteContentProvider.Products.COLNM_BLOB, "null");
	        values.put(SQLiteContentProvider.Products.COLNM_NAME, i.name);
	        values.put(SQLiteContentProvider.Products.COLNM_DESC, i.description);
	        values.put(SQLiteContentProvider.Products.COLNM_ICON, i.icon);
	        values.put(SQLiteContentProvider.Products.COLNM_ATTACHLOC, i.attachesOnAvatarAtLocation);
	        values.put(SQLiteContentProvider.Products.COLNM_BUYPRICE, i.buyPrice);
			a.getContentResolver().insert(SQLiteContentProvider.StoreUri.PRODUCTS, values);
		}
	}
	

	/**
	 * Logs the columns, datatypes, and values of a Cursor's resultset. Intended to aid understanding what your content provider is returning. 
	 * @param c
	 */
	public static void logCursorResultSet(String logTag, Cursor c) {
		ArrayList<String> keys = new ArrayList<String>();
		ArrayList<Integer> types = new ArrayList<Integer>();
		ArrayList<String> vals = new ArrayList<String>();
		while(c.moveToNext()) {
			for(int i = 0; i < c.getColumnCount(); i++) {
				keys.add(c.getColumnName(i));
				types.add(c.getType(i));
				vals.add(c.getString(i));
			}
			Log.d(logTag, keys.toString() + " (" + types.toString() + ") = " + vals.toString());
			keys.clear();
			types.clear();
			vals.clear();
		}
	}
	
}
