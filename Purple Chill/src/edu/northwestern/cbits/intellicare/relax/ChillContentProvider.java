package edu.northwestern.cbits.intellicare.relax;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

/**
 * Created by Gwen on 6/13/2014.
 */
public class ChillContentProvider extends ContentProvider {

    private static final int USE = 1;

    private static final String AUTHORITY = "edu.northwestern.cbits.intellicare.relax";

    private static final String USE_TABLE = "ratings";

    protected static final String USE_START_TIME = "start_time";
    protected static final String USE_END_TIME = "end_time";
    protected static final String USE_START_STRESS = "start_stress";
    protected static final String USE_END_STRESS = "end_stress";
    protected static final String USE_RESOURCE_ID = "resource_id";

    private static final int DATABASE_VERSION = 1;
    public static final String ID = "_id";

    private UriMatcher _matcher = new UriMatcher(UriMatcher.NO_MATCH);
    private SQLiteDatabase _db = null;

    public ChillContentProvider()
    {
        super();

        this._matcher.addURI(AUTHORITY, USE_TABLE, USE);
    }


    public boolean onCreate()
    {
        final Context context = this.getContext().getApplicationContext();

        SQLiteOpenHelper helper = new SQLiteOpenHelper(context, "chill.db", null, ChillContentProvider.DATABASE_VERSION)
        {
            public void onCreate(SQLiteDatabase db)
            {
                db.execSQL(context.getString(R.string.db_create_uses_table));

                this.onUpgrade(db, 0, ChillContentProvider.DATABASE_VERSION);
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
            case ChillContentProvider.USE:
                return this._db.delete(ChillContentProvider.USE_TABLE, where, whereArgs);
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
            case ChillContentProvider.USE:
                newId = this._db.insert(ChillContentProvider.USE_TABLE, null, values);
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
            case ChillContentProvider.USE:
                return this._db.query(ChillContentProvider.USE_TABLE, columns, selection, selectionArgs, null, null, orderBy);

        }

        return null;
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        switch(this._matcher.match(uri))
        {
            case ChillContentProvider.USE:
                return this._db.update(ChillContentProvider.USE_TABLE, values, selection, selectionArgs);

        }

        return 0;
    }
}
