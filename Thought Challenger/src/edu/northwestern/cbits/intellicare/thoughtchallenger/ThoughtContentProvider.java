package edu.northwestern.cbits.intellicare.thoughtchallenger;
import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.preference.PreferenceManager;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class ThoughtContentProvider extends ContentProvider 
{
	public static final String SAVED_TAGS = "saved_tags";

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

		Cursor c = context.getContentResolver().query(ThoughtContentProvider.THOUGHT_PAIR_URI, null, null, null, null);
		
		if (c.getCount() > 0)
		{
			while (c.moveToNext())
			{
				String s = c.getString(c.getColumnIndex(ThoughtContentProvider.PAIR_RATIONAL_RESPONSE));
				
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
		}
		else
		{
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
		}
		
		c.close();
		
		return words;
	}

	public static String[] fetchTags(Context context) 
	{
		ArrayList<String> allTags = new ArrayList<String>();
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		try 
		{
			JSONArray savedTags = new JSONArray(prefs.getString(ThoughtContentProvider.SAVED_TAGS, "[]"));

			for (int i = 0; i < savedTags.length(); i++)
			{
				String tag = savedTags.getString(i);
				
				if (allTags.contains(tag) == false)
					allTags.add(tag);
			}
		} 
		catch (JSONException e) 
		{
			LogManager.getInstance(context).logException(e);
		}
		
		Cursor c = context.getContentResolver().query(ThoughtContentProvider.THOUGHT_PAIR_URI, null, null, null, null);
		
		while (c.moveToNext())
		{
			try 
			{
				JSONArray tags = new JSONArray(c.getString(c.getColumnIndex(ThoughtContentProvider.PAIR_TAGS)));
				
				for (int i = 0; i < tags.length(); i++)
				{
					String tag = tags.getString(i);
					
					if (allTags.contains(tag) == false)
						allTags.add(tag);
				}
			} 
			catch (JSONException e) 
			{
				LogManager.getInstance(context).logException(e);
			}
		}
		
		c.close();

		Collections.sort(allTags);
		
		String[] tags = new String[allTags.size()];
		
		for (int i = 0; i < tags.length; i++)
		{
			tags[i] = allTags.get(i);
		}
		
		return tags;
	}

	public static void addTag(Context context, long id, String tag) 
	{
		String where = ThoughtContentProvider.ID + " = ?";
		String[] args = { "" + id };
 		
		Cursor c = context.getContentResolver().query(ThoughtContentProvider.THOUGHT_PAIR_URI, null, where, args, null);
		
		if (c.moveToNext())
		{
			try 
			{
				JSONArray oldTags = new JSONArray(c.getString(c.getColumnIndex(ThoughtContentProvider.PAIR_TAGS)));
				
				boolean add = true;
				
				for (int i = 0; i < oldTags.length() && add == true; i++)
				{
					String oldTag = oldTags.getString(i);
					
					if (oldTag.trim().equalsIgnoreCase(tag) == true)
						add = false;
				}
				
				if (add)
				{
					oldTags.put(tag.trim());
					
					ContentValues values = new ContentValues();
					values.put(ThoughtContentProvider.PAIR_TAGS, oldTags.toString());
					
					context.getContentResolver().update(ThoughtContentProvider.THOUGHT_PAIR_URI, values, where, args);
				}
			} 
			catch (JSONException e) 
			{
				LogManager.getInstance(context).logException(e);
			} 
		}
		
		c.close();
	}

	public static void removeTag(Context context, long id, String tag) 
	{
		String where = ThoughtContentProvider.ID + " = ?";
		String[] args = { "" + id };
 		
		Cursor c = context.getContentResolver().query(ThoughtContentProvider.THOUGHT_PAIR_URI, null, where, args, null);
		
		if (c.moveToNext())
		{
			try 
			{
				JSONArray oldTags = new JSONArray(c.getString(c.getColumnIndex(ThoughtContentProvider.PAIR_TAGS)));
				
				JSONArray newTags = new JSONArray();
				
				boolean add = true;
				
				for (int i = 0; i < oldTags.length() && add == true; i++)
				{
					String oldTag = oldTags.getString(i);
					
					if (oldTag.trim().equalsIgnoreCase(tag) == false)
						newTags.put(oldTag);
				}
				
				if (add)
				{
					ContentValues values = new ContentValues();
					values.put(ThoughtContentProvider.PAIR_TAGS, newTags.toString());
					
					context.getContentResolver().update(ThoughtContentProvider.THOUGHT_PAIR_URI, values, where, args);
				}
			} 
			catch (JSONException e) 
			{
				LogManager.getInstance(context).logException(e);
			} 
		}
		
		c.close();
	}

	public static JSONArray fullWordArray(Context context) 
	{
		JSONArray positive = ThoughtContentProvider.positiveWordArray(context);
		JSONArray negative = ThoughtContentProvider.negativeWordArray(context);
		
		JSONArray all = new JSONArray();

		try 
		{
			for (int i = 0; i < positive.length(); i++)
			{
					all.put(positive.getString(i));
			}
		}
		catch (JSONException e) 
		{
			LogManager.getInstance(context).logException(e);
		}
		
		try 
		{
			for (int i = 0; i < negative.length(); i++)
			{
					all.put(negative.getString(i));
			}
		}
		catch (JSONException e) 
		{
			LogManager.getInstance(context).logException(e);
		}

		return all;
	}

	public static JSONArray negativeWordArray(Context context) 
	{
		JSONArray words = new JSONArray();

		Cursor c = context.getContentResolver().query(ThoughtContentProvider.THOUGHT_PAIR_URI, null, null, null, null);
		
		if (c.getCount() > 0)
		{
			while (c.moveToNext())
			{
				String s = c.getString(c.getColumnIndex(ThoughtContentProvider.PAIR_AUTOMATIC_THOUGHT));
				
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
		}
		else
		{
			String[] thoughts = context.getResources().getStringArray(R.array.list_negative_thoughts);
			
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
		}
		
		c.close();
		
		return words;
	}
}
