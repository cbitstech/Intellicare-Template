package edu.northwestern.cbits.intellicare.slumbertime;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class EntriesContentProvider extends ContentProvider
{
    private static final String AUTHORITY = "edu.northwestern.cbits.intellicare.slumbertime.content";
	private static final String FILENAME = "content.sqlite3";
	private static final String CONTENT_TABLE = "content_entries";

	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + CONTENT_TABLE);

	public static final String SLUG = "slug";;
	public static final String TITLE = "title";
	public static final String TEXT = "text";

	private SQLiteDatabase _db = null;

	public boolean onCreate() 
	{
		File parent = this.getContext().getFilesDir();
		
		if (parent.exists() == false)
			parent.mkdirs();
		
		File path = new File(parent, EntriesContentProvider.FILENAME);
		
		try 
		{
			FileOutputStream fout = new FileOutputStream(path);
			
		    AssetManager assetManager = this.getContext().getAssets();

		    InputStream in = assetManager.open(EntriesContentProvider.FILENAME);
		    
		    byte[] buffer = new byte[4096];
		    int read = 0;
		    
		    while ((read = in.read(buffer, 0, buffer.length)) != -1)
		    {
		    	fout.write(buffer, 0, read);
		    }
		    
		    in.close();
		    fout.flush();
		    fout.close();
		}
		catch (IOException e) 
		{
			LogManager.getInstance(this.getContext()).logException(e);
		}
		
		if (this._db  == null)
			this._db = SQLiteDatabase.openDatabase(path.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);

		return true;
	}

	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) 
	{
		return this._db.query(EntriesContentProvider.CONTENT_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
	}

	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) 
	{
		return 0;
	}

	public int delete(Uri uri, String where, String[] args) 
	{
		return 0;
	}

	public String getType(Uri uri) 
	{
    	return "vnd.android.cursor.dir/" + AUTHORITY + ".entry";
	}

	public Uri insert(Uri uri, ContentValues values) 
	{
		return null;
	}
}
