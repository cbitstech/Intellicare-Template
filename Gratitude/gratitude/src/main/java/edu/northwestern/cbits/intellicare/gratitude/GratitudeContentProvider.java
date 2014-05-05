package edu.northwestern.cbits.intellicare.relax.gratitude.gratitude;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

/**
 * Created by Gwen on 4/14/2014.
 */
public class GratitudeContentProvider extends ContentProvider {

    public static final int ANYTIME_ENTRY = 1;
    private static final int HUMBLE_ENTRY = 2;
    private static final int NAIKAN_ENTRY = 3;
    private static final int SPECIFICITY_ENTRY = 4;

    private static final String AUTHORITY = "edu.northwestern.cbits.intellicare.gratitude";

    private static final String ANYTIME_TABLE = "profile";
    private static final String HUMBLE_TABLE = "humble";
    private static final String NAIKAN_TABLE = "naikan";
    private static final String SPECIFICITY_TABLE = "specificity";

    public static final Uri ANYTIME_URI = Uri.parse("content://" + AUTHORITY + "/" + ANYTIME_TABLE);
    public static final Uri HUMBLE_URI = Uri.parse("content://" + AUTHORITY + "/" + HUMBLE_TABLE);
    public static final Uri NAIKAN_URI = Uri.parse("content://" + AUTHORITY + "/" + NAIKAN_TABLE);
    public static final Uri SPECIFICITY_URI = Uri.parse("content://" + AUTHORITY + "/" + SPECIFICITY_TABLE);

    private static final int DATABASE_VERSION = 1;

    public static final String ANYTIME_ENTRY_ID = "_id";
    public static final String ANYTIME_TIMESTAMP= "anytime_timestamp";

    public static final String HUMBLE_ENTRY_ID = "_id";
    public static final String HUMBLE_TIMESTAMP= "humble_timestamp";

    public static final String NAIKAN_ENTRY_ID = "_id";
    public static final String NAIKAN_TIMESTAMP= "naikan_timestamp";

    public static final String SPECIFICITY_ENTRY_ID = "_id";
    public static final String SPECIFICITY_TIMESTAMP= "specificity_timestamp";


    private UriMatcher _matcher = new UriMatcher(UriMatcher.NO_MATCH);
    private SQLiteDatabase _db = null;

    public GratitudeContentProvider()
    {
        super();

        this._matcher.addURI(AUTHORITY, ANYTIME_TABLE, ANYTIME_ENTRY);
        this._matcher.addURI(AUTHORITY, HUMBLE_TABLE, HUMBLE_ENTRY);
        this._matcher.addURI(AUTHORITY, NAIKAN_TABLE, NAIKAN_ENTRY);
        this._matcher.addURI(AUTHORITY, SPECIFICITY_TABLE, SPECIFICITY_ENTRY);

    }

    public boolean onCreate()
    {
        final Context context = this.getContext().getApplicationContext();

        SQLiteOpenHelper helper = new SQLiteOpenHelper(context, "gratitude.db", null, GratitudeContentProvider.DATABASE_VERSION)
        {
            public void onCreate(SQLiteDatabase db)
            {
                db.execSQL(context.getString(R.string.db_create_anytime_table));
                db.execSQL(context.getString(R.string.db_create_humble_table));
                db.execSQL(context.getString(R.string.db_create_naikan_table));
                db.execSQL(context.getString(R.string.db_create_specificity_table));

                this.onUpgrade(db, 0, GratitudeContentProvider.DATABASE_VERSION);
            }

            // Called when DB version number in DB file is not same as code. Updates database in place instead of blowing away and rebuilding (migration)...

            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
            {
                switch (oldVersion)
                {
                    case 0:

                  /*  case 1:
                        db.execSQL(context.getString(R.string.db_update_profile_table));
                   case 2:
                        db.execSQL(context.getString(R.string.db_update_wizard_one_table));
                 */
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
            case GratitudeContentProvider.ANYTIME_ENTRY:
                return this._db.delete(GratitudeContentProvider.ANYTIME_TABLE, where, whereArgs);
            case GratitudeContentProvider.HUMBLE_ENTRY:
                return this._db.delete(GratitudeContentProvider.HUMBLE_TABLE, where, whereArgs);
            case GratitudeContentProvider.SPECIFICITY_ENTRY:
                return this._db.delete(GratitudeContentProvider.SPECIFICITY_TABLE, where, whereArgs);
            case GratitudeContentProvider.NAIKAN_ENTRY:
                return this._db.delete(GratitudeContentProvider.NAIKAN_TABLE, where, whereArgs);
        }

        return 0;
    }

    @Override
    public String getType(Uri uri)
    {
        switch(this._matcher.match(uri))
        {
            case GratitudeContentProvider.ANYTIME_ENTRY:
                return "vnd.android.cursor.dir/" + AUTHORITY + ".anytime";
            case GratitudeContentProvider.NAIKAN_ENTRY:
                return "vnd.android.cursor.dir/" + AUTHORITY + ".naikan";
            case GratitudeContentProvider.HUMBLE_ENTRY:
                return "vnd.android.cursor.dir/" + AUTHORITY + ".humble";
            case GratitudeContentProvider.SPECIFICITY_ENTRY:
                return "vnd.android.cursor.dir/" + AUTHORITY + ".specificity";

        }

        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        long newId = -1;
        switch(this._matcher.match(uri))
        {
            case GratitudeContentProvider.ANYTIME_ENTRY:
                newId = this._db.insert(GratitudeContentProvider.ANYTIME_TABLE, null, values);

                break;
            case GratitudeContentProvider.HUMBLE_ENTRY:
                newId = this._db.insert(GratitudeContentProvider.HUMBLE_TABLE, null, values);

                break;

            case GratitudeContentProvider.NAIKAN_ENTRY:
                newId = this._db.insert(GratitudeContentProvider.NAIKAN_TABLE, null, values);

                break;

            case GratitudeContentProvider.SPECIFICITY_ENTRY:
                newId = this._db.insert(GratitudeContentProvider.SPECIFICITY_TABLE, null, values);

                break;
        }


        return null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        switch(this._matcher.match(uri))
        {
            case GratitudeContentProvider.ANYTIME_ENTRY:
                return this._db.query(GratitudeContentProvider.ANYTIME_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
            case GratitudeContentProvider.HUMBLE_ENTRY:
                return this._db.query(GratitudeContentProvider.HUMBLE_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
            case GratitudeContentProvider.SPECIFICITY_ENTRY:
                return this._db.query(GratitudeContentProvider.SPECIFICITY_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
            case GratitudeContentProvider.NAIKAN_ENTRY:
                return this._db.query(GratitudeContentProvider.NAIKAN_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
        }

        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs)
    {
        switch(this._matcher.match(uri))
        {
            case GratitudeContentProvider.ANYTIME_ENTRY:
                return this._db.update(GratitudeContentProvider.ANYTIME_TABLE, values, where, whereArgs);
            case GratitudeContentProvider.HUMBLE_ENTRY:
                return this._db.update(GratitudeContentProvider.HUMBLE_TABLE, values, where, whereArgs);
            case GratitudeContentProvider.NAIKAN_ENTRY:
                return this._db.update(GratitudeContentProvider.NAIKAN_TABLE, values, where, whereArgs);
            case GratitudeContentProvider.SPECIFICITY_ENTRY:
                return this._db.update(GratitudeContentProvider.SPECIFICITY_TABLE, values, where, whereArgs);
        }

        return 0;

    }
}
