package edu.northwestern.cbits.intellicare.icope;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;


public class CopeContentProvider extends ContentProvider 
{
	private static final int REMINDERS = 1;
	private static final int CARDS = 2;
	
    private static final String AUTHORITY = "edu.northwestern.cbits.intellicare.icope";

    private static final String REMINDER_TABLE = "reminders";
    private static final String CARD_TABLE = "cards";

	protected static final Uri REMINDER_URI = Uri.parse("content://" + AUTHORITY + "/" + REMINDER_TABLE);;
	protected static final Uri CARD_URI = Uri.parse("content://" + AUTHORITY + "/" + CARD_TABLE);;

	protected static final String REMINDER_CARD_ID = "card_id";
	protected static final String REMINDER_YEAR = "year";
	protected static final String REMINDER_MONTH = "month";
	protected static final String REMINDER_DAY = "day";
	protected static final String REMINDER_HOUR = "hour";
	protected static final String REMINDER_MINUTE = "minute";
	protected static final String REMINDER_SECOND = "second";

	protected static final String CARD_EVENT = "event";
	protected static final String CARD_REMINDER = "reminder";
	protected static final String CARD_ID = "_id";

	private static final int DATABASE_VERSION = 1;

    private UriMatcher _matcher = new UriMatcher(UriMatcher.NO_MATCH);
	private SQLiteDatabase _db = null;
	
    public CopeContentProvider()
    {
    	super();
    	
        this._matcher.addURI(AUTHORITY, REMINDER_TABLE, REMINDERS);
        this._matcher.addURI(AUTHORITY, CARD_TABLE, CARDS);
    }

    public boolean onCreate() 
	{
        final Context context = this.getContext().getApplicationContext();

        SQLiteOpenHelper helper = new SQLiteOpenHelper(context, "icope.db", null, CopeContentProvider.DATABASE_VERSION)
        {
            public void onCreate(SQLiteDatabase db) 
            {
	            db.execSQL(context.getString(R.string.db_create_reminders_table));
	            db.execSQL(context.getString(R.string.db_create_cards_table));

	            this.onUpgrade(db, 0, CopeContentProvider.DATABASE_VERSION);
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
	        case CopeContentProvider.CARDS:
	            return this._db.delete(CopeContentProvider.CARD_TABLE, where, whereArgs);
	        case CopeContentProvider.REMINDERS:
	            return this._db.delete(CopeContentProvider.REMINDER_TABLE, where, whereArgs);
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
	        case CopeContentProvider.CARDS:
	        	newId = this._db.insert(CopeContentProvider.CARD_TABLE, null, values);
	        	break;
	        case CopeContentProvider.REMINDERS:
	        	newId = this._db.insert(CopeContentProvider.REMINDER_TABLE, null, values);
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
	        case CopeContentProvider.CARDS:
	        	return this._db.query(CopeContentProvider.CARD_TABLE, columns, selection, selectionArgs, null, null, orderBy);
	        case CopeContentProvider.REMINDERS:
	        	return this._db.query(CopeContentProvider.REMINDER_TABLE, columns, selection, selectionArgs, null, null, orderBy);
        }
		
		return null;
	}

	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) 
	{
		switch(this._matcher.match(uri))
        {
	        case CopeContentProvider.CARDS:
	        	return this._db.update(CopeContentProvider.CARD_TABLE, values, selection, selectionArgs);
	        case CopeContentProvider.REMINDERS:
	        	return this._db.update(CopeContentProvider.REMINDER_TABLE, values, selection, selectionArgs);
        }
		
		return 0;
	}
}
