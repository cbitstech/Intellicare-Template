package edu.northwestern.cbits.intellicare.store;

import java.util.Dictionary;
import java.util.Hashtable;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Defines the BL for a user's interaction with products.
 * @author mohrlab
 *
 */
public class UserProductInteractions {
	
	private static String LOG_PREFIX = "IntellicareStore.UserProductInteractions";
	public static String PREFS_KEY_SAVINGS = "savings";

	
	public static boolean hasEnoughSavingsToBuyProduct(long savings, Product p) {
		return savings >= p.buyPrice;
	}
	
	
	/**
	 * Entry-point to the buying logic. User attempts to buy the product; now what?
	 * @param settings
	 * @param p
	 * @return
	 */
	public static Hashtable<String, String> attemptToBuyProduct(SharedPreferences settings, Product p) {
		long savings = settings.getLong(PREFS_KEY_SAVINGS, 0);
		Hashtable<String, String> ret = new Hashtable();

		if(hasEnoughSavingsToBuyProduct(savings, p)) {
			long newSavings = buyProduct(settings, savings, p);
			ret.put(PREFS_KEY_SAVINGS, String.valueOf(newSavings));
			ret.put("text", "Successfully purchased " + p.name + ".");
			ret.put("success", "1");
		}
		else {
			ret.put(PREFS_KEY_SAVINGS, String.valueOf(settings.getLong(PREFS_KEY_SAVINGS, 0)));
			ret.put("text", "Sorry, not enough savings available to buy " + p.name + ".");
			ret.put("success", "0");
		}
		return ret;
	}
	
	private static long buyProduct(SharedPreferences settings, long prevSavings, Product p) {
		// deduct the price from the savings and update the savings in shared prefs.
		SharedPreferences.Editor e = settings.edit();
		long newSavings = (long) (prevSavings - p.buyPrice);
		e.putLong(PREFS_KEY_SAVINGS, newSavings);
		e.commit();
		Log.d(LOG_PREFIX + ".buyProduct", "prevSavings = " + prevSavings + "; newSavings = " + settings.getLong(PREFS_KEY_SAVINGS, 0));
		
		applyProductToAvatarProfile(p);
		return newSavings;
	}
	
	private static void applyProductToAvatarProfile(Product p) {
		Log.d(LOG_PREFIX + ".applyProductToAvatarProfile", "TODO: NOT IMPLEMENTED");
		
		displayProductOnAvatar(p);
	}
	
	private static void displayProductOnAvatar(Product p) {
		Log.d(LOG_PREFIX + ".displayProductOnAvatar", "TODO: NOT IMPLEMENTED");
	}
	
}
