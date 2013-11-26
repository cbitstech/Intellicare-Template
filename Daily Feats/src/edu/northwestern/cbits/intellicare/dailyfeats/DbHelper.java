package edu.northwestern.cbits.intellicare.dailyfeats;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Gabe on 9/24/13.
 */
public class DbHelper extends SQLiteOpenHelper
{

    public static final String ID_COLUMN = "_id";
    public static final String CHECKLISTS_TABLE = AppConstants.checklistsTableName;
    public static final String FEAT_RESPONSES_TABLE = AppConstants.featResponsesTableName;
    public static final int    DATABASE_VERSION = 1;

    public DbHelper(Context context)
    {
        super(context, AppConstants.dbName, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate (SQLiteDatabase db) {
//        super.onCreate(db);
        // Android takes care of wrapping in a transaction.
        // http://developer.android.com/reference/android/database/sqlite/SQLiteOpenHelper.html


        db.execSQL("CREATE TABLE IF NOT EXISTS "+CHECKLISTS_TABLE+"(" +
                ID_COLUMN+" INTEGER PRIMARY KEY NOT NULL," +
                AppConstants.DEPRESSION_LEVEL + " INTEGER NOT NULL," +
                AppConstants.dateTakenKey + " VARCHAR(23) NOT NULL," +
                AppConstants.dateTimeTakenKey + " INTEGER NOT NULL," +
                AppConstants.featOfStrengthKey + " TEXT" +
                ");");

        db.execSQL("CREATE INDEX IF NOT EXISTS chklst_"+AppConstants.DEPRESSION_LEVEL+
                " ON "+CHECKLISTS_TABLE+"("+AppConstants.DEPRESSION_LEVEL+");");
        db.execSQL("CREATE INDEX IF NOT EXISTS chklst_"+AppConstants.dateTakenKey+
                " ON "+CHECKLISTS_TABLE+"("+AppConstants.dateTakenKey+");");
        db.execSQL("CREATE INDEX IF NOT EXISTS chklst_"+AppConstants.DEPRESSION_LEVEL+
                " ON "+CHECKLISTS_TABLE+"("+AppConstants.DEPRESSION_LEVEL+");");


        db.execSQL("CREATE TABLE IF NOT EXISTS "+FEAT_RESPONSES_TABLE+"("+
                    ID_COLUMN+" INTEGER PRIMARY KEY NOT NULL,"+
                    AppConstants.checklistIdKey+" INTEGER KEY NOT NULL,"+
                    AppConstants.featNameKey+" VARCHAR(63) NOT NULL,"+
                    AppConstants.featLevelKey+" INTEGER NOT NULL,"+
                    AppConstants.featCompletedKey+" TINYINT,"+
                    AppConstants.featDetailsKey+" TEXT"+
                   ");");

        db.execSQL("CREATE INDEX IF NOT EXISTS fr_"+AppConstants.checklistIdKey+
                    " ON feat_responses("+AppConstants.checklistIdKey+");");
        db.execSQL("CREATE INDEX IF NOT EXISTS fr_"+AppConstants.featLevelKey+
                " ON "+FEAT_RESPONSES_TABLE+"("+AppConstants.featLevelKey+");");
        db.execSQL("CREATE INDEX IF NOT EXISTS fr_"+AppConstants.featNameKey+
                " ON "+FEAT_RESPONSES_TABLE+"("+AppConstants.featNameKey+");");
        db.execSQL("CREATE INDEX IF NOT EXISTS fr_"+AppConstants.featCompletedKey+
                " ON "+FEAT_RESPONSES_TABLE+"("+AppConstants.featCompletedKey+");");

        // cannot answer a feat on an individual checklist more than once.
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS unique_chklst_feat_response"+
                " ON "+FEAT_RESPONSES_TABLE+"("+AppConstants.checklistIdKey+","+AppConstants.featNameKey+");");


    }

    @Override
    public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion) {
//        super.onUpgrade(db, oldVersion, newVersion);
        /* don't need to implement this yet? */
    }

}
