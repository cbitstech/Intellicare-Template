package edu.northwestern.cbits.intellicare.mantra;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.util.Log;

import edu.northwestern.cbits.intellicare.mantra.tests.ImageExtractorTest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
	public static Set<String> getImageList(String url, boolean returnRelativePaths) throws SocketTimeoutException, UnknownHostException, IOException {
		Set<String> ret = new HashSet<String>();
			Document doc = Jsoup.connect(url).get();
			Elements links = doc.select("img");
	
			// TODO: is there a less memory-intensive (e.g. Jsoup-internal) way to convert this to a standard Java datatype? 
			for (Element e : links) { 
				ret.add(
					// if a relative path is requested, then return it...
					returnRelativePaths
					? e.attr("src")
							// ...else, an absolute path is requested, so let's determine whether the image source already contains an absolute path, and return it if so, else prepend the base URL and return.
							// no idea why regexes (dynamically-compiled and pre-compiled, respectively) above weren't working, but the following works, and should use fewer resources. 
							: (e.attr("src").startsWith("http://") || e.attr("src").startsWith("https://") || e.attr("src").startsWith("HTTP://") || e.attr("src").startsWith("HTTPS://"))
								? e.attr("src")
								: e.baseUri() + e.attr("src") 
					);
			}
			return ret;
//		}
	}
	
	/**
	 * Gets the file size of the remote content at some URL.
	 * @param url
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static int getRemoteContentLength(String url) throws MalformedURLException, IOException {
		log("getRemoteContentLength", "entered; url = " + url);
		URLConnection oldConn = (new URL(url)).openConnection();
		HttpURLConnection conn = ((HttpURLConnection) oldConn);
		conn.setRequestMethod("HEAD");
		
		return conn.getContentLength();
	}
	
	/**
	 * Gets a set of content sizes for a set of URLs. Size will be -1 if an exception in getting the size of a file occurs.
	 * @param imageUrlList
	 * @return
	 */
	public static Map<String, Integer> getRemoteContentLength(Set<String> imageUrlList) {
		Map<String, Integer> ret = new HashMap<String, Integer>();

		for(String u : imageUrlList) {
			try {
				ret.put(u, getRemoteContentLength(u));
			} catch (MalformedURLException e) {
				e.printStackTrace();
				ret.put(u, -1);
			} catch (IOException e) {
				e.printStackTrace();
				ret.put(u, -1);
			}
		}
		
		return ret;
	}

	
	
	/***** utility functions *****/
	
	public static void log(String fn, String msg) {
		System.out.println("[" + fn + "] " + msg);
	}
	
	
	/**
	 * Gets the host name of a URL.
	 * @param url
	 * @return
	 * @throws URISyntaxException
	 */
	public static String extractHostName(String url) throws URISyntaxException {
		// src: http://stackoverflow.com/questions/9607903/get-domain-name-from-given-url
		return (new URI(url)).getHost();
	}

	
	/**
	 * Gets the file or route leaf-name from a URL. 
	 * @param u
	 * @return
	 * @throws URISyntaxException 
	 */
	public static String extractUrlFileName(String u) throws URISyntaxException {
		// src: http://stackoverflow.com/questions/6250200/how-to-get-the-size-of-an-image-in-java
		return ((new URI(u)).getPath()).replaceAll("^.*/(.*)$", "$1");
	}

	
	/**
	 * @param fileName
	 * @return
	 */
	private static String getImageFileExtension(String fileName) {
		return fileName.matches(".*\\.(JPG|jpg).*") ? ".jpg" :
			fileName.matches(".*\\.(PNG|png).*") ? ".png" :
				fileName.matches(".*\\.(GIF|gif).*") ? ".gif" :
					fileName.matches(".*\\.(TIF|tif).*") ? ".tif" :
						".jpg";																// if all else fails, naively assume it's a JPG. EX: i.chzbgr.com uses extensionless GUIDs for image names. These will never appear in the download folder.
	}
	
	
	/**
	 * Saves an image.
	 * @param image
	 * @param fullFilePath
	 * @throws IOException
	 */
	public static void saveImage(byte[] image, String fullFilePath) throws IOException {
		FileOutputStream out = (new FileOutputStream(new java.io.File(fullFilePath)));
		log(methodName(), "Writing image to: " + fullFilePath);
        out.write(image);           // resultImageResponse.body() is where the image's contents are.
        out.close();
	}
	
	/**
	 * Like the method says - it downloads and saves images.
	 * @param outputFolder
	 * @param imageUrlList
	 */
	public static void downloadAndSaveImages(String outputFolder, Set<String> imageUrlList) {
		String MN = methodName();

		// create the containing folder if it doesn't exist
		(new File(outputFolder)).mkdirs();

		// download and write the files
		for(String u : imageUrlList) {
			try {
				String fileName = extractUrlFileName(u);
				String outputFileName = java.util.UUID.randomUUID().toString() + getImageFileExtension(fileName);
				try{
					saveImage(ImageExtractor.getImage(u), outputFolder + outputFileName);
				} catch(Exception e) {
					log(MN, "ERROR: Couldn't write file to: " + outputFolder + outputFileName + " from URL (" + u + "). Reason: " + e.getMessage());
				}
			} catch (URISyntaxException e1) {
				e1.printStackTrace();
			}
		}
	}


	/**
	 * Like the method says - it downloads and saves images.
	 * @param outputFolder
	 * @param imageUrlList
	 * @return 
	 */
	public static String downloadAndSaveImage(String outputFolder, String imageUrl) {
		String MN = methodName();

		// create the containing folder if it doesn't exist
		(new File(outputFolder)).mkdirs();

		// download and write the files
		try {
			String fileName = extractUrlFileName(imageUrl);
			String outputFileName = java.util.UUID.randomUUID().toString() + getImageFileExtension(fileName);
			try{
				saveImage(ImageExtractor.getImage(imageUrl), outputFolder + outputFileName);
				return outputFileName;
			} catch(Exception e) {
				log(MN, "ERROR: Couldn't write file to: " + outputFolder + outputFileName + " from URL (" + imageUrl + "). Reason: " + e.getMessage());
			}
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		return null;
	}
	
	
	// enables getting the currently-executing method name.
	// src: http://stackoverflow.com/questions/442747/getting-the-name-of-the-current-executing-method/8592871#8592871
    private static final int CLIENT_CODE_STACK_INDEX;
    static {
        // Finds out the index of "this code" in the returned stack trace - funny but it differs in JDK 1.5 and 1.6
        int i = 0;
        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
            i++;
            if (ste.getClassName().equals(ImageExtractor.class.getName())) {
                break;
            }
        }
        CLIENT_CODE_STACK_INDEX = i;
    }
    public static String methodName() {
        return Thread.currentThread().getStackTrace()[CLIENT_CODE_STACK_INDEX].getMethodName();
    }
}