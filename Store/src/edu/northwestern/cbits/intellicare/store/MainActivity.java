package edu.northwestern.cbits.intellicare.store;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	public static String LOG_PREFIX = "IntellicareStore";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		SQLiteContentProvider.deleteAndRepopulateAllProducts(this);
		
		// query & display the data
		Cursor c = this.getContentResolver().query(SQLiteContentProvider.StoreUri.PRODUCTS, null, null, null, null);
		SQLiteContentProvider.logCursorResultSet(LOG_PREFIX, c);
		
		fillInventoryList(c);		
		fillMarketList(c);
		
		// close the DB connection
		c.close();
	}


	private void fillInventoryList(Cursor c) {
		ArrayList<Product> products = new ArrayList<Product>();
		
		// filter the product list to only those owned by the user
		for(Product p : ProductData.getAllProductsAsArrayListFromCursor(c)) {
			if(p.userOwnsThis == 1)
				products.add(p);
		}
		
		// find the inventory list
		ListView inventory = (ListView)this.findViewById(R.id.inventoryItems);
		
		// create an adapter to adapt the product data to the view, and instantiate the view from its XML declaration
		ArrayAdapter<Product> adapter = new ArrayAdapter<Product>(this, R.layout.activity_main, products)
		{
			public View getView(int position, View convertView, ViewGroup parent)
			{
                if (convertView == null)
                {
                    LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                    convertView = inflater.inflate(R.layout.inventory_row, parent, false);
                }
                
                TextView name = (TextView) convertView.findViewById(R.id.inventory_row_container_label_name);
                TextView description = (TextView) convertView.findViewById(R.id.inventory_row_container_label_description);
                TextView attachesOnAvatarAtLocation = (TextView) convertView.findViewById(R.id.inventory_row_container_label_attachesOnAvatarAtLocation);
                TextView buyPrice = (TextView) convertView.findViewById(R.id.inventory_row_container_label_buyPrice);
                
                Product p = this.getItem(position);
                
                name.setText(p.name);
                description.setText(p.description);
                attachesOnAvatarAtLocation.setText(p.attachesOnAvatarAtLocation);
                buyPrice.setText(String.valueOf(p.buyPrice));
                
                return convertView;
			}
		};
		inventory.setAdapter(adapter);
	}


	/**
	 * fill the Market list
	 * inspiration src: https://github.com/nupmmarkbegale/Intellicare-Template/blob/master/Slumber%20Time/src/edu/northwestern/cbits/intellicare/slumbertime/HomeActivity.java#L39-L70
	 */
	private void fillMarketList(Cursor c) {
		ArrayList<Product> products = new ArrayList<Product>();
		products.addAll(ProductData.getAllProductsAsArrayListFromCursor(c));
		ListView market = (ListView)this.findViewById(R.id.marketItems);
		
		ArrayAdapter<Product> adapter = new ArrayAdapter<Product>(this, R.layout.activity_main, products)
		{
			public View getView(int position, View convertView, ViewGroup parent)
			{
                if (convertView == null)
                {
                    LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                    convertView = inflater.inflate(R.layout.market_row, parent, false);
                }
                
                TextView name = (TextView) convertView.findViewById(R.id.market_row_container_label_name);
                TextView description = (TextView) convertView.findViewById(R.id.market_row_container_label_description);
                TextView attachesOnAvatarAtLocation = (TextView) convertView.findViewById(R.id.market_row_container_label_attachesOnAvatarAtLocation);
                TextView buyPrice = (TextView) convertView.findViewById(R.id.market_row_container_label_buyPrice);
                
                Product p = this.getItem(position);
                
                name.setText(p.name);
                description.setText(p.description);
                attachesOnAvatarAtLocation.setText(p.attachesOnAvatarAtLocation);
                buyPrice.setText(String.valueOf(p.buyPrice));
                
                return convertView;
			}
		};
		market.setAdapter(adapter);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
