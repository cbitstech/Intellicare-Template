package edu.northwestern.cbits.intellicare.store;

import java.util.ArrayList;

import android.database.Cursor;
import android.text.method.MovementMethod;
import android.util.Log;

public class ProductData {
	
	public static Product[] getAllProductsAsArray() {
		return new Product[]{
			new Product("Alpha","A hat","","headTop",22, 1),
			new Product("Beta","B shoulder pad","","shouldersBoth",45, 0),
			new Product("Charlie","C armor","","torsoFront",300, 0),
			new Product("Delta","D pistol","","thighRight",450, 1),
			new Product("Epsilon","E boots","","feet",32, 0)
		};
	}
	
	public static ArrayList<Product> getAllProductsAsArrayList() {
		ArrayList<Product> ret = new ArrayList<Product>();
		
		ret.add(new Product("Alpha","A hat","","headTop",22, 1));
		ret.add(new Product("Beta","B shoulder pad","","shouldersBoth",45, 0));
		ret.add(new Product("Charlie","C armor","","torsoFront",300, 0));
		ret.add(new Product("Delta","D pistol","","thighRight",450, 1));
		ret.add(new Product("Epsilon","E boots","","feet",32, 0));
		
		return ret;
	}
	
	public static ArrayList<Product> getAllProductsAsArrayListFromCursor(Cursor c) {
		ArrayList<Product> ret = new ArrayList<Product>();

		c.moveToPosition(-1);
		for(int j = 0; c.moveToNext(); j++) {
			Product p = new Product();
			for(int i = 0; i < c.getColumnCount(); i++) {
				p.userOwnsThis = c.getInt(c.getColumnIndex(SQLiteContentProvider.Products.COLNM_USERHAZIT));
				p.name = c.getString(c.getColumnIndex(SQLiteContentProvider.Products.COLNM_NAME));
				p.description = c.getString(c.getColumnIndex(SQLiteContentProvider.Products.COLNM_DESC));
				p.icon = c.getString(c.getColumnIndex(SQLiteContentProvider.Products.COLNM_ICON));
				p.attachesOnAvatarAtLocation = c.getString(c.getColumnIndex(SQLiteContentProvider.Products.COLNM_ATTACHLOC));
				p.buyPrice = c.getDouble(c.getColumnIndex(SQLiteContentProvider.Products.COLNM_BUYPRICE));
//				Log.d("IntellicareStore.getAllProductsAsArrayListFromCursor", p.toString());
			}
			ret.add(p);
//			Log.d("IntellicareStore.getAllProductsAsArrayListFromCursor", "j = " + j);
		}
		Log.d("IntellicareStore.getAllProductsAsArrayListFromCursor", "ret.size() = " + ret.size());
		
		return ret;
	}
}
