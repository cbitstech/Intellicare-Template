package edu.northwestern.cbits.intellicare.mantra;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;


/**
 * Class to enable image extraction from web pages.
 * @author mohrlab
 *
 */
public class ImageExtractor
{
	/**
	 * Returns an image from some URL, passing-along cookies (e.g. for HTTP authentication - for authN, pass a Hashtable containing the key-value pairs for keys "username" and "password").
	 * @param url
	 * @param cookies 
	 * @return
	 * @throws IOException
	 */
	public static byte[] getImage(String url, Map<String, String> cookies) throws IOException {
		// via: http://stackoverflow.com/questions/12465586/how-can-i-download-an-image-using-jsoup
		byte[] imageResponse = Jsoup
				.connect(url)
				.cookies(cookies)
				.ignoreContentType(true)
				.execute()
				.bodyAsBytes();
		return imageResponse;
	}

	/**
	 * Returns an image from some URL.
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static byte[] getImage(String url) throws IOException {
		// via: http://stackoverflow.com/questions/12657592/using-jsoup-to-save-the-contents-of-this-url-http-www-aw20-co-uk-images-logo
		byte[] imageResponse = Jsoup.connect(url)
			.ignoreContentType(true)
			.execute()
			.bodyAsBytes();
		return imageResponse;
	}
	
	/**
	 * Returns a list of image URLs from some page at a specified URL.
	 * Src: http://jsoup.org/cookbook/extracting-data/example-list-links
	 * @return
	 * @throws IOException 
	 */
	public static ArrayList<String> getImageList(String url) throws IOException {
		ArrayList<String> ret = new ArrayList<String>();
		
		Document doc = Jsoup.connect(url).get();
		Elements links = doc.select("img");
		
		for (Element e : links) { ret.add(e.val()); }
		
		return ret;
	}
	
}