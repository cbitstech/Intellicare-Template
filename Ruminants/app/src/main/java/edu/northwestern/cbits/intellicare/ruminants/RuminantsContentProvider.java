package edu.northwestern.cbits.intellicare.ruminants;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

    public class RuminantsContentProvider extends ContentProvider
    {
    //public static final int PROFILE = 1;
    private static final int WIZARD_ONE = 2;
    private static final int WPT_USE = 3;
    private static final int LOG_USE = 4;
    private static final int DIDACTIC_USE = 5;

    public static long LOG_COUNT = 0;
    public static long DIDACTIC_COUNT = 0;
    public static long WPT_COUNT = 0;


    private static final String AUTHORITY = "edu.northwestern.cbits.intellicare.ruminants";

    //private static final String PROFILE_TABLE = "profile";
    private static final String WIZARD_ONE_TABLE = "wizard_one";
    private static final String WPT_USE_TABLE = "wpt_use";
    private static final String LOG_USE_TABLE = "log_use";
    private static final String DIDACTIC_USE_TABLE = "didactic_use";

   // public static final Uri PROFILE_URI = Uri.parse("content://" + AUTHORITY + "/" + PROFILE_TABLE);
    public static final Uri WIZARD_ONE_URI = Uri.parse("content://" + AUTHORITY + "/" + WIZARD_ONE_TABLE);
    public static final Uri WPT_USE_URI = Uri.parse("content://" + AUTHORITY + "/" + WPT_USE_TABLE);
    public static final Uri LOG_USE_URI = Uri.parse("content://" + AUTHORITY + "/" + LOG_USE_TABLE);
    public static final Uri DIDACTIC_USE_URI = Uri.parse("content://" + AUTHORITY + "/" + DIDACTIC_USE_TABLE);


    private static final int DATABASE_VERSION = 1;

    public static final String WIZARD_ONE_ID = "_id";
    public static final String WIZARD_ONE_ATTEMPTED_STOP_METHOD = "attempted_stop_method";
    public static final String WIZARD_ONE_DURATION = "duration";
    public static final String WIZARD_ONE_EMOTION = "emotion";
    public static final String WIZARD_ONE_RUMINATION_ISOVER = "rumination_isover";
    public static final String WIZARD_ONE_RUMINATION_TERMINATION_CAUSE = "rumination_termination_cause";
    public static final String WIZARD_ONE_TRIGGER = "trigger";

    public static final String WIZARD_ONE_TIMESTAMP= "wizard_one_timestamp";

   /* public static final String PROFILE_ID = "_id";
    public static final String PROFILE_RUMINATION_CONCERNS = "concerns";
    public static final String PROFILE_HELP_FREQUENCY = "help_frequency";
    public static final String PROFILE_RUMINATING_LATELY = "ruminating_lately";

    public static final String PROFILE_TIMESTAMP= "profile_timestamp"; */


    public static final String WPT_USE_ID = "_id";
    public static final String WPT_USE_TIMESTAMP= "wpt_timestamp";

    public static final String LOG_USE_ID = "_id";
    public static final String LOG_USE_TIMESTAMP= "log_timestamp";

    public static final String DIDACTIC_USE_ID = "_id";
    public static final String DIDACTIC_TIMESTAMP= "didactic_timestamp";


    private UriMatcher _matcher = new UriMatcher(UriMatcher.NO_MATCH);
        private SQLiteDatabase _db = null;



    public RuminantsContentProvider()
    {
        super();

       // this._matcher.addURI(AUTHORITY, PROFILE_TABLE, PROFILE);
        this._matcher.addURI(AUTHORITY, WIZARD_ONE_TABLE, WIZARD_ONE);
        this._matcher.addURI(AUTHORITY, WPT_USE_TABLE, WPT_USE);
        this._matcher.addURI(AUTHORITY, LOG_USE_TABLE, LOG_USE);
        this._matcher.addURI(AUTHORITY, DIDACTIC_USE_TABLE, DIDACTIC_USE);

    }

    public boolean onCreate()
    {
        final Context context = this.getContext().getApplicationContext();

        SQLiteOpenHelper helper = new SQLiteOpenHelper(context, "ruminants.db", null, RuminantsContentProvider.DATABASE_VERSION)
        {
            public void onCreate(SQLiteDatabase db)
            {
              //  db.execSQL(context.getString(R.string.db_create_profile_table));
                db.execSQL(context.getString(R.string.db_create_wizard_one_table));
                db.execSQL(context.getString(R.string.db_create_wpt_use_table));
                db.execSQL(context.getString(R.string.db_create_log_use_table));
                db.execSQL(context.getString(R.string.db_create_didactic_use_table));

                this.onUpgrade(db, 0, RuminantsContentProvider.DATABASE_VERSION);
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
       /*     case RuminantsContentProvider.PROFILE:
                return this._db.delete(RuminantsContentProvider.PROFILE_TABLE, where, whereArgs); */
            case RuminantsContentProvider.WIZARD_ONE:
                return this._db.delete(RuminantsContentProvider.WIZARD_ONE_TABLE, where, whereArgs);
            case RuminantsContentProvider.WPT_USE:
                return this._db.delete(RuminantsContentProvider.WPT_USE_TABLE, where, whereArgs);
            case RuminantsContentProvider.LOG_USE:
                return this._db.delete(RuminantsContentProvider.LOG_USE_TABLE, where, whereArgs);
            case RuminantsContentProvider.DIDACTIC_USE:
                return this._db.delete(RuminantsContentProvider.DIDACTIC_USE_TABLE, where, whereArgs);
        }

        return 0;
    }

    public void useCounter()
    {
        LOG_COUNT = DatabaseUtils.queryNumEntries(this._db, LOG_USE_TABLE);
        WPT_COUNT = DatabaseUtils.queryNumEntries(this._db, WPT_USE_TABLE);
        DIDACTIC_COUNT = DatabaseUtils.queryNumEntries(this._db, DIDACTIC_USE_TABLE);

    }

    @Override
    public String getType(Uri uri)
    {
        switch(this._matcher.match(uri))
        {
     /*       case RuminantsContentProvider.PROFILE:
                return "vnd.android.cursor.dir/" + AUTHORITY + ".profile"; */
            case RuminantsContentProvider.WIZARD_ONE:
                return "vnd.android.cursor.dir/" + AUTHORITY + ".wizard_one";
            case RuminantsContentProvider.WPT_USE:
                return "vnd.android.cursor.dir/" + AUTHORITY + ".wpt_use";
            case RuminantsContentProvider.LOG_USE:
                return "vnd.android.cursor.dir/" + AUTHORITY + ".log_use";
            case RuminantsContentProvider.DIDACTIC_USE:
                return "vnd.android.cursor.dir/" + AUTHORITY + ".didactic_use";
        }

        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        long newId = -1;
        switch(this._matcher.match(uri))
        {
     /*       case RuminantsContentProvider.PROFILE:
                newId = this._db.insert(RuminantsContentProvider.PROFILE_TABLE, null, values);

                break; */
            case RuminantsContentProvider.WIZARD_ONE:
                newId = this._db.insert(RuminantsContentProvider.WIZARD_ONE_TABLE, null, values);

                break;

            case RuminantsContentProvider.WPT_USE:
                newId = this._db.insert(RuminantsContentProvider.WPT_USE_TABLE, null, values);

                break;

            case RuminantsContentProvider.LOG_USE:
                newId = this._db.insert(RuminantsContentProvider.LOG_USE_TABLE, null, values);

                break;

            case RuminantsContentProvider.DIDACTIC_USE:
                newId = this._db.insert(RuminantsContentProvider.DIDACTIC_USE_TABLE, null, values);

                break;
        }


        if (newId != -1) {
            this.useCounter();

            return Uri.withAppendedPath(uri, "" + newId);
        }

        return null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        switch(this._matcher.match(uri))
        {
      /*      case RuminantsContentProvider.PROFILE:
                return this._db.query(RuminantsContentProvider.PROFILE_TABLE, projection, selection, selectionArgs, null, null, sortOrder); */
            case RuminantsContentProvider.WIZARD_ONE:
                return this._db.query(RuminantsContentProvider.WIZARD_ONE_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
            case RuminantsContentProvider.WPT_USE:
                return this._db.query(RuminantsContentProvider.WPT_USE_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
            case RuminantsContentProvider.LOG_USE:
                return this._db.query(RuminantsContentProvider.LOG_USE_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
            case RuminantsContentProvider.DIDACTIC_USE:
                return this._db.query(RuminantsContentProvider.DIDACTIC_USE_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
        }

        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs)
    {
        switch(this._matcher.match(uri))
        {
     /*       case RuminantsContentProvider.PROFILE:
                return this._db.update(RuminantsContentProvider.PROFILE_TABLE, values, where, whereArgs); */
            case RuminantsContentProvider.WIZARD_ONE:
                return this._db.update(RuminantsContentProvider.WIZARD_ONE_TABLE, values, where, whereArgs);
            case RuminantsContentProvider.WPT_USE:
                return this._db.update(RuminantsContentProvider.WPT_USE_TABLE, values, where, whereArgs);
            case RuminantsContentProvider.LOG_USE:
                return this._db.update(RuminantsContentProvider.LOG_USE_TABLE, values, where, whereArgs);
            case RuminantsContentProvider.DIDACTIC_USE:
                return this._db.update(RuminantsContentProvider.DIDACTIC_USE_TABLE, values, where, whereArgs);
        }

        return 0;

    }

}