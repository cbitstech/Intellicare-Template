package edu.northwestern.cbits.intellicare.messages;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class ContentProvider extends android.content.ContentProvider 
{
	static final String LAST_UPDATE = "last_update";
	
	private static final String AUTHORITY = "edu.northwestern.cbits.intellicare.messages";

	private static final int LESSONS_LIST = 1;
	private static final int PAGES_LIST = 2;
	private static final int MESSAGE_GROUPS_LIST = 3;
	private static final int LESSON = 4;
	private static final int PAGE = 5;
	private static final int MESSAGE_GROUP = 6;

	private static final String LESSONS_TABLE = "lessons";
	private static final String PAGES_TABLE = "pages";
	private static final String MESSAGE_GROUPS_TABLE = "message_groups";

	public final static Uri LESSONS_URI = Uri.parse("content://" + AUTHORITY + "/" + LESSONS_TABLE);
	public final static Uri PAGES_URI = Uri.parse("content://" + AUTHORITY + "/" + PAGES_TABLE);
	public final static Uri MESSAGE_GROUPS_URI = Uri.parse("content://" + AUTHORITY + "/" + MESSAGE_GROUPS_TABLE);

	private UriMatcher _uriMatcher = null;
	private SQLiteDatabase _db = null;
	
	public int delete(Uri uri, String selection, String[] selectionArgs) 
	{
		return 0;
	}
	
	public boolean onCreate()
	{
		this._uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		
		this._uriMatcher.addURI(ContentProvider.AUTHORITY, "lessons", ContentProvider.LESSONS_LIST);
		this._uriMatcher.addURI(ContentProvider.AUTHORITY, "lesson/#", ContentProvider.LESSON);
		this._uriMatcher.addURI(ContentProvider.AUTHORITY, "pages", ContentProvider.PAGES_LIST);
		this._uriMatcher.addURI(ContentProvider.AUTHORITY, "pages/#", ContentProvider.PAGE);
		this._uriMatcher.addURI(ContentProvider.AUTHORITY, "message_groups", ContentProvider.MESSAGE_GROUPS_LIST);
		this._uriMatcher.addURI(ContentProvider.AUTHORITY, "message_groups/#", ContentProvider.MESSAGE_GROUP);

		final File folder = this.getContext().getFilesDir();

		if (folder.exists() == false)
			folder.mkdirs();

		final File database = new File(folder, "database.sqlite3");

		if (database.exists() == false)
		{
			try 
			{
				InputStream in = this.getContext().getAssets().open("database.sqlite3");

				FileOutputStream fout = new FileOutputStream(database);

				byte[] buffer = new byte[4096];
				int read = 0;

				while((read = in.read(buffer, 0, buffer.length)) != -1)
				{
					fout.write(buffer, 0, read);
				}

				fout.close();
			}
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}

		this._db = SQLiteDatabase.openDatabase(database.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
		
		return true;
	}

	public String getType(Uri uri) 
	{
		switch(this._uriMatcher.match(uri))
		{
			case ContentProvider.LESSON:
				return "vnd.android.cursor.item/vnd.edu.northwestern.cbits.intellicare.lesson";
			case ContentProvider.LESSONS_LIST:
				return "vnd.android.cursor.dir/vnd.edu.northwestern.cbits.intellicare.lesson";
			case ContentProvider.PAGE:
				return "vnd.android.cursor.item/vnd.edu.northwestern.cbits.intellicare.page";
			case ContentProvider.PAGES_LIST:
				return "vnd.android.cursor.dir/vnd.edu.northwestern.cbits.intellicare.page";
			case ContentProvider.MESSAGE_GROUP:
				return "vnd.android.cursor.item/vnd.edu.northwestern.cbits.intellicare.message_group";
			case ContentProvider.MESSAGE_GROUPS_LIST:
				return "vnd.android.cursor.dir/vnd.edu.northwestern.cbits.intellicare.message_group";
		}

		return null;
	}

	public Uri insert(Uri uri, ContentValues values) 
	{
		return null;
	}

	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) 
	{
		switch(this._uriMatcher.match(uri))
		{
			case ContentProvider.LESSON:
				return this._db.query(ContentProvider.LESSONS_TABLE, projection, this.buildSingleSelection(selection), this.buildSingleSelectionArgs(uri, selectionArgs), null, null, sortOrder);
			case ContentProvider.LESSONS_LIST:
				return this._db.query(ContentProvider.LESSONS_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
			case ContentProvider.PAGE:
				return this._db.query(ContentProvider.PAGES_TABLE, projection, this.buildSingleSelection(selection), this.buildSingleSelectionArgs(uri, selectionArgs), null, null, sortOrder);
			case ContentProvider.PAGES_LIST:
				return this._db.query(ContentProvider.PAGES_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
			case ContentProvider.MESSAGE_GROUP:
				return this._db.query(ContentProvider.MESSAGE_GROUPS_TABLE, projection, this.buildSingleSelection(selection), this.buildSingleSelectionArgs(uri, selectionArgs), null, null, sortOrder);
			case ContentProvider.MESSAGE_GROUPS_LIST:
				return this._db.query(ContentProvider.MESSAGE_GROUPS_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
		}
		
		return null;
	}

	private String[] buildSingleSelectionArgs(Uri uri, String[] selectionArgs) 
	{
		if (selectionArgs == null)
		{
			selectionArgs = new String[1];
			selectionArgs[0] = uri.getLastPathSegment();
		}
		else
		{
			String[] newSelectionArgs = new String[selectionArgs.length + 1];

			for (int i = 0; i < selectionArgs.length; i++)
			{
				newSelectionArgs[i] = selectionArgs[i];
			}

			newSelectionArgs[selectionArgs.length] = uri.getLastPathSegment();

			selectionArgs = newSelectionArgs;
		}

		return selectionArgs;
	}

	private String buildSingleSelection(String selection) 
	{
		if (selection == null)
			selection = "_id = ?";
		else
			selection = " AND _id = ?";
		
		return selection;
	}

	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) 
	{
		return 0;
	}
}