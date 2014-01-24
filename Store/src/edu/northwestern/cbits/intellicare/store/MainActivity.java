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
		
		deleteAndRepopulateAllProducts();
		
		// query & display the data
		Cursor c = this.getContentResolver().query(SQLiteContentProvider.StoreUri.PRODUCTS, null, null, null, null);
		SQLiteContentProvider.logCursorResultSet("IntellicareStore", c);
		
		// close the DB connection
		c.close();
	}


	/**
	 * Clears and repopulates the Products table.
	 */
	private void deleteAndRepopulateAllProducts() {
		// REFRESH ALL DB DATA: delete all data, then re-insert.
		this.getContentResolver().delete(SQLiteContentProvider.StoreUri.PRODUCTS, null, null);
		ContentValues values = new ContentValues();
		for(Product i : ProductData.getAllProducts()) {
			// TODO this is ugly and verbose; find a list+functional-ish way of dealing w/ the cols and values to insert.
	        values.put(SQLiteContentProvider.Products.COLNM_USERHAZIT, 0);
	        //values.put(SQLiteContentProvider.Products.COLNM_BLOB, "null");
	        values.put(SQLiteContentProvider.Products.COLNM_NAME, i.name);
	        values.put(SQLiteContentProvider.Products.COLNM_DESC, i.description);
	        values.put(SQLiteContentProvider.Products.COLNM_ICON, i.icon);
	        values.put(SQLiteContentProvider.Products.COLNM_ATTACHLOC, i.attachesOnAvatarAtLocation);
	        values.put(SQLiteContentProvider.Products.COLNM_BUYPRICE, i.buyPrice);
			this.getContentResolver().insert(SQLiteContentProvider.StoreUri.PRODUCTS, values);
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
