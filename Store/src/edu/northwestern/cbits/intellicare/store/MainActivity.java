package edu.northwestern.cbits.intellicare.store;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.os.Bundle;
import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		ContentValues values = new ContentValues();
		
		// TODO this is ugly and verbose; find a list+functional-ish way of dealing w/ the cols and values to insert.
        values.put(SQLiteContentProvider.Products.COLNM_USERHAZIT, 1);
//        values.put(SQLiteContentProvider.Products.COLNM_BLOB, "null");
        values.put(SQLiteContentProvider.Products.COLNM_NAME, "Alpha");
        values.put(SQLiteContentProvider.Products.COLNM_DESC, "A hat");
        values.put(SQLiteContentProvider.Products.COLNM_ICON, "whatever");
        values.put(SQLiteContentProvider.Products.COLNM_ATTACHLOC, "wherever");
        values.put(SQLiteContentProvider.Products.COLNM_BUYPRICE, 22);
		this.getContentResolver().insert(SQLiteContentProvider.StoreUri.PRODUCTS, values);
		
		Cursor c = this.getContentResolver().query(SQLiteContentProvider.StoreUri.PRODUCTS, null, null, null, null);
		SQLiteContentProvider.logCursorResultSet("IntellicareStore", c);
		c.close();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
