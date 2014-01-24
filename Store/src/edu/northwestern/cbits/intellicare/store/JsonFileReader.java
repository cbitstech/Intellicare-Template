package edu.northwestern.cbits.intellicare.store;

import java.io.BufferedReader;
import java.io.InputStream;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;


public class JsonFileReader {
	
//	private enum JSONTypes {
//		STR, INT, DBL, OBJ, ARR, DATETIME
//	}
	
	
	public String getJsonFromFile(Context ctx, String filePath)
	{
		String jsonText = "";

		BufferedReader reader = null;
		try {
			AssetManager am = ctx.getAssets();
			InputStream is = am.open(filePath);
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();
			jsonText = new String(buffer, "UTF-8");
		}
		catch(Exception e) {
			Log.e("Store.DataService", "Couldn't read input file: " + filePath, e);
		}
		
		return jsonText;
	}
	
	public <T> T getJSONvalue(String pathInJsonObj, String jsonText)		// JSONTypes valueType, 
	{
		T ret = null;
		try {
			JSONArray arr = new JSONArray(jsonText);
			JSONObject obj = arr.getJSONObject(0);
			ret = (T) obj.getString(pathInJsonObj);
			return ret;	
		}
		catch (Exception e) {
			Log.e("Store.DataService", "Couldn't get JSON value at: " + pathInJsonObj, e);
		}
		return ret;
	}
}
