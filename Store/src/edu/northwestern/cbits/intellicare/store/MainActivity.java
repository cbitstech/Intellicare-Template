package edu.northwestern.cbits.intellicare.store;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	public static String LOG_PREFIX = "IntellicareStore";
	public static String PREFS_NAME = "intellicareStoreSharedPrefs";
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// get or create the shared prefs
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor e = settings.edit();
		long initSavings = 1000;
		e.putLong(UserProductInteractions.PREFS_KEY_SAVINGS, initSavings);
		e.commit();
		
		updateSavingsDisplay(initSavings);
		
		// repopulate (refresh) the product set
		SQLiteContentProvider.deleteAndRepopulateAllProducts(this);
		
		// query & display the data
		Cursor c = this.getContentResolver().query(SQLiteContentProvider.StoreUri.PRODUCTS, null, null, null, null);
		SQLiteContentProvider.logCursorResultSet(LOG_PREFIX, c);
		
		fillInventoryList(c);		
		fillMarketList(c);
		
		// simulate a purchase
//		UserProductInteractions.attemptToBuyProduct(settings, (ProductData.getMarketItemList(c).get(0)));
		
		// close the DB connection
		c.close();
	}


	/**
	 * @param savings
	 */
	private void updateSavingsDisplay(long savings) {
		TextView savingsTextBox = (TextView) this.findViewById(R.id.label_savings);
		String currSavingsText = (String) savingsTextBox.getText();
		String newSavingsText = currSavingsText.replaceFirst("( P | \\d+ )", String.valueOf(" " + savings + " "));
		savingsTextBox.setText(newSavingsText);
//		Log.d(LOG_PREFIX, "For savings = " + String.valueOf(savings) + " did the UI display newSavingsText = " + newSavingsText);
	}
	
	
	/**
	 * Displays a dialog when the user indicates they wish to buy an item. 
	 * inspirational src: http://stackoverflow.com/questions/2478517/how-to-display-a-yes-no-dialog-box-in-android
	 */
	private void displayBuyPrompt(final Product selectedProduct) {
		final Context me = this;
		
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        switch (which){
		        case DialogInterface.BUTTON_POSITIVE:
		            //Yes button clicked
		        	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		        	Cursor c = me.getContentResolver().query(SQLiteContentProvider.StoreUri.PRODUCTS, null, null, null, null);
		        			        	
		        	Hashtable<String, String> buyResult = UserProductInteractions.attemptToBuyProduct(settings, selectedProduct);
		        	
		        	// if buy succeeded
		        	if(buyResult.get("success")=="1") {
		        		updateSavingsDisplay(Long.parseLong(buyResult.get(UserProductInteractions.PREFS_KEY_SAVINGS)));
		        	}
		        	else {
		        		Log.d(LOG_PREFIX, "Can't afford " + selectedProduct.name + "; making toast instead... Toast text: " + buyResult.get("text"));
		        		Toast.makeText(me, buyResult.get("text"), Toast.LENGTH_SHORT).show();
		        	}
		            break;

		        case DialogInterface.BUTTON_NEGATIVE:
		            //No button clicked
		            break;
		        }
		    }
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder
			.setMessage("Are you sure you want to buy \"" + selectedProduct.name + "\" for " + selectedProduct.buyPrice + " points?")
			.setPositiveButton("Yes", dialogClickListener)
		    .setNegativeButton("No", dialogClickListener)
		    .show();
	}

	

	

	private void fillInventoryList(Cursor c) {
		ArrayList<Product> products = ProductData.getInventoryItemList(c);
		
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
		final ArrayList<Product> products = ProductData.getMarketItemList(c);
		ListView market = (ListView)this.findViewById(R.id.marketItems);
		
		// define the click-handler for an item in the Market list
		market.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// map the row list index to the appropriate product
				Product selectedProduct = products.get(position);
				
				// display the dialog 
				displayBuyPrompt(selectedProduct);
			}
			
		});
		
		// fill the market list with a set of products
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
