package edu.northwestern.cbits.intellicare.thoughtchallenger;
import org.json.JSONArray;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class ThoughtContentProvider extends ContentProvider 
{
	private static final int PAIRS = 1;
	
    private static final String AUTHORITY = "edu.northwestern.cbits.intellicare.thoughtchallenger";

    private static final String PAIR_TABLE = "pairs";

	protected static final Uri THOUGHT_PAIR_URI = Uri.parse("content://" + AUTHORITY + "/" + PAIR_TABLE);;

	protected static final String PAIR_AUTOMATIC_THOUGHT = "automatic_thought";
	protected static final String PAIR_RATIONAL_RESPONSE = "rational_response";
	protected static final String PAIR_TAGS = "tags";
	protected static final String PAIR_DISTORTIONS = "distortions";

	private static final int DATABASE_VERSION = 1;
	public static final String ID = "_id";

    private UriMatcher _matcher = new UriMatcher(UriMatcher.NO_MATCH);
	private SQLiteDatabase _db = null;
	
    public ThoughtContentProvider()
    {
    	super();
    	
        this._matcher.addURI(AUTHORITY, PAIR_TABLE, PAIRS);
    }

    public boolean onCreate() 
	{
        final Context context = this.getContext().getApplicationContext();

        SQLiteOpenHelper helper = new SQLiteOpenHelper(context, "thoughts.db", null, ThoughtContentProvider.DATABASE_VERSION)
        {
            public void onCreate(SQLiteDatabase db) 
            {
	            db.execSQL(context.getString(R.string.db_create_pairs_table));

	            this.onUpgrade(db, 0, ThoughtContentProvider.DATABASE_VERSION);
            }

            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
            {
            	switch (oldVersion)
            	{
	                case 0:
	                	
	                default:
                        break;
            	}
            }
        };
        
        this._db  = helper.getWritableDatabase();

        return true;
	}

    public int delete(Uri uri, String where, String[] whereArgs) 
	{
        switch(this._matcher.match(uri))
        {
	        case ThoughtContentProvider.PAIRS:
	            return this._db.delete(ThoughtContentProvider.PAIR_TABLE, where, whereArgs);
        }
        
        return 0;
	}

	@Override
	public String getType(Uri arg0) 
	{
		return null;
	}

	public Uri insert(Uri uri, ContentValues values) 
	{
		long newId = -1;

		switch(this._matcher.match(uri))
        {
	        case ThoughtContentProvider.PAIRS:
	        	newId = this._db.insert(ThoughtContentProvider.PAIR_TABLE, null, values);
	        	break;
        }
		
		if (newId != -1)
			return Uri.withAppendedPath(uri, "" + newId);
		
		return null;
	}	

	public Cursor query(Uri uri, String[] columns, String selection, String[] selectionArgs, String orderBy) 
	{
		switch(this._matcher.match(uri))
        {
	        case ThoughtContentProvider.PAIRS:
	        	return this._db.query(ThoughtContentProvider.PAIR_TABLE, columns, selection, selectionArgs, null, null, orderBy);
        }
		
		return null;
	}

	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) 
	{
		switch(this._matcher.match(uri))
        {
	        case ThoughtContentProvider.PAIRS:
	        	return this._db.update(ThoughtContentProvider.PAIR_TABLE, values, selection, selectionArgs);
        }
		
		return 0;
	}

	public static JSONArray positiveWordArray(Context context) 
	{
		JSONArray words = new JSONArray();

		String[] thoughts = context.getResources().getStringArray(R.array.list_positive_thoughts);
		
		for (String s : thoughts)
		{
			s = s.toLowerCase();
			
			s = s.replace(".", " ");
			s = s.replace(",", " ");
			s = s.replace("!", " ");
			s = s.replace("?", " ");
			
			String[] tokens = s.split(" ");
			
			for (String token : tokens)
			{
				token = token.trim();
				
				if (token.length() > 0)
					words.put(token);
			}
		}
		
		return words;
	}
}
