package edu.northwestern.cbits.intellicare.store;

import android.os.Bundle;
import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.view.Menu;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		SQLiteContentProvider.deleteAndRepopulateAllProducts(this);
		
		// query & display the data
		Cursor c = this.getContentResolver().query(SQLiteContentProvider.StoreUri.PRODUCTS, null, null, null, null);
		SQLiteContentProvider.logCursorResultSet("IntellicareStore", c);
		
		// close the DB connection
		c.close();
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
