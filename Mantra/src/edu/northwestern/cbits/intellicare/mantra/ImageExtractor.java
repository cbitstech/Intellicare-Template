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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
	 * @param url
	 * @param returnRelativePaths
	 * @return
	 * @throws IOException
	 */
	public static ArrayList<String> getImageList(String url, boolean returnRelativePaths) throws IOException {
		ArrayList<String> ret = new ArrayList<String>();
		
		Document doc = Jsoup.connect(url).get();
		Elements links = doc.select("img");

		// TODO: is there a less memory-intensive (e.g. Jsoup-internal) way to convert this to a standard Java datatype? 
		for (Element e : links) { 
			ret.add(
				// if a relative path is requested, then return it...
				returnRelativePaths
				? e.attr("src")
//						: e.attr("src").matches("(?i)https{0,1}://")
//						: (REGEX_HTTP_HTTPS_URL_PROTO.matcher(e.attr("src"))).matches()
						
						// ...else, an absolute path is requested, so let's determine whether the image source already contains an absolute path, and return it if so, else prepend the base URL and return.
						// no idea why regexes (dynamically-compiled and pre-compiled, respectively) above weren't working, but the following works, and should use fewer resources. 
						: (e.attr("src").startsWith("http://") || e.attr("src").startsWith("https://") || e.attr("src").startsWith("HTTP://") || e.attr("src").startsWith("HTTPS://"))
							? e.attr("src")
							: e.baseUri() + e.attr("src") 
				);
		}
		
		return ret;
	}
//	private static final Pattern REGEX_HTTP_HTTPS_URL_PROTO = Pattern.compile("http", Pattern.CASE_INSENSITIVE);		// case-insensitive match of HTTP/HTTPS
	
}